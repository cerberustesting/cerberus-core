/**
 * Cerberus Copyright (C) 2013 - 2025 cerberustesting
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This file is part of Cerberus.
 *
 * Cerberus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cerberus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cerberus.core.servlet.crud.transversaltables;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import static java.util.Arrays.asList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.Label;
import org.cerberus.core.crud.entity.TestCaseLabel;
import org.cerberus.core.crud.service.ILabelService;
import org.cerberus.core.crud.service.ITestCaseLabelService;
import org.cerberus.core.crud.service.impl.LabelService;
import org.cerberus.core.crud.service.impl.TestCaseLabelService;
import org.cerberus.core.dto.TreeNode;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.cerberus.core.util.answer.AnswerUtil;
import org.cerberus.core.util.servlet.ServletUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author bcivel
 */
@WebServlet(name = "ReadLabel", urlPatterns = {"/ReadLabel"})
public class ReadLabel extends HttpServlet {

    private ILabelService labelService;
    private ITestCaseLabelService testCaseLabelService;

    private static final Logger LOG = LogManager.getLogger(ReadLabel.class);

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     * @throws org.cerberus.core.exception.CerberusException
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, CerberusException {
        String echo = request.getParameter("sEcho");
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

        response.setContentType("application/json");
        response.setCharacterEncoding("utf8");

        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);

        // Default message to unexpected error.
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        /**
         * Parsing and securing all required parameters.
         */
        // Nothing to do here as no parameter to check.
        //
        // Global boolean on the servlet that define if the user has permition to edit and delete object.
        boolean userHasPermissions = request.isUserInRole("Label");

        //Get Parameters
        String columnName = ParameterParserUtil.parseStringParam(request.getParameter("columnName"), "");
        Boolean likeColumn = ParameterParserUtil.parseBooleanParam(request.getParameter("likeColumn"), false);

        // Init Answer with potencial error from Parsing parameter.
        AnswerItem answer = new AnswerItem<>(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));
        AnswerItem answer1 = new AnswerItem<>(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));

        try {
            JSONObject jsonResponse = new JSONObject();
            if ((request.getParameter("id") == null) && (request.getParameter("system") == null) && StringUtil.isEmptyOrNull(columnName)) {
                answer = findLabelList(null, appContext, userHasPermissions, request);
                jsonResponse = (JSONObject) answer.getItem();
            } else {
                if (request.getParameter("id") != null) {
                    Integer id = Integer.valueOf(policy.sanitize(request.getParameter("id")));
                    answer = findLabelByKey(id, appContext, userHasPermissions);
                    jsonResponse = (JSONObject) answer.getItem();
                } else if (request.getParameter("system") != null && !StringUtil.isEmptyOrNull(columnName)) {
                    List<String> systems = ParameterParserUtil.parseListParamAndDeleteEmptyValue(request.getParameterValues("system"), Arrays.asList("DEFAULT"), "UTF-8");
                    answer = findDistinctValuesOfColumn(systems, appContext, request, columnName);
                    jsonResponse = (JSONObject) answer.getItem();
                } else if (request.getParameter("system") != null) {
                    List<String> system = ParameterParserUtil.parseListParamAndDeleteEmptyValue(request.getParameterValues("system"), Arrays.asList("DEFAULT"), "UTF-8");
                    answer = findLabelList(system, appContext, userHasPermissions, request);
                    jsonResponse = (JSONObject) answer.getItem();
                }
            }
            if ((request.getParameter("withHierarchy") != null)) {
                List<String> system = ParameterParserUtil.parseListParamAndDeleteEmptyValue(request.getParameterValues("system"), Arrays.asList("DEFAULT"), "UTF-8");
                answer1 = getLabelHierarchy(system, appContext, userHasPermissions, request, (request.getParameter("isSelectable") != null), (request.getParameter("hasButtons") != null));
                JSONObject jsonHierarchy = (JSONObject) answer1.getItem();
                jsonResponse.put("labelHierarchy", jsonHierarchy);

            }

            jsonResponse.put("messageType", answer.getResultMessage().getMessage().getCodeString());
            jsonResponse.put("message", answer.getResultMessage().getDescription());
            jsonResponse.put("sEcho", echo);

            response.getWriter().print(jsonResponse.toString());

        } catch (JSONException e) {
            LOG.error("JSON Exception", e);
            //returns a default error message with the json format that is able to be parsed by the client-side
            response.getWriter().print(AnswerUtil.createGenericErrorAnswer());
        } catch (Exception e) {
            LOG.error("General Exception", e);
            //returns a default error message with the json format that is able to be parsed by the client-side
            response.getWriter().print(AnswerUtil.createGenericErrorAnswer());
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (CerberusException ex) {
            LOG.warn(ex);
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (CerberusException ex) {
            LOG.warn(ex);
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    private AnswerItem<JSONObject> findLabelList(List<String> system, ApplicationContext appContext, boolean userHasPermissions, HttpServletRequest request) throws JSONException {

        AnswerItem<JSONObject> item = new AnswerItem<>();
        JSONObject object = new JSONObject();
        labelService = appContext.getBean(LabelService.class);

        int startPosition = Integer.valueOf(ParameterParserUtil.parseStringParam(request.getParameter("iDisplayStart"), "0"));
        int length = Integer.valueOf(ParameterParserUtil.parseStringParam(request.getParameter("iDisplayLength"), "0"));
        /*int sEcho  = Integer.valueOf(request.getParameter("sEcho"));*/

        String searchParameter = ParameterParserUtil.parseStringParam(request.getParameter("sSearch"), "");
        int columnToSortParameter = Integer.parseInt(ParameterParserUtil.parseStringParam(request.getParameter("iSortCol_0"), "1"));
        String sColumns = ParameterParserUtil.parseStringParam(request.getParameter("sColumns"), "System,Label,Color,Display,parentLabelId,Description");
        String columnToSort[] = sColumns.split(",");
        String columnName = columnToSort[columnToSortParameter];
        String sort = ParameterParserUtil.parseStringParam(request.getParameter("sSortDir_0"), "asc");
        List<String> individualLike = new ArrayList<>(Arrays.asList(ParameterParserUtil.parseStringParam(request.getParameter("sLike"), "").split(",")));
        boolean strictSystemFilter = ParameterParserUtil.parseBooleanParam(request.getParameter("bStrictSystemFilter"), false);

        Map<String, List<String>> individualSearch = new HashMap<>();
        for (int a = 0; a < columnToSort.length; a++) {
            if (null != request.getParameter("sSearch_" + a) && !request.getParameter("sSearch_" + a).isEmpty()) {
                List<String> search = new ArrayList<>(Arrays.asList(request.getParameter("sSearch_" + a).split(",")));
                if (individualLike.contains(columnToSort[a])) {
                    individualSearch.put(columnToSort[a] + ":like", search);
                } else {
                    individualSearch.put(columnToSort[a], search);
                }
            }
        }
        AnswerList<Label> resp = labelService.readByVariousByCriteria(system, strictSystemFilter, new ArrayList<>(), startPosition, length, columnName, sort, searchParameter, individualSearch);

        JSONArray jsonArray = new JSONArray();
        if (resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {//the service was able to perform the query, then we should get all values
            for (Label label : resp.getDataList()) {
                JSONObject labelObject = convertLabelToJSONObject(label);
                if (label.getParentLabelID() > 0) {
                    AnswerItem parentLabel = labelService.readByKey(label.getParentLabelID());
                    if (parentLabel.getItem() != null) {
                        labelObject.put("labelParentObject", convertLabelToJSONObject((Label) parentLabel.getItem()));
                    }
                }
                jsonArray.put(labelObject);
            }
        }
        object.put("hasPermissions", userHasPermissions);
        object.put("contentTable", jsonArray);
        object.put("iTotalRecords", resp.getTotalRows());
        object.put("iTotalDisplayRecords", resp.getTotalRows());
        item.setItem(object);
        item.setResultMessage(resp.getResultMessage());
        return item;
    }

    private AnswerItem<JSONObject> getLabelHierarchy(List<String> system, ApplicationContext appContext, boolean userHasPermissions, HttpServletRequest request, boolean isSelectable, boolean hasButtons) throws JSONException {
        testCaseLabelService = appContext.getBean(TestCaseLabelService.class);

        AnswerItem<JSONObject> item = new AnswerItem<>();
        JSONObject object = new JSONObject();

        List<TestCaseLabel> labelList = new ArrayList<>();
        HashMap<Integer, Integer> labelFromTestCaseList = new HashMap<>();
        if (request.getParameter("testSelect") != null) {
            // If parameter 'testSelect' is defined, we load the labels attached to 'testSelect' and 'testCaseSelect' in order to select the corresponding values when building the list of labels.
            try {
                String charset = request.getCharacterEncoding() == null ? "UTF-8" : request.getCharacterEncoding();
                String test1 = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("testSelect"), null, charset);
                String testCase1 = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("testCaseSelect"), null, charset);
                labelList = testCaseLabelService.convert(testCaseLabelService.readByTestTestCase(test1, testCase1, new ArrayList<>()));
            } catch (CerberusException ex) {
                LOG.error("Could not get TestCase Label", ex);
            }
            for (TestCaseLabel testCaseLabel : labelList) {
                labelFromTestCaseList.put(testCaseLabel.getLabelId(), 0);
            }
        }

        JSONArray jsonObject = new JSONArray();
        jsonObject = getTree(system, Label.TYPE_REQUIREMENT, appContext, isSelectable, hasButtons, labelFromTestCaseList);
        object.put("requirements", jsonObject);

        jsonObject = new JSONArray();
        jsonObject = getTree(system, Label.TYPE_STICKER, appContext, isSelectable, hasButtons, labelFromTestCaseList);
        object.put("stickers", jsonObject);

        jsonObject = new JSONArray();
        jsonObject = getTree(system, Label.TYPE_BATTERY, appContext, isSelectable, hasButtons, labelFromTestCaseList);
        object.put("batteries", jsonObject);

        item.setItem(object);

        return item;
    }

    private JSONArray getTree(List<String> system, String type, ApplicationContext appContext, boolean isSelectable, boolean hasButtons, HashMap<Integer, Integer> labelFromTestCaseToSelect) throws JSONException {
        labelService = appContext.getBean(LabelService.class);
        testCaseLabelService = appContext.getBean(TestCaseLabelService.class);
        TreeNode node;
        JSONArray jsonArray = new JSONArray();

        AnswerList<Label> resp = labelService.readByVarious(system, new ArrayList<>(asList(type)));

        // Building tree Structure;
        if (resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {

            HashMap<Integer, TreeNode> inputList = new HashMap<>();

            for (Label label : resp.getDataList()) {

                String text = "";
                if (hasButtons) {
                    text += "<button id='editLabel' onclick=\"stopPropagation(event);editEntryClick(\'" + label.getId() + "\', \'" + label.getSystem() + "\');\" class='editLabel btn-tree btn btn-default btn-xs margin-right5' name='editLabel' title='Edit Label' type='button'>";
                    text += " <span class='glyphicon glyphicon-pencil'></span></button>";

                    text += "<button id='deleteLabel' onclick=\"stopPropagation(event);deleteEntryClick(\'" + label.getId() + "\', \'" + label.getLabel() + "\');\" class='deleteLabel btn-tree btn btn-default btn-xs margin-right5' name='deleteLabel' title='Delete Label' type='button'>";
                    text += " <span class='glyphicon glyphicon-trash'></span></button>";

                    text += "<button id='tc1Label' onclick=\"stopPropagation(event);window.open('./TestCaseList.jsp?label=" + label.getLabel() + "','_blank');\" class='btn-tree btn btn-default btn-xs margin-right5' name='tcLabel' title='Open Testcase list in new window' type='button'>";
                    text += " <span class='glyphicon glyphicon-list'></span></button>";

                    text += "<button id='tc1Label' onclick=\"stopPropagation(event);window.location.href = './TestCaseList.jsp?label=" + label.getLabel() + "';\" class='btn-tree btn btn-primary btn-xs margin-right5' name='tcLabel' title='Open Testcase list.' type='button'>";
                    text += " <span class='glyphicon glyphicon-list'></span></button>";
                }

                text += "<span class='label label-primary' style='background-color:" + label.getColor() + "' data-toggle='tooltip' data-labelid='" + label.getId() + "' title='' data-original-title=''>" + label.getLabel() + "</span>";
                text += "<span style='margin-left: 5px; margin-right: 5px;' class=''>" + label.getDescription() + "</span>";

                text += "%COUNTER1TEXT%";
                text += "%COUNTER1WITHCHILDTEXT%";
                text += "%NBNODESWITHCHILDTEXT%";

                // Specific pills
                //text += "<span class='badge badge-pill badge-secondary'>666</span>";
                // Standard pills
                List<String> attributList = new ArrayList<>();
                if (Label.TYPE_REQUIREMENT.equals(label.getType())) {
                    if (!StringUtil.isEmptyOrNull(label.getRequirementType()) && !"unknown".equalsIgnoreCase(label.getRequirementType())) {
                        attributList.add("<span class='badge badge-pill badge-secondary'>" + label.getRequirementType() + "</span>");
                    }
                    if (!StringUtil.isEmptyOrNull(label.getRequirementStatus()) && !"unknown".equalsIgnoreCase(label.getRequirementStatus())) {
                        attributList.add("<span class='badge badge-pill badge-secondary'>" + label.getRequirementStatus() + "</span>");
                    }
                    if (!StringUtil.isEmptyOrNull(label.getRequirementCriticity()) && !"unknown".equalsIgnoreCase(label.getRequirementCriticity())) {
                        attributList.add("<span class='badge badge-pill badge-secondary'>" + label.getRequirementCriticity() + "</span>");
                    }
                }
                if ("".equals(label.getSystem())) {
                    attributList.add("GLOBAL");
                }

                // Create Node.
                node = new TreeNode(label.getId() + "-" + label.getSystem() + "-" + label.getLabel(), label.getSystem(), label.getLabel(), label.getId(), label.getParentLabelID(), text, null, null, false);
                node.setCounter1(label.getCounter1());
                node.setCounter1WithChild(label.getCounter1());
                node.setTags(attributList);
                node.setLabelObj(label);
                node.setCounter1Text("<span style='background-color:#000000' class='cnt1 badge badge-pill badge-secondary'>%COUNTER1%</span>");
                node.setCounter1WithChildText("<span class='cnt1WC badge badge-pill badge-secondary'>%COUNTER1WITHCHILD%</span>");
                node.setNbNodesText("<span style='background-color:#337ab7' class='nbNodes badge badge-pill badge-primary'>%NBNODESWITHCHILD%</span>");
                // If label is in HashMap, we set it as selected.
                if (labelFromTestCaseToSelect.containsKey(label.getId())) {
                    node.setSelected(true);
                } else {
                    node.setSelected(false);
                }
                if (isSelectable) {
                    node.setSelectable(true);
                }
                inputList.put(label.getId(), node);
            }

            jsonArray = new JSONArray();

            for (TreeNode treeNode : labelService.hierarchyConstructor(inputList)) {
                jsonArray.put(treeNode.toJson());
            }

        }

        return jsonArray;
    }

    private AnswerItem<JSONObject> findLabelByKey(Integer id, ApplicationContext appContext, boolean userHasPermissions) throws JSONException, CerberusException {
        AnswerItem<JSONObject> item = new AnswerItem<>();
        JSONObject object = new JSONObject();

        labelService = appContext.getBean(ILabelService.class);

        //finds the project     
        AnswerItem answer = labelService.readByKey(id);

        if (answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item and convert it to JSONformat
            Label label = (Label) answer.getItem();
            JSONObject labelObject = convertLabelToJSONObject(label);
            if (label.getParentLabelID() > 0) {
                AnswerItem answerParent = labelService.readByKey(label.getParentLabelID());
                if (answerParent.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && (answerParent.getItem() != null)) {
                    labelObject.put("labelParentObject", convertLabelToJSONObject((Label) answerParent.getItem()));
                }

            }
            JSONObject response = labelObject;
            object.put("contentTable", response);
        }

        object.put("hasPermissions", userHasPermissions);
        item.setItem(object);
        item.setResultMessage(answer.getResultMessage());

        return item;
    }

    private JSONObject convertLabelToJSONObject(Label label) throws JSONException {

        Gson gson = new Gson();
        JSONObject result = new JSONObject(gson.toJson(label));
        JSONObject display = new JSONObject();
        display.put("label", label.getLabel());
        display.put("color", label.getColor());
        result.put("display", display);
        return result;
    }

    private AnswerItem<JSONObject> findDistinctValuesOfColumn(List<String> systems, ApplicationContext appContext, HttpServletRequest request, String columnName) throws JSONException {
        AnswerItem<JSONObject> answer = new AnswerItem<>();
        JSONObject object = new JSONObject();

        labelService = appContext.getBean(ILabelService.class);

        String searchParameter = ParameterParserUtil.parseStringParam(request.getParameter("sSearch"), "");
        String sColumns = ParameterParserUtil.parseStringParam(request.getParameter("sColumns"), "System,Label,Color,Display,parentLabelId,Description");
        String columnToSort[] = sColumns.split(",");

        List<String> individualLike = new ArrayList<>(Arrays.asList(ParameterParserUtil.parseStringParam(request.getParameter("sLike"), "").split(",")));

        Map<String, List<String>> individualSearch = new HashMap<>();
        for (int a = 0; a < columnToSort.length; a++) {
            if (null != request.getParameter("sSearch_" + a) && !request.getParameter("sSearch_" + a).isEmpty()) {
                List<String> search = new ArrayList<>(Arrays.asList(request.getParameter("sSearch_" + a).split(",")));
                if (individualLike.contains(columnToSort[a])) {
                    individualSearch.put(columnToSort[a] + ":like", search);
                } else {
                    individualSearch.put(columnToSort[a], search);
                }
            }
        }

        AnswerList testCaseList = labelService.readDistinctValuesByCriteria(systems, searchParameter, individualSearch, columnName);

        object.put("distinctValues", testCaseList.getDataList());

        answer.setItem(object);
        answer.setResultMessage(testCaseList.getResultMessage());
        return answer;
    }

}
