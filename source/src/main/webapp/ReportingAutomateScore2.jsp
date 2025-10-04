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
    Document   : ReportingAutomateScore
    Created on : 8 May 2025
    Author     : vertigo17
--%>

<%@ page contentType="text/html" pageEncoding="UTF-8" isELIgnored="true" %>
<!DOCTYPE html>
<html class="h-full" x-data="{ filtersOpen: true }">
<head>
  <meta name="active-menu" content="reporting">
  <meta name="active-submenu" content="ReportingAutomateScore.jsp">
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <%@ include file="include/global/dependenciesInclusions.html" %>
  <script type="text/javascript" src="dependencies/Moment-2.30.1/moment-with-locales.min.js"></script>
  <script type="text/javascript" src="dependencies/Chart.js-2.9.3/Chart.min.js"></script>
  <script type="text/javascript" src="dependencies/Bootstrap-datetimepicker-4.17.47/bootstrap-datetimepicker.min.js"></script>
  <script type="text/javascript" src="js/pages/ReportingAutomateScore.js"></script>
  <title id="pageTitle">Automate Score</title>
  <!-- SCRIPT MULTISELECT -->
  <script>
    function filtersPanel() {
      return { filtersOpen: false }
    }

    function multiSelect(id, initialOptions) {
      return {
        open: false,
        options: initialOptions || [],
        selected: [],
        pos: {top:0, left:0, width:200},
        toggleOpen(triggerEl) {
          this.open = !this.open;
          if (this.open && triggerEl) {
            const rect = triggerEl.getBoundingClientRect();
            this.pos = {
              top: rect.bottom + window.scrollY,
              left: rect.left + window.scrollX,
              width: rect.width
            };
          }
        },
        toggle(option) {
          if (this.selected.includes(option)) {
            this.selected = this.selected.filter(o => o !== option)
          } else {
            this.selected.push(option)
          }
        },
        selectedLabel() {
          return this.selected.length ? this.selected.join(', ') : 'Select...'
        },
        dropdownStyle() {
          return `position:absolute; top:${this.pos.top}px; left:${this.pos.left}px; width:${this.pos.width}px; z-index:9999;`;
        },
        loadSystems() {
          let user = JSON.parse(sessionStorage.getItem('user') || '{}')
          this.options = user.system || []
        },
        loadCampaigns() {
          $.getJSON("ReadCampaign").done((data) => {
            this.options = data.contentTable.map(c => c.campaign + " - " + c.description)
          })
        }
      }
    }
  </script>
</head>
<body x-data x-cloak class="crb_body">
<jsp:include page="include/global/header2.html"/>
<jsp:include page="include/templates/dropdown.html"/>
<main class="crb_main" :class="$store.sidebar.expanded ? 'crb_main_sidebar-expanded' : 'crb_main_sidebar-collapsed'">
  <%@ include file="include/global/messagesArea.html"%>

<div x-data="filtersPanel()" class="space-y-4">

  <!-- Titre + bouton -->
  <div class="flex items-center justify-between">
    <h1 class="page-title-line" id="title">Automate Score</h1>
    <button @click="filtersOpen = !filtersOpen"
            class="flex items-center gap-2 px-3 py-1 border border-gray-500 rounded text-sm">
      <svg xmlns="http://www.w3.org/2000/svg" class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2"
           viewBox="0 0 24 24"><path d="M3 4h18M6 8h12M9 12h6"/></svg>
      Filters
    </button>
  </div>

  <!-- FILTERS -->
  <div id="FiltersPanel" class="crb_card" x-show="filtersOpen" x-transition>

    <div class="grid grid-cols-1 md:grid-cols-4 gap-4">

    <div class="w-80" x-init="mountDropdown($el, { items: ['WS1','WS2'], label: 'Workspaces' })"></div>

      <!-- SYSTEM MULTISELECT -->
      <div class="mb-4 relative" x-data="multiSelect('systemSelect', [])" x-init="loadSystems()">
        <label for="systemSelect" class="block text-sm mb-1">System</label>

        <!-- Trigger -->
        <button type="button" @click="toggleOpen($event.currentTarget)" class="w-full h-10 flex items-center justify-between rounded-md border px-3 py-2 text-sm
                 bg-slate-800 border-slate-600 text-slate-300 hover:bg-slate-700 focus:outline-none">
          <span x-text="selectedLabel()"></span>
          <svg xmlns="http://www.w3.org/2000/svg" :class="open ? 'rotate-90' : ''"
               class="w-4 h-4 ml-auto transition-transform duration-200"
               fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/>
          </svg>
        </button>

        <!-- Dropdown -->
        <template x-teleport="body">
          <div x-show="open" x-transition
               :style="dropdownStyle()"
               class="absolute z-50 mt-1 w-56 rounded-md border border-slate-600 bg-slate-800 shadow-lg">
            <ul class="py-1 text-sm text-slate-300">
              <template x-for="option in options" :key="option">
                <li>
                  <button type="button" @click.stop="toggle(option)"
                          class="w-full flex items-center gap-2 px-3 py-2 hover:bg-slate-700">
                    <svg x-show="selected.includes(option)" xmlns="http://www.w3.org/2000/svg"
                         class="h-3 w-3 text-blue-500" fill="none" stroke="currentColor"
                         stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <path d="M20 6 9 17l-5-5"/>
                    </svg>
                    <span x-text="option"></span>
                  </button>
                </li>
              </template>
            </ul>
          </div>
        </template>

        <input type="hidden" id="systemSelect" :value="selected.join(',')">
      </div>

      <!-- CAMPAIGN MULTISELECT -->
      <div class="mb-4 relative" x-data="multiSelect('campaignSelect', [])" x-init="loadCampaigns()">
        <label for="campaignSelect" class="block text-sm mb-1">Campaign</label>

        <!-- Trigger -->
        <button type="button" @click="toggleOpen($event.currentTarget)"
                class="w-full h-10 flex items-center justify-between rounded-md border px-3 py-2 text-sm
                 bg-slate-800 border-slate-600 text-slate-300 hover:bg-slate-700 focus:outline-none">
          <span x-text="selectedLabel()"></span>
          <svg xmlns="http://www.w3.org/2000/svg" :class="open ? 'rotate-90' : ''"
               class="w-4 h-4 ml-auto transition-transform duration-200"
               fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/>
          </svg>
        </button>

        <!-- Dropdown -->
        <template x-teleport="body">
          <div x-show="open" x-transition
               :style="dropdownStyle()"
               class="absolute z-50 mt-1 w-72 rounded-md border border-slate-600 bg-slate-800 shadow-lg">
            <ul class="py-1 text-sm text-slate-300">
              <template x-for="option in options" :key="option">
                <li>
                  <button type="button" @click.stop="toggle(option)"
                          class="w-full flex items-center gap-2 px-3 py-2 hover:bg-slate-700">
                    <svg x-show="selected.includes(option)" xmlns="http://www.w3.org/2000/svg"
                         class="h-3 w-3 text-blue-500" fill="none" stroke="currentColor"
                         stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <path d="M20 6 9 17l-5-5"/>
                    </svg>
                    <span x-text="option"></span>
                  </button>
                </li>
              </template>
            </ul>
          </div>
        </template>

        <input type="hidden" id="campaignSelect" :value="selected.join(',')">
      </div>

      <!-- DATE -->
      <div class="mb-4">
        <label for="topicker" class="block text-sm mb-1 text-slate-300">Until</label>
        <input id="topicker" type="date"
               class="w-full h-10 rounded-md border border-slate-600 bg-slate-800 text-slate-300 px-3 py-2 text-sm
                hover:bg-slate-700 focus:outline-none focus:ring-2 focus:ring-blue-500/30 transition-all">
      </div>

      <!-- WEEKS -->
      <div class="mb-4">
        <label for="trendWeeks" class="block text-sm mb-1 text-slate-300">Trend Weeks</label>
        <input id="trendWeeks" type="number" value="15"
               class="w-full h-10 rounded-md border border-slate-600 bg-slate-800 text-slate-300 px-3 py-2 text-sm
                hover:bg-slate-700 focus:outline-none focus:ring-2 focus:ring-blue-500/30 transition-all">
      </div>
    </div>

    <div class="mt-4">
      <button id="loadbutton"
              onclick="loadKPIGraphBars(true)"
              class="w-full bg-sky-500 hover:bg-sky-600 text-white py-2 px-4 rounded shadow">
        Load
      </button>
    </div>
  </div>
</div>
  <!-- KPI SCORE PANEL -->
  <div id="KPIPanel" class="grid md:grid-cols-2 gap-6 mb-6">
    <!-- Left -->
    <div class="crb_card mb-0">
  <h3 class="text-lg font-semibold mb-2">In Value Automate</h3>
  <div class="flex items-center gap-2 mb-4">
    <span id="ASValue" class="text-2xl font-bold">B</span>
    <div class="flex gap-1" id="ASButtons">
      <span id="ASA" class="w-6 h-6 bg-green-500 text-xs flex items-center justify-center rounded cursor-pointer">A</span>
      <span id="ASB" class="w-6 h-6 bg-yellow-500 text-xs flex items-center justify-center rounded cursor-pointer">B</span>
      <span id="ASC" class="w-6 h-6 bg-orange-500 text-xs flex items-center justify-center rounded cursor-pointer">C</span>
      <span id="ASD" class="w-6 h-6 bg-pink-500 text-xs flex items-center justify-center rounded cursor-pointer">D</span>
      <span id="ASE" class="w-6 h-6 bg-red-500 text-xs flex items-center justify-center rounded cursor-pointer">E</span>
    </div>
  </div>
  <ul class="text-sm space-y-1">
    <li><span id="scopeCampaigns">3 Campaigns</span> <span class="text-sky-400">Active</span></li>
    <li><span id="scopeTests">398 Tests</span> <span class="text-sky-400">Running</span></li>
    <li><span id="scopeApplications">7 Applications</span> <span class="text-sky-400">Monitored</span></li>
  </ul>
  <p class="text-xs text-gray-400 mt-4">
    The Automate Score measures the effectiveness of your testing automation across four critical dimensions:
    execution frequency, stability, duration and maintenance effort.
  </p>
</div>
    <!-- Right metrics -->
    <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
      <div class="crb_card mb-0">
        <h4 class="text-sm font-semibold mb-1">Execution Frequency</h4>
        <p class="text-2xl font-bold">7/week</p>
        <p class="text-green-400 text-sm">▲ +22.2%</p>
      </div>
      <div class="crb_card mb-0">
        <h4 class="text-sm font-semibold mb-1">Stability</h4>
        <p class="text-2xl font-bold">3.62%</p>
        <p class="text-red-400 text-sm">▼ -7%</p>
      </div>
      <div class="crb_card mb-0">
        <h4 class="text-sm font-semibold mb-1">Duration</h4>
        <p class="text-2xl font-bold">4h 2min</p>
        <p class="text-green-400 text-sm">▼ -25.8%</p>
      </div>
      <div class="crb_card mb-0">
        <h4 class="text-sm font-semibold mb-1">Maintenance Effort</h4>
        <p class="text-2xl font-bold">7h</p>
        <p class="text-red-400 text-sm">▲ +10.1%</p>
      </div>
    </div>
  </div>

  <!-- RECOMMENDATIONS -->
  <h3 class="text-lg font-semibold mb-2">Key Recommendations</h3>
  <div class="grid md:grid-cols-2 gap-6">
    <div class="crb_card mb-0">
      <h4 class="text-sky-400 font-semibold mb-2">Score Improvement Actions <span class="text-gray-400 text-sm">Current Grade: B</span></h4>
      <div class="bg-red-900/40 border border-red-500 text-sm p-3 rounded">
        <strong>E Stability:</strong> Reduce flaky tests by improving wait strategies, using more stable selectors,
        and implementing retry mechanisms. Target &lt;3% failure rate.
      </div>
    </div>
    <div class="crb_card mb-0">
      <h4 class="text-sky-400 font-semibold mb-2">Business Impact <span class="text-gray-400 text-sm">Expected Improvements</span></h4>
      <ul class="list-disc list-inside text-sm space-y-1">
        <li>Accelerated release cycles: 2–3 days faster</li>
        <li>Reduced manual testing: 36 hours saved per release</li>
      </ul>
    </div>
  </div>

</main>
</body>
</html>
