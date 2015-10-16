<%-- 
    Document   : RunTest2
    Created on : 14 oct. 2015, 16:07:31
    Author     : cerberus
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@ include file="include/dependenciesInclusions.html" %>
        <script type="text/javascript" src="js/pages/RunTest.js"></script>
        <title>Run Test</title>
    </head>
    <body>
        <%@ include file="include/header.html"%>
        <div class="container-fluid center" id="page-layout">
            <h1 class="page-title-line">Run Test</h1>

            <div class="panel panel-default">
                <div class="panel-heading card" data-toggle="collapse" data-target="#testFilters">
                    <span class="toggle glyphicon glyphicon-chevron-right pull-right"></span>
                    Filters
                </div>
                <div class="panel-body collapse in" id="testFilters">
                    <select class="form-control" multiple="multiple" id="system" ></select>  
                </div>
            </div>

            <div class="panel panel-default">
                <div class="panel-heading">
                    Choose Test
                </div>
                <div class="panel-body" id="chooseTest">

                </div>
            </div>

            <div class="panel panel-default">
                <div class="panel-heading">
                    Environment and country settings
                </div>
                <div class="panel-body" id="envCountrySettings">
                    <div class="row">
                        <div class="col-lg-6">
                            <label class="bold">Environment :</label>
                            <select multiple class="form-control">
                                <option>1</option>
                                <option>2</option>
                                <option>3</option>
                                <option>4</option>
                                <option>5</option>
                            </select>
                        </div>
                        <div class="col-lg-6">
                            <label for="countryList" id="countryListLabel" class="bold">Country List :</label>
                            <div id="countryList" name="countryList" style="padding-top: 4%;"></div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="col-lg-5">
                    <div class="panel panel-default">
                        <div class="panel-heading">
                            Robot settings
                        </div>
                        <div class="panel-body" id="robotSettings">
                            <form class="form-horizontal" id="robotSettingsForm">
                                <div class="form-group">
                                    <label for="robotConfig" class="col-sm-3 control-label bold">Select Robot Config</label>
                                    <div class="col-sm-9">
                                        <select type="robotConfig" class="form-control input-sm" id="robotConfig" name="robotConfig"></select>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label for="seleniumIP" class="col-sm-3 control-label bold">Selenium Server IP</label>
                                    <div class="col-sm-9">
                                        <input type="seleniumIP" class="form-control input-sm" id="seleniumIP" name="seleniumIP"/>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label for="seleniumPort" class="col-sm-3 control-label bold">Selenium Server Port</label>
                                    <div class="col-sm-9">
                                        <input type="seleniumPort" class="form-control input-sm" id="seleniumPort" name="seleniumPort"/>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label for="browser" class="col-sm-3 control-label bold">Browser</label>
                                    <div class="col-sm-9">
                                        <select type="browser" class="form-control input-sm" id="browser" name="browser" multiple></select>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label for="version" class="col-sm-3 control-label">Version (Optional)</label>
                                    <div class="col-sm-9">
                                        <input type="version" class="form-control input-sm" id="version" name="version"/>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label for="platform" class="col-sm-3 control-label">Platform (Optional)</label>
                                    <div class="col-sm-9">
                                        <select type="platform" class="form-control input-sm" id="platform" name="platform"></select>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label for="screenSize" class="col-sm-3 control-label bold">Screen Size</label>
                                    <div class="col-sm-9">
                                        <select type="screenSize" class="form-control input-sm" id="screenSize" name="screenSize"></select>
                                    </div>
                                </div>
                            </form>
                            <div class="col-sm-offset-3 col-sm-9">
                                <button class="btn btn-default btn-sm pull-right" id="saveRobotPreferences">Record my Robot Preferences</button>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="col-lg-5">
                    <div class="panel panel-default">
                        <div class="panel-heading">
                            Execution settings
                        </div>
                        <div class="panel-body" id="executionSettings">
                            <form class="form-horizontal"id="executionSettingsForm">
                                <div class="form-group">
                                    <label for="tag" class="col-sm-3 control-label bold">Tag</label>
                                    <div class="col-sm-9">
                                        <input type="tag" class="form-control input-sm" id="tag" name="tag"/>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label for="outputFormat" class="col-sm-3 control-label bold">Output Format</label>
                                    <div class="col-sm-9">
                                        <select type="outputFormat" class="form-control input-sm" id="outputFormat" name="outputFormat"></select>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label for="verbose" class="col-sm-3 control-label bold">Verbose</label>
                                    <div class="col-sm-9">
                                        <select type="verbose" class="form-control input-sm" id="verbose" name="verbose"></select>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label for="screenshot" class="col-sm-3 control-label bold">Screenshot</label>
                                    <div class="col-sm-9">
                                        <select type="screenshot" class="form-control input-sm" id="screenshot" name="screenshot"></select>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label for="pageSource" class="col-sm-3 control-label bold">Page Source</label>
                                    <div class="col-sm-9">
                                        <select type="pageSource" class="form-control input-sm" id="pageSource" name="pageSource"></select>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label for="seleniumLog" class="col-sm-3 control-label bold">Selenium Log</label>
                                    <div class="col-sm-9">
                                        <select type="seleniumLog" class="form-control input-sm" id="seleniumLog" name="seleniumLog"></select>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label for="synchroneous" class="col-sm-3 control-label bold">Synchroneous</label>
                                    <div class="col-sm-9">
                                        <select type="synchroneous" class="form-control input-sm" id="synchroneous" name="synchroneous"></select>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label for="timeout" class="col-sm-3 control-label bold">Timeout</label>
                                    <div class="col-sm-9">
                                        <input type="timeout" class="form-control input-sm" id="timeout" name="timeout"/>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label for="retries" class="col-sm-3 control-label bold">Retries</label>
                                    <div class="col-sm-9">
                                        <select type="retries" class="form-control input-sm" id="retries" name="retries"></select>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label for="manualExecution" class="col-sm-3 control-label bold">Manual Execution</label>
                                    <div class="col-sm-9">
                                        <select type="manualExecution" class="form-control input-sm" id="manualExecution" name="manualExecution"></select>
                                    </div>
                                </div>
                            </form>
                            <div class="col-sm-offset-3 col-sm-9">
                                <button type="" class="btn btn-default btn-sm pull-right" id="saveExecutionParams">Record my Execution Parameters</button>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="col-lg-2">
                    <p>Launching N tests on the environments : TATA, TITI, TOTO in the countries : FR, IT, BE on the browser : firefox, chrome, android</p>
                    <div style="padding-top: 15px;">
                        <button type="button" class="btn btn-primary btn-lg btn-block">Run</button>
                    </div>
                </div>
            </div>

            <footer class="footer">
                <div class="container-fluid" id="footer"></div>
            </footer>
        </div>
    </body>
</html>
