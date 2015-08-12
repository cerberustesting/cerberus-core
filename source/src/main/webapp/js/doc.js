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

/*LANG COOKIE HANDLING - START*/

$(document).ready(function () {
    InitLanguage();
});

function InitLanguage() {
    var langCookie = getCookie("lang");
    if (langCookie === null) {
        //replace "en" by GetBrowserLanguage() when the other languages are supported
        setCookie("lang", "en");
    }
    $("#MyLang option[value=" + langCookie + "]").attr("selected", "selected");
}

function ChangeLanguage() {
    var select = document.getElementById("MyLang");
    var selectValue = select.options[select.selectedIndex].value;
    setCookie("lang", selectValue);
    sessionStorage.clear();
    location.reload();
}

function setCookie(name, value) {
    var today = new Date();
    var expires = new Date();
    expires.setTime(today.getTime() + (365 * 24 * 60 * 60 * 1000));
    document.cookie = name + "=" + encodeURIComponent(value) + ";expires=" + expires.toGMTString();
}

function getCookie(sName) {
    var cookContent = document.cookie;
    var cookEnd;
    var i, j, c;

    sName = sName + "=";
    for (i = 0, c = cookContent.length; i < c; i++) {
        j = i + sName.length;
        if (cookContent.substring(i, j) === sName) {
            cookEnd = cookContent.indexOf(";", j);
            if (cookEnd === -1) {
                cookEnd = cookContent.length;
            }
            return decodeURIComponent(cookContent.substring(j, cookEnd));
        }
    }
    return null;
}

/*LANG COOKIE HANDLING - END*/

/**
 * get language configuration for dataTable creation
 * @returns {JSONObject} 
 */
function getDataTableLanguage(){
  var doc = getDoc();
  var docTable = doc.dataTable;
  var res = {
          "table": {
        "sEmptyTable": docTable.sEmptyTable.docLabel,
        "sInfo": docTable.sInfo.docLabel,
        "sInfoEmpty": docTable.sInfoEmpty.docLabel,
        "sInfoFiltered": docTable.sInfoFiltered.docLabel,
        "sInfoPostFix": docTable.sInfoPostFix.docLabel,
        "sInfoThousands": docTable.sInfoThousands.docLabel,
        "sLengthMenu": docTable.sLengthMenu.docLabel,
        "sLoadingRecords": docTable.sLoadingRecords.docLabel,
        "sProcessing": docTable.sProcessing.docLabel,
        "sSearch": docTable.sSearch.docLabel,
        "sSearchPlaceholder": docTable.sSearchPlaceholder.docLabel,
        "sZeroRecords": docTable.sZeroRecords.docLabel,
        "oPaginate": {
            "sFirst": docTable.sFirst.docLabel,
            "sLast": docTable.sLast.docLabel,
            "sNext": docTable.sNext.docLabel,
            "sPrevious": docTable.sPrevious.docLabel 
        },
        "oAria": {
            "sSortAscending": docTable.sSortAscending.docLabel,
            "sSortDescending": docTable.sSortDescending.docLabel
        }
    },
    "colVis": {"buttonText": docTable.colVis.docLabel}
  };
  return res;
};

/**
 * Load the documentation from the database in sessionStorage
 * @param {String} lang
 * @returns {void}
 */
function readDocFromDatabase(lang) {
    $.ajax({url: "ReadDocumentation",
        data: {lang: lang},
        async: false,
        dataType: 'json',
        success: function (data) {
            var doc = data["labelTable"];
            sessionStorage.setItem("doc", JSON.stringify(doc));
        }
    });
}
/**
 * Get the documentation from sessionStorage
 * @returns {JSONObject} Full documentation in defined language from sessionStorage
 */
function getDoc() {
    var lang = getCookie("lang");
    var doc;

    if (sessionStorage.getItem("doc") === null) {
        readDocFromDatabase(lang);
    }
    doc = sessionStorage.getItem("doc");
    doc = JSON.parse(doc);
    return doc;
}

/**
 * generate the string with a link to online documentation
 * @param {JSONObject} docObj
 * @returns {String} A String which contains the Label and the link to online doc
 */
function displayDocLink(docObj) {
    var res;

    res = docObj.docLabel + " <a class=\"docOnline\" href=\'javascript:popup(\"Documentation.jsp?DocTable=" + docObj.docTable +
            "&DocField=" + docObj.docField + "&Lang=" + getCookie("lang") + "\")\' onclick=\"stopPropagation(event)\"><span class=\"glyphicon glyphicon-question-sign\"></span></a>";
    return res;
}