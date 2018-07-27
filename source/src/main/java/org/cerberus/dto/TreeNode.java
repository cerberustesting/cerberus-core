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
package org.cerberus.dto;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author cerberus
 */
public class TreeNode {

    private String key;
    private Integer id;
    private Integer parentId;
    private String text;
    private String icon;
    private String href;
    private boolean selectable;
    private List<TreeNode> nodes;
    private List<String> tags;
    private Integer counter1;

    private static final Logger LOG = LogManager.getLogger(TreeNode.class);
    
    public TreeNode(String key,Integer id, Integer parentId, String text, String icon, String href, boolean selectable) {
        this.key = key;
        this.id = id;
        this.parentId = parentId;
        this.text = text;
        this.icon = icon;
        this.href = href;
        this.selectable = selectable;
        this.nodes = new ArrayList<>();
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Integer getCounter1() {
        return counter1;
    }

    public void setCounter1(Integer counter1) {
        this.counter1 = counter1;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
    
    
    public List<TreeNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<TreeNode> nodes) {
        this.nodes = nodes;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public boolean isSelectable() {
        return selectable;
    }

    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }

    public JSONObject toJson() {
        JSONObject result = new JSONObject();
        try {
            result.put("id", this.getId());
            result.put("text", this.getText());
            result.put("icon", this.getIcon());
            result.put("href", this.getHref());
            result.put("selectable", this.isSelectable());
            JSONArray array = new JSONArray();
            if (this.getNodes() != null) {
                for (Object childList : this.getNodes()) {
                    array.put(((TreeNode) childList).toJson());
                }
            }
            result.put("nodes", array);
        } catch (JSONException ex) {
            LOG.error(ex.toString());
        }
        return result;
    }

    @Override
    public String toString() {
        return key;
    }

}
