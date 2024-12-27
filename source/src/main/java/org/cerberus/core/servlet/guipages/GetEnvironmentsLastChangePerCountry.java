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
package org.cerberus.core.servlet.guipages;

import com.google.gson.Gson;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.CountryEnvParam_log;
import org.cerberus.core.crud.entity.Invariant;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.crud.service.ICountryEnvParam_logService;
import org.cerberus.core.crud.service.IInvariantService;
import org.cerberus.core.enums.MessageEventEnum;
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
 * @author ip100003
 */
@WebServlet(name = "GetEnvironmentsLastChangePerCountry", urlPatterns = {"/GetEnvironmentsLastChangePerCountry"})
public class GetEnvironmentsLastChangePerCountry extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(GetEnvironmentsLastChangePerCountry.class);

    private IInvariantService invariantService;
    private ICountryEnvParam_logService ceplService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String echo = request.getParameter("sEcho");
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

        response.setContentType("application/json");

        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);

        // Default message to unexpected error.
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        /**
         * Parsing and securing all required parameters.
         */
        String system = policy.sanitize(request.getParameter("system"));
        String envGp = policy.sanitize(request.getParameter("envgp"));
        Integer nbDays = 10;
        boolean nbdays_error = false;
        try {
            if (request.getParameter("nbdays") != null && !request.getParameter("nbdays").isEmpty()) {
                nbDays = Integer.valueOf(policy.sanitize(request.getParameter("nbdays")));
            }
        } catch (Exception ex) {
            nbdays_error = true;
        }

        //
        // Global boolean on the servlet that define if the user has permition to edit and delete object.
        boolean userHasPermissions = true;

        // Init Answer with potencial error from Parsing parameter.
        AnswerItem answer = new AnswerItem<>(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));

        try {
            JSONObject jsonResponse = new JSONObject();
            if (StringUtil.isEmptyOrNull(system)) {
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "Environment Last Change per Country")
                        .replace("%OPERATION%", "Read")
                        .replace("%REASON%", "System is missing."));
                answer.setResultMessage(msg);
            } else if (nbdays_error) {
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "Environment Last Change per Country")
                        .replace("%OPERATION%", "Read")
                        .replace("%REASON%", "Could not manage to convert nbdays to an integer value."));
                answer.setResultMessage(msg);
            } else if (request.getParameter("system") != null) {
                answer = findBuildRevList(system, envGp, nbDays, appContext, userHasPermissions, request);
                jsonResponse = (JSONObject) answer.getItem();
            }

            jsonResponse.put("messageType", answer.getResultMessage().getMessage().getCodeString());
            jsonResponse.put("message", answer.getResultMessage().getDescription());
            jsonResponse.put("sEcho", echo);

            response.getWriter().print(jsonResponse.toString());

        } catch (JSONException e) {
            LOG.warn(e);
            //returns a default error message with the json format that is able to be parsed by the client-side
            response.getWriter().print(AnswerUtil.createGenericErrorAnswer());
        }

    }

    private AnswerItem<JSONObject> findBuildRevList(String system, String envGp, Integer nbDays, ApplicationContext appContext, boolean userHasPermissions, HttpServletRequest request) throws JSONException {

        AnswerItem<JSONObject> item = new AnswerItem<>();
        JSONObject object = new JSONObject();
        invariantService = appContext.getBean(IInvariantService.class);
        ceplService = appContext.getBean(ICountryEnvParam_logService.class);

        AnswerList<Invariant> resp = invariantService.readCountryListEnvironmentLastChanges(system, nbDays);

        JSONArray jsonArray = new JSONArray();
        if (resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {//the service was able to perform the query, then we should get all values
            for (Invariant countryInvariant : resp.getDataList()) {

                JSONObject countryJSON;
                countryJSON = convertToJSONObject(countryInvariant);

                AnswerList<CountryEnvParam_log> resp1 = ceplService.readLastChanges(system, countryInvariant.getValue(), nbDays, envGp);
                JSONArray jsonArray1 = new JSONArray();
                if (resp1.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {//the service was able to perform the query, then we should get all values
                    for (CountryEnvParam_log countryepl : resp1.getDataList()) {
                        jsonArray1.put(convertToJSONObject(countryepl));

                    }
                }
                countryJSON.put("contentTable", jsonArray1);
                jsonArray.put(countryJSON);
            }
        }

        object.put("contentTable", jsonArray);
        object.put("iTotalRecords", resp.getTotalRows());
        object.put("iTotalDisplayRecords", resp.getTotalRows());

        item.setItem(object);
        item.setResultMessage(resp.getResultMessage());
        return item;
    }

    private JSONObject convertToJSONObject(Invariant object) throws JSONException {
        Gson gson = new Gson();
        JSONObject result = new JSONObject(gson.toJson(object));
        return result;
    }

    private JSONObject convertToJSONObject(CountryEnvParam_log object) throws JSONException {
        Gson gson = new Gson();
        JSONObject result = new JSONObject(gson.toJson(object));
        return result;
    }
}
