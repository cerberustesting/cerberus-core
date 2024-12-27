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
/**
 * get language configuration for dataTable creation
 * @returns {JSONObject}
 */
function getDataTableLanguage() {
    var doc = new Doc();

    var res = {
        "table": {
            "sEmptyTable": doc.getDocLabel("dataTable", "sEmptyTable"),
            "sInfo": doc.getDocLabel("dataTable", "sInfo"),
            "sInfoEmpty": doc.getDocLabel("dataTable", "sInfoEmpty"),
            "sInfoFiltered": doc.getDocLabel("dataTable", "sInfoFiltered"),
            "sInfoPostFix": doc.getDocLabel("dataTable", "sInfoPostFix"),
            "sInfoThousands": doc.getDocLabel("dataTable", "sInfoThousands"),
            "sLengthMenu": doc.getDocLabel("dataTable", "sLengthMenu"),
            "sLoadingRecords": doc.getDocLabel("dataTable", "sLoadingRecords"),
            "sProcessing": doc.getDocLabel("dataTable", "sProcessing"),
            "sSearch": doc.getDocLabel("dataTable", "sSearch"),
            "sSearchPlaceholder": doc.getDocLabel("dataTable", "sSearchPlaceholder"),
            "sZeroRecords": doc.getDocLabel("dataTable", "sZeroRecords"),
            "oPaginate": {
                "sFirst": doc.getDocLabel("dataTable", "sFirst"),
                "sLast": doc.getDocLabel("dataTable", "sLast"),
                "sNext": doc.getDocLabel("dataTable", "sNext"),
                "sPrevious": doc.getDocLabel("dataTable", "sPrevious")
            },
            "oAria": {
                "sSortAscending": doc.getDocLabel("dataTable", "sSortAscending"),
                "sSortDescending": doc.getDocLabel("dataTable", "sSortDescending")
            }
        },
        "colVis": {"buttonText": doc.getDocLabel("dataTable", "colVis")}
    };
    return res;
}

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
        if (user !== null) {
            readDocFromDatabase(user.language);
        } else {
            readDocFromDatabase("en");
        }
    }
    doc = sessionStorage.getItem("doc");
    doc = JSON.parse(doc);
    return doc;
}

function Doc() {
    this.table = getDoc();
}

/**
 * Function that manage the documentation in text format.
 * @param {docTable} Table of the documentation data.
 * @param {docField} Field of the documentation data.
 * @returns {String} text value of the field name from the documentation
 */
Doc.prototype.getDocLabel = function (docTable, docField) {
    try {
        if (!(this.table.hasOwnProperty(docTable)))
            throw "docTable " + docTable + " not found";
        if (!(this.table[docTable].hasOwnProperty(docField)))
            throw "docField " + docField + " not found";
        return this.table[docTable][docField].docLabel;
    } catch (err) {
        var res;
        res = docField + " -- Missing doc !!!";
        return res;
    }
};

/**
 * Function that manage the documentation in text format.
 * @param {docTable} Table of the documentation data.
 * @param {docField} Field of the documentation data.
 * @returns {String} text value of the Description from the documentation
 */
Doc.prototype.getDocDescription = function (docTable, docField) {
    try {
        if (!(this.table.hasOwnProperty(docTable)))
            throw "docTable " + docTable + " not found";
        if (!(this.table[docTable].hasOwnProperty(docField)))
            throw "docField " + docField + " not found";
        return this.table[docTable][docField].docDesc;
    } catch (err) {
        var res;
        res = docField + " -- Missing doc !!!";
        return res;
    }
};

/**
 * Function that manage the documentation in html format.
 * @param {docTable} Table of the documentation data.
 * @param {docField} Field of the documentation data.
 * @returns {String} html value of the field name from the documentation
 */
Doc.prototype.getDocOnline = function (docTable, docField) {
    var res;
    var user = getUser();

    try {
        if (!(this.table.hasOwnProperty(docTable))) {
            throw "docTable " + docTable + " not found";
        }
        if (!(this.table[docTable].hasOwnProperty(docField))) {
            throw "docField " + docField + " not found";
        }
        if (!(this.table[docTable][docField].havedocDesc)) { // If the entry has no detail documentation, we do not display the ? with access to the detail.
            res = this.table[docTable][docField].docLabel;
        } else {
            var linkToDoc = "";
            if (this.table[docTable][docField].haveDocAnchor) {
                res = this.table[docTable][docField].docLabel +
                        " <a tabindex='1' data-html='true' href='./documentation/D1/documentation_en.html#" + this.table[docTable][docField].docAnchor + "' class=\"docOnline\" onclick=\"window.open=('this.href'); target='_blank';stopPropagation(event)\" \n\
                data-toggle='popover' \n\
                data-placement='auto' \n\
                data-trigger='hover' \n\
                title='" + this.table[docTable][docField].docLabel + "' \n\
                data-content='" + $("<div>" + this.table[docTable][docField].docDesc.split("'").join("&#8217;") + "</div>").prop('outerHTML') + "'>\n\
                <span class=\"glyphicon glyphicon-question-sign\"></span></a>";
                //linkToDoc = "</br><br><a href='./Documentation.jsp#" + this.table[docTable][docField].docAnchor + "'><span class=\"glyphicon glyphicon-search\"> Documentation</span></a>";
            } else {
                res = this.table[docTable][docField].docLabel +
                        " <a tabindex='1' data-html='true'  class=\"docOnline\" onclick=\"stopPropagation(event)\" \n\
                data-toggle='popover' \n\
                data-placement='auto' \n\
                data-trigger='hover' \n\
                title='" + this.table[docTable][docField].docLabel + "' \n\
                data-content='" + $("<div>" + this.table[docTable][docField].docDesc.split("'").join("&#8217;") + "</div>").prop('outerHTML') + "'>\n\
                <span class=\"glyphicon glyphicon-question-sign\"></span></a>";
            }


        }
    } catch (err) {
        console.error("no doc found for table = " + doctable + " and field = " + docfield);
        res = docField + " <a class=\"nodoc\" onclick=\"stopPropagation(event)\"><span class=\"glyphicon glyphicon-exclamation-sign\"></span></a>";
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
