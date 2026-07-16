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
<%@page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html class="h-full">
    <head>
        <meta name="active-menu" content="insights">
        <meta name="active-submenu" content="ReportingAutomateScore.jsp">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@ include file="include/global/dependenciesInclusions.html" %>

        <script type="text/javascript" src="js/pages/ReportingAutomateScore.js?v=${appVersion}"></script>
        <link rel="stylesheet" type="text/css" href="css/pages/ReportingAutomateScore.css?v=${appVersion}"/>

        <title id="pageTitle">Automate Score</title>
    </head>
    <body x-data x-cloak class="crb_body" :class="$store.rightPanel.open ? 'rp-open' : ''">
        <jsp:include page="include/global/header2.html"/>
        <jsp:include page="include/global/modalInclusions.jsp"/>
        <jsp:include page="include/global/rightPanel.html"/>
        <main class="crb_main_wrp" :class="$store.rightPanel.isResizing ? '' : 'transition-all duration-200'"
              :style="{marginLeft: ($store.sidebar.hidden ? 0 : ($store.sidebar.expanded ? 288 : 80)) + 'px',
                      width: 'calc(100vw - ' + ($store.sidebar.hidden ? 0 : ($store.sidebar.expanded ? 288 : 80))
                          + 'px - '+ ($store.rightPanel.open ? $store.rightPanel.width : 0) + 'px)'}">
            <%@ include file="include/global/messagesArea.html" %>

            <div x-data="automateScore()" x-init="init()" class="v2as-page" id="automateScoreRoot">

                <!-- Page title above the bar, like every list page -->
                <div class="v2in-pagetitle">
                    <h1 class="page-title-line">Automate Score</h1>
                </div>

                <%@ include file="include/pages/reportingautomatescore/headerBar.html" %>

                <template x-if="error">
                    <div class="crb_card v2as-card">
                        <div class="v2as-card-body text-center py-10">
                            <div class="text-sm font-semibold mb-1" x-text="error"></div>
                            <div class="text-xs" style="color: var(--crb-grey-color)">Adjust the filters above and reload</div>
                        </div>
                    </div>
                </template>
                <template x-if="loading && !loaded">
                    <div class="crb_card v2as-card">
                        <div class="v2as-card-body text-center py-14">
                            <svg class="w-6 h-6 animate-spin mx-auto mb-2" style="color: var(--crb-blue-color)" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 11-6.22-8.56"/></svg>
                            <div class="text-xs" style="color: var(--crb-grey-color)">Computing the automate score...</div>
                        </div>
                    </div>
                </template>

                <template x-if="loaded && !error">
                    <div class="v2as-page">
                        <%@ include file="include/pages/reportingautomatescore/scoreHero.html" %>
                        <%@ include file="include/pages/reportingautomatescore/tables.html" %>
                    </div>
                </template>

            </div>

            <footer class="footer">
                <div class="container-fluid" id="footer"></div>
            </footer>
        </main>
    </body>
</html>
