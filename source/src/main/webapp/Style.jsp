<%--

    Cerberus Copyright (C) 2013 - 2025 cerberustesting
    DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.

    This file is part of Cerberus.

    Cerberus is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Cerberus is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%--
    Document   : ReportingExecutionByTag2
    Created on : 3 août 2015, 11:02:49
    Author     : cerberus
--%>

<%@page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@ include file="include/global/dependenciesInclusions.html" %>
        <script type="text/javascript" src="dependencies/Tinymce-6.7.0/tinymce.min.js"></script>
        <script type="text/javascript" src="dependencies/D3js-3.x.x/js/d3.min.js"></script>
        <script type="text/javascript" src="dependencies/D3-tip-0.6.7/js/index.js"></script>
        <script type="text/javascript" src="dependencies/Bootstrap-treeview-1.2.0/js/bootstrap-treeview.js"></script>
        <script type="text/javascript" src="js/pages/Style.js"></script>
        <script type="text/javascript" src="js/transversalobject/TestCaseExecutionQueue.js"></script>
        <script type="text/javascript" src="js/transversalobject/TestCase.js"></script>
        <script type="text/javascript" src="js/transversalobject/Campaign.js"></script>
        <script type="text/javascript" src="js/transversalobject/Application.js"></script>
        <title id="pageTitle">Style Page</title>
    </head>
    <body>
        <jsp:include page="include/global/header2.html"/>
        <div class="container-fluid center" id="page-layout">
            <%@ include file="include/global/messagesArea.html" %>
            <%@ include file="include/transversalobject/TestCaseExecutionQueue.html" %>
            <%@ include file="include/transversalobject/TestCase.html" %>
            <%@ include file="include/transversalobject/Campaign.html" %>
            <%@ include file="include/transversalobject/Application.html" %>
            <%@ include file="include/pages/testcampaign/viewStatcampaign.html" %>

            <h1 class="page-title-line" id="title">Reporting</h1>
            <div class="row">

                <div class="col-lg-6" id="FiltersPanel">
                    <div class="panel panel-default">
                        <div class="panel-heading card">
                            <span class="fa fa-tag fa-fw"></span>
                            <label id="filters">Filters</label>
                        </div>
                        <div class="panel-body">
                            <div class="row">
                                <div class="col-lg-12" id="filterContainer">

                                    <label for="selectTag">Tag :</label>
                                    <div class="input-group">
                                        <select class="form-control col-lg-7" name="Tag" id="selectTag"></select>
                                        <span class="input-group-btn">
                                            <button type="button" class="btn btn-sm btn-default" style="margin-left: 10px;" id="loadbutton" onclick="loadAllReports()">
                                                <span class="glyphicon glyphicon-refresh"></span> Load
                                            </button>
                                        </span>
                                    </div>

                                    <div class="row">
                                        <div class="col-lg-12">
                                            <label id="countryLabel" class="bold marginTop10">Country :</label>
                                            <button id="countrySelectAll" class="glyphicon glyphicon-check btn-default"
                                                    title="select all countries"></button>
                                            <button id="countryUnselectAll" class="glyphicon glyphicon-unchecked btn-default"
                                                    title="unselect all countries"></button>
                                            <button class="btn btn-default pull-right marginTop10" type="button" data-toggle="collapse" data-target="#countryFilter" aria-expanded="true" aria-controls="col1">
                                                <span class="glyphicon glyphicon-chevron-down"></span>
                                            </button>

                                            <div class="form-group collapse in" id="countryFilter">
                                            </div>
                                        </div>
                                    </div>

                                    <label id="statusLabel" class="bold marginTop10">Status :</label>
                                    <button id="statusSelectAll" class="glyphicon glyphicon-check btn-default"
                                            title="select all status"></button>
                                    <button id="statusUnselectAll" class="glyphicon glyphicon-unchecked btn-default"
                                            title="unselect all status"></button>
                                    <div class="form-group marginBottom20" id="statusFilter">
                                        <label class="checkbox-inline">
                                            <input type="checkbox" name="OK" checked/>
                                            <span class="fontOK">OK</span>
                                        </label>
                                        <label class="checkbox-inline">
                                            <input type="checkbox" name="KO" checked/>
                                            <span class="fontKO">KO</span>
                                        </label>
                                        <label class="checkbox-inline">
                                            <input type="checkbox" name="FA" checked/>
                                            <span class="fontFA">FA</span>
                                        </label>
                                        <label class="checkbox-inline">
                                            <input type="checkbox" name="NA" checked/>
                                            <span class="fontNA">NA</span>
                                        </label>
                                        <label class="checkbox-inline">
                                            <input type="checkbox" name="NE" checked/>
                                            <span class="fontNE">NE</span>
                                        </label>
                                        <label class="checkbox-inline">
                                            <input type="checkbox" name="WE" checked/>
                                            <span class="fontWE">WE</span>
                                        </label>
                                        <label class="checkbox-inline">
                                            <input type="checkbox" name="PE" checked/>
                                            <span class="fontPE">PE</span>
                                        </label>
                                        <label class="checkbox-inline">
                                            <input type="checkbox" name="QU" checked/>
                                            <span class="fontQU">QU</span>
                                        </label>
                                        <label class="checkbox-inline">
                                            <input type="checkbox" name="QE" checked/>
                                            <span class="fontQE">QE</span>
                                        </label>
                                        <label class="checkbox-inline">
                                            <input type="checkbox" name="PA" checked/>
                                            <span class="fontQU">PA</span>
                                        </label>
                                        <label class="checkbox-inline">
                                            <input type="checkbox" name="CA" checked/>
                                            <span class="fontCA">CA</span>
                                        </label>
                                    </div>

                                    <label class="bold marginTop10">Display Stats By Folder :</label>
                                    <div class="btn-group btn-toggle marginRight10" id="displayFolder"> 
                                        <button class="btn btn-ON btn-xs btn-default">ON</button>
                                        <button class="btn btn-OFF btn-xs btn-info active">OFF</button>
                                    </div>

                                    <div class="input-group-btn">
                                        <a id="buttonDownloadPdfReport" class="pull-left marginTop10">
                                            <button type="button" id="buttonDownloadPdfReportButton" class="btn btn-default marginRight10">
                                                <span class="glyphicon glyphicon-floppy-save"></span> Download Report
                                            </button>
                                        </a>
                                        <a id="buttonSeeStatsCampaign" class="pull-left marginTop10">
                                            <button type="button" class="btn btn-default marginRight10">
                                                <span class="glyphicon glyphicon-stats"></span> Campaign History
                                            </button>
                                        </a>
                                        <a id="buttonRunCampaign" class="pull-left marginTop10">
                                            <button type="button" class="btn btn-default marginRight10">
                                                <span class="glyphicon glyphicon-forward"></span> (Re)Run Campaign
                                            </button>
                                        </a>

                                        <a id="buttonOpenQueue" class="pull-left marginTop10">
                                            <button type='button' class='btn btn-default marginRight10'>
                                                <span class='glyphicon glyphicon-list'></span> Open Queue
                                            </button>
                                        </a>                                        

                                    </div>



                                </div>
                            </div>
                        </div>
                    </div>


                </div>

                <div class="col-lg-6" id="ReportByStatusPanel">
                    <div class="panel panel-default">
                        <div class="panel-heading card refreshButtonHeader">
                            <span class="fa fa-pie-chart fa-fw"></span>
                            <label id="reportStatus">Report by Status</label>
                            <!--                            <span class="toggle glyphicon glyphicon-chevron-right pull-right"></span>-->
                        </div>
                        <div class="panel-body collapse in" id="ReportByStatus">
                            <div class="row">
                                <div class="col-xs-6" id="ReportByStatusTable">

                                    <div class="panel panelOK">
                                        <div class="panel-heading">
                                            <div class="row">
                                                <div class="col-xs-6 status">
                                                    <span class="glyphicon glyphicon-ok" style="margin-right: 5px;"></span>OK
                                                </div>
                                                <div class="col-xs-6 text-right">
                                                    <div class="total">7</div>
                                                </div>
                                            </div>
                                            <div class="row">
                                                <div class="percentage pull-right">Percentage : 63.64%</div>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="panel panelKO">
                                        <div class="panel-heading">
                                            <div class="row">
                                                <div class="col-xs-6 status">
                                                    <span class="glyphicon glyphicon-ok" style="margin-right: 5px;"></span>KO
                                                </div>
                                                <div class="col-xs-6 text-right">
                                                    <div class="total">7</div>
                                                </div>
                                            </div>
                                            <div class="row">
                                                <div class="percentage pull-right">Percentage : 63.64%</div>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="panel panelFA">
                                        <div class="panel-heading">
                                            <div class="row">
                                                <div class="col-xs-6 status">
                                                    <span class="glyphicon glyphicon-ok" style="margin-right: 5px;"></span>FA
                                                </div>
                                                <div class="col-xs-6 text-right">
                                                    <div class="total">7</div>
                                                </div>
                                            </div>
                                            <div class="row">
                                                <div class="percentage pull-right">Percentage : 63.64%</div>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="panel panelNA">
                                        <div class="panel-heading">
                                            <div class="row">
                                                <div class="col-xs-6 status">
                                                    <span class="glyphicon glyphicon-ok" style="margin-right: 5px;"></span>NA
                                                </div>
                                                <div class="col-xs-6 text-right">
                                                    <div class="total">7</div>
                                                </div>
                                            </div>
                                            <div class="row">
                                                <div class="percentage pull-right">Percentage : 63.64%</div>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="panel panelNE">
                                        <div class="panel-heading">
                                            <div class="row">
                                                <div class="col-xs-6 status">
                                                    <span class="glyphicon glyphicon-ok" style="margin-right: 5px;"></span>NE
                                                </div>
                                                <div class="col-xs-6 text-right">
                                                    <div class="total">7</div>
                                                </div>
                                            </div>
                                            <div class="row">
                                                <div class="percentage pull-right">Percentage : 63.64%</div>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="panel panelWE">
                                        <div class="panel-heading">
                                            <div class="row">
                                                <div class="col-xs-6 status">
                                                    <span class="glyphicon glyphicon-ok" style="margin-right: 5px;"></span>WE
                                                </div>
                                                <div class="col-xs-6 text-right">
                                                    <div class="total">7</div>
                                                </div>
                                            </div>
                                            <div class="row">
                                                <div class="percentage pull-right">Percentage : 63.64%</div>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="panel panelPE">
                                        <div class="panel-heading">
                                            <div class="row">
                                                <div class="col-xs-6 status">
                                                    <span class="glyphicon glyphicon-ok" style="margin-right: 5px;"></span>PE
                                                </div>
                                                <div class="col-xs-6 text-right">
                                                    <div class="total">7</div>
                                                </div>
                                            </div>
                                            <div class="row">
                                                <div class="percentage pull-right">Percentage : 63.64%</div>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="panel panelQU">
                                        <div class="panel-heading">
                                            <div class="row">
                                                <div class="col-xs-6 status">
                                                    <span class="glyphicon glyphicon-ok" style="margin-right: 5px;"></span>QU
                                                </div>
                                                <div class="col-xs-6 text-right">
                                                    <div class="total">7</div>
                                                </div>
                                            </div>
                                            <div class="row">
                                                <div class="percentage pull-right">Percentage : 63.64%</div>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="panel panelQE">
                                        <div class="panel-heading">
                                            <div class="row">
                                                <div class="col-xs-6 status">
                                                    <span class="glyphicon glyphicon-ok" style="margin-right: 5px;"></span>QE
                                                </div>
                                                <div class="col-xs-6 text-right">
                                                    <div class="total">7</div>
                                                </div>
                                            </div>
                                            <div class="row">
                                                <div class="percentage pull-right">Percentage : 63.64%</div>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="panel panelPA">
                                        <div class="panel-heading">
                                            <div class="row">
                                                <div class="col-xs-6 status"><span class="glyphicon glyphicon-pause blink" style="margin-right: 5px;"></span>PA</div>                                                
                                                <div class="col-xs-6 text-right">
                                                    <div class="total">7</div>
                                                </div>
                                            </div>
                                            <div class="row">
                                                <div class="percentage pull-right">Percentage : 63.64%</div>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="panel panelCA">
                                        <div class="panel-heading">
                                            <div class="row">
                                                <div class="col-xs-6 status">
                                                    <span class="glyphicon glyphicon-ok" style="margin-right: 5px;"></span>CA
                                                </div>
                                                <div class="col-xs-6 text-right">
                                                    <div class="total">7</div>
                                                </div>
                                            </div>
                                            <div class="row">
                                                <div class="percentage pull-right">Percentage : 63.64%</div>
                                            </div>
                                        </div>
                                    </div>

                                </div>
                            </div>
                            <div class="row">
                                <div class="col-xs-5" id="TagcampaignCICel1">

                                    <div class="row">
                                        <div class="col-xs-7">
                                            <label for="tagDetailCI">CI Result :</label>
                                            <div class="marginTop10" id="tagDetailCI">
                                            </div>
                                        </div>
                                        <div class="col-xs-5 marginTop20">
                                            <span class="input-group-btn">
                                                <button id="falseNegative" class="btn btn-default" title="Declare/Undeclare this campaign execution as a False Negative"><span class="glyphicon glyphicon-ok"></span></button>
                                            </span>
                                        </div>
                                    </div>
                                    <div class="marginTop10" id="extraKPI" >
                                    </div>

                                </div>
                                <div class="col-xs-6">
                                    <div class="marginTop10" id="tagDetailBar">
                                        <div class="progress" data-toggle="tooltip" data-html="true" title="" data-original-title="&lt;div&gt;
                                             &lt;span class='color-box statusOK'&gt;&lt;/span&gt;
                                             &lt;strong&gt; OK : &lt;/strong&gt;8&lt;/div&gt;&lt;div&gt;
                                             &lt;span class='color-box statusFA'&gt;&lt;/span&gt;
                                             &lt;strong&gt; FA : &lt;/strong&gt;2&lt;/div&gt;&lt;div&gt;
                                             &lt;span class='color-box statusQE'&gt;&lt;/span&gt;
                                             &lt;strong&gt; QE : &lt;/strong&gt;1&lt;/div&gt;&lt;/div&gt;"><div class="progress-bar statusOK" role="progressbar" style="width:72.72727272727273%;">72.7%</div><div class="progress-bar statusFA" role="progressbar" style="width:18.181818181818183%;">18.2%</div><div class="progress-bar statusQE" role="progressbar" style="width:9.090909090909092%;">9.1%</div></div>
                                    </div>
                                </div>
                            </div>


                            <div class="row" id="cancelTagRow">
                                <div class="col-xs-6 marginTop20" id="cancelTagButton">
                                    <span class="input-group-btn">
                                        <button id="cancelTag" class="btn btn-default" title="Cancel all non executed queue entries"><span class="glyphicon glyphicon-stop"></span> Cancel all non already triggered executions</button>
                                    </span>
                                </div>
                                <div class="col-xs-6 marginTop20" id="pauseTagButton">
                                    <span class="input-group-btn">
                                        <button id="pauseTag" class="btn btn-default" title="Pause all non executed queue entries"><span class="glyphicon glyphicon-pause"></span> Pause all non already triggered executions</button>
                                    </span>
                                </div>
                                <div class="col-xs-6 marginTop20" id="resumeTagButton">
                                    <span class="input-group-btn">
                                        <button id="resumeTag" class="btn btn-default" title="Resume all paused queue entries"><span class="glyphicon glyphicon-play"></span> Resume all paused executions</button>
                                    </span>
                                </div>
                            </div>


                        </div>
                    </div>


                </div>
            </div>

            <div class="row">
                <div class="col-sm-12">

                    <div class="panel panel-default">
                        <div class="panel-heading card refreshButtonHeader" data-toggle="collapse" data-target="#TagInfo">
                            <span class="fa fa-tag fa-fw"></span>
                            <label id="TagDetailLab">Tag Information</label>
                            <span class="toggle glyphicon glyphicon-chevron-right pull-right"></span>
                        </div>
                        <div class="panel-body collapse in" id="TagInfo">
                            <div class="row">
                                <div class="col-sm-4">
                                    <label for="startExe">Submitted :</label>
                                    <input type="text" class="form-control" name="submitted" id="submitted" readonly
                                           aria-describedby="basic-addon1">
                                </div>
                                <div class="col-sm-4">
                                    <label for="startExe">Started :</label>
                                    <input type="text" class="form-control" name="startExe" id="startExe" readonly
                                           aria-describedby="basic-addon1">
                                </div>
                                <div class="col-sm-2">
                                    <label for="endExe">End :</label>
                                    <input type="text" class="form-control" name="endExe" id="endExe" readonly
                                           aria-describedby="basic-addon1">
                                </div>
                                <div class="col-sm-2">
                                    <label for="durExe">Dur (Min) :</label>
                                    <input type="text" class="form-control" name="durExe" id="durExe" readonly
                                           aria-describedby="basic-addon1">
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-sm-6" id="TagDescCel">
                                    <label class="marginTop10" for="TagDesc">Description :</label>
                                    <div class="input-group">
                                        <textarea class="form-control wysiwyg" name="TagDesc" id="TagDesc" aria-describedby="basic-addon1"></textarea>
                                        <!--                                        <input type="text" class="form-control" name="TagDesc" id="TagDesc" readonly
                                                                                       aria-describedby="basic-addon1">-->
                                        <span class="input-group-btn">
                                            <button id="editTagDesc" class="btn btn-default" style="display : none;">Edit</button>
                                            <button id="saveTagDesc" class="btn btn-primary">Save</button>
                                        </span>
                                    </div>
                                    <!--                                    <input type="text" class="form-control" name="TagDesc" id="TagDesc" readonly aria-describedby="basic-addon1" >-->
                                </div>
                                <div class="col-sm-6" id="TagCommentCel">
                                    <label class="marginTop10" for="TagComment">Comment :</label>
                                    <div class="input-group">
                                        <textarea type="text" class="form-control" name="TagComment" id="TagComment" readonly
                                                  aria-describedby="basic-addon1"></textarea>
                                        <span class="input-group-btn">
                                            <button id="editTagComment" class="btn btn-default">Edit</button>
                                            <button id="saveTagComment" class="btn btn-primary" style="display : none;">Save</button>
                                        </span>
                                    </div>
                                </div>



                            </div>
                            <div class="row marginTop10">
                                <div class="col-sm-6" id="TagcampaignCel1">
                                    <label for="Tagcampaign">Campaign :</label>
                                    <div class="input-group">
                                        <input type="text" class="form-control" name="Tagcampaign" id="Tagcampaign" readonly aria-describedby="basic-addon1">
                                        <span class="input-group-btn">
                                            <a id="buttonEditCampaign">
                                                <button type="button" class="btn btn-default" >
                                                    <span class="glyphicon glyphicon-pencil"></span>
                                                </button>
                                            </a>
                                        </span>
                                    </div>
                                </div>

                                <div class="col-sm-6" id="xRayTestExecutionBlock">
                                    <label for="xRayTestExecution">JIRA Xray :</label>
                                    <div class="input-group">
                                        <input type="text" class="form-control" name="xRayTestExecution" id="xRayTestExecution" readonly aria-describedby="basic-addon1">
                                        <span class="input-group-btn">
                                            <a id="buttonJIRAXray">
                                                <button type="button" class="btn btn-default" >
                                                    <img src="./images/jira.png" width="20">
                                                </button>
                                            </a>
                                        </span>
                                    </div>
                                </div>
                            </div>

                        </div>
                    </div>
                </div>
            </div>


            <div class="row" id="reportByLabel">
                <div class="col-lg-12">
                    <div class="panel panel-default">
                        <div class="panel-heading card clearfix refreshButtonHeader">
                            <label id="envCountryBrowser">Report by Label</label>
                            <!--                            <span class="toggle glyphicon glyphicon-chevron-right pull-right"></span>-->
                            <ul class="nav nav-tabs pull-right">
                                <li class="active" id="stickers"><a>Stickers</a></li>
                                <li class="" id="requirements"><a>Requirements</a></li>
                            </ul>
                        </div>
                        <div class="panel-body collapse in" id="reportLabel">
                            <div class='marginTop20' id="mainTreeExeS"></div>
                            <div class='marginTop20' id="mainTreeExeR" style="display: none;"></div>
                        </div>
                    </div>
                </div>
            </div>


            <div class="row">
                <div class="col-lg-12" id="ListPanel">
                    <div class="panel panel-default">
                        <div class="panel-heading card refreshButtonsHeader" id="listPanelHeader">
                            <span class="glyphicon glyphicon-list"></span>
                            <label id="List">List</label>
                        </div>
                        <div class="panel-body collapse in" id="listReport">
                            <div id="tableArea">
                                <form id="massActionForm" name="massActionForm" title="" role="form">
                                    <table id="listTable" class="table display" name="listTable"></table>
                                </form>
                                <div class="marginBottom20"></div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>


            <footer class="footer">
                <div class="container-fluid" id="footer"></div>
            </footer>

        </div>
    </body>
</html>
