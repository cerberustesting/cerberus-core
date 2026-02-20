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
    <meta content="text/html; charset=UTF-8" http-equiv="content-type">
    <title>My Dashboard</title>

    <%@ include file="include/global/dependenciesInclusions.html" %>

    <script src="https://cdn.jsdelivr.net/npm/chart.js@4"></script>
    <script src="https://cdn.jsdelivr.net/npm/chartjs-adapter-date-fns"></script>

</head>

<body x-data x-cloak class="crb_body">

<jsp:include page="include/global/header2.html"/>

<main class="crb_main"
      :class="$store.sidebar.expanded ? 'crb_main_sidebar-expanded' : 'crb_main_sidebar-collapsed'">

<div x-data="dashboard()" x-init="init()" @save-dashboard.window="save()" class="p-4">

    <%@ include file="include/global/messagesArea.html" %>

    <!-- TOOLBAR -->
    <div class="flex items-center gap-4 mb-4">
        <!-- TITLE -->
        <h1 class="page-title-line !pb-0 !mb-0">My Dashboard</h1>
        <button x-show="!editMode"
        class="text-gray-500 hover:text-blue-600 transition"
                @click="toggleEdit()"
                aria-label="Configuration">
                <i data-lucide="sliders-horizontal" class="w-8 h-8"></i>
        </button>

        <button x-show="editMode"
                @click="toggleEdit()"
                class="px-3 py-1 rounded bg-green-600 text-white">
            Save
        </button>
        <button x-show="editMode"
                @click="openAddWidget()"
                class="px-3 py-1 rounded bg-green-600 text-white">
            Add Widget
        </button>
    </div>

    <div class="inline-flex items-center gap-1 rounded-lg bg-slate-200 dark:bg-slate-700 p-1">
    <template x-for="p in periods" :key="p">
        <button
            @click="timePeriod = p"
            :class="timePeriod === p
                ? 'bg-white dark:bg-slate-900 font-semibold shadow'
                : 'text-slate-700 hover:text-slate-900 dark:text-slate-300 dark:hover:text-white'"
            class="w-40 px-3 py-1.5 rounded-md transition-colors duration-200"
            x-text="p + ' jours'">
        </button>
    </template>
</div>

    <!-- GRID -->
    <div class="relative w-full overflow-hidden"
         :style="gridStyle()"
         :class="editMode ? 'border rounded bg-gray-50' : ''"
         @mousemove="onMouseMove"
         @mouseup="stopDrag">

         <!-- LOOP WIDGETS -->
        <template x-for="w in widgets" :key="w.id">
            <div class="absolute p-1 select-none"
                 :style="widgetStyle(w)">

                <!-- TEXT -->
                <template x-if="w.type === 'text'">
                    <div x-data="widgetText(w)" x-init="load()"
                         class="w-full h-full flex flex-col">
                        <jsp:include page="js/widgets/widgetText.html"/>
                    </div>
                </template>

                <!-- COUNT -->
                <template x-if="w.type === 'count'">
                    <div x-data="widgetCount(w)" x-init="load()"
                         class="w-full h-full flex flex-col">
                        <jsp:include page="js/widgets/widgetCount.html"/>
                    </div>
                </template>

                <!-- TIMELINE -->
                <template x-if="w.type === 'timeline'">
                    <div x-data="widgetTimeline(w)" x-init="load()"
                         class="w-full h-full crb_widget flex flex-col">
                        <jsp:include page="js/widgets/widgetTimeline.html"/>
                    </div>
                </template>

            </div>
        </template>

    </div>

    <!-- MODAL BACKDROP -->
    <div x-show="showAddModal"
         class="fixed inset-0 bg-black/40 z-40"
         @click="closeAddWidget()"></div>

    <!-- MODAL -->
    <div x-show="showAddModal"
         x-transition
         class="fixed inset-0 flex items-center justify-center z-50"
         @keydown.escape.window="closeAddWidget()">

        <div class="bg-white rounded-xl shadow-xl w-[400px] p-6">

            <div class="flex justify-between mb-4">
                <h2 class="text-lg font-semibold">Ajouter un widget</h2>
                <button @click="closeAddWidget()">✕</button>
            </div>

            <div class="grid gap-2">
                <button @click="addWidget('text','Application')"
                        class="px-3 py-2 bg-gray-100 rounded hover:bg-gray-200">
                    Text (2×1)
                </button>
                <button @click="addWidget('count','Application')"
                        class="px-3 py-2 bg-gray-100 rounded hover:bg-gray-200">
                    Count (2×1)
                </button>

                <button @click="addWidget('timeline','Count')"
                        class="px-3 py-2 bg-gray-100 rounded hover:bg-gray-200">
                    Availability (8×2)
                </button>
            </div>
        </div>
    </div>

</div>

</main>

<script>
function dashboard() {

    return {
        editMode: false,
        timePeriod: '7',
        periods: ['7','30','90'],
        showAddModal: false,
        cols: 12,
        rows: 20,
        cellSize: 60,
        widgets: [],
        dragging: null,
        gap: 10,

        init() {
            const saved = localStorage.getItem("widgets");
            this.widgets = saved ? JSON.parse(saved) : this.defaultWidgets();


        },

        defaultWidgets() {
    return [
        {
            id:1,
            type:"count",
            option:"Application",
            content:"Count",
            x:0,
            y:0,
            w:4,
            h:2,
            title:"Application",
            icon:"star",
            color:"green",
            editing:false
        }
    ];
},

        toggleEdit() {
            this.editMode = !this.editMode;
            if (!this.editMode) this.save();
        },

        save() {
            localStorage.setItem("widgets", JSON.stringify(this.widgets));
        },

        deleteWidget(id) {
            this.widgets = this.widgets.filter(w => w.id !== id);
            this.save();
        },

        gridStyle() {
            return { height: (this.rows * this.cellSize) + 'px' };
        },

        widgetStyle(w) {
            return {
                left: (w.x * this.cellSize) + 'px',
                top: (w.y * this.cellSize) + 'px',
                width: (w.w * this.cellSize) + 'px',
                height: (w.h * this.cellSize) + 'px'
            };
        },

        /* MODAL */
        openAddWidget() { this.showAddModal = true; },
        closeAddWidget() { this.showAddModal = false; },

        addWidget(type, option) {
            const sizes = {
                text: {w:4,h:1},
                count: {w:4,h:2},
                availability: {w:6,h:4},
                timeline: {w:16,h:4}
            };
            const s = sizes[type];

            const newId = this.widgets.length
                ? Math.max(...this.widgets.map(w => w.id)) + 1
                : 1;

            this.widgets.push({
                id:newId,
                type,
                option,
                content:"Count",
                x:0,
                y:0,
                w:s.w,
                h:s.h,
                title: option,
                icon:"star",
                color:"green",
                editing:false
            });

            this.save();
            this.closeAddWidget();
        },

        /* DRAG */
        startDrag(e, w) {
            if (!this.editMode) return;
            this.dragging = { w };
        },

        onMouseMove(e) {
            if (!this.dragging) return;

            const rect = e.currentTarget.getBoundingClientRect();
            const gx = Math.floor((e.clientX - rect.left) / this.cellSize);
            const gy = Math.floor((e.clientY - rect.top) / this.cellSize);

            this.dragging.w.x = Math.max(0, gx);
            this.dragging.w.y = Math.max(0, gy);
        },

        stopDrag() {
            if (this.dragging) this.save();
            this.dragging = null;
        }
    }
}
</script>

</body>
</html>