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
        <meta name="active-submenu" content="QuickStart.jsp">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@ include file="include/global/dependenciesInclusions_new.html" %>
        <title id="pageTitle">Quick Start</title>

    </head>
    <body x-data x-cloak class="crb_body">
        <jsp:include page="include/global/header2.html"/>
        <jsp:include page="include/transversalobject/TestCaseSimpleCreation.html"/>
        <jsp:include page="include/transversalobject/TestCaseSimpleCreationAI.html"/>
        <jsp:include page="include/templates/dropdownApplication.html"/>
        <jsp:include page="include/templates/dropdownTest.html"/>
        <main class="crb_main" :class="$store.sidebar.expanded ? 'crb_main_sidebar-expanded' : 'crb_main_sidebar-collapsed'">

            <h1 class="page-title-line" id="title" x-text="$store.labels.getLabel('page_quickstart','title')">Quick Start</h1>
            <p class="page-subtitle-line" x-text="$store.labels.getLabel('page_quickstart','subtitle')">Choose your preferred method to create and manage test cases quickly</p>
            <div class="w-full">

    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">

      <!-- 1-Click Bootstrap -->
      <div class="crb_card">
        <div class="flex items-center gap-3 mb-4">
          <div class="bg-gray-100 dark:bg-gray-700 rounded-xl w-12 h-12 flex items-center justify-center">
            <i data-lucide="zap" class="text-green-500 w-10 h-10"></i>
          </div>
          <div>
                <h3 class="text-lg font-semibold mt-2 mb-0" x-text="$store.labels.getLabel('page_quickstart','oneclickboostraptitle')">1-Click Bootstrap</h3>
                <p class="text-gray-400" x-text="$store.labels.getLabel('page_quickstart','oneclickboostrapsubtitle')">Automatically generate test cases from your application</p>
          </div>
        </div>
        <div class="flex flex-wrap gap-2 mb-6">
          <span class="flex items-center bg-gray-100 dark:bg-gray-700 text-xs px-3 py-1 rounded-full gap-1"><i data-lucide="circle-check-big" class="w-3 h-3"></i>
            <span x-text="$store.labels.getLabel('page_quickstart','oneclickboostraptag1')"></span>
          </span>
          <span class="flex items-center bg-gray-100 dark:bg-gray-700 text-xs px-3 py-1 rounded-full gap-1"><i data-lucide="circle-check-big" class="w-3 h-3"></i>
            <span x-text="$store.labels.getLabel('page_quickstart','oneclickboostraptag2')"></span>
          </span>
          <span class="flex items-center bg-gray-100 dark:bg-gray-700 text-xs px-3 py-1 rounded-full gap-1"><i data-lucide="circle-check-big" class="w-3 h-3"></i>
            <span x-text="$store.labels.getLabel('page_quickstart','oneclickboostraptag3')"></span>
          </span>
        </div>
        <button class="w-full bg-blue-500 hover:bg-blue-600 text-white py-2 rounded-lg font-medium transition"
        onclick="window.dispatchEvent(new CustomEvent('testcase-modal-open', { detail: { defaultTest: 'TEST001' } }))">
          <span x-text="$store.labels.getLabel('page_quickstart','oneclickboostrapbutton')"></span>
        </button>
      </div>

      <!-- Recorder -->
      <div class="crb_card">
        <div class="flex items-center gap-3 mb-4">
        <div class="bg-gray-100 dark:bg-gray-700 rounded-xl w-12 h-12 flex items-center justify-center">
            <i data-lucide="video" class="text-blue-500 w-10 h-10"></i>
          </div>
          <div>
                <h3 class="text-lg font-semibold mt-2 mb-0" x-text="$store.labels.getLabel('page_quickstart','recordertitle')">Recorder</h3>
                <p class="text-gray-400" x-text="$store.labels.getLabel('page_quickstart','recordersubtitle')">Record test cases using browser automation tools</p>
          </div>
        </div>
        <div class="flex flex-wrap gap-2 mb-6">
          <span class="flex items-center bg-gray-100 dark:bg-gray-700 text-xs px-3 py-1 rounded-full gap-1"><i data-lucide="circle-check-big" class="w-3 h-3"></i>
            <span x-text="$store.labels.getLabel('page_quickstart','recordertag1')"></span>
          </span>
          <span class="flex items-center bg-gray-100 dark:bg-gray-700 text-xs px-3 py-1 rounded-full gap-1"><i data-lucide="circle-check-big" class="w-3 h-3"></i>
            <span x-text="$store.labels.getLabel('page_quickstart','recordertag2')"></span>
          </span>
          <span class="flex items-center bg-gray-100 dark:bg-gray-700 text-xs px-3 py-1 rounded-full gap-1"><i data-lucide="circle-check-big" class="w-3 h-3"></i>
            <span x-text="$store.labels.getLabel('page_quickstart','recordertag3')"></span>
          </span>
        </div>
        <button class="w-full bg-blue-500 hover:bg-blue-600 text-white py-2 rounded-lg font-medium transition">
            <span x-text="$store.labels.getLabel('page_quickstart','recorderbutton')"></span>
        </button>
      </div>

      <!-- Test Creation Copilot (disabled) -->
      <div class="crb_card">
        <span class="absolute top-3 right-3 bg-green-500 text-black text-xs font-bold px-2 py-1 rounded">
          Early Access
        </span>
        <div class="flex items-center gap-3 mb-4">
        <div class="bg-gray-100 dark:bg-gray-700 rounded-xl w-12 h-12 flex items-center justify-center">
            <i data-lucide="bot" class="text-purple-500 w-10 h-10"></i>
          </div>
          <div>
                <h3 class="text-lg font-semibold mt-2 mb-0" x-text="$store.labels.getLabel('page_quickstart','copilottitle')">Test Creation Copilot</h3>
                <p class="text-gray-400" x-text="$store.labels.getLabel('page_quickstart','copilotsubtitle')">AI-assisted test case creation with natural language</p>
          </div>
        </div>
        <div class="flex flex-wrap gap-2 mb-6">
          <span class="flex items-center bg-gray-100 dark:bg-gray-700 text-xs px-3 py-1 rounded-full gap-1"><i data-lucide="circle-check-big" class="w-3 h-3"></i>
            <span x-text="$store.labels.getLabel('page_quickstart','copilottag1')"></span>
          </span>
          <span class="flex items-center bg-gray-100 dark:bg-gray-700 text-xs px-3 py-1 rounded-full gap-1"><i data-lucide="circle-check-big" class="w-3 h-3"></i>
            <span x-text="$store.labels.getLabel('page_quickstart','copilottag2')"></span>
          </span>
          <span class="flex items-center bg-gray-100 dark:bg-gray-700 text-xs px-3 py-1 rounded-full gap-1"><i data-lucide="circle-check-big" class="w-3 h-3"></i>
            <span x-text="$store.labels.getLabel('page_quickstart','copilottag3')"></span>
          </span>
        </div>
        <button class="w-full bg-blue-500 hover:bg-blue-600 text-white py-2 rounded-lg font-medium transition"
        onclick="window.dispatchEvent(new CustomEvent('create-test-ai-modal-open'))">
          <span x-text="$store.labels.getLabel('page_quickstart','copilotbutton')"></span>
        </button>
      </div>

      <!-- Test Templates Library (disabled) -->
      <div class="crb_card">
        <span class="absolute top-3 right-3 bg-yellow-500 text-black text-xs font-bold px-2 py-1 rounded">
          Coming Soon
        </span>
        <div class="flex items-center gap-3 mb-4">
        <div class="bg-gray-100 dark:bg-gray-700 rounded-xl w-12 h-12 flex items-center justify-center">
            <i data-lucide="library" class="text-orange-500 w-10 h-10"></i>
          </div>
          <div>
                <h3 class="text-lg font-semibold mt-2 mb-0" x-text="$store.labels.getLabel('page_quickstart','templatetitle')">Test Templates Library</h3>
                <p class="text-gray-400" x-text="$store.labels.getLabel('page_quickstart','templatesubtitle')">Browse and use pre-built test case templates</p>
          </div>
        </div>
        <div class="flex flex-wrap gap-2 mb-6">
          <span class="flex items-center bg-gray-100 dark:bg-gray-700 text-xs px-3 py-1 rounded-full gap-1"><i data-lucide="circle-check-big" class="w-3 h-3"></i>
            <span x-text="$store.labels.getLabel('page_quickstart','templatetag1')"></span>
          </span>
          <span class="flex items-center bg-gray-100 dark:bg-gray-700 text-xs px-3 py-1 rounded-full gap-1"><i data-lucide="circle-check-big" class="w-3 h-3"></i>
            <span x-text="$store.labels.getLabel('page_quickstart','templatetag2')"></span>
          </span>
          <span class="flex items-center bg-gray-100 dark:bg-gray-700 text-xs px-3 py-1 rounded-full gap-1"><i data-lucide="circle-check-big" class="w-3 h-3"></i>
            <span x-text="$store.labels.getLabel('page_quickstart','templatetag3')"></span>
          </span>
        </div>
        <button class="w-full bg-gray-600 text-white py-2 rounded-lg font-medium" disabled>
          <span x-text="$store.labels.getLabel('page_quickstart','templatebutton')"></span>
        </button>
      </div>

    </div>
  </div>
            <footer class="footer">
                <div class="container-fluid" id="footer"></div>
            </footer>
        </div>
        </main>
    </body>
</html>
