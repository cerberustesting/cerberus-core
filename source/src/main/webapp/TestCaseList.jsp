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
    Test Case list, on the Alpine table component (js/global/crbTable.js).

    Replaced the DataTables version on 2026-08-29 after validation. The previous
    implementation is still served at TestCaseListV1.jsp (loading
    js/pages/TestCaseList.js) - to roll back, copy TestCaseListV1.jsp over this
    file. Both read the same ReadTestCase endpoint, so no data or Java change is
    involved either way.
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html class="h-full">
<head>
    <meta name="active-menu" content="maintain">
    <meta name="active-submenu" content="TestCaseList.jsp">
    <meta name="active-page" content="TestCaseList.jsp">
    <meta name="page" content="Test Case List">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <%@ include file="include/global/dependenciesInclusions.html" %>
    <%--
        Page-specific dependencies. These are NOT part of the global includes and
        must match what the V1 page loaded, because the shared modals this page
        opens depend on them at call time:
          - tinymce           : the rich Detailed Description editor inside the
                                Edit Test Case Header modal. Without it,
                                initModalTestCase() throws "tinymce is not defined"
                                and NO action modal opens at all.
          - bootstrap-treeview: the Label tab's tree inside that same modal.
          - TestCaseSimpleExecution : the Run action's execution modal.
        Losing any of these breaks buttons silently, so keep this block in sync
        with TestCaseListV1.jsp for as long as that fallback exists.
    --%>
    <script type="text/javascript" src="dependencies/Tinymce-6.7.0/tinymce.min.js"></script>
    <script type="text/javascript" src="dependencies/Bootstrap-treeview-1.2.0/js/bootstrap-treeview.js"></script>
    <script type="text/javascript" src="js/transversalobject/TestCaseSimpleExecution.js?v=${appVersion}"></script>
    <script type="text/javascript" src="js/pages/TestCaseListV2.js?v=${appVersion}"></script>

    <title id="pageTitle">Test Case</title>
</head>
<body x-data x-cloak class="crb_body">
<jsp:include page="include/global/header2.html"/>
<jsp:include page="include/global/modalInclusions.jsp"/>
<jsp:include page="include/global/rightPanel.html"/>
<main class="crb_main_wrp" :class="$store.rightPanel.isResizing ? '' : 'transition-all duration-200'"
      :style="{marginLeft: ($store.sidebar.hidden ? 0 : ($store.sidebar.expanded ? 288 : 80)) + 'px',
                      width: 'calc(100vw - ' + ($store.sidebar.hidden ? 0 : ($store.sidebar.expanded ? 288 : 80))
                          + 'px - '+ ($store.rightPanel.open ? $store.rightPanel.width : 0) + 'px)'}">
        <%@ include file="include/global/messagesArea.html"%>
        <%@ include file="include/utils/modal-confirmation.html"%>

        <h1 class="page-title-line" id="title">Test Case</h1>
        <div>
            <div id="testCaseList"></div>
            <div class="marginBottom20"></div>
        </div>

        <%--
            Modal markup this page's actions target. These are NOT in
            modalInclusions.jsp - the V1 page pulled them in itself, and each one
            is the listener for an action the table fires:
              TestCaseSimpleExecution   <- the Run button ('open-execution' event)
              TestCaseListMassActionUpdate <- mass "Update" ('mass-update-open')
              TestCaseListMassActionLabel  <- mass "Label"  ('mass-label-open')
              importTestCaseFromTestLink   <- the TestLink import flow
            Dropping any of them leaves the button working but nothing listening,
            so the click silently does nothing. Keep in sync with TestCaseListV1.jsp.

            Note: V1 also included include/templates/selectDropdown.html here even
            though modalInclusions.jsp already pulls it in. Not repeated, to avoid
            injecting that template's markup (and its ids) into the page twice.
        --%>
        <jsp:include page="include/transversal/TestCaseSimpleExecution.html"/>
        <jsp:include page="include/transversal/TestCaseListMassActionUpdate.html"/>
        <jsp:include page="include/transversal/TestCaseListMassActionLabel.html"/>
        <jsp:include page="include/pages/testcaselist/importTestCaseFromTestLink.html"/>
        <footer class="footer">
            <div class="container-fluid" id="footer"></div>
        </footer>
        <jsp:include page="include/global/aiBottomBar.html"/>
    </main>
</body>
</html>
