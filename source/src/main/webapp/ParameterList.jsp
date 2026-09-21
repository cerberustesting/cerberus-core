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
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html class="h-full">
    <head>
        <meta name="active-menu" content="admin">
        <meta name="active-submenu" content="ParameterList.jsp">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@ include file="include/global/dependenciesInclusions.html" %>
        <title id="pageTitle">Parameter</title>
        <%--
            V2 page: the list is js/global/crbTable.js driven by ParameterListV2.js.
            ParameterList.js (V1) is NOT loaded. ParameterListV1.jsp is the rollback copy.
        --%>
        <script type="text/javascript" src="js/pages/ParameterListV2.js?v=${appVersion}"></script>

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


            <h1 class="page-title-line" x-text="$store.labels.getLabel('pageParameter','title')">Invariants</h1>
            <%-- Subtitle line dropped: none of the other migrated list pages carries one. --%>

            <div x-data="{ tab: 'all' }" class="w-full">
                <!-- Tabs -->
                                <%-- Shared tab bar (.crb_tabs, components.css), same component as
                     Label.jsp / TestCaseScriptV2 / TestCaseExecutionV2 / the queue page. --%>
                <div class="crb_tabs">
                    <!-- All -->
                    <button @click="tab = 'all';displayAllParametersTable();"
                            :class="tab === 'all' ? 'crb_tab--active' : ''"
                            class="crb_tab">
                        <i data-lucide="list" class="w-4 h-4"></i>All
                    </button>
                    <!-- AI -->
                    <button @click="tab = 'ai';displayFilteredParametersTable('ai');"
                            :class="tab === 'ai' ? 'crb_tab--active' : ''"
                            class="crb_tab">
                        <i data-lucide="bot" class="w-4 h-4"></i>AI
                    </button>
                    <!-- SMTP -->
                    <button @click="tab = 'smtp';displayFilteredParametersTable('smtp');"
                            :class="tab === 'smtp' ? 'crb_tab--active' : ''"
                            class="crb_tab">
                        <i data-lucide="mail" class="w-4 h-4"></i>SMTP
                    </button>
                </div>
                <div id="parameterList"></div>

            </div>
            <footer class="footer">
                <div class="container-fluid" id="footer"></div>
            </footer>
        </main>
    </body>
</html>