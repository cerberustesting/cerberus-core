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
        <meta name="active-menu" content="test">
        <meta name="active-submenu" content="Label.jsp">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@ include file="include/global/dependenciesInclusions.html" %>
        <script type="text/javascript" src="dependencies/Tinymce-6.7.0/tinymce.min.js"></script>
        <script type="text/javascript" src="dependencies/Bootstrap-treeview-1.2.0/js/bootstrap-treeview.js"></script>
        <script type="text/javascript" src="js/pages/Label.js"></script>
        <title id="pageTitle">Label</title>
    </head>
    <body x-data x-cloak class="crb_body">
        <jsp:include page="include/global/header2.html"/>
        <main class="crb_main" :class="$store.sidebar.expanded ? 'crb_main_sidebar-expanded' : 'crb_main_sidebar-collapsed'">
            <%@ include file="include/global/messagesArea.html"%>
            <%@ include file="include/utils/modal-confirmation.html"%>
            <%@ include file="include/pages/label/addLabel.html"%> 
            <%@ include file="include/pages/label/editLabel.html"%> 

            <h1 class="page-title-line" id="title">Label</h1>

            <div x-data="{ tab: 'list' }" class="w-full">
                  <!-- Onglets -->
               <div class="w-full flex bg-slate-200 dark:bg-slate-700 p-1 rounded-lg shadow-sm mb-8 h-10">
                    <!-- List -->
                    <button
                        @click="tab = 'list'"
                        :class="tab === 'list' ? 'bg-slate-50 font-semibold dark:bg-slate-900' : 'bg-slate-200 dark:bg-slate-700 text-slate-700 hover:text-slate-900 dark:text-slate-300 dark:hover:text-white'"
                        class="flex-1 flex items-center justify-center gap-2 px-4 py-3 rounded-md transition-colors duration-200">
                        <i data-lucide="list" class="w-4 h-4"></i>
                        List
                    </button>

                    <!-- Requirement Tree -->
                    <button
                        @click="tab = 'treeR'"
                        :class="tab === 'treeR' ? 'bg-slate-50 font-semibold dark:bg-slate-900' : 'bg-slate-200 dark:bg-slate-700 text-slate-700 hover:text-slate-900 dark:text-slate-300 dark:hover:text-white'"
                        class="flex-1 flex items-center justify-center gap-2 px-4 py-3 rounded-md transition-colors duration-200">
                        <i data-lucide="git-branch" class="w-4 h-4"></i>
                        Requirement Tree
                    </button>

                    <!-- Sticker Tree -->
                    <button
                        @click="tab = 'treeS'"
                        :class="tab === 'treeS' ? 'bg-slate-50 font-semibold dark:bg-slate-900' : 'bg-slate-200 dark:bg-slate-700 text-slate-700 hover:text-slate-900 dark:text-slate-300 dark:hover:text-white'"
                        class="flex-1 flex items-center justify-center gap-2 px-4 py-3 rounded-md transition-colors duration-200">
                        <i data-lucide="tag" class="w-4 h-4"></i>
                        Sticker Tree
                    </button>

                    <!-- Battery Tree -->
                    <button
                        @click="tab = 'treeB'"
                        :class="tab === 'treeB' ? 'bg-slate-50 font-semibold dark:bg-slate-900' : 'bg-slate-200 dark:bg-slate-700 text-slate-700 hover:text-slate-900 dark:text-slate-300 dark:hover:text-white'"
                        class="flex-1 flex items-center justify-center gap-2 px-4 py-3 rounded-md transition-colors duration-200">
                        <i data-lucide="battery" class="w-4 h-4"></i>
                        Battery Tree
                    </button>
                </div>

               <!-- Contenu onglets -->
               <div class="">
                    <!-- List -->
                    <div x-show="tab === 'list'" x-transition.opacity>
                      <table id="labelsTable" class="table table-hover display"></table>
                    </div>

                    <!-- Requirement Tree -->
                    <div x-show="tab === 'treeR'" x-transition.opacity>
                        <div class="mb-5 ml-6 flex space-x-2">
                            <button id="refreshButtonTreeR" type="button" class="px-3 py-2 rounded-md border text-sm bg-white hover:bg-gray-50 text-gray-700 shadow-sm flex items-center gap-1">
                              <i data-lucide="refresh-ccw" class="w-4 h-4"></i> Refresh
                            </button>
                            <button id="createLabelButtonTreeR" type="button" class="px-3 py-2 rounded-md border text-sm bg-white hover:bg-gray-50 text-gray-700 shadow-sm flex items-center gap-1">
                              <i data-lucide="plus" class="w-4 h-4"></i> Create
                            </button>
                            <button id="collapseAllTreeR" type="button" class="px-3 py-2 rounded-md border text-sm bg-white hover:bg-gray-50 text-gray-700 shadow-sm flex items-center gap-1">
                              <i data-lucide="chevron-up" class="w-4 h-4"></i> Collapse All
                            </button>
                            <button id="expandAllTreeR" type="button" class="px-3 py-2 rounded-md border text-sm bg-white hover:bg-gray-50 text-gray-700 shadow-sm flex items-center gap-1">
                              <i data-lucide="chevron-down" class="w-4 h-4"></i> Expand All
                            </button>
                        </div>
                        <div class="mt-5" id="mainTreeR"></div>
                    </div>

                    <!-- Sticker Tree -->
                    <div x-show="tab === 'treeS'" x-transition.opacity>
                      <div class="mb-5 ml-6 flex space-x-2">
                        <button id="refreshButtonTreeS" type="button" class="px-3 py-2 rounded-md border text-sm bg-white hover:bg-gray-50 text-gray-700 shadow-sm flex items-center gap-1">
                          <i data-lucide="refresh-ccw" class="w-4 h-4"></i> Refresh
                        </button>
                        <button id="createLabelButtonTreeS" type="button" class="px-3 py-2 rounded-md border text-sm bg-white hover:bg-gray-50 text-gray-700 shadow-sm flex items-center gap-1">
                          <i data-lucide="plus" class="w-4 h-4"></i> Create
                        </button>
                        <button id="collapseAllTreeS" type="button" class="px-3 py-2 rounded-md border text-sm bg-white hover:bg-gray-50 text-gray-700 shadow-sm flex items-center gap-1">
                          <i data-lucide="chevron-up" class="w-4 h-4"></i> Collapse All
                        </button>
                        <button id="expandAllTreeS" type="button" class="px-3 py-2 rounded-md border text-sm bg-white hover:bg-gray-50 text-gray-700 shadow-sm flex items-center gap-1">
                          <i data-lucide="chevron-down" class="w-4 h-4"></i> Expand All
                        </button>
                      </div>
                      <div class="mt-5" id="mainTreeS"></div>
                    </div>

                    <!-- Battery Tree -->
                    <div x-show="tab === 'treeB'" x-transition.opacity>
                      <div class="mb-5 ml-6 flex space-x-2">
                        <button id="refreshButtonTreeB" type="button" class="px-3 py-2 rounded-md border text-sm bg-white hover:bg-gray-50 text-gray-700 shadow-sm flex items-center gap-1">
                          <i data-lucide="refresh-ccw" class="w-4 h-4"></i> Refresh
                        </button>
                        <button id="createLabelButtonTreeB" type="button" class="px-3 py-2 rounded-md border text-sm bg-white hover:bg-gray-50 text-gray-700 shadow-sm flex items-center gap-1">
                          <i data-lucide="plus" class="w-4 h-4"></i> Create
                        </button>
                        <button id="collapseAllTreeB" type="button" class="px-3 py-2 rounded-md border text-sm bg-white hover:bg-gray-50 text-gray-700 shadow-sm flex items-center gap-1">
                          <i data-lucide="chevron-up" class="w-4 h-4"></i> Collapse All
                        </button>
                        <button id="expandAllTreeB" type="button" class="px-3 py-2 rounded-md border text-sm bg-white hover:bg-gray-50 text-gray-700 shadow-sm flex items-center gap-1">
                          <i data-lucide="chevron-down" class="w-4 h-4"></i> Expand All
                        </button>
                      </div>
                      <div class="mt-5" id="mainTreeB"></div>
                    </div>
                  </div>
                </div>

            <footer class="footer">
                <div class="container-fluid" id="footer"></div>
            </footer>
        </main>
    </body>
</html>
