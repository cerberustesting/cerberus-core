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

var frDt = {
    "table": {
        "sProcessing": "Traitement en cours...",
        "sSearch": "",
        "sLengthMenu": "_MENU_",
        "sInfo": "Affichage de l'&eacute;l&eacute;ment _START_ &agrave; _END_ sur _TOTAL_ &eacute;l&eacute;ments",
        "sInfoEmpty": "Affichage de l'&eacute;l&eacute;ment 0 &agrave; 0 sur 0 &eacute;l&eacute;ments",
        "sInfoFiltered": "(filtr&eacute; de _MAX_ &eacute;l&eacute;ments au total)",
        "sInfoPostFix": "",
        "sLoadingRecords": "Chargement en cours...",
        "sZeroRecords": "Aucun &eacute;l&eacute;ment &agrave; afficher",
        "sEmptyTable": "Aucune donn&eacute;e disponible dans le tableau",
        "sSearchPlaceholder": "Rechercher...",
        "oPaginate": {
            "sFirst": "Premier",
            "sPrevious": "Pr&eacute;c&eacute;dent",
            "sNext": "Suivant",
            "sLast": "Dernier"
        },
        "oAria": {
            "sSortAscending": ": activer pour trier la colonne par ordre croissant",
            "sSortDescending": ": activer pour trier la colonne par ordre d&eacute;croissant"
        }
    },
    "colVis": {"buttonText": "Afficher/Cacher les colonnes"}
};

var enDt = {
    "table": {
        "sEmptyTable": "No data available in table",
        "sInfo": "Showing _START_ to _END_ of _TOTAL_ entries",
        "sInfoEmpty": "Showing 0 to 0 of 0 entries",
        "sInfoFiltered": "(filtered from _MAX_ total entries)",
        "sInfoPostFix": "",
        "sInfoThousands": ",",
        "sLengthMenu": "_MENU_",
        "sLoadingRecords": "Loading...",
        "sProcessing": "Processing...",
        "sSearch": "_INPUT_",
        "sSearchPlaceholder": "Search...",
        "sZeroRecords": "No matching records found",
        "oPaginate": {
            "sFirst": "First",
            "sLast": "Last",
            "sNext": "Next",
            "sPrevious": "Previous"
        },
        "oAria": {
            "sSortAscending": ": activate to sort column ascending",
            "sSortDescending": ": activate to sort column descending"
        }
    },
    "colVis": {"buttonText": "Show/Hide columns"}
};

var langDt = {
    "fr": frDt,
    "en": enDt
};

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
    localStorage.clear();
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

function setDoc(lang) {
    $.ajax({url: "ReadDocumentation",
        data: {lang: lang},
        async: false,
        dataType: 'json',
        success: function (data) {
            var doc = data["labelTable"];
            localStorage.setItem("doc", JSON.stringify(doc));
        }
    });
}

function getDoc() {
    var lang = getCookie("lang");
    var doc;

    if (localStorage.getItem("doc") === null) {
        setDoc(lang);
    }
    doc = localStorage.getItem("doc");
    doc = JSON.parse(doc);
    return doc;
}

function getDocByPage(pageName) {
    var doc = getDoc();
    var res = [];

    for (var i = 0; i < doc.length; i++) {
        if (doc[i].docTable === pageName) {
            res.push(doc[i]);
        }
    }
    return res;
}

function displayDocLink(docObj) {
    var res;

    res = docObj.docLabel + " <a class=\"docOnline\" href=\'javascript:popup(\"Documentation.jsp?DocTable=" + docObj.docTable +
            "&DocField=" + docObj.docField + "&Lang=" + getCookie("lang") + "\")\' onclick=\"stopPropagation(event)\"><span class=\"glyphicon glyphicon-question-sign\"></span></a>";
    return res;
}