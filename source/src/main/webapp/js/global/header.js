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

    //collaspe if the navbar was collaspe in the previous page
    collaspeHandler(localStorage.getItem("navbar-toggle"));

    //highlight the current page link
    currentPageLinkHighlight();

    //set up the listenners
    $(window).resize(function () {
        adapteSize();
    });

    $('#controlToggle').click(function () {
        collaspePage();
    });

    $('.navbar-side-choice').hover(function () {
        $(this).unbind("click");
        if ($("#page-layout").hasClass("extended")) {
            $(this).find('> ul').addClass('in');
            $(this).addClass('active');
            $(this).find('> ul').css("height", "");//remove display bug
        }
    }, function () {
        if ($("#page-layout").hasClass("extended")) {
            $(this).find('> ul').removeClass('in');
            $(this).removeClass('active');
        }
    });

    $('.navbar-side-choice').click(function () {
        if ($(this).hasClass("active")) {
            $(this).toggleClass("active");
        }
    });

    $('#navMenuTest #menuTestCaseCreate').on('click', function () {
        openModalTestCaseSimple();
    });

});

/*
 * highlight the current page link
 * @returns {undefined}
 */
function currentPageLinkHighlight() {
    for (var i in document.getElementsByClassName("nav nav-second-level collapse in")) {

        if (document.getElementsByClassName("nav nav-second-level collapse in")[i].parentElement !== undefined) {
            if (!$("#page-layout").hasClass("extended")) {
                document.getElementsByClassName("nav nav-second-level collapse in")[i].parentElement.className += " active";
                document.getElementsByClassName("nav nav-second-level collapse in")[i].parentElement.style.color = "white";
            } else {
                document.getElementsByClassName("nav nav-second-level collapse in")[i].className = "nav nav-second-level collapse";
            }
        }
    }
}

/*
 * close all the submenu on the side bar
 * @returns {undefined}
 */
function closeEveryNavbarMenu() {

    $('.sidebar-nav .navbar-side-choice').each(function (i, obj) {
        $(obj).removeClass("active");
    });
    $('.sidebar-nav .nav-second-level').each(function (i, obj) {
        $(obj).removeClass("in");
    });
}

/*
 * open the one sub menu
 * @param {type} classSelector
 * @returns {undefined}
 */
function openNavbarMenu(idNavMenu) {
    //close all other navbar menu
    closeEveryNavbarMenu();
    //open the menu selected if the navbar is active
    if (!$("#page-layout").hasClass("extended")) {
        $('.sidebar-nav .navbar-side-choice').each(function (i, obj) {
            if ($(obj).attr('id') === idNavMenu) {
                $(obj).addClass("active")
                var subMenuList = $(obj).find($(".nav-second-level"));
                subMenuList.removeClass("collaspe");
                subMenuList.addClass("in");
            }
        });
    }
}


/*
 * Adapte the #page-layout and the dataTable size for the screen size
 * @returns {undefined}
 */
function adapteSize() {
    updateTheDisplayOfTheLayout();
    handleSmallScreenSize(768);
    if ($(".dataTables_scrollHeadInner").width() < $(".dataTables_scrollHead").width()) {
        var tables = $('.dataTable').DataTable();
        tables.draw();
    }
}


/**
 * Change the height of the layout to make it fit the element inside the page
 * @returns {void}
 */

function updateTheDisplayOfTheLayout() {
    //resize to take the whole page space
    if ($(window).height() < $(document).height())
        $("#page-layout").css("height", "");//no need to set a height
    else {
        $("#page-layout").height($(window).height());
    }
}

/*
 * adapte the navbar for screen below the width size
 * @param {type} width
 * @returns {undefined}
 */
var pageCollapsedBeforeMinification = false;//global var
function handleSmallScreenSize(width) {

    if ($(window).width() <= width) {
        if ($("#page-layout").hasClass("extended")) {
            collaspeHandler("collaspe");
            pageCollapsedBeforeMinification = true;
        }
        $("#page-layout").css("margin", "0px");
        $(".navbar-static-top").css("margin", "0px");
    } else {
        //reverse to the previous state
        if (pageCollapsedBeforeMinification)
            collaspeHandler("extended");
        stateOfCollaspeBeforeMinification = false;

        $("#page-layout").css("margin", "");
        $(".navbar-static-top").css("margin", "");
    }
}

/*
 * collaspe the navbar with collaspeHandler and set up the transition style and redraw the dataTable if needed at the end
 * @returns {undefined}
 */
function collaspePage() {

    collaspeSubMenu();
    //set page layout transition style after the first drawing of the table
    setElementCssForTransition("#page-layout", "0.5");
    setElementCssForTransition("#sidebar", "0.5");
    setElementCssForTransition("#topbar", "0.5");

    if ($("#page-layout").hasClass("extended")) {
        collaspeHandler("collaspe");
    } else {
        collaspeHandler("extended");
    }

    $("#side-menu").css("opacity", "0");
    $("#side-menu").delay(500).fadeTo("quick", 1, function () {
        //reDraw the table if datable is not overflowing
        if ($(".dataTables_scrollHeadInner").width() < $(".dataTables_scrollHead").width()) {
            //updateTheDisplayOfTheLayout();
            var tables = $('.dataTable').DataTable();
            tables.draw();
        }
    });

    setElementCssForTransition("#page-layout", "0");
    setElementCssForTransition("#sidebar", "0");
    setElementCssForTransition("#topbar", "0");
}


/*
 * 
 * @param {type} action
 * @returns {undefined}
 */
function collaspeHandler(action) {
    if (action === "collaspe") {
        $('.controlToggleIcon').removeClass("fa fa-arrow-circle-right hit");
        $('.controlToggleIcon').addClass("fa fa-arrow-circle-left hit");
        localStorage.setItem("navbar-toggle", "collaspe");

        if ($("#page-layout").hasClass("extended")) {
            $('.navbar-default').toggleClass('collapsed');
            $('.navbar-static-top').toggleClass('collapsed');
            $('#page-layout').toggleClass('extended');
        }
        localStorage.setItem("navbar-toggle", true);
        $('#page-layout').css('margin-left', '250px');
        $('#side-menu').css('min-width', '250px');
    } else if (action === "extended") {
        $('.controlToggleIcon').removeClass("fa fa-arrow-circle-left hit");
        $('.controlToggleIcon').addClass("fa fa-arrow-circle-right hit");
        localStorage.setItem("navbar-toggle", "extended");

        if (!$("#page-layout").hasClass("extended")) {
            $('.navbar-default').toggleClass('collapsed');
            $('.navbar-static-top').toggleClass('collapsed');
            $('#page-layout').toggleClass('extended');
        }
        $('#page-layout').css('margin-left', '60px');
        $('#side-menu').css('min-width', '60px');
    } else {//first loading
        $('.controlToggleIcon').addClass("fa fa-arrow-circle-left hit");
    }
}

/*
 * Set up the transition style for the element
 * @param {type} element
 * @param {type} seconde
 * @returns {undefined}
 */
function setElementCssForTransition(element, seconde) {
    $(element).css("-webkit-transition", "all " + seconde + "s ease-in-out");
    $(element).css("-moz-transition", "all  " + seconde + "s ease-in-out");
    $(element).css("transition", "all  " + seconde + "s ease-in-out");
}


/*
 * collapse the submenu of the navbar
 * @returns {undefined}
 */
function collaspeSubMenu() {
    for (var i = 0; i < $('#side-menu').children("li").length; i++) {
        var currentSubNavbar = $($('#side-menu').children("li")[i]);
        currentSubNavbar.find('> ul').removeClass('in');
        currentSubNavbar.removeClass('active');
    }
}


function displayHeaderLabel(doc) {
    var user = getUser();

    if (user !== null) {
        // Display Menu
        displayMenuItem(doc);

        // Header User Menu
        $("#headerUserName").html(user.menu.nameDisplay);

        if (user.menu.accountLink === "") {
            $("#menuAccount").attr("href", user.menu.accountLink);
            $("#menuAccount").attr("target", "_blank");
            $("#menuAccount").attr("style", "display: none;");
        } else {
            $("#menuAccount").attr("href", user.menu.accountLink);
            $("#menuAccount").attr("target", "_blank");
            $("#menuAccount").attr("style", "display: block;");
        }

        if (user.menu.logoutLink === "") {
            $("#menuLogout").attr("style", "display: none;");
        } else {
            // Get the current URL
            var aL = "";
            var aLA = window.location.href.split("/");
            var i = 1;
            for (var s in aLA) {
                if ((i < aLA.length)) {
                    aL = aL + aLA[s] + "/";
                } else {
                    if ((aLA[s].indexOf(".jsp") === -1) && (aLA[s].length > 0)) {
                        aL = aL + aLA[s] + "/";
                    }
                }
                i++;
            }
            aL = aL + "Logout.jsp";
            $("#menuLogout").attr("href", user.menu.logoutLink.replace('%LOGOUTURL%', encodeURIComponent(aL)));
            $("#menuLogout").attr("style", "display: block;");
        }

        // Refresh History Menu
        $("#userDropdownMenu").on("mouseenter", function () {
            refreshHistoryMenu();
        });


        // Refresh Combo user menu
        loadUserSystemCombo();

        // Language menu
        var languages = getLanguageFromSessionStorage();
        $("#MyLang option").remove();
        for (var l in languages) {
            $("#MyLang").append($('<option></option>').text(languages[l].description).val(languages[l].value));
            if (languages[l].value === "en") {
                $("option[value=" + languages[l].value + "]").prepend($('<span class="flag gb"></span>'));
            } else {
                $("option[value=" + languages[l].value + "]").prepend($('<span class="flag ' + languages[l].value + '"></span>'));
            }
        }
        $("#MyLang option[value=" + user.language + "]").attr("selected", "selected");

    }
}


function loadUserSystemCombo() {
    // System menu
    var user = getUser();
//        var systems = getSystem();
    $("#MySystem option").remove();
    for (var s in user.system) {
        $("#MySystem").append($('<option></option>').text(user.system[s]).val(user.system[s]));
    }
    for (var s in user.defaultSystems) {
        $("#MySystem option[value='" + user.defaultSystems[s] + "']").attr("selected", "selected");
    }


    var select = $("#MySystem");
    select.multiselect(new multiSelectConfSystem("MySystem"));

    $("#MySystem").on("onChange", function () {
        console.info("onChange");
    });
    $("#MySystem").on("onDropdownHidden", function () {
        console.info("onDropdownHidden");
    });
    $("#MySystem").change(function () {
        console.info("onDropdownHidden");
    });



}


function refreshHistoryMenu() {
    $(".histo").remove();

    let entryList = localStorage.getItem("historyTestcases");
    entryList = JSON.parse(entryList);
    if (entryList !== null && entryList.length > 0) {
        $("#userMenu").append("<li class='menuSeparator histo'><span>Last Seen Testcases</span></li>");
        for (var item in entryList) {
            let newitem = entryList.length - item - 1;
            let desc = "<div></div>";
            if ((entryList[newitem].description !== undefined) && (entryList[newitem].description !== "")) {
                desc = "<div style='font-size: 10px;min-width: 350px'> " + entryList[newitem].description + "</div>";
            }
            $("#userMenu").append("<li class='histo'><a name='menuitem' href='TestCaseScript.jsp?test=" + encodeURIComponent(entryList[newitem].test) + "&testcase=" + encodeURIComponent(entryList[newitem].testcase) + "'><i class='fa fa-bars'></i>" +
                    "<span>  " + entryList[newitem].test + " " + entryList[newitem].testcase + "</span>" + desc + "</a></li>");
        }
    }
    entryList = localStorage.getItem("historyExecutions");
    entryList = JSON.parse(entryList);
    if (entryList !== null && entryList.length > 0) {
        $("#userMenu").append("<li class='menuSeparator histo'><span>Last Seen Executions</span></li>");
        for (var item in entryList) {
            let newitem = entryList.length - item - 1;
            let desc = "<div style='font-size: 10px;min-width: 350px'> " + entryList[newitem].test + " " + entryList[newitem].testcase + " | " + entryList[newitem].country + " " + entryList[newitem].environment + " " + entryList[newitem].robot + "</div>";
            if ((entryList[newitem].description !== undefined) && (entryList[newitem].description !== "")) {
                desc += "<div style='font-size: 10px;min-width: 350px'> " + entryList[newitem].description + "</div>";
            }
            $("#userMenu").append("<li class='histo'><a name='menuitem' href='TestCaseExecution.jsp?executionId=" + entryList[newitem].id + "'><i class='fa fa-gear status" + entryList[newitem].controlStatus + "'></i>" +
                    "<span class='status" + entryList[newitem].controlStatus + "'>  " + entryList[newitem].id + "</span>" + desc + "</a></li>");
        }
    }
    entryList = localStorage.getItem("historyCampaigns");
    entryList = JSON.parse(entryList);
    if (entryList !== null && entryList.length > 0) {
        $("#userMenu").append("<li class='menuSeparator histo'><span>Last Seen Campaigns</span></li>");
        for (var item in entryList) {
            let newitem = entryList.length - item - 1;
            $("#userMenu").append("<li class='histo'><a name='menuitem' href='ReportingExecutionByTag.jsp?Tag=" + encodeURIComponent(entryList[newitem].tag) + "'><i class='fa fa-gears'></i><span>  " + entryList[newitem].tag + "</span></a></li>");
        }
    }
}

function multiSelectConfSystem(name) {
    this.maxHeight = 450;
    this.checkboxName = name;
    this.buttonWidth = "100%";
    this.enableFiltering = true;
    this.enableCaseInsensitiveFiltering = true;
    this.includeSelectAllOption = true;
    this.includeSelectAllIfMoreThan = 2;
}

function ChangeLanguage() {
    var select = document.getElementById("MyLang");
    var selectValue = select.options[select.selectedIndex].value;
    var user = getUser();

    $.ajax({url: "UpdateMyUser",
        data: {id: user.login, column: "language", value: selectValue},
        async: false,
        success: function () {
            sessionStorage.clear();
            location.reload();
        }
    });
}

function ChangeSystem() {

    var user = getUser();

    $.ajax({url: "UpdateMyUserSystem",
        data: "id=" + user.login + "&" + $("#SysFilter").serialize(),
        async: false,
        success: function () {
            sessionStorage.removeItem("user");
            location.reload(true);
        }
    });
}

function updateUserPreferences(objectWaitingLayer) {
    var objectWL = $(objectWaitingLayer);
    if (objectWaitingLayer !== undefined) {
        showLoader(objectWL);
    }
    let tempLocalStorage = [];
    tempLocalStorage = localStorage;

//    tempLocalStorage.removeItem("historyTestcases");
//    tempLocalStorage.removeItem("historyExecutions");
//    tempLocalStorage.removeItem("historyCampaigns");
//    tempLocalStorage.removeItem("properties");
//    tempLocalStorage.removeItem("secondaryProperties");
//    tempLocalStorage.removeItem("listReport");

    for (var i = 0; i < tempLocalStorage.length; i++) {
        if (tempLocalStorage.key(i).startsWith("DataTables_")) {
            let temp = JSON.parse(tempLocalStorage.getItem(tempLocalStorage.key(i)));
            temp.start = 0;
            tempLocalStorage.setItem(tempLocalStorage.key(i), JSON.stringify(temp));
        }
    }
//    console.info(tempLocalStorage);
    var uPref = "";
    uPref = JSON.stringify(localStorage);
//    console.info("upload user perf to Cerberus :");
//    console.info(uPref);

    $.ajax({url: "UpdateMyUser",
        type: "POST",
        data: {column: "userPreferences", value: uPref},
        async: false,
        success: function (data) {
            var messageType = getAlertType(data.messageType);
            if (messageType === "success") {
                readUserFromDatabase();
            }
            //show message in the main page
            showMessageMainPage(messageType, data.message, true);
        }
    });
    if (objectWaitingLayer !== undefined) {
        hideLoader(objectWL);
    }
}

function displayMenuItem(doc) {
    // Translate Normal menu entries.
    var menuItems = document.getElementsByName('menu');
    $(menuItems).each(function () {
        var id = $(this).attr('id');
        if ($(this).attr('class') === "dropdown-toggle") {
            $(this).html(doc.getDocLabel("page_header", id) + " <span class=\"caret\"></span>");
        } else {
            $(this).html(doc.getDocLabel("page_header", id));
        }
    });
    // Translate Beta menu entries.
    var menuItems = document.getElementsByName('menuBeta');
    $(menuItems).each(function () {
        var id = $(this).attr('id');
        if ($(this).attr('class') === "dropdown-toggle") {
            $(this).html(doc.getDocLabel("page_header", id) + " <span class=\"caret\"></span>");
        } else {
            $(this).html(doc.getDocLabel("page_header", id) + "<input type=\"button\" class=\"btn btn-warning btn-small active\" value=\"Beta\" style=\"padding: 0px; margin-left: 5px\">");
        }
    });
    // Translate Deprecated menu entries.
    var menuItems = document.getElementsByName('menuDeprecated');
    $(menuItems).each(function () {
        var id = $(this).attr('id');
        if ($(this).attr('class') === "dropdown-toggle") {
            $(this).html(doc.getDocLabel("page_header", id) + " <span class=\"caret\"></span>");
        } else {
            $(this).html(doc.getDocLabel("page_header", id) + "<input type=\"button\" class=\"btn btn-danger btn-small active\" value=\"Deprecated\" style=\"padding: 0px; margin-left: 5px\">");
        }
    });
    /**
     * Display Menu accordingly to the user right
     */
    var user = getUser();
    if (user !== null) {
        for (var group in user.group) {
            $('.' + user.group[group] + '.navlist').removeAttr('style');
        }
    }
    $("#openInteractiveTutoModal").html(doc.getDocLabel("page_header", "menuTutorial"));

}

function readSystem() {
    $.ajax({url: "FindInvariantByID",
        data: {idName: "SYSTEM"},
        async: false,
        dataType: 'json',
        success: function (data) {
            var sys = data;
            sessionStorage.setItem("systems", JSON.stringify(sys));
        }
    });
}

/**
 * Get the documentation from sessionStorage
 * @returns {JSONObject} Full documentation in defined language from sessionStorage
 */
function getSystem() {
    var sys;

    if (sessionStorage.getItem("systems") === null) {
        readSystem();
    }
    sys = sessionStorage.getItem("systems");
    sys = JSON.parse(sys);
    return sys;
}

function readLanguage() {
    $.ajax({url: "FindInvariantByID",
        data: {idName: "LANGUAGE"},
        async: false,
        dataType: 'json',
        success: function (data) {
            var lang = data;
            sessionStorage.setItem("language", JSON.stringify(lang));
        }
    });
}

/**
 * Get the documentation from sessionStorage
 * @returns {JSONObject} Full documentation in defined language from sessionStorage
 */
function getLanguageFromSessionStorage() {
    var lang;

    if (sessionStorage.getItem("language") === null) {
        readLanguage();
    }
    lang = sessionStorage.getItem("language");
    lang = JSON.parse(lang);
    return lang;
}
