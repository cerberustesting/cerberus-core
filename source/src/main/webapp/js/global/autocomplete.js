/*
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
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
 * Function that allow to autoComplete Input with different data and regex that decide which data to show
 * @param {type} identifier jquery identifier to find the input to affect the autocomplete
 * @param {type} Tags array of Tags order by priority (first regex find will display its list only):
 *             {
 *                  array : array of String to analyse and display,
 *                  regex : regex to detect if we have to show the list or not,
 *                  addBefore : String to add before the value when selected,
 *                  addAfter : String to add after the value when selected
 *             }
 *
 */
function autocompleteWithTags(identifier, Tags) {
    function split(val, separator) {
        return val.split(new RegExp(separator + "(?!.*" + separator + ")"))
    }

    function extractLast(term, separator) {
        if (!separator.includes("%"))
            return term;
        return split(term, separator).pop();
    }

    function extractAllButLast(term, separator) {
        if (!separator.includes("%"))
            return '';
        var last = split(term, separator).pop();
        var index = term.lastIndexOf(last);
        return term.substring(0, index);
    }

    $.ui.autocomplete.filter = function (array, term) {
        var matcher = new RegExp("^" + $.ui.autocomplete.escapeRegex(term), "i");
        return $.grep(array, function (value) {
            return matcher.test(value.label || value.value || value);
        });
    };

    $(identifier)
            // don't navigate away from the field on tab when selecting an item
            .on("keydown", function (event) {
                if (event.keyCode === $.ui.keyCode.TAB &&
                        $(this).autocomplete("instance").menu.active) {
                    event.preventDefault();
                }
                // We hide the message generated by autocomplete because we don't want it
                $("span[role='status']").hide();
            })
            .autocomplete({
                minLength: 0,
                messages: {
                    noResults: '',
                    results: function () {
                    }
                },
                open: function () {
                    //If autocomplete is in modal, needs to be upper the modal
                    if ($(this).closest($(".modal")).length > 0) {
                        $(this).autocomplete('widget').css('z-index', 1050);
                    }
                    return false;
                },
                source: function (request, response) {
                    //Get the part of the string we want (between the last % before our cursor and the cursor)
                    var selectionStart = this.element[0].selectionStart;
                    var stringToAnalyse = this.term.substring(0, selectionStart);
                    var identifier = stringToAnalyse.substring(stringToAnalyse.lastIndexOf("%"));

                    //If there is a pair number of % it means there is no open variable that needs to be autocompleted

                    if ((this.term.match(/%/g) || []).length % 2 > 0 || (this.term.match("((^[a-zA-Z])|(^$))") && !this.term.includes("%"))) {
                        //Start Iterating on Tags
                        var tag = 0;
                        var found = false;
                        while (tag < Tags.length && !found) {
                            //If We find the separator, then we filter with the already written part
                            if ((identifier.match(new RegExp(Tags[tag].regex)) || []).length > 0) {
                                var arrayLabels = [];
                                if (Tags[tag].regex === "%object\\.") {
                                    Tags[tag].array.forEach(function (data) {
                                        arrayLabels.push(data.object);
                                    });
                                } else {
                                    arrayLabels = Tags[tag].array;
                                }
                                this.currentIndexTag = tag;
                                var arrayToDisplay = $.ui.autocomplete.filter(
                                        arrayLabels, extractLast(identifier, Tags[tag].regex));
                                if (Tags[tag].isCreatable && extractLast(identifier, Tags[tag].regex) != "") {
                                    arrayToDisplay.push(extractLast(identifier, Tags[tag].regex));
                                }
                                response(arrayToDisplay);
                                found = true;
                            }
                            tag++;
                        }
                    }
                },
                focus: function () {
                    // Hide that 'ui-helper-hidden-accessible' span. It appears for an unknown reason
                    $(".ui-helper-hidden-accessible").hide();
                    $('a[data-toggle="tooltip"]').each(function (idx, data) {
                        var direction = "top";
                        if (idx < 4)
                            direction = "bottom";
                        $(data).tooltip({
                            animated: 'fade',
                            placement: direction,
                            html: true
                        });
                        var parent = $(data).parent().parent();
                        if (parent.hasClass("ui-autocomplete")) {
                            parent.css("min-height", "120px"); // add height to do place to display tooltip. else overflow:auto hide tooltip
                        }
                    });
                    // prevent value inserted on focus
                    return false;
                },
                select: function (event, ui) {
                    //Get the part of the string we want (between the last % before our cursor and the cursor)
                    var stringToAnalyse = this.value.substring(0, this.selectionStart);
                    var identifier = stringToAnalyse.substring(stringToAnalyse.lastIndexOf("%"));
                    //Start iterating on Tags
                    var found = false;
                    var tag = 0;
                    while (tag < Tags.length && !found) {
                        //If we find our separator, we compute the output
                        if ((identifier.match(new RegExp(Tags[tag].regex)) || []).length > 0) {
                            // remove the current input
                            var beforeRegex = extractAllButLast(this.value.substring(0, this.selectionStart), Tags[tag].regex);
                            var afterCursor = this.value.substring(this.selectionStart, this.value.length);
                            // add the selected item and eventually the content to add
                            var value;
                            if (ui.item.value === "%") {
                                value = Tags[tag].addBefore + ui.item.value;
                            } else {
                                value = Tags[tag].addBefore + ui.item.value + Tags[tag].addAfter;
                            }
                            //If it is the end of the variable, we automaticly add a % at the end of the line

                            this.value = beforeRegex + value + afterCursor;
                            this.setSelectionRange((beforeRegex + value).length, (beforeRegex + value).length);

                            found = true;
                        }
                        tag++;
                    }
                    // We trigger input to potentially display an image if there is one
                    $(this).trigger("input").trigger("change");
                    return false;
                },
                close: function (event, ui) {
                    $(this).autocomplete(close);
                    return false;
                }
            }).click(function () {
        $(this).autocomplete("search");
    });


}

/**
 * Function that allow to autoComplete Input with empty data
 * @param {type} identifier jquery identifier to find the input to affect the autocomplete
 *
 */
function autocompleteSpecificFields(identifier) {
    $(identifier).autocomplete({
        source: [],
        minLength: 0,
        messages: {
            noResults: '',
            results: function () {
            }
        },
        select: function (event, ui) {
            this.value = ui.item.value;
            $(this).trigger("input").trigger("change");
            return false;
        },
        close: function (event, ui) {
            val = $(this).val();
            return false;
        }
    }).on("focus", function () {
        $(this).autocomplete("search", "");
//    }).data("ui-autocomplete")._renderItem = function (ul, item) {
//        console.info()
//        return $("<li>")
//                .data("ui-autocomplete-item", item)
//                .append("<a>" + item.label + "</a>")
//                .appendTo(ul);
//    };
//    $(identifier).on("focus", function () {
//        $(this).autocomplete("search", "");
    });
}

function modifyAutocompleteSource(identifier, url, data) {
    $(identifier).autocomplete('option', 'source', function (request, response) {
        if (!data) {
            $.ajax({
                url: url,
                dataType: "json",
                success: function (data) {
                    var MyArray = $.map(data.contentTable, function (item) {
                        return {
                            label: item.service,
                            value: item.service
                        };
                    });
                    response($.ui.autocomplete.filter(MyArray, request.term));
                }
            })
        } else {
            var MyArray = $.map(data, function (item) {
                return {
                    label: item.name,
                    value: item.name
                };
            });
            response($.ui.autocomplete.filter(MyArray, request.term));

        }
    });
}

function loadApplicationObject(dataInit) {
    return new Promise(function (resolve, reject) {
        var array = [];
        $.ajax({
            url: "ReadApplicationObject?application=" + dataInit,
            dataType: "json",
            success: function (data) {
                for (var i = 0; i < data.contentTable.length; i++) {
                    array.push(data.contentTable[i]);
                }
                resolve(array);
            }
        });
    });
}

function loadDatalib() {
    return new Promise(function (resolve, reject) {
        var array = [];
        $.ajax({
            url: "ReadTestDataLib?columnName=tdl.Name",
            dataType: "json",
            success: function (data) {
                for (var i = 0; i < data.distinctValues.length; i++) {
                    array.push(data.distinctValues[i]);
                }
                resolve(array);
            }
        });
    });
}

function initTags(configs, context) {

    var inheritedProperties = [], testCaseProperties = [], objectsPromise = [], datalibPromise = [];

//    console.info(configs);

    if (configs.property && context instanceof Object) {
        inheritedProperties = context.contentTable[0].properties.inheritedProperties.map(prop => prop.property);
        testCaseProperties = context.contentTable[0].properties.testCaseProperties.map(prop => prop.property);
        objectsPromise = loadApplicationObject(context.contentTable[0].application);
        datalibPromise = loadDatalib();
    }

    if (configs.object && !configs.property && context instanceof String) {
        objectsPromise = loadApplicationObject(context);
        datalibPromise = loadDatalib();
    }

    return Promise.all([objectsPromise, datalibPromise]).then(function (data) {
        var availableObjects = data[0];
        var datalibObjects = data[1];
//        console.info(availableObjects);
//        console.info(datalibObjects);
        var availableProperties = testCaseProperties.concat(inheritedProperties.filter(function (item) {
            return testCaseProperties.indexOf(item) < 0;
        }));
        var availableTags = [
            "property",
            "system",
            "object",
            "datalib"
        ];
        var availableObjectProperties = [
            "value",
//            "picturepath",
            "pictureurl",
            "base64"
        ];
        var availableDatalibProperties = [
            "value",
//            "picturepath",
//            "pictureurl",
            "base64"
        ];
        var availableSystemValues = [
            "SYSTEM",
            "APPLI",
            "BROWSER", "ROBOT", "ROBOTDECLI", "SCREENSIZE",
            "APP_DOMAIN", "APP_HOST", "APP_CONTEXTROOT", "EXEURL", "APP_VAR1", "APP_VAR2", "APP_VAR3", "APP_VAR4", "APP_SECRET1", "APP_SECRET2",
            "ENV", "ENVGP",
            "COUNTRY", "COUNTRYGP1", "COUNTRYGP2", "COUNTRYGP3", "COUNTRYGP4", "COUNTRYGP5", "COUNTRYGP6", "COUNTRYGP7", "COUNTRYGP8", "COUNTRYGP9",
            "TEST",
            "TESTCASE", "TESTCASEDESCRIPTION",
            "SSIP", "SSPORT",
            "TAG",
            "EXECUTIONID",
            "EXESTART", "EXEELAPSEDMS",
            "EXESTORAGEURL",
            "STEP.n.n.RETURNCODE", "CURRENTSTEP_INDEX", "CURRENTSTEP_STARTISO", "CURRENTSTEP_ELAPSEDMS", "CURRENTSTEP_SORT",
            "LASTSERVICE_HTTPCODE", "LASTSERVICE_CALL", "LASTSERVICE_RESPONSE",
            "TODAY-yyyy", "TODAY-MM", "TODAY-dd", "TODAY-D", "TODAY-HH", "TODAY-mm", "TODAY-ss",
            "YESTERDAY-yyyy", "YESTERDAY-MM", "YESTERDAY-dd", "YESTERDAY-D", "YESTERDAY-HH", "YESTERDAY-mm", "YESTERDAY-ss",
            "TOMORROW-yyyy", "TOMORROW-MM", "TOMORROW-dd", "TOMORROW-D"
        ];
        var availableIdentifiers = [
            "id",
            "xpath",
            "name",
            "class",
            "css",
            "link",
            "data-cerberus",
            "querySelector",
            "erratum",
            "picture",
            "text",
            "coord",
            "offset"
        ];
        var availableIdentifiersSwitch = [
            "title",
            "regexTitle",
            "url",
            "regexUrl"
        ];
        var availableIdentifiersSelect = [
            "label",
            "regexLabel",
            "value",
            "regexValue",
            "index",
            "regexIndex"
        ];
        var availableIdentifiersBoolean = [
            "true",
            "false"
        ];
        var availableIdentifiersFileSortFlag = [
            "LASTMODIFIED",
            "IGNORECASEDESC",
            "IGNORECASEASC",
            "DESC",
            "ASC"
        ];
        var availableIdentifiersFileUploadFlag = [
            "EMPTYFOLDER"
        ];
        var availableIdentifiersHeaderValues = [
            "application/java-archive",
            "application/EDI-X12",
            "application/EDIFACT",
            "application/javascript",
            "application/octet-stream",
            "application/ogg",
            "application/pdf",
            "application/xhtml+xml",
            "application/x-shockwave-flash",
            "application/json",
            "application/ld+json",
            "application/xml",
            "application/zip",
            "application/x-www-form-urlencoded",
            "application/vnd.android.package-archive",
            "application/vnd.oasis.opendocument.text",
            "application/vnd.oasis.opendocument.spreadsheet",
            "application/vnd.oasis.opendocument.presentation",
            "application/vnd.oasis.opendocument.graphics",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.mozilla.xul+xml",
            "audio/mpeg",
            "audio/x-ms-wma",
            "audio/vnd.rn-realaudio",
            "audio/x-wav",
            "image/gif",
            "image/jpeg",
            "image/png",
            "image/tiff",
            "image/vnd.microsoft.icon",
            "image/x-icon",
            "image/vnd.djvu",
            "image/svg+xml",
            "multipart/mixed",
            "multipart/alternative",
            "multipart/related",
            "multipart/form-data",
            "text/css",
            "text/csv",
            "text/html",
            "text/html; charset=UTF-8",
            "text/javascript",
            "text/plain",
            "text/xml",
            "video/mpeg",
            "video/mp4",
            "video/quicktime",
            "video/x-ms-wmv",
            "video/x-msvideo",
            "video/x-flv",
            "video/webm",
            "en",
            "fr"
        ];
        var availableIdentifiersHeader = [
// Standard Heasders
            "A-IM",
            "Accept",
            "Accept-Charset",
            "Accept-Datetime",
            "Accept-Encoding",
            "Accept-Language",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers",
            "Authorization",
            "Cache-Control",
            "Connection",
            "Content-Encoding",
            "Content-Length",
            "Content-MD5",
            "Content-Type",
            "Cookie",
            "Date",
            "Expect",
            "Forwarded",
            "From",
            "Host",
            "HTTP2-Settings",
            "If-Match",
            "If-Modified-Since",
            "If-None-Match",
            "If-Range",
            "If-Unmodified-Since",
            "Max-Forwards",
            "Origin",
            "Pragma",
            "Prefer",
            "Proxy-Authorization",
            "Range",
            "Referer",
            "TE",
            "Trailer",
            "Transfer-Encoding",
            "User-Agent",
            "Upgrade",
            "Via",
            "Warning",
// Non Standard Heasders
            "X-Api-Key",
            "DNT",
            "X-Forwarded-For",
            "X-Forwarded-Host",
            "X-Forwarded-Proto",
            "X-Http-Method-Override",
            "Proxy-Connection",
            "X-Requested-With"
        ];
        let tags = [
            {
                name: 'objectProperty',
                array: availableObjectProperties,
                regex: "%object\\.[^\\.]*\\.",
                addBefore: "",
                addAfter: "%",
                isCreatable: false
            },
            {
                name: 'object',
                array: availableObjects,
                regex: "%object\\.",
                addBefore: "",
                addAfter: ".",
                isCreatable: true
            },
            {
                name: 'datalibProperty',
                array: availableDatalibProperties,
                regex: "%datalib\\.[^\\.]*\\.",
                addBefore: "",
                addAfter: "%",
                isCreatable: false
            },
            {
                name: 'datalib',
                array: datalibObjects,
                regex: "%datalib\\.",
                addBefore: "",
                addAfter: ".",
                isCreatable: true
            },
            {
                name: 'property',
                array: availableProperties,
                regex: "%property\\.",
                addBefore: "",
                addAfter: "%",
                isCreatable: true
            },
            {
                name: 'system',
                array: availableSystemValues,
                regex: "%system\\.",
                addBefore: "",
                addAfter: "%",
                isCreatable: false
            },
            {
                name: 'tag',
                array: availableTags,
                regex: "%",
                addBefore: "",
                addAfter: ".",
                isCreatable: false
            }
        ];
        var finalArray = [];
        let finalAddAfter = "=";
        if (configs.identifier === "element") {
            finalArray.push(...availableIdentifiers);
        } else if (configs.identifier === "switch") {
            finalArray.push(...availableIdentifiersSwitch);
        } else if (configs.identifier === "select") {
            finalArray.push(...availableIdentifiersSelect);
        } else if (configs.identifier === "boolean") {
            finalArray.push(...availableIdentifiersBoolean);
            finalAddAfter = "";
        } else if (configs.identifier === "fileuploadflag") {
            finalArray.push(...availableIdentifiersFileUploadFlag);
            finalAddAfter = "";
        } else if (configs.identifier === "filesortflag") {
            finalArray.push(...availableIdentifiersFileSortFlag);
            finalAddAfter = "";
        } else if (configs.identifier === "header") {
            finalArray.push(...availableIdentifiersHeader);
            finalAddAfter = "";
        } else if (configs.identifier === "headervalue") {
            finalArray.push(...availableIdentifiersHeaderValues);
            finalAddAfter = "";
        }

        if (configs.identifier !== "none") {
            tags.push({
                name: 'identifier',
                array: finalArray,
                regex: "((^[a-zA-Z])|(^$))",
                addBefore: "",
                addAfter: finalAddAfter,
                isCreatable: false
            });
        }

//        console.info("return tags");
//        console.info(tags);
//        console.info(configs);
//        console.info(context);

        return tags;
    });
}

/**
 * Function that allow to autoComplete many inputs using tags
 * @param el : Array of HTMLElement
 * @param configs : Object
 * example : var configs = {
 *   	'system': true,
 *   	'object': false,
 *   	'propertie': false,
 *   	'identifier': true
 *   }
 * @param context : Object || String
 * context is an object (data returns from servlet ReadTestCase) or a String (application name)
 * @returns
 */
function initAutocompleteWithTags(el, configs, context) {
    initTags(configs, context).then(function (tags) {
        $(el).each(data => {
            autocompleteWithTags(el[data], tags);
        });
    });
}

function initAutocompleteWithTagsNoElement(el, configs, context) {
    initTags(configs, context).then(function (tags) {
        $(el).each(data => {
            // remove array of elements (xpath=, data-cerberus=, etc...) from tags in order not to propose them.
//            tags.splice(5, 1);
            autocompleteWithTags(el[data], tags);
        });
    });
}

function initAutocompleteforSpecificFields(el) {
    $(el).each(data => {
        autocompleteSpecificFields(el[data]);
    });

}
