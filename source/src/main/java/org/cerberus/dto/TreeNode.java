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
    private Integer nbNodesWithChild;
    private String nbNodesText;
    private Integer counter1;
    private String counter1Text;
    private Integer counter1WithChild;
    private String counter1WithChildText;

    private static final Logger LOG = LogManager.getLogger(TreeNode.class);

    public TreeNode(String key, Integer id, Integer parentId, String text, String icon, String href, boolean selectable) {
        this.key = key;
        this.id = id;
        this.parentId = parentId;
        this.text = text;
        this.icon = icon;
        this.href = href;
        this.selectable = selectable;
        this.nodes = new ArrayList<>();
        this.nbNodesWithChild = 0;
    }

    public String getCounter1WithChildText() {
        return counter1WithChildText;
    }

    public void setCounter1WithChildText(String counter1WithChildText) {
        this.counter1WithChildText = counter1WithChildText;
    }

    public String getNbNodesText() {
        return nbNodesText;
    }

    public void setNbNodesText(String nbNodesText) {
        this.nbNodesText = nbNodesText;
    }

    public String getCounter1Text() {
        return counter1Text;
    }

    public void setCounter1Text(String counter1Text) {
        this.counter1Text = counter1Text;
    }

    public Integer getNbNodesWithChild() {
        return nbNodesWithChild;
    }

    public void setNbNodesWithChild(Integer nbNodesWithChild) {
        this.nbNodesWithChild = nbNodesWithChild;
    }

    public Integer getCounter1WithChild() {
        return counter1WithChild;
    }

    public void setCounter1WithChild(Integer counter1WithChild) {
        this.counter1WithChild = counter1WithChild;
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

            String cnt1Text = "";
            if (this.getCounter1() > 0) {
                cnt1Text = this.getCounter1Text();
            }
            String cnt1WCText = "";
            if ((this.getCounter1WithChild() > 0) && (this.getCounter1WithChild() != this.getCounter1())) {
                cnt1WCText = this.getCounter1WithChildText();
            }
            String nbNodText = "";
            if (this.getNbNodesWithChild() > 0) {
                nbNodText = this.getNbNodesText();
            }
            result.put(
                    "text", this.getText().replace("%COUNTER1TEXT%", cnt1Text)
                            .replace("%COUNTER1WITHCHILDTEXT%", cnt1WCText)
                            .replace("%NBNODESWITHCHILDTEXT%", nbNodText)
                            .replace("%COUNTER1%", this.getCounter1().toString())
                            .replace("%COUNTER1WITHCHILD%", this.getCounter1WithChild().toString())
                            .replace("%NBNODESWITHCHILD%", this.getNbNodesWithChild().toString())
            );
            result.put("icon", this.getIcon());
            result.put("href", this.getHref());
            result.put("selectable", this.isSelectable());
            result.put("nbNodesWithChild", this.getNbNodesWithChild());
            result.put("counter1", this.getCounter1());
            result.put("counter1WithChild", this.getCounter1WithChild());
            result.put("tags", this.getTags());
            if (this.getNodes() != null) {
                JSONArray array = new JSONArray();
                for (Object childList : this.getNodes()) {
                    array.put(((TreeNode) childList).toJson());
                }
                result.put("nodes", array);
            }
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
