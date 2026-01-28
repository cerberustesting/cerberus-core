/*
 * Cerberus Copyright (C) 2013 - 2025 cerberustesting
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This file is part of Cerberus.
 *
 * Cerberus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cerberus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.
 */

$(document).ready(function () {

    $('#navMenuTest #menuTestCaseCreate').on('click', function () {
       openModalTestCaseSimple();
    });

});


function workspaceSelector() {
    return {
        open: false,
        selected: [],
        workspaces: [],
        search: '',
        previousSelected: [],

        init() {
            let user = JSON.parse(sessionStorage.getItem("user")) || {};
            this.workspaces = user.system || [];
            this.selected = user.defaultSystems || [];
            this.previousSelected = [...this.selected];

            window.addEventListener('user-loaded', (event) => {
                const user = event.detail;
                this.workspaces = user.system || [];
                this.selected = user.defaultSystems || [];
                this.previousSelected = [...this.selected];
            });
        },

        toggleOpen() {
            this.open = !this.open;
        },
        toggle(workspace) {
            const idx = this.selected.indexOf(workspace);
            if (idx === -1) this.selected.push(workspace);
            else this.selected.splice(idx, 1);
        },

        selectAll() {
            this.selected = [...this.workspaces];
        },

        selectNone() {
            this.selected = [];
        },

        selectedLabel() {
            if (this.selected.length === 0) return 'Select...';
            if (this.selected.length === this.workspaces.length) return 'All selected';
            if (this.selected.length === 1) return this.selected[0];
            return `${this.selected.length} selected`;
        },

        filteredWorkspaces() {
            if (!this.search) return this.workspaces
            return this.workspaces.filter(w =>
                w.toLowerCase().includes(this.search.toLowerCase())
            )
        },

        close() {
            this.open = false;
            this.$nextTick(() => {
                if (!arraysEqual(this.selected, this.previousSelected)) {
                    this.previousSelected = [...this.selected];
                    ChangeWorkspace(this.selected);
                }
            });
        }
    }
}

function arraysEqual(a, b) {
    if (a.length !== b.length) return false;
    for (let i = 0; i < a.length; i++) {
        if (a[i] !== b[i]) return false;
    }
    return true;
}



function userMenu() {
    return {
        user: JSON.parse(sessionStorage.getItem('user') || '{}'),
        username: '',
        initials: '',
        roleText: '',
        userMenuOpen: false,
        language: "fr",
        logoutHref: '',

        init() {
            this.applyTheme(Alpine.store('user').theme);
            this.updateLogoutLink();
            this.userInfo();
        },

        toggleOpen() {
            this.userMenuOpen = !this.userMenuOpen;
            //if (this.open) this.refreshHistoryMenu();
        },

        close() {
            this.userMenuOpen = false;
        },

        applyTheme(theme) {
            if (theme === 'dark') {
                document.documentElement.classList.add('dark');
            } else if (theme === 'light') {
                document.documentElement.classList.remove('dark');
            } else if (theme === 'system') {
                // applique la préférence système
                if (window.matchMedia('(prefers-color-scheme: dark)').matches) {
                    document.documentElement.classList.add('dark');
                } else {
                    document.documentElement.classList.remove('dark');
                }
            }
            localStorage.setItem('theme', theme);
            Alpine.store('user').theme = theme;
        },

        changeLanguage(language) {
            const user = getUser();
            const lang = language;

            $.ajax({
                url: "UpdateMyUser",
                data: {
                    id: user.login,
                    column: "language",
                    value: lang
                },
                async: false,
                success: function () {
                    sessionStorage.clear();
                    getUser();
                    Alpine.store('user').language = lang;
                    Alpine.store('labels').language = lang;

                    //location.reload();
                },
                error: function (xhr, status, error) {
                    console.error("Erreur lors du changement de langue :", error);
                }
            });
        },

        updateLogoutLink() {
            const user = getUser();
            if (!user.menu.logoutLink) {
                this.logoutHref = '';
            } else {
                let aL = "";
                let aLA = window.location.href.split("/");
                for (let i = 0; i < aLA.length; i++) {
                    if (i < aLA.length - 1) {
                        aL += aLA[i] + "/";
                    } else {
                        if (aLA[i].indexOf(".jsp") === -1 && aLA[i].length > 0) {
                            aL += aLA[i] + "/";
                        }
                    }
                }
                aL += "Logout.jsp";
                this.logoutHref = user.menu.logoutLink.replace('%LOGOUTURL%', encodeURIComponent(aL));
            }
        },

        userInfo(){
            const user = getUser();
            this.username = (user.name && user.name.trim() !== '') ? user.name : user.login;

            // Initiales
            if (this.username.includes(' ')) {
                const parts = this.username.split(' ');
                this.initials = (parts[0][0] || '') + (parts[1][0] || '');
            } else {
                this.initials = this.username.slice(0, 2);
            }
            this.initials = this.initials.toUpperCase();

            // Role (To improve)
            this.roleText = user.isAdmin ? 'Administrator' : 'Automatician';
        }
    }
}

function quickHistoryMenu() {
    return {
        openMenu: false,
        testcases: [],
        executions: [],
        campaigns: [],

        init() {
            this.testcases = JSON.parse(localStorage.getItem('historyTestcases') || '[]')
                .slice(-6)
                .reverse()
                .map(t => ({
                    name: `${t.test} ${t.testcase}`,
                    desc: t.description || '',
                    href: `TestCaseScript.jsp?test=${encodeURIComponent(t.test)}&testcase=${encodeURIComponent(t.testcase)}`
                }));

            this.executions = JSON.parse(localStorage.getItem('historyExecutions') || '[]')
                .slice(-6)
                .reverse()
                .map(e => ({
                    id: e.id,
                    status: e.controlStatus,
                    name: `${e.test} ${e.testcase}`,
                    env: `${e.country} ${e.environment} ${e.robot}`,
                    href: `TestCaseExecution.jsp?executionId=${e.id}`
                }));

            this.campaigns = JSON.parse(localStorage.getItem('historyCampaigns') || '[]')
                .slice(-6)
                .reverse()
                .map(c => ({
                    tag: c.tag,
                    href: `ReportingExecutionByTag.jsp?Tag=${encodeURIComponent(c.tag)}`
                }));
        },

        open() {
            this.openMenu = true;
            this.$nextTick(() => this.position());
        },

        close() {
            this.openMenu = false;
        },

        position() {
            const sidebar = document.getElementById('crb_sidebar').getBoundingClientRect();
            const dropdown = this.$refs.dropdown;
            const offset = 12;

            dropdown.style.left = `${sidebar.right}px`;
            dropdown.style.bottom = `${window.innerHeight - sidebar.bottom}px`;
        }
    }
}


/*function refreshHistoryMenu() {

    var lastExecutions= [];
    var lastCampaigns= [];
    var lastTests= [];

        // Testcases
    let testcases = JSON.parse(localStorage.getItem("historyTestcases") || "[]").reverse();
    lastTests = testcases.map(item => ({
        name: item.test + " " + item.testcase,
        description: item.description || "",
        href: "TestCaseScript.jsp?test=" + encodeURIComponent(item.test) + "&testcase=" + encodeURIComponent(item.testcase)
    }));

    // Executions
    let executions = JSON.parse(localStorage.getItem("historyExecutions") || "[]").reverse();
    lastExecutions = executions.map(item => ({
        id: item.id,
        status: item.controlStatus,
        description: (item.test + " " + item.testcase + " | " + item.country + " " + item.environment + " " + item.robot) +
            (item.description ? " | " + item.description : ""),
        href: "TestCaseExecution.jsp?executionId=" + item.id
    }));

    // Campaigns
    let campaigns = JSON.parse(localStorage.getItem("historyCampaigns") || "[]").reverse();
    lastCampaigns = campaigns.map(item => ({
        tag: item.tag,
        href: "ReportingExecutionByTag.jsp?Tag=" + encodeURIComponent(item.tag)
    }));
}*/

/**
 * CHANGE SYSTEM (WORKSPACE)
 * @param selectedWorkspaces
 * @constructor
 */
function ChangeWorkspace(selectedWorkspaces) {
    var user = getUser();

    if (!user || !user.login) {
        console.error("Aucun utilisateur trouvé");
        return;
    }

    $.ajax({
        url: "UpdateMyUserSystem",
        type: "POST",
        data: $.param({
            id: user.login,
            MySystem: selectedWorkspaces // <-- ton tableau de workspaces sélectionnés
        }, true), // 'true' pour traditional, répéter le paramètre
        async: false,
        success: function () {
            user.defaultSystems = [...selectedWorkspaces];
            user.defaultSystemsQuery = selectedWorkspaces.reduce(
                (acc, s) => acc + '&system=' + encodeURIComponent(s),
                ''
            );
            sessionStorage.setItem("user", JSON.stringify(user));
            window.dispatchEvent(new CustomEvent('user-loaded', { detail: user }));
            location.reload(true);
        },
        error: function (xhr, status, error) {
            console.error("Erreur lors de la mise à jour du système :", error);
        }
    });
}

function getWorkspace() {
    const workspaceEl = document.querySelector('[x-data="workspaceSelector()"]');
    if (!workspaceEl) return null;
    const alpineData = workspaceEl.__x.$data;
    return alpineData.selected;
}


