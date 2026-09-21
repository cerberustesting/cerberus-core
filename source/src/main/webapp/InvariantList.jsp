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
    <%--
        V2 page: both lists are js/global/crbTable.js driven by InvariantListV2.js.
        InvariantList.js (V1) is NOT loaded. InvariantListV1.jsp is the rollback copy.
    --%>
    <script type="text/javascript" src="js/pages/InvariantListV2.js?v=${appVersion}"></script>
</head>
<body x-data x-cloak class="crb_body">
    <jsp:include page="include/global/header2.html"/>
    <jsp:include page="include/global/modalInclusions.jsp"/>
        <jsp:include page="include/global/rightPanel.html"/>
    <main class="crb_main_wrp" :class="$store.rightPanel.isResizing ? '' : 'transition-all duration-200'"
              :style="{marginLeft: ($store.sidebar.hidden ? 0 : ($store.sidebar.expanded ? 288 : 80)) + 'px',
                      width: 'calc(100vw - ' + ($store.sidebar.hidden ? 0 : ($store.sidebar.expanded ? 288 : 80))
                          + 'px - '+ ($store.rightPanel.open ? $store.rightPanel.width : 0) + 'px)'}">
        <%@ include file="include/transversal/Invariant.html"%>
        <jsp:include page="include/templates/selectDropdown.html"/>

        <h1 class="page-title-line" x-text="$store.labels.getLabel('pageInvariant','title')">Invariants</h1>
        <%-- Subtitle line dropped: none of the other migrated list pages carries one. --%>

        <div x-data="{ tab: 'public' }" class="w-full">
            <!-- Tabs -->
            <%-- Shared tab bar (.crb_tabs, components.css). --%>
            <div class="crb_tabs">
                <!-- Public -->
                <button @click="tab = 'public';displayPublicTable();"
                        :class="tab === 'public' ? 'crb_tab--active' : ''"
                        class="crb_tab">
                    <i data-lucide="users" class="w-4 h-4"></i>Public
                </button>
                <!-- Private -->
                <button @click="tab = 'private';displayPrivateTable();"
                        :class="tab === 'private' ? 'crb_tab--active' : ''"
                        class="crb_tab">
                    <i data-lucide="lock" class="w-4 h-4"></i>Private
                </button>
            </div>
            <!-- Content Public -->
            <div x-show="tab === 'public'" x-cloak>
                <div id="invariantList"></div>
            </div>
            <!-- Content Private -->
            <div x-show="tab === 'private'" x-cloak>
                <div id="invariantPrivateList"></div>
            </div>
        </div>
        <footer class="footer">
            <div class="container-fluid" id="footer"></div>
        </footer>
    </main>
</body>
</html>