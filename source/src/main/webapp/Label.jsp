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
        <meta name="active-menu" content="maintain">
        <meta name="active-submenu" content="Label.jsp">
        <meta name="active-page" content="Label.jsp">
        <meta name="page" content="Label">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@ include file="include/global/dependenciesInclusions.html" %>
        <script type="text/javascript" src="dependencies/Tinymce-6.7.0/tinymce.min.js"></script>
        <script type="text/javascript" src="dependencies/Bootstrap-treeview-1.2.0/js/bootstrap-treeview.js"></script>
        <%--
            Label.js is still loaded: the add/edit modals and their save/close
            handlers all live there and are unchanged. LabelV2.js is loaded AFTER
            it and overrides initPage() (the table) and generateLabelTree() (the
            three tree tabs, now js/global/crbLabelTree.js) - see the comment at
            the top of that file about the idempotence guard, without which the
            page would initialise twice.
            bootstrap-treeview is no longer used by this page. It stays loaded
            only because LabelV1.jsp, the rollback copy, still drives it.
        --%>
        <script type="text/javascript" src="js/pages/Label.js?v=${appVersion}"></script>
        <script type="text/javascript" src="js/pages/LabelV2.js?v=${appVersion}"></script>
        <title id="pageTitle">Label</title>
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
            <%@ include file="include/pages/label/addLabel.html"%> 
            <%@ include file="include/pages/label/editLabel.html"%> 

            <h1 class="page-title-line" id="title">Label & Tag</h1>

            <div x-data="{ tab: 'list' }" class="w-full">
                  <%--
                      Tab bar on the shared .crb_tabs component (see components.css).
                      Replaces four copies of the same ~6 utility classes per button,
                      and drops the hardcoded colours this page had drifted to instead
                      of the charter's crb_tab_* classes. Same markup shape as
                      TestCaseExecutionV2 / TestCaseScriptV2, which now use it too.
                  --%>
                  <div class="crb_tabs">
                    <button @click="tab = 'list'" :class="tab === 'list' ? 'crb_tab--active' : ''" class="crb_tab">
                        <i data-lucide="list" class="w-4 h-4"></i>
                        <span>List</span>
                    </button>
                    <button @click="tab = 'treeR'" :class="tab === 'treeR' ? 'crb_tab--active' : ''" class="crb_tab">
                        <i data-lucide="git-branch" class="w-4 h-4"></i>
                        <span>Requirement Tree</span>
                    </button>
                    <button @click="tab = 'treeS'" :class="tab === 'treeS' ? 'crb_tab--active' : ''" class="crb_tab">
                        <i data-lucide="tag" class="w-4 h-4"></i>
                        <span>Sticker Tree</span>
                    </button>
                    <button @click="tab = 'treeB'" :class="tab === 'treeB' ? 'crb_tab--active' : ''" class="crb_tab">
                        <i data-lucide="battery" class="w-4 h-4"></i>
                        <span>Battery Tree</span>
                    </button>
                </div>

               <%--
                   Panels use plain x-show + x-cloak, like TestCaseScriptV2 and
                   TestCaseExecutionV2. The opacity transition that used to be here
                   drives itself with requestAnimationFrame, which browsers freeze
                   on a backgrounded tab: the leave/enter pair stalled halfway and
                   every panel stayed on screen at once until the next click.
               --%>
               <!-- Contenu onglets -->
               <div class="">
                    <!-- List -->
                    <div x-show="tab === 'list'" x-cloak>
                      <div id="labelList"></div>
                    </div>

                    <%--
                        The three browse trees.
                        Each panel is now just a mount point: js/global/crbLabelTree.js
                        draws its own control bar (search, expand/collapse, refresh,
                        create) on the SAME classes as the table's bar, so the four tabs
                        of this page share one toolbar vocabulary. The four hand-rolled
                        buttons that used to be repeated verbatim in each panel - and the
                        #refreshButtonTree*/#createLabelButtonTree*/#collapseAllTree*/
                        #expandAllTree* ids Label.js binds them by - are gone with them;
                        see the note in LabelV2.js#generateLabelTree about those now-dead
                        jQuery handlers.
                        x-show, not x-if: a template x-if destroys and rebuilds the panel
                        on every tab change, which would throw away the mounted Alpine
                        tree along with its expand state.
                    --%>
                    <!-- Requirement Tree -->
                    <div x-show="tab === 'treeR'" x-cloak>
                        <div id="mainTreeR"></div>
                    </div>

                    <!-- Sticker Tree -->
                    <div x-show="tab === 'treeS'" x-cloak>
                      <div id="mainTreeS"></div>
                    </div>

                    <!-- Battery Tree -->
                    <div x-show="tab === 'treeB'" x-cloak>
                      <div id="mainTreeB"></div>
                    </div>
                  </div>
                </div>

            <footer class="footer">
                <div class="container-fluid" id="footer"></div>
            </footer>
            <jsp:include page="include/global/aiBottomBar.html"/>
        </main>
    </body>
</html>
