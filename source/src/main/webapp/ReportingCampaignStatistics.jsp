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
        <meta name="active-menu" content="insights">
        <meta name="active-submenu" content="ReportingCampaignStatistics.jsp">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@ include file="include/global/dependenciesInclusions.html" %>
        <title id="pageTitle">Campaign statistics</title>
        <script type="text/javascript" src="dependencies/Tinymce-6.7.0/tinymce.min.js"></script>
        <script type="text/javascript" src="dependencies/Moment-2.30.1/moment-with-locales.min.js"></script>
        <script type="text/javascript" src="dependencies/Bootstrap-datetimepicker-4.17.47/bootstrap-datetimepicker.min.js"></script>
        <link rel="stylesheet" href="dependencies/Bootstrap-datetimepicker-4.17.47/bootstrap-datetimepicker.min.css" />
        <script type="text/javascript" src="js/transversalobject/Campaign.js?v=${appVersion}"></script>
        <script type="text/javascript" src="js/pages/ReportingCampaignStatistics.js?v=${appVersion}"></script>
    </head>
    <body x-data x-cloak class="crb_body">
        <jsp:include page="include/global/header2.html"/>
        <jsp:include page="include/global/modalInclusions.jsp"/>
        <main class="crb_main" :class="$store.sidebar.expanded ? 'crb_main_sidebar-expanded' : 'crb_main_sidebar-collapsed'">
            <%@ include file="include/global/messagesArea.html"%>
            <%@ include file="include/utils/modal-confirmation.html"%>
            <%@ include file="include/pages/testcampaign/viewStatcampaign.html"%>
            <jsp:include page="include/templates/datepicker.html"/>
             <jsp:include page="include/templates/selectMultipleDropdown.html"/>
             <jsp:include page="include/templates/selectDropdown.html"/>
            <h1 class="page-title-line" id="title">Campaign Statistics </h1>
            <div class="crb_card">
                <div id="filters" x-data="reportingCampaignStatiticsForm()" x-ref="filters" @load-stats.window="loadStatistics()">
                    <div class="hidden" id="envCountryFilters">
                        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                            <div>
                                <label for="environmentSelect" class="block mb-2 text-sm font-medium text-gray-700">Environnement</label>
                                <select id="environmentSelect" multiple class="w-full border border-gray-300 rounded-md p-2 focus:border-blue-500 focus:ring focus:ring-blue-200"></select>
                            </div>
                            <div>
                                <label for="countrySelect" class="block mb-2 text-sm font-medium text-gray-700">Pays</label>
                                <select id="countrySelect" multiple class="w-full border border-gray-300 rounded-md p-2 focus:border-blue-500 focus:ring focus:ring-blue-200"></select>
                            </div>
                        </div>
                    </div>
                    <div class="mb-6" id="systemAppGroup1Filters">
                        <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
                            <!-- Workspace -->
                            <div>
                                <label class="font-semibold block mb-1" x-text="$store.labels.getLabel('reportingCampaignStatistics','filterworkspace')">Workspace</label>
                                <div x-data="multiSelectDropdown({
                                            id:'workspaceSelect',
                                            model: form,
                                            modelField: 'system',
                                            labelField:'name',
                                            valueField:'id',
                                            loader: () => {
                                                const userRaw = sessionStorage.getItem('user');
                                                if (!userRaw) return [];
                                                const user = JSON.parse(userRaw);
                                                return Array.isArray(user.system) ? user.system.map(s => ({ name: s, id: s })) : [];
                                            },
                                            returnType:'value',
                                            preselected:[]
                                        })"
                                     x-ref="workspaceDropdownComponent"
                                     class="w-full">
                                </div>
                            </div>

                            <!-- Application -->
                            <div>
                                <label class="font-semibold block mb-1" x-text="$store.labels.getLabel('reportingCampaignStatistics','filterapplication')">Application</label>
                                <div x-data="multiSelectDropdown({
                                    id:'applicationSelect',
                                    model: form,
                                    modelField: 'application',
                                    labelField:'application',
                                    valueField:'application',
                                    loader: async (workspaces = []) => {
                                        if (!workspaces.length) return [];
                                        const systemsQ = workspaces.map(s => '&system=' + encodeURIComponent(s)).join('');
                                        const resp = await fetch('ReadApplication?' + systemsQ);
                                        const data = await resp.json();
                                        return data.contentTable || [];
                                      },
                                    returnType:'value',
                                    preselected:[]
                                })"
                                x-ref="applicationDropdownComponent"
                                x-bind:class="selectedWorkspaces.length === 0 ? 'opacity-50 pointer-events-none' : ''"
                                class="w-full opacity-50 pointer-events-none"
                                @refresh-items.window="async e => {
                                    $data.items = await $data.loader(e.detail);
                                    // active le dropdown si items présents
                                    if(e.detail.length > 0) {
                                        $el.classList.remove('opacity-50', 'pointer-events-none');
                                    } else {
                                        $el.classList.add('opacity-50', 'pointer-events-none');
                                    }
                                }">
                                </div>
                            </div>
                            <!-- Group 1 -->
                            <div>
                                <label class="font-semibold block mb-1" x-text="$store.labels.getLabel('reportingCampaignStatistics','filtergroup1')">Application</label>
                                <div x-data="singleSelectDropdown({
                                  id:'workspaceUniqueSelect',
                                  model: form,
                                  modelField: 'group1',
                                  labelField:'name',
                                  valueField:'id',
                                  loader:() => []
                                })"
                                class="w-full opacity-50 pointer-events-none">
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                        <!-- From picker -->
                        <div>
                            <label class="block mb-2 text-sm font-medium">From2</label>
                            <div x-data="dateTimePicker({
                                id:'fromPicker',
                                model: form,
                                modelField: 'from',
                                onChange: val => console.log('From:', val)
                                })"
                                class="w-full"></div>
                        </div>


                        <!-- To picker -->
                        <div>
                            <label class="block mb-2 text-sm font-medium">To</label>
                            <div x-data="dateTimePicker({
                                id:'toPicker',
                                model: form,
                                modelField: 'to',
                                onChange: val => console.log('To:', val)
                                })" class="relative mb-4"></div>
                        </div>

                        <div class="mt-6">
                            <button
                              class="w-full bg-blue-600 hover:bg-blue-700 text-white font-semibold py-2 px-4 rounded-md h-10"
                              @click.prevent="$dispatch('load-stats')">
                              Load
                            </button>
                        </div>
                    </div>
                </div>
            </div>
            <div class="crb_card">
                <div id="loading" style="display: none; text-align: center; position: absolute; left: 50%; top: 50%; transform: translate(-50%, -50%); z-index: 1000;">
                    <img src="images/loading.gif" alt="Loading...">
                </div>
                <div id="tagStatisticList">
                    <table id="tagStatisticTable" class="table table-hover display" name="tagStatisticTable"></table>
                    <div class="marginBottom20"></div>
                </div>
            </div>
            <div class="crb_card">
                <div class="panel-body" id="tagStatisticDetailList"  style="display: none;">
                    <table id="tagStatisticDetailTable" class="table table-hover display" name="tagStatisticDetailTable"></table>
                    <div class="marginBottom20"></div>
                </div>
            </div>
            <footer class="footer">
                <div class="container-fluid" id="footer"></div>
            </footer>
        </main>
    </body>
</html>