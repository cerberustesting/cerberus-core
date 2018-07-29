/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
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
package org.cerberus.crud.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.cerberus.crud.dao.ILabelDAO;
import org.cerberus.crud.entity.Label;
import org.cerberus.engine.entity.MessageGeneral;
import org.cerberus.crud.service.ILabelService;
import org.cerberus.dto.TreeNode;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
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

    private static final Logger LOG = LogManager.getLogger("LabelService");

    private final String OBJECT_NAME = "Label";

    @Override
    public AnswerItem readByKey(Integer id) {
        return labelDAO.readByKey(id);
    }

    @Override
    public AnswerList readAll() {
        return readByVariousByCriteria(new ArrayList<>(), false, new ArrayList<>(), 0, 0, "Label", "asc", null, null);
    }

    @Override
    public AnswerList readBySystem(List<String> system) {
        return labelDAO.readBySystemByCriteria(system, false, new ArrayList<>(), 0, 0, "Label", "asc", null, null);
    }

    @Override
    public AnswerList readByVarious(List<String>  system, List<String>  type) {
        return labelDAO.readBySystemByCriteria(system, false, type, 0, 0, "Label", "asc", null, null);
    }

    @Override
    public AnswerList readByCriteria(int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch) {
        return labelDAO.readBySystemByCriteria(new ArrayList<>(), false, new ArrayList<>(), startPosition, length, columnName, sort, searchParameter, individualSearch);
    }

    @Override
    public AnswerList readByVariousByCriteria(List<String> system, boolean strictSystemFilter, List<String> type, int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch) {
        return labelDAO.readBySystemByCriteria(system, strictSystemFilter, type, startPosition, length, columnName, sort, searchParameter, individualSearch);
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
                Label parentLabel = (Label) answerLabelParent.getItem();
                if ((!parentLabel.getSystem().equals(object.getSystem())) && (!StringUtil.isNullOrEmpty(parentLabel.getSystem()))) {
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
                if ((parentLabel.getId().equals(object.getParentLabelID())) && (object.getId().equals(parentLabel.getParentLabelID()))) {
                    // Parent & Child have different types.
                    msg.setDescription(msg.getDescription()
                            .replace("%LABEL%", object.getLabel())
                            .replace("%LABELPARENT%", parentLabel.getLabel())
                            .replace("%DESCRIPTION%", "'" + parentLabel.getLabel() + "' is already attached to '" + object.getLabel()+ "' and recursive links are not allowed"));
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
    public Label convert(AnswerItem<Label> answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return (Label) answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<Label> convert(AnswerList<Label> answerList) throws CerberusException {
        if (answerList.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return (List<Label>) answerList.getDataList();
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
    public AnswerList<List<String>> readDistinctValuesByCriteria(String system, String searchParameter, Map<String, List<String>> individualSearch, String columnName) {
        return labelDAO.readDistinctValuesByCriteria(system, searchParameter, individualSearch, columnName);
    }
    
    @Override
    public List<TreeNode> hierarchyConstructor(HashMap<Integer, TreeNode> inputList) {
        // Preparing data structure.
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

        // Loop on maximum hierarchy levels.
        int i = 0;
        while (i < 50 && !nodeList.isEmpty()) {
//                LOG.debug(i + ".start : " + nodeList);
            List<TreeNode> listToRemove = new ArrayList<>();

            for (Map.Entry<Integer, TreeNode> entry : nodeList.entrySet()) {
                Integer key = entry.getKey();
                TreeNode value = entry.getValue();
//                    LOG.debug(value.getId() + " " + value.getParentId() + " " + value.getNodes().size());

                boolean hasChild = false;
                for (TreeNode treeNode : treeParent) {
                    if (treeNode.getParentId() == value.getId()) {
                        hasChild = true;
                    }
                }

                if (!hasChild) {
                    if ((i == 0) && (value.getNodes().isEmpty())) {
                        value.setNodes(null);
                    }
//                        LOG.debug("Pas de fils.");
                    if (value.getParentId() <= 0) {
//                            LOG.debug("Adding to final result and remove from list." + i);
                        finalList.add(value);
                        listToRemove.add(value);
                    } else {
//                            LOG.debug("Moving to parent and remove from list." + i);
                        // Mettre sur le fils sur son pere.
                        TreeNode father = nodeList.get(value.getParentId());
                        if (father != null) {
                            List<TreeNode> sons = father.getNodes();
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
                            nodeList.put(key, father);
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

        return finalList;
    }
    

}
