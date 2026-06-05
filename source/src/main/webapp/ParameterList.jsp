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
        <script type="text/javascript" src="js/pages/ParameterList.js?v=${appVersion}"></script>

    </head>
    <body x-data x-cloak class="crb_body">
        <jsp:include page="include/global/header2.html"/>
        <jsp:include page="include/global/modalInclusions.jsp"/>
        <main class="crb_main" :class="$store.sidebar.expanded ? 'crb_main_sidebar-expanded' : 'crb_main_sidebar-collapsed'">
            <%@ include file="include/global/messagesArea.html"%>
            <%@ include file="include/utils/modal-confirmation.html"%>


            <h1 class="page-title-line" x-text="$store.labels.getLabel('pageParameter','title')">Invariants</h1>
            <p class="page-subtitle-line" x-text="$store.labels.getLabel('pageParameter','subtitle')">Manage the application’s constants and fixed elements.</p>

            <div x-data="{ tab: 'all' }" class="w-full">
                <!-- Tabs -->
                <div class="w-full flex bg-slate-200 dark:bg-slate-700 p-1 rounded-lg shadow-sm mb-8 h-10">
                    <!-- All -->
                    <button @click="tab = 'all';displayAllParametersTable();"
                            :class="tab === 'all' ? 'bg-slate-50 font-semibold dark:bg-slate-900' : 'bg-slate-200 dark:bg-slate-700 text-slate-700 hover:text-slate-900 dark:text-slate-300 dark:hover:text-white'"
                            class="flex-1 flex items-center justify-center gap-2 px-4 py-3 rounded-md transition-colors duration-200">
                        <i data-lucide="list" class="w-4 h-4"></i>All
                    </button>
                    <!-- AI -->
                    <button @click="tab = 'ai';displayFilteredParametersTable('ai');"
                            :class="tab === 'ai' ? 'bg-slate-50 font-semibold dark:bg-slate-900' : 'bg-slate-200 dark:bg-slate-700 text-slate-700 hover:text-slate-900 dark:text-slate-300 dark:hover:text-white'"
                            class="flex-1 flex items-center justify-center gap-2 px-4 py-3 rounded-md transition-colors duration-200">
                        <i data-lucide="bot" class="w-4 h-4"></i>AI
                    </button>
                    <!-- SMTP -->
                    <button @click="tab = 'smtp';displayFilteredParametersTable('smtp');"
                            :class="tab === 'smtp' ? 'bg-slate-50 font-semibold dark:bg-slate-900' : 'bg-slate-200 dark:bg-slate-700 text-slate-700 hover:text-slate-900 dark:text-slate-300 dark:hover:text-white'"
                            class="flex-1 flex items-center justify-center gap-2 px-4 py-3 rounded-md transition-colors duration-200">
                        <i data-lucide="mail" class="w-4 h-4"></i>SMTP
                    </button>
                </div>
                <div id="parameterList">
                        <table id="parametersTable" class="table table-hover display" name="parametersTable"></table>
                        <div class="marginBottom20"></div>
                </div>

            </div>
            <footer class="footer">
                <div class="container-fluid" id="footer"></div>
            </footer>
        </main>
    </body>
</html>