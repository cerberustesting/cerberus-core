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


function setPlaceholderProperty(property, propertyObject) {

    var user = getUser();
    //var propertySelect = property.find('select[name="propertyType"]');
    var placeHolders = newPropertyPlaceholder[propertyObject.type];

    if (typeof placeHolders.database !== 'undefined') {
        property.find("div[class*='database']")
                .removeClass("col-lg-2 col-lg-3 col-lg-4 col-lg-5 col-lg-6 col-lg-7 col-lg-8 col-lg-9 crb-autocomplete-element crb-autocomplete-property crb-autocomplete-service crb-autocomplete-variable crb-autocomplete-fileuploadflag crb-autocomplete-filesortflag crb-autocomplete-boolean crb-autocomplete-select crb-autocomplete-switch crb-contextual-button")
                .addClass(placeHolders.database.class);
        property.find("div[class*='database']").show();
        property.find("div[class*='database']").find('input').attr("placeholder", placeHolders.database.label[user.language]);
        property.find("div[class*='database']").find('#propertyDatabaseAddon').attr("data-toggle", "tooltip").attr("data-original-title", placeHolders.database.label[user.language]);
        if (typeof placeHolders.database.picto !== 'undefined') {
            property.find("div[class*='database']").find('img').attr("src", placeHolders.database.picto);
        }

        property.find("div[class*='database']").insertAfter(property.find("div[class*='" + placeHolders.database.insertAfter + "']"));
    } else {
        property.find("div[class*='database']").hide();
    }

    if (typeof placeHolders.value1 !== 'undefined') {
        var className = "value1";
        if (typeof placeHolders.value1.editorMode === 'undefined') {
            className = "valueInput1";
            property.find("div[class*='value1']").hide();
        } else {
            className = "value1";
            property.find("div[class*='valueInput1']").hide();
        }
        property.find("div[class*='" + className + "']")
                .removeClass("col-lg-2 col-lg-3 col-lg-4 col-lg-5 col-lg-6 col-lg-7 col-lg-8 col-lg-9 crb-autocomplete-element crb-autocomplete-property crb-autocomplete-service crb-autocomplete-variable crb-autocomplete-fileuploadflag crb-autocomplete-filesortflag crb-autocomplete-boolean crb-autocomplete-select crb-autocomplete-switch crb-contextual-button")
                .addClass(placeHolders.value1.class);
        property.find("div[class*='" + className + "']").show();
        property.find("div[class*='" + className + "']").find('input').attr("placeholder", placeHolders.value1.label[user.language]);
        property.find("div[class*='" + className + "']").find('#propertyValue1Addon').attr("data-toggle", "tooltip").attr("data-original-title", placeHolders.value1.label[user.language]);
        if (typeof placeHolders.value1.picto !== 'undefined') {
            property.find("div[class*='" + className + "']").find('img').attr("src", placeHolders.value1.picto);
        }

        property.find("div[class*='" + className + "']").insertAfter(property.find("div[class*='" + placeHolders.value1.insertAfter + "']"));

        if (propertyObject.type === 'getFromDataLib') {

            var editor = ace.edit($(property.find("pre[name='propertyValue']"))[0]);
            $(property.find("pre[name='propertyValue']"))[0].style.fontSize = '16px';
            var escaped = propertyObject.value1;

            property.find("div[class*='editDataLib']").show();
            setDatalibButtonOnClick(property.find("div[class*='editDataLib']"), escaped, editor.container.id);
            setDatalibListener(editor, property, propertyObject);

        } else {
            property.find("div[class*='editDataLib']").hide();
        }
    } else {
        property.find("div[class*='value1']").hide();
        property.find("div[class*='valueInput1']").hide();
    }

    if (typeof placeHolders.value2 !== 'undefined') {
        var display = displayField(placeHolders.value2.displayConditions, propertyObject);
        if (display) {
            property.find("div[class*='value2']")
                    .removeClass("col-lg-2 col-lg-3 col-lg-4 col-lg-5 col-lg-6 col-lg-7 col-lg-8 col-lg-9 crb-autocomplete-element crb-autocomplete-property crb-autocomplete-service crb-autocomplete-variable crb-autocomplete-fileuploadflag crb-autocomplete-filesortflag crb-autocomplete-boolean crb-autocomplete-select crb-autocomplete-switch crb-contextual-button")
                    .addClass(placeHolders.value2.class);
            property.find("div[class*='value2']").show();
            property.find("div[class*='value2']").find('input').attr("placeholder", placeHolders.value2.label[user.language]);
            property.find("div[class*='value2']").find('#propertyValue2Addon').attr("data-toggle", "tooltip").attr("data-original-title", placeHolders.value2.label[user.language]);
            if (typeof placeHolders.value2.picto !== 'undefined') {
                property.find("div[class*='value2']").find('img').attr("src", placeHolders.value2.picto);
            }
            property.find("div[class*='value2']").insertAfter(property.find("div[class*='" + placeHolders.value2.insertAfter + "']"));
        } else {
            property.find("div[class*='value2']").hide();
        }
    } else {
        property.find("div[class*='value2']").hide();
    }

    if (typeof placeHolders.value3 !== 'undefined') {
        property.find("div[class*='value3']")
                .removeClass("col-lg-2 col-lg-3 col-lg-4 col-lg-5 col-lg-6 col-lg-7 col-lg-8 col-lg-9 crb-autocomplete-element crb-autocomplete-property crb-autocomplete-service crb-autocomplete-variable crb-autocomplete-fileuploadflag crb-autocomplete-filesortflag crb-autocomplete-boolean crb-autocomplete-select crb-autocomplete-switch crb-contextual-button")
                .addClass(placeHolders.value3.class);
        property.find("div[class*='value3']").show();
        property.find("div[class*='value3']").find('#propertyValue3Addon').attr("data-toggle", "tooltip").attr("data-original-title", placeHolders.value3.label[user.language]);
        if (typeof placeHolders.value3.picto !== 'undefined') {
            property.find("div[class*='value3']").find('img').attr("src", placeHolders.value3.picto);
        }
        property.find("div[class*='value3']").insertAfter(property.find("div[class*='" + placeHolders.value3.insertAfter + "']"));
        feedOption(propertyObject.type, "value3", property.find("div[class*='value3']").find('select'), propertyObject.value3);
    } else {
        property.find("div[class*='value3']").hide();
    }


    if (typeof placeHolders.length !== 'undefined') {
        var display = displayField(placeHolders.length.displayConditions, propertyObject);
        if (display) {
            property.find("div[class*='length']")
                    .removeClass("col-lg-2 col-lg-3 col-lg-4 col-lg-5 col-lg-6 col-lg-7 col-lg-8 col-lg-9 crb-autocomplete-element crb-autocomplete-property crb-autocomplete-service crb-autocomplete-variable crb-autocomplete-fileuploadflag crb-autocomplete-filesortflag crb-autocomplete-boolean crb-autocomplete-select crb-autocomplete-switch crb-contextual-button")
                    .addClass(placeHolders.length.class);
            property.find("div[class*='length']").show();
            property.find("div[class*='length']").find('input').attr("placeholder", placeHolders.length.label[user.language]);
            property.find("div[class*='length']").find('#propertyLengthAddon').attr("data-toggle", "tooltip").attr("data-original-title", placeHolders.length.label[user.language]);
            if (typeof placeHolders.length.picto !== 'undefined') {
                property.find("div[class*='length']").find('img').attr("src", placeHolders.length.picto);
            }
            property.find("div[class*='length']").insertAfter(property.find("div[class*='" + placeHolders.length.insertAfter + "']"));
        } else {
            property.find("div[class*='length']").hide();
        }
    } else {
        property.find("div[class*='length']").hide();
    }

    if (typeof placeHolders.rowLimit !== 'undefined') {
        property.find("div[class*='rowLimit']")
                .removeClass("col-lg-2 col-lg-3 col-lg-4 col-lg-5 col-lg-6 col-lg-7 col-lg-8 col-lg-9 crb-autocomplete-element crb-autocomplete-property crb-autocomplete-service crb-autocomplete-variable crb-autocomplete-fileuploadflag crb-autocomplete-filesortflag crb-autocomplete-boolean crb-autocomplete-select crb-autocomplete-switch crb-contextual-button")
                .addClass(placeHolders.rowLimit.class);
        property.find("div[class*='rowLimit']").show();
        property.find("div[class*='rowLimit']").find('input').attr("placeholder", placeHolders.rowLimit.label[user.language]);
        property.find("div[class*='rowLimit']").find('#propertyRowLimitAddon').attr("data-toggle", "tooltip").attr("data-original-title", placeHolders.rowLimit.label[user.language]);
        if (typeof placeHolders.rowLimit.picto !== 'undefined') {
            property.find("div[class*='rowLimit']").find('img').attr("src", placeHolders.rowLimit.picto);
        }
        property.find("div[class*='rowLimit']").insertAfter(property.find("div[class*='" + placeHolders.rowLimit.insertAfter + "']"));
    } else {
        property.find("div[class*='rowLimit']").hide();
    }

    if (typeof placeHolders.nature !== 'undefined') {
        var display = displayField(placeHolders.nature.displayConditions, propertyObject);
        if (display) {
            property.find("div[class*='nature']")
                    .removeClass("col-lg-2 col-lg-3 col-lg-4 col-lg-5 col-lg-6 col-lg-7 col-lg-8 col-lg-9 crb-autocomplete-element crb-autocomplete-property crb-autocomplete-service crb-autocomplete-variable crb-autocomplete-fileuploadflag crb-autocomplete-filesortflag crb-autocomplete-boolean crb-autocomplete-select crb-autocomplete-switch crb-contextual-button")
                    .addClass(placeHolders.nature.class);
            property.find("div[class*='nature']").show();
            property.find("div[class*='nature']").find('input').attr("placeholder", placeHolders.nature.label[user.language]);
            property.find("div[class*='nature']").find('#propertyNatureAddon').attr("data-toggle", "tooltip").attr("data-original-title", placeHolders.nature.label[user.language]);
            if (typeof placeHolders.nature.picto !== 'undefined') {
                property.find("div[class*='nature']").find('img').attr("src", placeHolders.nature.picto);
            }
            property.find("div[class*='nature']").insertAfter(property.find("div[class*='" + placeHolders.nature.insertAfter + "']"));
            feedOption(propertyObject.type, "nature", property.find("div[class*='nature']").find('select'), propertyObject.nature);
        } else {
            property.find("div[class*='nature']").hide();
        }
    } else {
        property.find("div[class*='nature']").hide();
    }

    if (typeof placeHolders.cacheExpire !== 'undefined') {
        property.find("div[class*='cacheExpire']")
                .removeClass("col-lg-2 col-lg-3 col-lg-4 col-lg-5 col-lg-6 col-lg-7 col-lg-8 col-lg-9 crb-autocomplete-element crb-autocomplete-property crb-autocomplete-service crb-autocomplete-variable crb-autocomplete-fileuploadflag crb-autocomplete-filesortflag crb-autocomplete-boolean crb-autocomplete-select crb-autocomplete-switch crb-contextual-button")
                .addClass(placeHolders.cacheExpire.class);
        property.find("div[class*='cacheExpire']").show();
        property.find("div[class*='cacheExpire']").find('input').attr("placeholder", placeHolders.cacheExpire.label[user.language]);
        property.find("div[class*='cacheExpire']").find('#propertyCacheExpireAddon').attr("data-toggle", "tooltip").attr("data-original-title", placeHolders.cacheExpire.label[user.language]);
        if (typeof placeHolders.cacheExpire.picto !== 'undefined') {
            property.find("div[class*='cacheExpire']").find('img').attr("src", placeHolders.cacheExpire.picto);
        }
        property.find("div[class*='cacheExpire']").insertAfter(property.find("div[class*='" + placeHolders.cacheExpire.insertAfter + "']"));
    } else {
        property.find("div[class*='cacheExpire']").hide();
    }

    if (typeof placeHolders.retryNb !== 'undefined') {
        property.find("div[class*='retryNb']")
                .removeClass("col-lg-2 col-lg-3 col-lg-4 col-lg-5 col-lg-6 col-lg-7 col-lg-8 col-lg-9 crb-autocomplete-element crb-autocomplete-property crb-autocomplete-service crb-autocomplete-variable crb-autocomplete-fileuploadflag crb-autocomplete-filesortflag crb-autocomplete-boolean crb-autocomplete-select crb-autocomplete-switch crb-contextual-button")
                .addClass(placeHolders.retryNb.class);
        property.find("div[class*='retryNb']").show();
        property.find("div[class*='retryNb']").find('input').attr("placeholder", placeHolders.retryNb.label[user.language]);
        property.find("div[class*='retryNb']").find('#propertyRetryNbAddon').attr("data-toggle", "tooltip").attr("data-original-title", placeHolders.retryNb.label[user.language]);
        if (typeof placeHolders.retryNb.picto !== 'undefined') {
            property.find("div[class*='retryNb']").find('img').attr("src", placeHolders.retryNb.picto);
        }
        property.find("div[class*='retryNb']").insertAfter(property.find("div[class*='" + placeHolders.retryNb.insertAfter + "']"));
    } else {
        property.find("div[class*='retryNb']").hide();
    }

    if (typeof placeHolders.retryPeriod !== 'undefined') {
        property.find("div[class*='retryPeriod']")
                .removeClass("col-lg-2 col-lg-3 col-lg-4 col-lg-5 col-lg-6 col-lg-7 col-lg-8 col-lg-9 crb-autocomplete-element crb-autocomplete-property crb-autocomplete-service crb-autocomplete-variable crb-autocomplete-fileuploadflag crb-autocomplete-filesortflag crb-autocomplete-boolean crb-autocomplete-select crb-autocomplete-switch crb-contextual-button")
                .addClass(placeHolders.retryPeriod.class);
        property.find("div[class*='retryPeriod']").show();
        property.find("div[class*='retryPeriod']").find('input').attr("placeholder", placeHolders.retryPeriod.label[user.language]);
        property.find("div[class*='retryPeriod']").find('#propertyRetryPeriodAddon').attr("data-toggle", "tooltip").attr("data-original-title", placeHolders.retryPeriod.label[user.language]);
        if (typeof placeHolders.retryPeriod.picto !== 'undefined') {
            property.find("div[class*='retryPeriod']").find('img').attr("src", placeHolders.retryPeriod.picto);
        }
        property.find("div[class*='retryPeriod']").insertAfter(property.find("div[class*='" + placeHolders.retryPeriod.insertAfter + "']"));
    } else {
        property.find("div[class*='retryPeriod']").hide();
    }

    if (typeof placeHolders.rank !== 'undefined') {
        var display = displayField(placeHolders.rank.displayConditions, propertyObject);
        if (display) {
            property.find("div[class*='rank']")
                    .removeClass("col-lg-2 col-lg-3 col-lg-4 col-lg-5 col-lg-6 col-lg-7 col-lg-8 col-lg-9 crb-autocomplete-element crb-autocomplete-property crb-autocomplete-service crb-autocomplete-variable crb-autocomplete-fileuploadflag crb-autocomplete-filesortflag crb-autocomplete-boolean crb-autocomplete-select crb-autocomplete-switch crb-contextual-button")
                    .addClass(placeHolders.rank.class);
            property.find("div[class*='rank']").show();
            property.find("div[class*='rank']").find('input').attr("placeholder", placeHolders.rank.label[user.language]);
            property.find("div[class*='rank']").find('#propertyRankAddon').attr("data-toggle", "tooltip").attr("data-original-title", placeHolders.rank.label[user.language]);
            if (typeof placeHolders.rank.picto !== 'undefined') {
                property.find("div[class*='rank']").find('img').attr("src", placeHolders.rank.picto);
            }
            property.find("div[class*='rank']").insertAfter(property.find("div[class*='" + placeHolders.rank.insertAfter + "']"));
        } else {
            property.find("div[class*='rank']").hide();
        }
    } else {
        property.find("div[class*='rank']").hide();
    }

    var editor = ace.edit($(property.find("pre[name='propertyValue']"))[0]);
    $(property.find("pre[name='propertyValue']"))[0].style.fontSize = '16px';

    editor.removeAllListeners('change');

    configureAceEditor(editor, newPropertyPlaceholder[propertyObject.type].value1.editorMode, propertyObject);

    $('[data-toggle="tooltip"]').tooltip();
}

function displayField(conditions, propertyObject) {
    var display = true;
    if (typeof conditions !== 'undefined') {
        conditions.forEach(function (condition) {
            if (condition.values.indexOf(propertyObject[condition.key]) === -1) {
                display = false;
            }
        });
    }
    return display;
}

function loadGuiProperties() {

    let propArr = new Object();

    $("div.property.list-group-item").each(function () {
        let editor;
        //Not the same DOM Structure between classic properties and inherited properties
        if ($(this).find("pre").attr("id")?.includes("inheritProperty")) {
            editor = ace.edit($(this).find("pre").attr("id"));
        } else {
            editor = ace.edit($(this).find("pre[name='propertyValue']")[0]);
        }

        let info = new Object();
        info["name"] = $(this).find("#propName").val();
        info["type"] = $(this).find("select").val();
        info["value"] = editor.getValue();
        if (!($(this).find("#propName").val() in propArr)) {
            propArr[$(this).find("#propName").val()] = info;
        }
    });
    return propArr;
}

function sortProperties(identifier) {
    var container = $(identifier);
    var list = container.children(".property");
    list.sort(function (a, b) {

        var aProp = $(a).find("#masterProp").data("property").property.toLowerCase(),
                bProp = $(b).find("#masterProp").data("property").property.toLowerCase();

        if (aProp > bProp) {
            return 1;
        }
        if (aProp < bProp) {
            return -1;
        }
        return 0;
    });
    container.append(list);
}

function loadPropertiesAndDraw(test, testcase, testcaseObject, propertyToFocus, canUpdate) {

    let array = [];
    let propertyList = [];

    let propertiesFromTestcase = testcaseObject.properties.testCaseProperties.sort((a, b) => {
        return compareStrings(a.property, b.property);
    });

    for (let i = 0; i < propertiesFromTestcase.length; i++) {
        let property = propertiesFromTestcase[i];

        array.push(propertiesFromTestcase[i].property);

        property.toDelete = false;
        let prop = drawProperty(property, testcaseObject, canUpdate, i);
        setPlaceholderProperty($(prop[0]), prop[1]);

        propertyList.push(property.property);

    }
    localStorage.setItem("properties", JSON.stringify(propertyList));

    let scope = undefined;
    if (propertyToFocus !== undefined && propertyToFocus !== null) {
        $("#propTable #propName").each(function (i) {
            if ($(this).val() === propertyToFocus) {
                scope = this;
                $("#propertiesModal").on("shown.bs.modal", function (e) {
                    $(scope).focus();
                    $(scope).click();
                });
            }
        });
    }

    let propertyListUnique = Array.from(new Set(propertyList));

    for (let index = 0; index < propertyListUnique.length; index++) {
        drawPropertyList(propertyListUnique[index], index);
    }

    array.sort(function (a, b) {
        return compareStrings(a, b);
    });
}

function drawProperty(property, testcaseObject, canUpdate, index) {
    var doc = new Doc();
    let uniqfieldid = 'p' + uniqid++;

    var deleteBtn = $("<button class='btn add-btn deleteItem-btn'></button>").append($("<span></span>").addClass("glyphicon glyphicon-trash"));
    deleteBtn.attr("disabled", !canUpdate);

    var table = $("#propTable");
    var content = $("<div class='row property list-group-item marginBottom10'></div>");
    var props = $("<div class='col-sm-11' name='propertyLine' id='propertyLine" + property.property + "'></div>");
    var right = $("<div class='col-sm-1 propertyButtons'></div>");
    var row1 = $("<div class='row' id='masterProp' name='masterProp' style='margin-top:10px;'></div>");
    var row2 = $("<div class='row' name='masterProp'></div>");
    var row3 = $("<div class='row' name='masterProp'></div>");

    //PROP NAME
    var propertyInput = $("<input onkeypress='return tec_keyispressed(event);' id='propName' style='width: 100%; font-size: 16px; font-weight: 600;' name='propName' placeholder='" + doc.getDocLabel("page_testcasescript", "feed_propertyname") + "'>").addClass("form-control").val(property.property);
    propertyInput.prop("readonly", !canUpdate);
    var propertyName = $("<div class='col-sm-4 form-group'></div>").append(propertyInput);

    //DESC
    var descriptionInput = $("<textarea rows='1' id='propDescription' placeholder='" + doc.getDocLabel("page_testcasescript", "feed_propertydescription") + "'>").addClass("form-control").val(property.description);
    descriptionInput.prop("readonly", !canUpdate);
    var description = $("<div class='col-sm-8 form-group'></div>").append(descriptionInput);

    // COUNTRY
    var country = $("<div class='col-sm-10'></div>").append(getTestCaseCountry(testcaseObject.countries, property.countries, !canUpdate));

    //TYPE
    var selectType = getSelectInvariant("PROPERTYTYPE", false, false);
    selectType.attr("name", "propertyType");
    selectType.addClass("propertyType");
    selectType.attr("disabled", !canUpdate);
    var type = $("<div class='pType col-sm-3 form-group'></div>").append(selectType.val(property.type));
    type.prop("readonly", !canUpdate);

    //DB
    var propertyDatabaseField = getSelectInvariant("PROPERTYDATABASE", false, false);
    propertyDatabaseField.val(property.database);
    propertyDatabaseField.prop("readonly", !canUpdate);
    var propertyDatabaseContainer = $("<div class='input-group'></div>");
    var propertyDatabaseAddon = $("<span></span>").attr("id", "propertyDatabaseAddon").addClass("input-group-addon").attr("style", "font-weight: 700;");
    propertyDatabaseAddon.append("<img width='15px' height='15px' src='images/action-website.png'>");
    propertyDatabaseField.attr("aria-describedby", "propertyDatabaseAddon");
    propertyDatabaseContainer.append(propertyDatabaseAddon).append(propertyDatabaseField);
    propertyDatabaseContainer = $("<div class='database'></div>").addClass("col-lg-5 form-group marginBottom15").append(propertyDatabaseContainer);

    //VALUE1
    var propertyValue1Field = $("<pre>").attr("data-toggle", "tooltip").attr("name", "propertyValue").attr("data-animation", "false").attr("data-html", "true").attr("data-container", "body").attr("data-placement", "top").attr("data-trigger", "manual").attr("type", "text").addClass("form-control");
    propertyValue1Field.val(property.value1);
    propertyValue1Field.prop("readonly", !canUpdate);
    var propertyValue1Container = $("<div class='input-group'></div>");
    var propertyValue1Addon = $("<span></span>").attr("id", "propertyValue1Addon").addClass("input-group-addon togglefullscreen").attr('for', uniqfieldid + 'v1').attr("style", "font-weight: 700;");
    propertyValue1Addon.append("<img width='15px' height='15px' src='images/action-website.png'>");
    propertyValue1Field.attr("aria-describedby", "propertyValue1Addon");
    var editDatalibButton = $('<div data-toggle="tooltip" class="editDataLib input-group-addon" style="text-align:center"><span class="glyphicon glyphicon-pencil"></span></div>');
    propertyValue1Container.append(propertyValue1Addon).append(propertyValue1Field).append(editDatalibButton);
    propertyValue1Container = $("<div class='value1'></div>").addClass("col-lg-5 form-group marginBottom15").attr('id', uniqfieldid + 'v1').append(propertyValue1Container);

    //propertyValue1Container.append(editDatalibButton);


    var propertyValue1InputField = $("<input>").attr("data-toggle", "tooltip").attr("data-animation", "false").attr("data-html", "true").attr("data-container", "body").attr("data-placement", "top").attr("data-trigger", "manual").attr("type", "text").addClass("form-control");
    propertyValue1InputField.val(property.value1);
    propertyValue1InputField.prop("readonly", !canUpdate);
    var propertyValue1InputContainer = $("<div class='input-group'></div>");
    var propertyValue1InputAddon = $("<span></span>").attr("id", "propertyValue1InputAddon").addClass("input-group-addon").attr('for', uniqfieldid + 'v1b').attr("style", "font-weight: 700;");
    propertyValue1InputAddon.append("<img width='15px' height='15px' src='images/action-website.png'>");
    propertyValue1InputField.attr("aria-describedby", "propertyValue1InputAddon");
    propertyValue1InputContainer.append(propertyValue1InputAddon).append(propertyValue1InputField);
    propertyValue1InputContainer = $("<div class='valueInput1'></div>").addClass("col-lg-5 form-group marginBottom15").attr('id', uniqfieldid + 'v1b').append(propertyValue1InputContainer);

    //VALUE2
    var propertyValue2Field = $("<input>").attr("data-toggle", "tooltip").attr("data-animation", "false").attr("data-html", "true").attr("data-container", "body").attr("data-placement", "top").attr("data-trigger", "manual").attr("type", "text").addClass("form-control");
    propertyValue2Field.val(property.value2);
    propertyValue2Field.prop("readonly", !canUpdate);
    var propertyValue2Container = $("<div class='input-group'></div>");
    var propertyValue2Addon = $("<span></span>").attr("id", "propertyValue2Addon").addClass("input-group-addon").attr('for', uniqfieldid + 'v2').attr("style", "font-weight: 700;");
    propertyValue2Addon.append("<img width='15px' height='15px' src='images/action-website.png'>");
    propertyValue2Field.attr("aria-describedby", "propertyValue2Addon");
    propertyValue2Container.append(propertyValue2Addon).append(propertyValue2Field);
    propertyValue2Container = $("<div class='value2'></div>").addClass("col-lg-5 form-group marginBottom15").attr('id', uniqfieldid + 'v2').append(propertyValue2Container);

    //VALUE3
    var propertyValue3Field = $("<select></select>").addClass("form-control");
    propertyValue3Field.prop("readonly", !canUpdate);
    var propertyValue3Container = $("<div class='input-group'></div>");
    var propertyValue3Addon = $("<span></span>").attr("id", "propertyValue3Addon").addClass("input-group-addon").attr('for', uniqfieldid + 'v3').attr("style", "font-weight: 700;");
    propertyValue3Addon.append("<img width='15px' height='15px' src='images/action-website.png'>");
    propertyValue3Field.attr("aria-describedby", "propertyValue3Addon");
    propertyValue3Container.append(propertyValue3Addon).append(propertyValue3Field);
    propertyValue3Container = $("<div class='value3'></div>").addClass("col-lg-5 form-group marginBottom15").attr('id', uniqfieldid + 'v3').append(propertyValue3Container);

    //LENGHT
    var propertyLengthField = $("<input>").attr("data-toggle", "tooltip").attr("data-animation", "false").attr("data-html", "true").attr("data-container", "body").attr("data-placement", "top").attr("data-trigger", "manual").attr("type", "text").addClass("form-control");
    propertyLengthField.val(property.length);
    propertyLengthField.prop("readonly", !canUpdate);
    var propertyLengthContainer = $("<div class='input-group'></div>");
    var propertyLengthAddon = $("<span></span>").attr("id", "propertyLengthAddon").addClass("input-group-addon").attr("style", "font-weight: 700;");
    propertyLengthAddon.append("<img width='15px' height='15px' src='images/action-website.png'>");
    propertyLengthField.attr("aria-describedby", "propertyLengthAddon");
    propertyLengthContainer.append(propertyLengthAddon).append(propertyLengthField);
    propertyLengthContainer = $("<div class='length'></div>").addClass("col-lg-5 form-group marginBottom15").append(propertyLengthContainer);

    //ROW LIMIT
    var propertyRowLimitField = $("<input>").attr("data-toggle", "tooltip").attr("data-animation", "false").attr("data-html", "true").attr("data-container", "body").attr("data-placement", "top").attr("data-trigger", "manual").attr("type", "text").addClass("form-control");
    propertyRowLimitField.val(property.rowLimit);
    propertyRowLimitField.prop("readonly", !canUpdate);
    var propertyRowLimitContainer = $("<div class='input-group'></div>");
    var propertyRowLimitAddon = $("<span></span>").attr("id", "propertyRowLimitAddon").addClass("input-group-addon").attr("style", "font-weight: 700;");
    propertyRowLimitAddon.append("<img width='15px' height='15px' src='images/action-website.png'>");
    propertyRowLimitField.attr("aria-describedby", "propertyRowLimitAddon");
    propertyRowLimitContainer.append(propertyRowLimitAddon).append(propertyRowLimitField);
    propertyRowLimitContainer = $("<div class='rowLimit'></div>").addClass("col-lg-5 form-group marginBottom15").append(propertyRowLimitContainer);

    //CACHE EXPIRE
    var propertyCacheExpireField = $("<input>").attr("data-toggle", "tooltip").attr("data-animation", "false").attr("data-html", "true").attr("data-container", "body").attr("data-placement", "top").attr("data-trigger", "manual").attr("type", "text").addClass("form-control");
    propertyCacheExpireField.val(property.cacheExpire);
    propertyCacheExpireField.prop("readonly", !canUpdate);
    var propertyCacheExpireContainer = $("<div class='input-group'></div>");
    var propertyCacheExpireAddon = $("<span></span>").attr("id", "propertyCacheExpireAddon").addClass("input-group-addon").attr("style", "font-weight: 700;");
    propertyCacheExpireAddon.append("<img width='15px' height='15px' src='images/action-website.png'>");
    propertyCacheExpireField.attr("aria-describedby", "propertyCacheExpireAddon");
    propertyCacheExpireContainer.append(propertyCacheExpireAddon).append(propertyCacheExpireField);
    propertyCacheExpireContainer = $("<div class='cacheExpire'></div>").addClass("col-lg-5 form-group marginBottom15").append(propertyCacheExpireContainer);

    //NATURE
    var propertyNatureField = $("<select></select>").addClass("form-control");
    propertyNatureField.prop("readonly", !canUpdate);
    var propertyNatureContainer = $("<div class='input-group'></div>");
    var propertyNatureAddon = $("<span></span>").attr("id", "propertyNatureAddon").addClass("input-group-addon").attr("style", "font-weight: 700;");
    propertyNatureAddon.append("<img width='15px' height='15px' src='images/action-website.png'>");
    propertyNatureField.attr("aria-describedby", "propertyNatureAddon");
    propertyNatureContainer.append(propertyNatureAddon).append(propertyNatureField);
    propertyNatureContainer = $("<div class='nature'></div>").addClass("col-lg-5 form-group marginBottom15").append(propertyNatureContainer);

    //NB RETRY
    var propertyRetryNbField = $("<input>").attr("data-toggle", "tooltip").attr("data-animation", "false").attr("data-html", "true").attr("data-container", "body").attr("data-placement", "top").attr("data-trigger", "manual").attr("type", "text").addClass("form-control");
    propertyRetryNbField.val(property.retryNb);
    propertyRetryNbField.prop("readonly", !canUpdate);
    var propertyRetryNbContainer = $("<div class='input-group'></div>");
    var propertyRetryNbAddon = $("<span></span>").attr("id", "propertyRetryNbAddon").addClass("input-group-addon").attr("style", "font-weight: 700;");
    propertyRetryNbAddon.append("<img width='15px' height='15px' src='images/action-website.png'>");
    propertyRetryNbField.attr("aria-describedby", "propertyRetryNbAddon");
    propertyRetryNbContainer.append(propertyRetryNbAddon).append(propertyRetryNbField);
    propertyRetryNbContainer = $("<div class='retryNb'></div>").addClass("col-lg-5 form-group marginBottom15").append(propertyRetryNbContainer);

    // RETRY PERIOD
    var propertyRetryPeriodField = $("<input>").attr("data-toggle", "tooltip").attr("data-animation", "false").attr("data-html", "true").attr("data-container", "body").attr("data-placement", "top").attr("data-trigger", "manual").attr("type", "text").addClass("form-control");
    propertyRetryPeriodField.val(property.retryPeriod);
    propertyRetryPeriodField.prop("readonly", !canUpdate);
    var propertyRetryPeriodContainer = $("<div class='input-group'></div>");
    var propertyRetryPeriodAddon = $("<span></span>").attr("id", "propertyRetryPeriodAddon").addClass("input-group-addon").attr("style", "font-weight: 700;");
    propertyRetryPeriodAddon.append("<img width='15px' height='15px' src='images/action-website.png'>");
    propertyRetryPeriodField.attr("aria-describedby", "propertyRetryPeriodAddon");
    propertyRetryPeriodContainer.append(propertyRetryPeriodAddon).append(propertyRetryPeriodField);
    propertyRetryPeriodContainer = $("<div class='retryPeriod'></div>").addClass("col-lg-5 form-group marginBottom15").append(propertyRetryPeriodContainer);

    // RANK
    var propertyRankField = $("<input>").attr("data-toggle", "tooltip").attr("data-animation", "false").attr("data-html", "true").attr("data-container", "body").attr("data-placement", "top").attr("data-trigger", "manual").attr("type", "text").addClass("form-control");
    propertyRankField.val(property.rank);
    propertyRankField.prop("readonly", !canUpdate);
    var propertyRankContainer = $("<div class='input-group'></div>");
    var propertyRankAddon = $("<span></span>").attr("id", "propertyRankAddon").addClass("input-group-addon").attr("style", "font-weight: 700;");
    propertyRankAddon.append("<img width='15px' height='15px' src='images/action-website.png'>");
    propertyRankField.attr("aria-describedby", "propertyRankAddon");
    propertyRankContainer.append(propertyRankAddon).append(propertyRankField);
    propertyRankContainer = $("<div class='rank'></div>").addClass("col-lg-5 form-group marginBottom15").append(propertyRankContainer);


    var selectAllBtn = $("<button></button>").addClass("btn btn-default btn-sm").append($("<span></span>").addClass("glyphicon glyphicon-check")).click(function () {
        country.find("input[type='checkbox']").prop('checked', true).trigger("change");
    });
    selectAllBtn.attr("disabled", !canUpdate);
    var selectNoneBtn = $("<button></button>").addClass("btn btn-default btn-sm").append($("<span></span>").addClass("glyphicon glyphicon-unchecked")).click(function () {
        country.find("input[type='checkbox']").prop('checked', false).trigger("change");
    });
    selectNoneBtn.attr("disabled", !canUpdate);
    var btnRow = $("<div class='col-sm-2'></div>").css("margin-top", "5px").css("margin-bottom", "5px").append(selectAllBtn).append(selectNoneBtn);

    deleteBtn.click(function () {
        // trigger when any deleteBtn is clicked
        var stopAllDelete = false;
        var stopNothing = false;
        var linkToProperty = null;
        var nothing = false;
        property.toDelete = !property.toDelete;

        if (property.toDelete) {
            content.addClass("list-group-item-danger");
        } else {
            content.removeClass("list-group-item-danger");
        }

        $(table).find("div.list-group-item").each(function () {
            if ($(this).find("#propName").val() === property.property) {
                if ($(this).hasClass("list-group-item-danger")) {
                    if (stopAllDelete !== true) {
                        allDelete = true;
                    }
                    if (stopNothing !== true) {
                        nothing = false;
                        stopNothing = true;
                    }
                } else {
                    if (stopAllDelete !== true) {
                        allDelete = false;
                        stopAllDelete = true;
                    }
                    if (stopNothing !== true) {
                        nothing = true;
                    }
                }
            }
        });

        $("#propListWrapper li a").each(function () {
            if ($(this).text() === property.property)
                linkToProperty = $(this).parent();
        });

        // go though every link and look for the right one
        if (linkToProperty !== null) {
            if (allDelete === true && nothing === false) {
                // set color to red
                linkToProperty.css("background-color", "#c94350");
            } else if (nothing === true) {
                // set color to white
                linkToProperty.css("background-color", "#fff");
            } else {
                // set color to pink
                linkToProperty.css("background-color", "#f2dede");
            }

        }
    });

    propertyInput.change(function () {
        property.property = $(this).val();
    });

    descriptionInput.change(function () {
        property.description = $(this).val();
    });

    selectType.change(function () {
        property.type = $(this).val();
        setPlaceholderProperty($(this).parents(".property"), property);
        displayPropertyHelper($(this).parents(".property"), property);
    });

    propertyDatabaseField.change(function () {
        property.database = $(this).val();
    });

    propertyValue1Field.change(function () {
        property.value1 = $(this).val();
        setPlaceholderProperty($(this).parents(".property"), property);
    });

    propertyValue1InputField.change(function () {
        property.value1 = $(this).val();
    });

    propertyValue2Field.change(function () {
        property.value2 = $(this).val();
    });

    propertyValue3Field.change(function () {
        property.value3 = $(this).val();
        setPlaceholderProperty($(this).parents(".property"), property);
        displayPropertyHelper($(this).parents(".property"), property);
    });

    propertyLengthField.change(function () {
        property.length = $(this).val();
        displayPropertyHelper($(this).parents(".property"), property);
    });

    propertyRowLimitField.change(function () {
        property.rowLimit = $(this).val();
    });

    propertyCacheExpireField.change(function () {
        property.cacheExpire = parseInt($(this).val());
    });

    propertyNatureField.change(function () {
        property.nature = $(this).val();
        setPlaceholderProperty($(this).parents(".property"), property);
        displayPropertyHelper($(this).parents(".property"), property);
    });

    propertyRetryNbField.change(function () {
        property.retryNb = $(this).val();
    });

    propertyRetryPeriodField.change(function () {
        property.retryPeriod = $(this).val();
    });

    propertyRankField.change(function () {
        property.rank = $(this).val();
        displayPropertyHelper($(this).parents(".property"), property);
    });

    row1.data("property", property);
    row1.append(propertyName);
    row1.append(description);
    props.append(row1);

    row2.append(btnRow);
    row2.append(country);
    row2.append($("<div class='helper'></div>"));
    props.append(row2);

    row3.append(type);
    row3.append(propertyValue1Container);
    row3.append(propertyValue1InputContainer);
    row3.append(propertyDatabaseContainer);
    row3.append(propertyValue2Container);
    row3.append(propertyValue3Container);
    row3.append(propertyLengthContainer);
    row3.append(propertyRowLimitContainer);
    row3.append(propertyNatureContainer);
    row3.append(propertyRetryNbContainer);
    row3.append(propertyRetryPeriodContainer);
    row3.append(propertyCacheExpireContainer);
    row3.append(propertyRankContainer);
    props.append(row3);

    right.append(deleteBtn);

    content.append(props).append(right);
    table.append(content);
    displayPropertyHelper(props, property);

    bindToggleFullscreen();

    return [props, property];
}

function displayPropertyHelper(elementLine, property) {

    var informationForHelper = newPropertyPlaceholder[property.type].helperInformation;
    var helperString = "helper";

    if (informationForHelper.indexOf("value3") !== -1) {
        helperString += "-" + property.value3;
    }
    if (informationForHelper.indexOf("rowLimit") !== -1) {
        helperString += property.rowLimit !== "0" ? "-x" : "-0";
    }
    if (informationForHelper.indexOf("nature") !== -1) {
        // Do not use nature 3 if value is count, valueList or rawList
        if (property.value3 === "count" || property.value3 === "valueList" || property.value3 === "valueSum" || property.value3 === "rawList") {
        } else {
            helperString += "-" + property.nature;
        }
    }
    if (informationForHelper.indexOf("length") !== -1) {
        helperString += property["length"] !== "0" ? "-x" : "-0";
    }
    if (informationForHelper.indexOf("rank") !== -1) {
        if (property.value3 !== "count" &&
                property.value3 !== "valueList" &&
                property.value3 !== "valueSum" &&
                property.value3 !== "rawList" &&
                property.nature !== 'RANDOM') {
            helperString += property.rank !== 0 ? "-x" : "-0";
        }
    }

    var helper = '<p class="col-sm-12" name="propertyHelper">' + newPropertyPlaceholder[property.type].helperMessages[helperString] + '</p>';

    console.log(helperString);
    $(elementLine).find(".helper").empty();
    $(elementLine).find(".helper").append(helper);
}
;

function feedOption(propertyType, propertyField, element, selectedValue) {
    if (typeof newPropertyPlaceholder[propertyType][propertyField] !== "undefined") {
        element.empty();
        for (var j = 0; j < newPropertyPlaceholder[propertyType][propertyField].options.length; j++) {
            element.append($("<option></option>").text(newPropertyPlaceholder[propertyType][propertyField].options[j].label.en).val(newPropertyPlaceholder[propertyType][propertyField].options[j].value));
        }
        //var propertyNatureField = getSelectInvariant("PROPERTYNATURE", false, false);
        element.val(selectedValue);
    }
}

function drawInheritedProperty(propList) {
    var doc = new Doc();

    var selectType = getSelectInvariant("PROPERTYTYPE", false, false).attr("disabled", true);
    selectType.attr("name", "inheritPropertyType");

    var selectDB = getSelectInvariant("PROPERTYDATABASE", false, false).attr("disabled", true);

    var table = $("#inheritedPropPanel");

    for (var index = 0; index < propList.length; index++) {
        var property = propList[index];
        var test = property.fromTest;
        var testcase = property.fromTestcase;

        var editBtn = $("<a href='./TestCaseScript.jsp?test=" + encodeURI(test) + "&testcase=" + encodeURI(testcase) + "&property=" + encodeURI(property.property) + "' class='btn btn-primary add-btn'></a>").append($("<span></span>").addClass("glyphicon glyphicon-pencil"));

        var content = $("<div class='row property list-group-item disabled'></div>");
        var props = $("<div class='col-sm-11' name='inheritPropertyLine' id='inheritPropertyLine" + property.property + "'></div>");
        var right = $("<div class='col-sm-1 propertyButtons'></div>");
        var row1 = $("<div class='row' id='masterProp' name='masterProp' style='margin-top:10px;'></div>");
        var row2 = $("<div class='row' name='masterProp'></div>");
        var row3 = $("<div class='row' name='masterProp''></div>");

        var placeHolders = newPropertyPlaceholder[property.type];

        //PROP NAME
        var propertyInput = $("<input id='propName' name='propName' style='width: 100%; font-size: 16px; font-weight: 600;' placeholder='" + doc.getDocLabel("page_testcasescript", "feed_propertyname") + "' readonly='readonly'>").addClass("form-control").val(property.property);
        var propertyName = $("<div class='col-sm-4 form-group'></div>").append(propertyInput);

        //DESC
        var descriptionInput = $("<textarea rows='1' id='propDescription' placeholder='" + doc.getDocLabel("page_testcasescript", "feed_propertydescription") + "' readonly='readonly'>").addClass("form-control").val(property.description);
        var description = $("<div class='col-sm-8 form-group'></div>").append(descriptionInput);

        //COUNTRY
        var country = $("<div class='col-sm-10'></div>").append(getTestCaseCountry(property.countries, property.countries, true));

        //TYPE
        var type = $("<div class='col-sm-2 form-group'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "type_field"))).append(selectType.clone().val(property.type));

        //DB
        var db = $("<div name='fieldDatabase'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "db_field"))).append(selectDB.val(property.database));
        if (typeof placeHolders.database !== 'undefined') {
            db.addClass(placeHolders.database.class)
        } else {
            db.hide();
        }

        //VALUE1
        var valueInput = $("<pre id='inheritPropertyValue" + index + "' style='min-height:150px'  rows='1' placeholder='" + doc.getDocLabel("page_applicationObject", "Value") + "' readonly='readonly'></textarea>").addClass("form-control").text(property.value1);
        var value = $("<div class='form-group' name='fieldValue1'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "value1_field"))).append(valueInput);
        if (typeof placeHolders.value1 !== 'undefined') {
            value.addClass(placeHolders.value1.class);
        } else {
            value.hide();
        }

        //VALUE2
        var value2Input = $("<textarea name='inheritPropertyValue2' rows='1' placeholder='" + doc.getDocLabel("page_applicationObject", "Value") + "' readonly='readonly'></textarea>").addClass("form-control").val(property.value2);
        var value2 = $("<div class='form-group' name='fieldValue2'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "value2_field"))).append(value2Input);
        if (typeof placeHolders.value2 !== 'undefined') {
            value2.addClass(placeHolders.value2.class);
        } else {
            value2.hide();
        }

        //VALUE3
        var value3Input = $("<input name='inheritPropertyValue3' rows='1' placeholder='" + doc.getDocLabel("page_applicationObject", "Value") + "' readonly='readonly'>").addClass("form-control").val(property.value3);
        var value3 = $("<div class='form-group' name='fieldValue3'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "value3_field"))).append(value3Input);
        if (typeof placeHolders.value3 !== 'undefined') {
            value3.addClass(placeHolders.value3.class);
        } else {
            value3.hide();
        }

        //LENGTH
        var lengthInput = $("<input placeholder='" + doc.getDocLabel("page_testcasescript", "length") + "' readonly='readonly'>").addClass("form-control").val(property.length);
        var length = $("<div class='form-group' name='fieldLength'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "length_field"))).append(lengthInput);
        if (typeof placeHolders.length !== 'undefined') {
            length.addClass(placeHolders.length.class);
        } else {
            length.hide();
        }

        //NATURE
        var natureInput = $("<input placeholder='" + doc.getDocLabel("page_testcasescript", "nature") + "' readonly='readonly'>").addClass("form-control").val(property.nature);
        var nature = $("<div class='form-group' name='fieldNature'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "nature_field"))).append(natureInput);
        if (typeof placeHolders.nature !== 'undefined') {
            nature.addClass(placeHolders.nature.class);
        } else {
            nature.hide();
        }

        //Row Limit
        var rowLimitInput = $("<input placeholder='" + doc.getDocLabel("page_testcasescript", "row_limit") + "' readonly='readonly'>").addClass("form-control").val(property.rowLimit);
        var rowLimit = $("<div class='form-group' name='fieldRowLimit'></div>").append($("<label></label>").text(doc.getDocLabel("page_testcasescript", "rowlimit_field"))).append(rowLimitInput);
        if (typeof placeHolders.rowLimit !== 'undefined') {
            rowLimit.addClass(placeHolders.rowLimit.class);
        } else {
            rowLimit.hide();
        }

        //Cache Expire
        var cacheExpireInput = $("<input placeholder='0' readonly='readonly'>").addClass("form-control").val(property.cacheExpire);
        var cacheExpire = $("<div class='form-group' name='fieldExpire'></div>").append($("<label></label>").text("cacheExpire")).append(cacheExpireInput);
        if (typeof placeHolders.cacheExpire !== 'undefined') {
            cacheExpire.addClass(placeHolders.cacheExpire.class);
        } else {
            cacheExpire.hide();
        }

        //Retry
        var retryNbInput = $("<input placeholder='" + doc.getDocLabel("testcasecountryproperties", "RetryNb") + "' readonly='readonly'>").addClass("form-control").val(property.retryNb);
        var retryNb = $("<div class='form-group' name='fieldRetryNb'></div>").append($("<label></label>").text(doc.getDocLabel("testcasecountryproperties", "RetryNb"))).append(retryNbInput);
        if (typeof placeHolders.retryNb !== 'undefined') {
            retryNb.addClass(placeHolders.retryNb.class);
        } else {
            retryNb.hide();
        }

        //Rank
        var rankInput = $("<input type='number' placeholder='" + doc.getDocLabel("testcasecountryproperties", "Rank") + "' readonly='readonly'>").addClass("form-control").val(property.rank);
        var rank = $("<div class='form-group' name='Rank'></div>").append($("<label></label>").text(doc.getDocLabel("testcasecountryproperties", "Rank"))).append(rankInput);
        if (typeof placeHolders.rank !== 'undefined') {
            rank.addClass(placeHolders.rank.class);
        } else {
            rank.hide();
        }

        //retryPeriod
        var retryPeriodInput = $("<input placeholder='" + doc.getDocLabel("testcasecountryproperties", "RetryPeriod") + "' readonly='readonly'>").addClass("form-control").val(property.retryPeriod);
        var retryPeriod = $("<div class='form-group' name='fieldRetryPeriod'></div>").append($("<label></label>").text(doc.getDocLabel("testcasecountryproperties", "RetryPeriod"))).append(retryPeriodInput);
        if (typeof placeHolders.retryPeriod !== 'undefined') {
            retryPeriod.addClass(placeHolders.retryPeriod.class);
        } else {
            retryPeriod.hide();
        }

        row1.data("property", property);
        row1.append(propertyName);
        row1.append(description);
        props.append(row1);

        row2.append(country);
        props.append(row2);

        row3.append(type);
        row3.append(db);
        row3.append(value);
        row3.append(value2);
        row3.append(value3);
        row3.append(db);
        row3.append(length);
        row3.append(cacheExpire);
        row3.append(rowLimit);
        row3.append(nature);
        row3.append(retryNb);
        row3.append(retryPeriod);
        row3.append(rank);
        props.append(row3);

        right.append(editBtn);

        content.append(props).append(right);
        table.append(content);

        var htmlElement = $("<li></li>").addClass("list-group-item list-group-item-calm row").css("margin-left", "0px");
        $(htmlElement).append($("<a></a>").attr("href", "#inheritPropertyLine" + property.property).text(property.property));

        $("#inheritPropList").append(htmlElement);
    }

}

function drawPropertyList(property, index) {
    var htmlElement = $("<li></li>").addClass("list-group-item list-group-item-calm row").css("margin-left", "0px");

    $(htmlElement).append($("<a ></a>").attr("href", "#propertyLine" + property).text(property));

    var deleteBtn = $("<button style='padding:0px;float:right;display:none' class='btn add-btn deleteItem-btn'></button>").append($("<span></span>").addClass("glyphicon glyphicon-trash"));
    deleteBtn.attr("disabled", !canUpdate);
    $(htmlElement).find("a").append(deleteBtn);

    deleteBtn.click(function (ev) {
        if (allDelete !== true) {
            $("div.list-group-item").each(function () {
                if ($(this).find("#propName").val() === property) {
                    if (!$(this).hasClass("list-group-item-danger")) {
                        $(this).find("button.btn.add-btn.deleteItem-btn").trigger("click");
                    }
                }
            });
        } else {
            $("div.list-group-item").each(function () {
                if ($(this).find("#propName").val() === property) {
                    $(this).find("button.btn.add-btn.deleteItem-btn").trigger("click");
                }
            });
        }
    });

    $("#propList").append(htmlElement);
}

// Add keywordValue as a new property
function addPropertyWithAce(keywordValue) {

    var test = GetURLParameter("test");
    var testcase = GetURLParameter("testcase");
    var info = GetURLParameter("testcase");
    var property = GetURLParameter("property");

    $.ajax({
        url: "ReadTestCase",
        data: {test: test, testCase: testcase, withSteps: true},
        dataType: "json",
        success: function (data) {

            testCaseObject = data.contentTable[0];
            loadTestCaseInfo(testCaseObject);

            var myCountry = [];
            $.each(testCaseObject.countries, function (index) {
                myCountry.push(index);
            });
            // Store the current saveScript button status and disable it
            var saveScriptOldStatus = $("#saveScript").attr("disabled");
            $("#saveScript").attr("disabled", true);

            var newProperty = {
                property: keywordValue,
                description: "",
                countries: myCountry,
                type: "text",
                database: "",
                value1: "",
                value2: "",
                value3: "value",
                length: "0",
                rowLimit: 0,
                nature: "STATIC",
                retryNb: "",
                retryPeriod: "",
                toDelete: false,
                rank: 0
            };

            var prop = drawProperty(newProperty, testCaseObject, true, $("div[name='propertyLine']").length);
            setPlaceholderProperty($(prop[0]), prop[1]);

            // Restore the saveScript button status
            $("#saveScript").attr("disabled", typeof saveScriptOldStatus !== typeof undefined && saveScriptOldStatus !== false);
        }
    });
    getKeywordList("property").push(keywordValue);
}

// set the list of ace annotion object as annotation
function setAceAnnotation(editor, annotationObjectList) {
    // Set annotation replace all the annotation so if you use it you need to
    // resend every annotation for each change
    editor.getSession().setAnnotations(annotationObjectList);
}

/*
 * Set a listenner for every left part of ace's lines in each line that will
 * resolve issue
 */
function createGuterCellListenner(editor) {

    var currentEditorGutter = editor.container.getElementsByClassName("ace_gutter")[0];
    var cellList = currentEditorGutter.getElementsByClassName("ace_gutter-cell");
    for (var i = 0; i < cellList.length; i++) {

        cellList[i].setAttribute("style", "cursor: pointer");
        cellList[i].onclick = function () {

            var lineClickedId = this.innerHTML - 1;// start at 1
            var annotationObjectList = editor.getSession().getAnnotations();

            for (var y = 0; y < annotationObjectList.length; y++) {
                if (annotationObjectList[y].lineNumber === lineClickedId && annotationObjectList[y].type === "warning") {

                    var keywordType = annotationObjectList[y].keywordType;
                    var keywordValue = annotationObjectList[y].keywordValue;
                    if (keywordType === "property") {
                        addPropertyWithAce(keywordValue);
                    }
                    if (keywordType === "object") {
                        addObjectWithAce(keywordValue);
                    }
                }
            }
            this.className = "ace_gutter-cell";// Remove the warning annotation
        };
    }
}

/*
 * main function of ace editor
 */
function configureAceEditor(editor, mode, property) {
    // command Name
    var commandNameForAutoCompletePopup = "cerberusPopup";
    var commandNameForIssueDetection = "cerberusIssueDetection";
    // event listenner
    editor.commands.on("afterExec", function (e) {
        var langTools = ace.require('ace/ext/language_tools');


        if (e.command.name === "insertstring" || e.command.name === "paste" || e.command.name === "backspace") {
            // recreate the array at each loop

            if (property.type === "getFromDataLib") {
                CompleterForAllDataLib();
                $("pre").off("input").on("input", function (e) {
                    editor.execCommand("startAutocomplete");
                });
                editor.setOptions({maxLines: 15, minLines: 2, enableBasicAutocompletion: true, enableLiveAutocompletion: false, useWorker: false});
            } else {
                editor.setOptions({maxLines: 15, minLines: 2, enableBasicAutocompletion: true, enableLiveAutocompletion: false, useWorker: false});
                var allKeyword = createAllKeywordList(getKeywordList("object"), getKeywordList("property"));
                // editor.completers = [allKeyword]
                if (e.command.name !== "backspace") {
                    addCommandForCustomAutoCompletePopup(editor, allKeyword, commandNameForAutoCompletePopup);
                    editor.commands.exec(commandNameForAutoCompletePopup);// set
                    // autocomplete
                    // popup*/
                } else {
                    addCommandToDetectKeywordIssue(editor, allKeyword, commandNameForIssueDetection);
                    editor.commands.exec(commandNameForIssueDetection);// set
                    // annotation
                }
            }
        }
        createGuterCellListenner(editor);
        property.value1 = editor.session.getValue();

    });

    // editor option
    editor.getSession().setMode(mode);
    editor.off('change');
    editor.setTheme("ace/theme/chrome");
    editor.$blockScrolling = "Infinity";// disable error message
    editor.setOptions({maxLines: 15, minLines: 2, enableBasicAutocompletion: true, enableLiveAutocompletion: false, useWorker: false});

    // set text previously input
    editor.setValue(property.value1);
    // lose focus when loaded
    var count = editor.getSession().getLength();
    editor.gotoLine(count, editor.getSession().getLine(count - 1).length);
}

function setDatalibButtonOnClick(element, datalib, editorId) {

    if (!isEmpty(datalib)) {
        $.ajax({
            url: "ReadTestDataLib",
            data: {
                name: datalib,
                limit: 15,
                like: "N"
            },
            async: true,
            method: "GET",
            success: function (data) {
                if (data.messageType === "OK") {
                    var service = data.contentTable;
                    if (service.length >= 1) {
                        element.attr("onclick", `openModalDataLib(null, '${datalib}', 'EDIT', 'TestCaseScript_Props', '${editorId}')`);
                        element.find("span[class*='glyphicon']").removeClass().addClass("glyphicon glyphicon-pencil");
                        element.attr("data-original-title", "Edit DataLib");
                    } else {
                        element.attr("onclick", `openModalDataLib(null, '${datalib}', 'ADD', 'TestCaseScript_Props', '${editorId}')`);
                        element.find("span[class*='glyphicon']").removeClass().addClass("glyphicon glyphicon-plus");
                        element.attr("data-original-title", "Add DataLib");
                    }
                }
            },
            error: showUnexpectedError
        });
    }
}

function setDatalibListener(editor, property, propertyObject) {
    //Usage of delay in order to avoid to send too much requests
    let typingTimer;
    const typingDelay = 1000;

    editor.getSession().on("change", function (e) {
        clearTimeout(typingTimer);
        typingTimer = setTimeout(() => {
            setDatalibButtonOnClick(property.find("div[class*='editDataLib']"), propertyObject.value1, editor.container.id);
        }, typingDelay);
    });
}