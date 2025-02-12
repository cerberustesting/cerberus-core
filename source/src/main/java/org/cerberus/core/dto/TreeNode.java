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
package org.cerberus.core.dto;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.Label;
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
    private String system;
    private String label;
    private Integer parentId;
    private String text;
    private String icon;
    private String href;
    private boolean selectable;
    private boolean selected;
    private List<String> tags; // This is not Cerberus tags but GUI hierarchy tag.
    private List<TreeNode> nodes;
    private String type;
    private Integer nbNodesWithChild;
    private String nbNodesText;
    private Integer counter1;
    private String counter1Text;
    private Integer counter1WithChild;
    private String counter1WithChildText;
    private Integer nbOK;
    private Integer nbKO;
    private Integer nbFA;
    private Integer nbNA;
    private Integer nbNE;
    private Integer nbWE;
    private Integer nbPE;
    private Integer nbQE;
    private Integer nbQU;
    private Integer nbCA;
    private Integer nbPA;
    private Label labelObj;

    private static final Logger LOG = LogManager.getLogger(TreeNode.class);

    public TreeNode(String key, String system, String label, Integer id, Integer parentId, String text, String icon, String href, boolean selectable) {
        this.key = key;
        this.system = system;
        this.label = label;
        this.id = id;
        this.parentId = parentId;
        this.text = text;
        this.icon = icon;
        this.href = href;
        this.selectable = selectable;
        this.nodes = new ArrayList<>();
        this.nbNodesWithChild = 0;
        this.nbOK = 0;
        this.nbKO = 0;
        this.nbFA = 0;
        this.nbNA = 0;
        this.nbNE = 0;
        this.nbWE = 0;
        this.nbPE = 0;
        this.nbQE = 0;
        this.nbQU = 0;
        this.nbPA = 0;
        this.nbCA = 0;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public Label getLabelObj() {
        return labelObj;
    }

    public void setLabelObj(Label labelObj) {
        this.labelObj = labelObj;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Integer getNbOK() {
        return nbOK;
    }

    public void setNbOK(Integer nbOK) {
        this.nbOK = nbOK;
    }

    public Integer getNbKO() {
        return nbKO;
    }

    public void setNbKO(Integer nbKO) {
        this.nbKO = nbKO;
    }

    public Integer getNbFA() {
        return nbFA;
    }

    public void setNbFA(Integer nbFA) {
        this.nbFA = nbFA;
    }

    public Integer getNbNA() {
        return nbNA;
    }

    public void setNbNA(Integer nbNA) {
        this.nbNA = nbNA;
    }

    public Integer getNbPA() {
        return nbPA;
    }

    public void setNbPA(Integer nbPA) {
        this.nbPA = nbPA;
    }

    public Integer getNbNE() {
        return nbNE;
    }

    public void setNbNE(Integer nbNE) {
        this.nbNE = nbNE;
    }

    public Integer getNbWE() {
        return nbWE;
    }

    public void setNbWE(Integer nbWE) {
        this.nbWE = nbWE;
    }

    public Integer getNbPE() {
        return nbPE;
    }

    public void setNbPE(Integer nbPE) {
        this.nbPE = nbPE;
    }

    public Integer getNbQE() {
        return nbQE;
    }

    public void setNbQE(Integer nbQE) {
        this.nbQE = nbQE;
    }

    public Integer getNbQU() {
        return nbQU;
    }

    public void setNbQU(Integer nbQU) {
        this.nbQU = nbQU;
    }

    public Integer getNbCA() {
        return nbCA;
    }

    public void setNbCA(Integer nbCA) {
        this.nbCA = nbCA;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    private float getP(Integer a, Integer b) {
        float c = (a * (float) 100) / b;
        return c;
    }

    private Integer getPI(Integer a, Integer b) {
        float c = (a * (float) 100) / b;
        return Math.round(c);
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
            String statusBar = "";
            if (this.getCounter1WithChild() > 0) {
                statusBar = "<div style='margin-left: 5px; margin-right: 5px;' class=''>";
                statusBar += "<span class=\"progress-bar statusOK\" role=\"progressbar\" style=\"height : 20px;width:"
                        + getP(this.getNbOK(), this.getCounter1WithChild()) + "%\">" + getPI(this.getNbOK(), this.getCounter1WithChild()) + "%</span>";
                statusBar += "<span class=\"progress-bar statusKO\" role=\"progressbar\" style=\"height : 20px;width:"
                        + getP(this.getNbKO(), this.getCounter1WithChild()) + "%\">" + getPI(this.getNbKO(), this.getCounter1WithChild()) + "%</span>";
                statusBar += "<span class=\"progress-bar statusFA\" role=\"progressbar\" style=\"height : 20px;width:"
                        + getP(this.getNbFA(), this.getCounter1WithChild()) + "%\">" + getPI(this.getNbFA(), this.getCounter1WithChild()) + "%</span>";
                if (this.getNbNA() > 0) {
                    statusBar += "<span class=\"progress-bar statusNA\" role=\"progressbar\" style=\"height : 20px;width:"
                            + getP(this.getNbNA(), this.getCounter1WithChild()) + "%\">" + getPI(this.getNbNA(), this.getCounter1WithChild()) + "%</span>";
                }
                if (this.getNbNE() > 0) {
                    statusBar += "<span class=\"progress-bar statusNE\" role=\"progressbar\" style=\"height : 20px;width:"
                            + getP(this.getNbNE(), this.getCounter1WithChild()) + "%\">" + getPI(this.getNbNE(), this.getCounter1WithChild()) + "%</span>";
                }
                if (this.getNbWE() > 0) {
                    statusBar += "<span class=\"progress-bar statusWE\" role=\"progressbar\" style=\"height : 20px;width:"
                            + getP(this.getNbWE(), this.getCounter1WithChild()) + "%\">" + getPI(this.getNbWE(), this.getCounter1WithChild()) + "%</span>";
                }
                if (this.getNbPE() > 0) {
                    statusBar += "<span class=\"progress-bar statusPE\" role=\"progressbar\" style=\"height : 20px;width:"
                            + getP(this.getNbPE(), this.getCounter1WithChild()) + "%\">" + getPI(this.getNbPE(), this.getCounter1WithChild()) + "%</span>";
                }
                if (this.getNbQE() > 0) {
                    statusBar += "<span class=\"progress-bar statusQE\" role=\"progressbar\" style=\"height : 20px;width:"
                            + getP(this.getNbQE(), this.getCounter1WithChild()) + "%\">" + getPI(this.getNbQE(), this.getCounter1WithChild()) + "%</span>";
                }
                if (this.getNbQU() > 0) {
                    statusBar += "<span class=\"progress-bar statusQU\" role=\"progressbar\" style=\"height : 20px;width:"
                            + getP(this.getNbQU(), this.getCounter1WithChild()) + "%\">" + getPI(this.getNbQU(), this.getCounter1WithChild()) + "%</span>";
                }
                if (this.getNbCA() > 0) {
                    statusBar += "<span class=\"progress-bar statusCA\" role=\"progressbar\" style=\"height : 20px;width:"
                            + getP(this.getNbCA(), this.getCounter1WithChild()) + "%\">" + getPI(this.getNbCA(), this.getCounter1WithChild()) + "%</span>";
                }
                statusBar += "</div>";
            }

            result.put(
                    "text", this.getText().replace("%COUNTER1TEXT%", cnt1Text)
                            .replace("%COUNTER1WITHCHILDTEXT%", cnt1WCText)
                            .replace("%NBNODESWITHCHILDTEXT%", nbNodText)
                            .replace("%COUNTER1%", this.getCounter1().toString())
                            .replace("%COUNTER1WITHCHILD%", this.getCounter1WithChild().toString())
                            .replace("%NBNODESWITHCHILD%", this.getNbNodesWithChild().toString())
                            .replace("%STATUSBAR%", statusBar)
                            .replace("%NBOK%", this.getNbOK().toString())
                            .replace("%NBKO%", this.getNbKO().toString())
                            .replace("%NBFA%", this.getNbFA().toString())
                            .replace("%NBNA%", this.getNbNA().toString())
                            .replace("%NBNE%", this.getNbNE().toString())
                            .replace("%NBWE%", this.getNbWE().toString())
                            .replace("%NBPE%", this.getNbPE().toString())
                            .replace("%NBQE%", this.getNbQE().toString())
                            .replace("%NBQU%", this.getNbQU().toString())
                            .replace("%NBPA%", this.getNbPA().toString())
                            .replace("%NBCA%", this.getNbCA().toString())
            );
            result.put("icon", this.getIcon());
            result.put("href", this.getHref());
            result.put("selectable", this.isSelectable());
            JSONObject state = new JSONObject();
            state.put("selected", this.isSelected());
            result.put("state", state);
            result.put("nbNodesWithChild", this.getNbNodesWithChild());
            result.put("counter1", this.getCounter1());
            result.put("counter1WithChild", this.getCounter1WithChild());
            result.put("tags", this.getTags());
            if (this.getLabelObj() != null) {
                result.put("label", this.getLabelObj().toJsonGUI());
            }
            JSONObject stats = new JSONObject();
            stats.put("nbOK", this.nbOK);
            stats.put("nbKO", this.nbKO);
            stats.put("nbCA", this.nbCA);
            stats.put("nbPE", this.nbPE);
            stats.put("nbFA", this.nbFA);
            stats.put("nbNA", this.nbNA);
            stats.put("nbNE", this.nbNE);
            stats.put("nbQE", this.nbQE);
            stats.put("nbQU", this.nbQU);
            stats.put("nbPA", this.nbPA);
            stats.put("nbWE", this.nbWE);
            stats.put("nbElement", this.counter1);
            stats.put("nbElementWithChild", this.counter1WithChild);
            stats.put("nbNodesWithChild", this.nbNodesWithChild);
            result.put("stats", stats);
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
