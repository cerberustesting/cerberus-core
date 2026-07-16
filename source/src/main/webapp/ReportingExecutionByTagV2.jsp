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
        <meta name="active-submenu" content="ReportingExecutionByTag.jsp">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@ include file="include/global/dependenciesInclusions.html" %>

        <!-- Dependencies for the transversal TestCase / Campaign modals -->
        <script type="text/javascript" src="dependencies/Tinymce-6.7.0/tinymce.min.js"></script>
        <script type="text/javascript" src="dependencies/Bootstrap-treeview-1.2.0/js/bootstrap-treeview.js"></script>

        <!-- V2 page -->
        <script type="text/javascript" src="js/pages/ReportingExecutionByTagV2.js?v=${appVersion}"></script>
        <link rel="stylesheet" type="text/css" href="css/pages/InsightsShared.css?v=${appVersion}"/>
        <link rel="stylesheet" type="text/css" href="css/pages/ReportingExecutionByTagV2.css?v=${appVersion}"/>

        <title id="pageTitle">Campaign Report</title>
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

            <!-- Run-again launcher: campaign mode of the transversal execution modal -->
            <jsp:include page="include/transversal/TestCaseSimpleExecution.html"/>

            <div x-data="campaignReportV2()" x-init="init()" class="v2rt-page" id="campaignReportV2Root">

                <!-- Page title above the bar, like every list page -->
                <div class="v2in-pagetitle">
                    <h1 class="page-title-line">Campaign Report</h1>
                </div>

                <%@ include file="include/pages/reportingexecutionbytagv2/headerBar.html" %>

                <!-- Error / empty states -->
                <template x-if="error">
                    <div class="crb_card v2rt-card">
                        <div class="v2rt-card-body text-center py-10">
                            <div class="text-sm font-semibold mb-1" x-text="error"></div>
                            <div class="text-xs" style="color: var(--crb-grey-color)">Pick another tag from the selector above</div>
                        </div>
                    </div>
                </template>
                <template x-if="!tag && !loading">
                    <div class="crb_card v2rt-card v2in-hero">
                        <div class="v2in-hero-icon">
                            <svg class="w-7 h-7" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path d="M20.59 13.41l-7.17 7.17a2 2 0 01-2.83 0L2 12V2h10l8.59 8.59a2 2 0 010 2.82z"/><line x1="7" y1="7" x2="7.01" y2="7"/></svg>
                        </div>
                        <div class="v2in-hero-title">Everything about one campaign execution</div>
                        <div class="v2in-hero-sub">
                            Pick a campaign execution (a tag) to get its full report: live status donut, CI verdict, every test
                            case execution with its history, flaky detection, filters, and a PDF / email report to share.
                            Results update in real time while the campaign is still running.
                        </div>
                        <div class="v2in-hero-cta">
                            <button type="button" class="v2rt-btn v2rt-btn--primary"
                                    @click="tagDdOpen = true; tagSearch = ''; window.scrollTo({top: 0, behavior: 'smooth'}); $nextTick(() => $refs.tagSearchInput && $refs.tagSearchInput.focus())">
                                <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/></svg>
                                Browse all executions
                            </button>
                        </div>
                        <template x-if="filteredTagOptions.length > 0">
                            <div class="v2in-hero-list">
                                <div class="v2in-hero-listtitle">Or jump into a recent one</div>
                                <template x-for="t in filteredTagOptions.slice(0, 6)" :key="'recent-' + t.tag">
                                    <button type="button" class="v2rt-tag-dd-item v2rt-recent-item" @click="pickTag(t.tag)">
                                        <span class="v2rt-tag-dd-line">
                                            <span class="v2rt-tag-dd-ci"
                                                  :class="t.ciResult === 'OK' ? 'v2rt-tag-dd-ci--ok' : (t.ciResult === 'KO' ? 'v2rt-tag-dd-ci--ko' : 'v2rt-tag-dd-ci--run')"
                                                  x-text="t.ciResult || 'PEND'"></span>
                                            <span class="v2rt-tag-dd-name" x-text="t.tag"></span>
                                            <span class="flex-1"></span>
                                            <span class="v2rt-tag-dd-date" x-text="relTime(t.DateCreated)"></span>
                                        </span>
                                        <span class="v2rt-tag-dd-line v2rt-tag-dd-line--sub">
                                            <span class="v2rt-chip v2rt-chip--campaign" x-show="t.campaign" x-text="t.campaign" style="cursor: inherit"></span>
                                            <span class="v2rt-tag-dd-stats" x-show="t.total > 0">
                                                <span x-text="t.total + ' exe'"></span>
                                                <span class="v2rt-tag-dd-okpct"
                                                      :style="'color:' + (t.okPct >= 100 ? 'var(--crb-green-color)' : (t.okPct >= 50 ? 'var(--crb-orange-color)' : 'var(--crb-red-color)'))"
                                                      x-text="t.okPct + '% OK'"></span>
                                            </span>
                                            <span class="v2rt-tag-dd-stats" x-show="t.total === 0">no execution</span>
                                        </span>
                                    </button>
                                </template>
                            </div>
                        </template>
                    </div>
                </template>
                <template x-if="loading && !loaded">
                    <div class="crb_card v2rt-card">
                        <div class="v2rt-card-body text-center py-14">
                            <svg class="w-6 h-6 animate-spin mx-auto mb-2" style="color: var(--crb-blue-color)" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 11-6.22-8.56"/></svg>
                            <div class="text-xs" style="color: var(--crb-grey-color)">Loading report...</div>
                        </div>
                    </div>
                </template>

                <template x-if="loaded && !error">
                    <div class="v2rt-page">
                        <%@ include file="include/pages/reportingexecutionbytagv2/infoCards.html" %>
                        <%@ include file="include/pages/reportingexecutionbytagv2/sections.html" %>
                        <%@ include file="include/pages/reportingexecutionbytagv2/executionGrid.html" %>
                    </div>
                </template>

                <%@ include file="include/pages/reportingexecutionbytagv2/pdfModal.html" %>

            </div>

            <footer class="footer">
                <div class="container-fluid" id="footer"></div>
            </footer>
        </main>
    </body>
</html>
