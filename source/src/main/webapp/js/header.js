/*
 * Cerberus  Copyright (C) 2013  vertigo17
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

function displayHeaderLabel() {
    var user = getUser();
    var doc = getDoc();
    $("#headerUserName").html(user.login);
    $("#menuEditTest").html("Test Edition");
    $("#menu-TestCase").html("Cas de Test"+"<span class=\"caret\">");
    $("#menuProject").html(doc.page_project.title.docLabel);
    var systems = getSystem();
    for (var s in systems){
            $("#MySystem").append($('<option></option>').text(systems[s].value).val(systems[s].value));
    }
    InitLanguage();
    }
    
    function InitLanguage() {
    var langCookie = getCookie("lang");
    if (langCookie === null) {
        //replace "en" by GetBrowserLanguage() when the other languages are supported
        setCookie("lang", "en");
    }
    $("#MyLang option[value=" + langCookie + "]").attr("selected", "selected");
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

    if (sessionStorage.getItem("sys") === null) {
        readSystem();
    }
    sys = sessionStorage.getItem("systems");
    sys = JSON.parse(sys);
    return sys;
}