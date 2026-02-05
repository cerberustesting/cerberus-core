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
    <meta name="active-submenu" content="InvariantList.jsp">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <%@ include file="include/global/dependenciesInclusions.html" %>
    <title>Invariant</title>
    <script type="text/javascript" src="js/pages/InvariantList.js"></script>
</head>
<body x-data x-cloak class="crb_body">
<jsp:include page="include/global/header2.html"/>
<jsp:include page="include/global/modalInclusions.jsp"/>
    <main class="crb_main" :class="$store.sidebar.expanded ? 'crb_main_sidebar-expanded' : 'crb_main_sidebar-collapsed'">
        <%@ include file="include/transversalobject/Invariant.html"%>
        <jsp:include page="include/templates/selectDropdown.html"/>

        <h1 class="page-title-line" x-text="$store.labels.getLabel('pageInvariant','title')">Invariants</h1>
        <p class="page-subtitle-line" x-text="$store.labels.getLabel('pageInvariant','subtitle')">Manage the application’s constants and fixed elements.</p>

        <div x-data="{ tab: 'public' }" class="w-full">
            <!-- Tabs -->
            <div class="w-full flex bg-slate-200 dark:bg-slate-700 p-1 rounded-lg shadow-sm mb-8 h-10">
                <!-- Public -->
                <button @click="tab = 'public';displayPublicTable();"
                        :class="tab === 'public' ? 'bg-slate-50 font-semibold dark:bg-slate-900' : 'bg-slate-200 dark:bg-slate-700 text-slate-700 hover:text-slate-900 dark:text-slate-300 dark:hover:text-white'"
                        class="flex-1 flex items-center justify-center gap-2 px-4 py-3 rounded-md transition-colors duration-200">
                    <i data-lucide="users" class="w-4 h-4"></i>Public
                </button>
                <!-- Private -->
                <button @click="tab = 'private';displayPrivateTable();"
                        :class="tab === 'private' ? 'bg-slate-50 font-semibold dark:bg-slate-900' : 'bg-slate-200 dark:bg-slate-700 text-slate-700 hover:text-slate-900 dark:text-slate-300 dark:hover:text-white'"
                        class="flex-1 flex items-center justify-center gap-2 px-4 py-3 rounded-md transition-colors duration-200">
                    <i data-lucide="lock" class="w-4 h-4"></i>Private
                </button>
            </div>
            <!-- Content Public -->
            <div x-show="tab === 'public'" class="">
                <div id="invariantList">
                    <table id="invariantsTable" class="table table-hover display"></table>
                </div>
            </div>
            <!-- Content Private -->
            <div x-show="tab === 'private'" class="">
                <div id="invariantPrivateList">
                    <table id="invariantsPrivateTable" class="table table-hover display"></table>
                </div>
            </div>
        </div>
        <footer class="footer">
            <div class="container-fluid" id="footer"></div>
        </footer>
    </main>
</body>
</html>