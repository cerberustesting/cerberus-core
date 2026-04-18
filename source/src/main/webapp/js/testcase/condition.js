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

// displayOverrideOptionsModal has been moved to manageActionControlOptions.html for Alpine.js integration

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