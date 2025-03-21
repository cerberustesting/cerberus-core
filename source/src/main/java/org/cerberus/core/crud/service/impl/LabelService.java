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
package org.cerberus.core.crud.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.dao.ILabelDAO;
import org.cerberus.core.crud.entity.Label;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseLabel;
import org.cerberus.core.crud.service.ILabelService;
import org.cerberus.core.crud.service.ITestCaseLabelService;
import org.cerberus.core.dto.TreeNode;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.enums.MessageGeneralEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class LabelService implements ILabelService {

    @Autowired
    private ILabelDAO labelDAO;
    @Autowired
    private ILabelService labelService;
    @Autowired
    private ITestCaseLabelService testCaseLabelService;

    private static final Logger LOG = LogManager.getLogger("LabelService");

    private final String OBJECT_NAME = "Label";

    @Override
    public AnswerItem<Label> readByKey(Integer id) {
        return labelDAO.readByKey(id);
    }

    @Override
    public AnswerList<Label> readAll() {
        return readByVariousByCriteria(new ArrayList<>(), false, new ArrayList<>(), 0, 0, "Label", "asc", null, null);
    }

    @Override
    public HashMap<Integer, Label> readAllToHash() {
        HashMap<Integer, Label> labels = new HashMap<>();
        for (Label label : this.readAll().getDataList()) {
            labels.put(label.getId(), label);
        }
        return labels;
    }

    @Override
    public AnswerList<Label> readAllLinks() {
        return labelDAO.readAllLinks();
    }

    @Override
    public AnswerList<Label> readBySystem(List<String> system) {
        return labelDAO.readBySystemByCriteria(system, false, new ArrayList<>(), 0, 0, "Label", "asc", null, null);
    }

    @Override
    public AnswerList<Label> readByVarious(List<String> system, List<String> type) {
        return labelDAO.readBySystemByCriteria(system, false, type, 0, 0, "Label", "asc", null, null);
    }

    @Override
    public AnswerList<Label> readByCriteria(int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch) {
        return labelDAO.readBySystemByCriteria(new ArrayList<>(), false, new ArrayList<>(), startPosition, length, columnName, sort, searchParameter, individualSearch);
    }

    @Override
    public AnswerList<Label> readByVariousByCriteria(List<String> system, boolean strictSystemFilter, List<String> type, int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch) {
        return labelDAO.readBySystemByCriteria(system, strictSystemFilter, type, startPosition, length, columnName, sort, searchParameter, individualSearch);
    }

    @Override
    public HashMap<String, List<Label>> findLabelsFromTestCase(String test, String testCase, List<TestCase> testCases) {

        HashMap<String, TestCaseLabel> testCaseLabels = this.testCaseLabelService.readByTestTestCaseToHash(test, testCase, testCases);
        HashMap<Integer, Label> labelsMap = this.readAllToHash();
        HashMap<String, List<Label>> labelsToReturn = new HashMap<>();

        testCaseLabels.forEach((key, value) -> {
            String keyTC = value.getTest() + "##" + value.getTestcase();
            if (labelsToReturn.containsKey(keyTC)) {
                labelsToReturn.get(keyTC).add(labelsMap.get(value.getLabelId()));
            } else {
                labelsToReturn.put(keyTC, new ArrayList<>());
                labelsToReturn.get(keyTC).add(labelsMap.get(value.getLabelId()));
            }
        });

        for (Map.Entry<String, List<Label>> entry : labelsToReturn.entrySet()) {
            String key = entry.getKey();
            List<Label> val = entry.getValue();

            // Sort Label List
            Collections.sort(val, (Label label1, Label label2) -> {
                String val1 = addParent(label1, labelsMap, 0);
                String val2 = addParent(label2, labelsMap, 0);
//                LOG.debug("Compare : '{}'   -  '{}'", val1, val2);
                int compareResult = val1.compareTo(val2);
                return compareResult;
            });

            labelsToReturn.put(key, val);

        }

        return labelsToReturn;
    }

    private String addParent(Label label, HashMap<Integer, Label> labelsMap, int protection) {
        if (label.getParentLabelID() == 0) {
            return label.getLabel();
        } else if (protection >= 6) {
            LOG.warn("Reached maximum recursive label hierarchy : " + protection);
            return label.getLabel();
        } else {
            return addParent(labelsMap.get(label.getParentLabelID()), labelsMap, ++protection) + "/" + label.getLabel();
        }
    }

    @Override
    public boolean exist(Integer id) {
        AnswerItem objectAnswer = readByKey(id);
        return (objectAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) && (objectAnswer.getItem() != null); // Call was successfull and object was found.
    }

    @Override
    public Answer create(Label object) {
        Answer answerChecks = checkLabelParentconsistency(object);
        if (answerChecks == null) {
            return labelDAO.create(object);
        } else {
            return answerChecks;
        }
    }

    @Override
    public Answer delete(Label object) {
        return labelDAO.delete(object);
    }

    @Override
    public Answer update(Label object) {
        Answer answerChecks = checkLabelParentconsistency(object);
        if (answerChecks == null) {
            return labelDAO.update(object);
        } else {
            return answerChecks;
        }
    }

    private Answer checkLabelParentconsistency(Label object) {
        // If parent label exist we check that it is consistent.
        if (object.getParentLabelID() != 0) {
            Answer response = new Answer();
            MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_LABEL);
            // Getting parent label.
            AnswerItem<Label> answerLabelParent = readByKey(object.getParentLabelID());
            if ((answerLabelParent.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) && (answerLabelParent.getItem() != null)) {
                Label parentLabel = answerLabelParent.getItem();
                if ((!parentLabel.getSystem().equals(object.getSystem())) && (!StringUtil.isEmptyOrNull(parentLabel.getSystem()))) {
                    // Parent Label system is not empty and different from child label system.
                    msg.setDescription(msg.getDescription()
                            .replace("%LABEL%", object.getLabel())
                            .replace("%LABELPARENT%", parentLabel.getLabel())
                            .replace("%DESCRIPTION%", "Parent label does not belong to the same system as child"));
                    response.setResultMessage(msg);
                    return response;
                }
                if (!parentLabel.getType().equals(object.getType())) {
                    // Parent & Child have different types.
                    msg.setDescription(msg.getDescription()
                            .replace("%LABEL%", object.getLabel())
                            .replace("%LABELPARENT%", parentLabel.getLabel())
                            .replace("%DESCRIPTION%", "Cannot attach " + object.getType() + " Parent label to " + parentLabel.getType() + " child label. Types must be consistent"));
                    response.setResultMessage(msg);
                    return response;
                }
                if ((parentLabel.getId().equals(object.getParentLabelID())) && (object.getId().equals(parentLabel.getParentLabelID())) && (object.getId() > 0)) {
                    // Parent & Child have different types. and current > 0 (means that we are not creating a new record.)
                    msg.setDescription(msg.getDescription()
                            .replace("%LABEL%", object.getLabel())
                            .replace("%LABELPARENT%", parentLabel.getLabel())
                            .replace("%DESCRIPTION%", "'" + parentLabel.getLabel() + "' is already attached to '" + object.getLabel() + "' and recursive links are not allowed"));
                    response.setResultMessage(msg);
                    return response;
                }
                if (object.getId() == object.getParentLabelID()) {
                    // Parent & Child have different types.
                    msg.setDescription(msg.getDescription()
                            .replace("%LABEL%", object.getLabel())
                            .replace("%LABELPARENT%", parentLabel.getLabel())
                            .replace("%DESCRIPTION%", "Label cannot be attached to itself"));
                    response.setResultMessage(msg);
                    return response;
                }
            } else {
                // Parent label does not exist.
                msg.setDescription(msg.getDescription()
                        .replace("%LABEL%", object.getLabel())
                        .replace("%LABELPARENT%", object.getParentLabelID().toString())
                        .replace("%DESCRIPTION%", "Parent label does not exist"));
                response.setResultMessage(msg);
                return response;
            }
        }
        return null;
    }

    @Override
    public List<Integer> enrichWithChild(List<Integer> labelIdList) {

        try {
            // Loading list of labelId into a map in order to dedup it.
            HashMap<Integer, Integer> finalMap = new HashMap<>();
            HashMap<Integer, Integer> initMap = new HashMap<>();
            // Dedup list on a MAP
            for (Integer labelId : labelIdList) {
                finalMap.put(labelId, 0);
                initMap.put(labelId, 0);
            }

            // Loading from database the list of links from parent to childs.
            List<Label> labelLinkList = labelService.convert(labelService.readAllLinks());

            // Looping of each campaign label and add the childs.
            Integer initSize = initMap.size();
            Integer finalSize = initSize;
            Integer i = 0;
            do {
                for (Map.Entry<Integer, Integer> entry : finalMap.entrySet()) {
                    Integer key = entry.getKey();
                    initMap.put(key, 0);
                }
                initSize = initMap.size();
                for (Map.Entry<Integer, Integer> entry : initMap.entrySet()) {
                    Integer key = entry.getKey();
                    Integer value = entry.getValue();
                    for (Label label : labelLinkList) {
                        if (Objects.equals(key, label.getParentLabelID())) {
                            finalMap.put(label.getId(), 0);
                        }
                    }
                }
                finalSize = finalMap.size();
                i++;
            } while (!Objects.equals(finalSize, initSize) && i < 50);

            List<Integer> finalList = new ArrayList<>();
            for (Map.Entry<Integer, Integer> entry : finalMap.entrySet()) {
                Integer key = entry.getKey();
                finalList.add(key);
            }

            return finalList;
        } catch (CerberusException ex) {
            LOG.error("Exception when enriching Labels with Child.", ex);
        }
        return new ArrayList<>();
    }

    @Override
    public Label convert(AnswerItem<Label> answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<Label> convert(AnswerList<Label> answerList) throws CerberusException {
        if (answerList.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return answerList.getDataList();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public void convert(Answer answer) throws CerberusException {
        if (answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return;
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public AnswerList<String> readDistinctValuesByCriteria(List<String> systems, String searchParameter, Map<String, List<String>> individualSearch, String columnName) {
        return labelDAO.readDistinctValuesByCriteria(systems, searchParameter, individualSearch, columnName);
    }

    @Override
    public List<TreeNode> hierarchyConstructor(HashMap<Integer, TreeNode> inputList) {

        // Preparing data structure.
        // Looping against inputList to build nodeList.
        // Also saving all entry that have a parent in treeParent.
        Map<Integer, TreeNode> nodeList = new HashMap<>();
        List<TreeNode> treeParent = new ArrayList<>();
        for (Map.Entry<Integer, TreeNode> entry : inputList.entrySet()) {
            Integer key = entry.getKey();
            TreeNode localNode = entry.getValue();
            nodeList.put(localNode.getId(), localNode);
            if (localNode.getParentId() > 0) {
                treeParent.add(localNode);
            }
        }

        // Building final list
        List<TreeNode> finalList = new ArrayList<>();

        try {
            // Loop on maximum hierarchy levels.
            int i = 0;
            while (i < 50 && !nodeList.isEmpty()) {
//                LOG.debug(i + ".start : " + nodeList);
                List<TreeNode> listToRemove = new ArrayList<>();

                // Looping against nodeList.
                for (Map.Entry<Integer, TreeNode> entry : nodeList.entrySet()) {
                    Integer key = entry.getKey();
                    TreeNode value = entry.getValue();
//                    LOG.debug(value.getId() + " " + value.getParentId() + " " + value.getNodes().size());

                    // Does current entry has at least a child ?
                    boolean hasChild = false;
                    for (TreeNode treeNode : treeParent) {
                        if (treeNode.getParentId() == value.getId()) {
                            hasChild = true;
                        }
                    }

                    if (!hasChild) {
                        // If entry has no more child, we can add it to finalList.
                        if ((i == 0) && (value.getNodes().isEmpty())) {
                            value.setNodes(null);
                        }
//                        LOG.debug("Pas de fils.");
                        if (value.getParentId() <= 0) {
//                            LOG.debug("Adding to final result and remove from list." + i);
                            if (value.getNodes() != null && !value.getNodes().isEmpty()) {
                                Collections.sort(value.getNodes(), new SortbyLabel());
                            }

                            finalList.add(value);
                            listToRemove.add(value);
                        } else {
//                        LOG.debug("Moving to parent and remove from list." + i + " Parent " + value.getParentId());
                            // Mettre sur le fils sur son pere.
                            TreeNode father = nodeList.get(value.getParentId());
                            if (father != null) {
                                List<TreeNode> sons = father.getNodes();
                                if (sons == null) {
                                    sons = new ArrayList<>();
                                }
                                if (value.getNodes() != null && !value.getNodes().isEmpty()) {
                                    Collections.sort(value.getNodes(), new SortbyLabel());
                                }
                                sons.add(value);
                                father.setNodes(sons);
                                father.setCounter1WithChild(father.getCounter1WithChild() + value.getCounter1WithChild());
                                father.setNbNodesWithChild(father.getNbNodesWithChild() + 1);
                                father.setNbOK(father.getNbOK() + value.getNbOK());
                                father.setNbKO(father.getNbKO() + value.getNbKO());
                                father.setNbFA(father.getNbFA() + value.getNbFA());
                                father.setNbNA(father.getNbNA() + value.getNbNA());
                                father.setNbNE(father.getNbNE() + value.getNbNE());
                                father.setNbWE(father.getNbWE() + value.getNbWE());
                                father.setNbPE(father.getNbPE() + value.getNbPE());
                                father.setNbQE(father.getNbQE() + value.getNbQE());
                                father.setNbQU(father.getNbQU() + value.getNbQU());
                                father.setNbCA(father.getNbCA() + value.getNbCA());
                                nodeList.put(father.getId(), father);
                            } else {
                                if (value.getNodes() != null && !value.getNodes().isEmpty()) {
//                            List<TreeNode> newTree = next.getNodes();
                                    Collections.sort(value.getNodes(), new SortbyLabel());
//                            next.setNodes(newTree);
                                }
                                // Father does not exist so we attach it to root.
                                finalList.add(value);
                                listToRemove.add(value);
                            }
                            listToRemove.add(value);
                            treeParent.remove(value);
                        }
                    }
                }
                // Removing all entries that has been clasified to finalList.
//                LOG.debug("To remove : " + listToRemove);
                for (TreeNode label : listToRemove) {
                    nodeList.remove(label.getId());
                }
                i++;
            }

            // We now sort the root level (other levels were already sorted).
            Collections.sort(finalList, new SortbyLabel());

        } catch (Exception e) {
            LOG.error("Exception in hierarchyConstructor.", e);
        }
        return finalList;
    }

    class SortbyLabel implements Comparator<TreeNode> {
        // Used for sorting in ascending order of
        // Label name.

        @Override
        public int compare(TreeNode a, TreeNode b) {
            if (a != null && b != null & a.getLabel() != null) {
                return a.getLabel().compareToIgnoreCase(b.getLabel());
            } else {
                return 1;
            }
        }
    }

}
