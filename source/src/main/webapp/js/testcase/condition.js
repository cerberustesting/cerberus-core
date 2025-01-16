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



function setPlaceholderCondition(conditionElement) {

//    console.log(conditionElement);
    var user = getUser();
    var placeHolders = conditionNewUIList[conditionElement.val()];

//    console.log(placeHolders);

    if (typeof placeHolders === 'undefined') {
        placeHolders = conditionNewUIList["always"];
    }

    if (typeof placeHolders.field1 !== 'undefined') {
        // $(actionElement).parents("div[class*='secondRow']").children("div[class*='v1']").removeClass("col-lg-2 col-lg-3 col-lg-4 col-lg-5 col-lg-6 col-lg-7 col-lg-8 col-lg-9").addClass(placeHolders.field1.class);
        $(conditionElement).parents("div[class*='conditions']").find(".v1").find("input")
            .removeClass("crb-autocomplete-element crb-autocomplete-property crb-autocomplete-service crb-autocomplete-variable crb-autocomplete-fileuploadflag crb-autocomplete-filesortflag crb-autocomplete-boolean crb-autocomplete-select crb-autocomplete-switch crb-contextual-button")
            .addClass(placeHolders.field1.class);
        $(conditionElement).parents("div[class*='conditions']").find("label[class='conditionVal1Label']").parent().show();
        $(conditionElement).parents("div[class*='conditions']").find("label[class='conditionVal1Label']").text(placeHolders.field1.label[user.language]);
        if (typeof placeHolders.field1.picto !== 'undefined') {
            //$(actionElement).parents("div[class*='secondRow']").children("div[class*='v1']").find('img').attr("src", placeHolders.field1.picto);
        }
    } else {
        $(conditionElement).parents("div[class*='conditions']").find("label[class='conditionVal1Label']").parent().hide();
    }

    if (typeof placeHolders.field2 !== 'undefined') {
        $(conditionElement).parents("div[class*='conditions']").find(".v2").find("input")
            .removeClass("crb-autocomplete-element crb-autocomplete-property crb-autocomplete-service crb-autocomplete-variable crb-autocomplete-fileuploadflag crb-autocomplete-filesortflag crb-autocomplete-boolean crb-autocomplete-select crb-autocomplete-switch crb-contextual-button")
            .addClass(placeHolders.field2.class);
        $(conditionElement).parents("div[class*='conditions']").find("label[class='conditionVal2Label']").parent().show();
        $(conditionElement).parents("div[class*='conditions']").find("label[class='conditionVal2Label']").text(placeHolders.field2.label[user.language]);

    } else {
        $(conditionElement).parents("div[class*='conditions']").find("label[class='conditionVal2Label']").parent().hide();
    }

    if (typeof placeHolders.field3 !== 'undefined') {
        $(conditionElement).parents("div[class*='conditions']").find(".v3").find("input")
            .removeClass("crb-autocomplete-element crb-autocomplete-property crb-autocomplete-service crb-autocomplete-variable crb-autocomplete-fileuploadflag crb-autocomplete-filesortflag crb-autocomplete-boolean crb-autocomplete-select crb-autocomplete-switch crb-contextual-button")
            .addClass(placeHolders.field3.class);
        $(conditionElement).parents("div[class*='conditions']").find("label[class='conditionVal3Label']").parent().show();
        $(conditionElement).parents("div[class*='conditions']").find("label[class='conditionVal3Label']").text(placeHolders.field3.label[user.language]);

    } else {
        $(conditionElement).parents("div[class*='conditions']").find("label[class='conditionVal3Label']").parent().hide();
    }

    if (typeof placeHolders.field4 !== 'undefined') {
        $(conditionElement).parents("div[class*='conditions']").find("label[class='conditionVal4Label']").parent().show();
        $(conditionElement).parents("div[class*='conditions']").find("label[class='conditionVal4Label']").text(placeHolders.field4.label[user.language]);

    } else {
        $(conditionElement).parents("div[class*='conditions']").find(".v4").hide();
    }

    if (typeof placeHolders.field5 !== 'undefined') {
        $(conditionElement).parents("div[class*='conditions']").find("label[class='conditionVal5Label']").parent().show();
        $(conditionElement).parents("div[class*='conditions']").find("label[class='conditionVal5Label']").text(placeHolders.field5.label[user.language]);

    } else {
        $(conditionElement).parents("div[class*='conditions']").find(".v5").hide();
    }

}

function displayOverrideOptionsModal(action, htmlElement) {

    var user = getUser();
    $("#modalOptions").find("h5").text("Override Option Values");

    //INIT MODAL
    initOptionModal("");
    initOptionModal("Condition");
    $("#fatalCheckbox").prop("checked", false);
    $("#screenshotBCheckbox").prop("checked", false);
    $("#screenshotACheckbox").prop("checked", false);
    $("#conditionSelect").val("");
    $("#actionconditionval1").val("");
    $("#actionconditionval2").val("");
    $("#actionconditionval3").val("");

    //FEED FIELDS
    //SCREENSHOT
    if (action.doScreenshotBefore) {
        $("#screenshotBCheckbox").prop("checked", true);
    }
    if (action.doScreenshotAfter) {
        $("#screenshotACheckbox").prop("checked", true);
    }
    $("#waitBVal").val(action.waitBefore);
    $("#waitAVal").val(action.waitAfter);
    //OPTIONS
    setOptionModal(action.options, "");
    setOptionModal(action.conditionOptions, "Condition");

    //FATAL
    if (action.isFatal) {
        $("#fatalCheckbox").prop("checked", true);
    }

    //CONDITION
    $("#conditionSelect").empty();
    for (var key in conditionNewUIList) {
        $("#conditionSelect").append($("<option></option>").text(conditionNewUIList[key].label[user.language]).val(conditionNewUIList[key].value));
    }

    $("#conditionSelect").val(action.conditionOperator);
    $("#actionconditionval1").val(action.conditionValue1);
    $("#actionconditionval2").val(action.conditionValue2);
    $("#actionconditionval3").val(action.conditionValue3);
    setPlaceholderCondition($("#conditionSelect"));

    if (conditionNewUIList[action.conditionOperator].type ==="combo") {
        appendActionsForConditionCombobox($("#actionconditionval4"), action.conditionOperator);
        if (conditionNewUIList[action.conditionOperator].level ==="step"){
            $("#actionconditionval4").val(action.conditionValue1);
        }
        if (conditionNewUIList[action.conditionOperator].level ==="action"){
            $("#actionconditionval4").val(action.conditionValue1+"-"+action.conditionValue2);
        }
        if (conditionNewUIList[action.conditionOperator].level ==="control"){
            $("#actionconditionval4").val(action.conditionValue1+"-"+action.conditionValue2+"-"+action.conditionValue3);
        }
    }
    if (conditionNewUIList[action.conditionOperator].field5 != undefined) {
        let checked = action.conditionValue2=="false"?false:true;
        $("#actionconditionval5").prop("checked", checked);
    }


    $("#conditionSelect").off("change");
    $("#conditionSelect").on("change", function () {
        setModif(true);
        setPlaceholderCondition($(this));
        appendActionsForConditionCombobox($("#actionconditionval4"), $(this).val());
    });
    $("#actionconditionval1").off("change");
    $("#actionconditionval1").on("change", function () {
        setModif(true);
    });
    $("#actionconditionval2").off("change");
    $("#actionconditionval2").on("change", function () {
        setModif(true);
    });
    $("#actionconditionval3").off("change");
    $("#actionconditionval3").on("change", function () {
        setModif(true);
    });
    $("#actionconditionval4").off("change");
    $("#actionconditionval4").on("change", function () {
        setModif(true);
    });
    $("#screenshotBCheckbox").off("change");
    $("#screenshotBCheckbox").on("change", function () {
        setModif(true);
    });
    $("#screenshotACheckbox").off("change");
    $("#screenshotACheckbox").on("change", function () {
        setModif(true);
    });
    $("#waitBVal").off("change");
    $("#waitBVal").on("change", function () {
        setModif(true);
    });
    $("#waitAVal").off("change");
    $("#waitAVal").on("change", function () {
        setModif(true);
    });
    $("#fatalCheckbox").off("change");
    $("#fatalCheckbox").on("change", function () {
        setModif(true);
    });


    $("#timeoutAct").off("change");
    $("#timeoutAct").on("change", function () {
        setModif(true);
    });
    $("#timeoutVal").off("change");
    $("#timeoutVal").on("change", function () {
        setModif(true);
    });
    $("#minSimilarityAct").off("change");
    $("#minSimilarityAct").on("change", function () {
        setModif(true);
    });
    $("#minSimilarityVal").off("change");
    $("#minSimilarityVal").on("change", function () {
        setModif(true);
    });
    $("#highlightAct").off("change");
    $("#highlightAct").on("change", function () {
        setModif(true);
    });
    $("#highlightVal").off("change");
    $("#highlightVal").on("change", function () {
        setModif(true);
    });
    $("#typeDelayAct").off("change");
    $("#typeDelayAct").on("change", function () {
        setModif(true);
    });
    $("#typeDelayVal").off("change");
    $("#typeDelayVal").on("change", function () {
        setModif(true);
    });


    $("#timeoutConditionAct").off("change");
    $("#timeoutConditionAct").on("change", function () {
        setModif(true);
    });
    $("#timeoutConditionVal").off("change");
    $("#timeoutConditionVal").on("change", function () {
        setModif(true);
    });
    $("#minSimilarityConditionAct").off("change");
    $("#minSimilarityConditionAct").on("change", function () {
        setModif(true);
    });
    $("#minSimilarityConditionVal").off("change");
    $("#minSimilarityConditionVal").on("change", function () {
        setModif(true);
    });
    $("#highlightConditionAct").off("change");
    $("#highlightConditionAct").on("change", function () {
        setModif(true);
    });
    $("#highlightConditionVal").off("change");
    $("#highlightConditionVal").on("change", function () {
        setModif(true);
    });
    $("#typeDelayConditionAct").off("change");
    $("#typeDelayConditionAct").on("change", function () {
        setModif(true);
    });
    $("#typeDelayConditionVal").off("change");
    $("#typeDelayConditionVal").on("change", function () {
        setModif(true);
    });


    //EVENT ON SAVE
    $("#optionsSave").off("click");
    $("#optionsSave").click(function () {

        action.isFatal = $("#fatalCheckbox").is(':checked');

        action.doScreenshotBefore = $("#screenshotBCheckbox").is(':checked');
        action.doScreenshotAfter = $("#screenshotACheckbox").is(':checked');
        action.waitBefore = $("#waitBVal").val();
        action.waitAfter = $("#waitAVal").val();

        action.conditionOperator = $("#conditionSelectContainer").find('select').val();
        action.conditionValue1 = $("#actionconditionval1").val();
        action.conditionValue2 = $("#actionconditionval2").val();
        action.conditionValue3 = $("#actionconditionval3").val();

        if (conditionNewUIList[action.conditionOperator].type === "combo") {
            action.conditionValue1 = $("#actionconditionval4 option:selected").attr("stepId");
            action.conditionValue2 = $("#actionconditionval4 option:selected").attr("actionId") === undefined ? "":$("#actionconditionval4 option:selected").attr("actionId");
            action.conditionValue3 = $("#actionconditionval4 option:selected").attr("controlId")=== undefined ? "":$("#actionconditionval4 option:selected").attr("controlId");
        }
        console.log(conditionNewUIList[action.conditionOperator].field5);
        if (conditionNewUIList[action.conditionOperator].field5 != undefined) {
            action.conditionValue2 = $("#actionconditionval5").is(':checked').toString();
        }

        let newOpts = [];
        newOpts.push({
            "act": $("#timeoutAct").prop("checked"),
            "value": $("#timeoutVal").val(),
            "option": "timeout"
        });
        newOpts.push({
            "act": $("#minSimilarityAct").prop("checked"),
            "value": $("#minSimilarityVal").val(),
            "option": "minSimilarity"
        });
        newOpts.push({
            "act": $("#highlightAct").prop("checked"),
            "value": $("#highlightVal").val(),
            "option": "highlightElement"
        });
        newOpts.push({
            "act": $("#typeDelayAct").prop("checked"),
            "value": $("#typeDelayVal").val(),
            "option": "typeDelay"
        });

        if (JSON.stringify(action.options) !== JSON.stringify(newOpts)) {
            action.options = newOpts;
        }

        let newConditionOpts = [];
        newConditionOpts.push({
            "act": $("#timeoutConditionAct").prop("checked"),
            "value": $("#timeoutConditionVal").val(),
            "option": "timeout"
        });
        newConditionOpts.push({
            "act": $("#minSimilarityConditionAct").prop("checked"),
            "value": $("#minSimilarityConditionVal").val(),
            "option": "minSimilarity"
        });
        newConditionOpts.push({
            "act": $("#highlightConditionAct").prop("checked"),
            "value": $("#highlightConditionVal").val(),
            "option": "highlightElement"
        });
        newConditionOpts.push({
            "act": $("#typeDelayConditionAct").prop("checked"),
            "value": $("#typeDelayConditionVal").val(),
            "option": "typeDelay"
        });

        if (JSON.stringify(action.conditionOptions) !== JSON.stringify(newConditionOpts)) {
            action.conditionOptions = newConditionOpts;
            setModif(true);
        }

        printLabelForOptions($($($(htmlElement)[0]).find(".boutonGroup")[0]).parent(), newOpts, newConditionOpts, "optionLabel");
        printLabelForCondition($($($(htmlElement)[0]).find(".boutonGroup")[0]).parent(), action.conditionOperator, action.conditionValue1, action.conditionValue2, action.conditionValue3);
        printLabel($($($(htmlElement)[0]).find(".boutonGroup")[0]).parent(), action.isFatal, "actionFatalLabel", "labelOrange", "Stop Execution on Failure")
    });
}

function initOptionModal(context) {
    $("#timeout" + context + "Val").val("");
    $("#timeout" + context + "Act").prop("checked", false);
    $("#highlight" + context + "Val").val("");
    $("#highlight" + context + "Act").prop("checked", false);
    $("#minSimilarity" + context + "Val").val("");
    $("#minSimilarity" + context + "Act").prop("checked", false);
    $("#typeDelay" + context + "Val").val("");
    $("#typeDelay" + context + "Act").prop("checked", false);
}

function setOptionModal(actionOption, context) {
    for (var item in actionOption) {
        switch (actionOption[item].option) {
            case "timeout":
                $("#timeout" + context + "Val").val(actionOption[item].value);
                $("#timeout" + context + "Act").prop("checked", actionOption[item].act);
                break;
            case "highlightElement":
                $("#highlight" + context + "Val").val(actionOption[item].value);
                $("#highlight" + context + "Act").prop("checked", actionOption[item].act);
                break;
            case "minSimilarity":
                $("#minSimilarity" + context + "Val").val(actionOption[item].value);
                $("#minSimilarity" + context + "Act").prop("checked", actionOption[item].act);
                break;
            case "typeDelay":
                $("#typeDelay" + context + "Val").val(actionOption[item].value);
                $("#typeDelay" + context + "Act").prop("checked", actionOption[item].act);
                break;
            default:
                break;
        }
    }
}

function hasOneOptionActive(options) {
    for (var item in options) {
        if (options[item].act)
            return true;
    }
    return false;
}

function getTitleFromOptionsActive(options) {
    let result = "";
    for (var item in options) {
        if (options[item].act)
            result = result + options[item].option + "=" + options[item].value + " / ";
    }
    if (result === "") {
        return "Configure Options";
    }
    return result.substring(0, result.length - 3);
}