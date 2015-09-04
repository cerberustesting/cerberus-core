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

/**
 * get language configuration for dataTable creation
 * @returns {JSONObject} 
 */
function getDataTableLanguage() {
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
}
;
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
    var doc;
    if (sessionStorage.getItem("doc") === null) {
        var user = getUser();
        readDocFromDatabase(user.language);
    }
    doc = sessionStorage.getItem("doc");
    doc = JSON.parse(doc);
    return doc;
}

function Doc() {
    this.table = getDoc();
}

Doc.prototype.getDocLabel = function (docTable, docField) {
    try {
        if (!(this.table.hasOwnProperty(docTable)))
            throw "docTable " + docTable + " not found";
        if (!(this.table[docTable].hasOwnProperty(docField)))
            throw "docField " + docField + " not found";
        return this.table[docTable][docField].docLabel;
    } catch (err) {
        var res;
        var user = getUser();
        
        res = docField + " <a class=\"nodoc\" href=\'javascript:popup(\"Documentation.jsp?DocTable=" + docTable +
                "&DocField=" + docField + "&Lang=" + user.language + "\")\' onclick=\"stopPropagation(event)\"><span class=\"glyphicon glyphicon-exclamation-sign\"></span></a>";
        return res;
    }
};

Doc.prototype.getDocOnline = function (docTable, docField) {
    var res;
    var user = getUser();

    try {
        if (!(this.table.hasOwnProperty(docTable)))
            throw "docTable " + docTable + " not found";
        if (!(this.table[docTable].hasOwnProperty(docField)))
            throw "docField " + docField + " not found";
        res = this.table[docTable][docField].docLabel + " <a class=\"docOnline\" href=\'javascript:popup(\"Documentation.jsp?DocTable=" + this.table[docTable][docField].docTable +
                "&DocField=" + this.table[docTable][docField].docField + "&Lang=" + user.language + "\")\' onclick=\"stopPropagation(event)\"><span class=\"glyphicon glyphicon-question-sign\"></span></a>";
    } catch (err) {
        res = docField + " <a class=\"nodoc\" href=\'javascript:popup(\"Documentation.jsp?DocTable=" + docTable +
                "&DocField=" + docField + "&Lang=" + user.language + "\")\' onclick=\"stopPropagation(event)\"><span class=\"glyphicon glyphicon-exclamation-sign\"></span></a>";
    } finally {
        return res;
    }
};

/**
 * generate the string with a link to online documentation
 * @param {JSONObject} docObj
 * @returns {String} A String which contains the Label and the link to online doc
 */
function displayDocLink(docObj) {
    var res;
    var user = getUser();
    res = docObj.docLabel + " <a class=\"docOnline\" href=\'javascript:popup(\"Documentation.jsp?DocTable=" + docObj.docTable +
            "&DocField=" + docObj.docField + "&Lang=" + user.language + "\")\' onclick=\"stopPropagation(event)\"><span class=\"glyphicon glyphicon-question-sign\"></span></a>";
    return res;
}
