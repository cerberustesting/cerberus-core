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
var controlUIList = {
    "fr": [
        {
            "type": "Unknown",
            "value1": null,
            "value2": null,
            "value3": null,
            "fatal": null
        },
        {
            "type": "verifyStringEqual",
            "value1": "String1", "acol1": "col-lg-3",
            "value2": "String2", "acol2": "col-lg-3",
            "value3": "[opt] Sensible à la Casse (Y/N)", "acol3": "col-lg-2",
            "fatal": ""
        },
        {
            "type": "verifyStringDifferent",
            "value1": "String1", "acol1": "col-lg-3",
            "value2": "String2", "acol2": "col-lg-3",
            "value3": "[opt] Sensible à la Casse (Y/N)", "acol3": "col-lg-2",
            "fatal": ""
        },
        {
            "type": "verifyStringGreater",
            "value1": "String1 (ex: ZZZ)", "acol1": "col-lg-3",
            "value2": "String2 (ex: AAA)", "acol2": "col-lg-3",
            "value3": "[opt] Sensible à la Casse (Y/N)", "acol3": "col-lg-2",
            "fatal": ""
        },
        {
            "type": "verifyStringMinor",
            "value1": "String1 (ex: AAA)", "acol1": "col-lg-4",
            "value2": "String2 (ex : ZZZ)", "acol2": "col-lg-4",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyStringContains",
            "value1": "String1 (ex : toto)", "acol1": "col-lg-4",
            "value2": "String2 (ex : ot)", "acol2": "col-lg-2",
            "value3": "[opt] Sensible à la Casse (Y/N)", "acol3": "col-lg-2",
            "fatal": ""
        },
        {
            "type": "verifyStringNotContains",
            "value1": "String1 (ex : toto)", "acol1": "col-lg-4",
            "value2": "String2 (ex : zot)", "acol2": "col-lg-2",
            "value3": "[opt] Sensible à la Casse (Y/N)", "acol3": "col-lg-2",
            "fatal": ""
        },
        {
            "type": "verifyStringArrayContains",
            "value2": "String",
            "acol2": "col-lg-3",
            "value1": "Tableau",
            "acol1": "col-lg-3",
            "value3": "[opt] Sensible à la Casse (Y/N)",
            "acol3": "col-lg-2",
            "fatal": ""
        },
        {
            "type": "verifyNumericEquals",
            "value1": "Integer1", "acol1": "col-lg-3",
            "value2": "Integer2", "acol2": "col-lg-3",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyNumericDifferent",
            "value1": "Integer1", "acol1": "col-lg-4",
            "value2": "Integer2", "acol2": "col-lg-4",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyNumericGreater",
            "value1": "Integer1 (ex : 20)", "acol1": "col-lg-4",
            "value2": "Integer2 (ex : 10)", "acol2": "col-lg-4",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyNumericGreaterOrEqual",
            "value1": "Integer1 (ex : 20)", "acol1": "col-lg-4",
            "value2": "Integer2 (ex : 10)", "acol2": "col-lg-4",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyNumericMinor",
            "value1": "Integer1 (ex : 10)", "acol1": "col-lg-4",
            "value2": "Integer2 (ex : 20)", "acol2": "col-lg-4",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyNumericMinorOrEqual",
            "value1": "Integer1 (ex : 10)", "acol1": "col-lg-4",
            "value2": "Integer2 (ex : 20)", "acol2": "col-lg-4",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyNumericArrayContains",
            "value2": "Nombre",
            "acol2": "col-lg-3",
            "value1": "Tableau",
            "acol1": "col-lg-3",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyElementPresent",
            "value1": "Element (ex : data-cerberus=fieldToto)", "acol1": "col-lg-8",
            "value2": null,
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyElementNotPresent",
            "value1": "Element (ex : data-cerberus=fieldToto)", "acol1": "col-lg-8",
            "value2": null,
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyElementVisible",
            "value1": "Element (ex : data-cerberus=fieldToto)", "acol1": "col-lg-8",
            "value2": null,
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyElementNotVisible",
            "value1": "Element (ex : data-cerberus=fieldToto)", "acol1": "col-lg-8",
            "value2": null,
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyElementEquals",
            "value1": "XPath of the element", "acol1": "col-lg-3",
            "value2": "Expected element", "acol2": "col-lg-3",
            "value3": "[opt] Sensible à la Casse (Y/N)", "acol3": "col-lg-2",
            "fatal": ""
        },
        {
            "type": "verifyElementDifferent",
            "value1": "XPath of the element", "acol1": "col-lg-3",
            "value2": "Not Expected element", "acol2": "col-lg-3",
            "value3": "[opt] Sensible à la Casse (Y/N)", "acol3": "col-lg-2",
            "fatal": ""
        },
        {
            "type": "verifyElementInElement",
            "value1": "Master Element", "acol1": "col-lg-4",
            "value2": "Sub Element", "acol2": "col-lg-4",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyElementClickable",
            "value1": "Element (ex : data-cerberus=fieldToto)", "acol1": "col-lg-8",
            "value2": null,
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyElementNotClickable",
            "value1": "Element (ex : data-cerberus=fieldToto)", "acol1": "col-lg-8",
            "value2": null,
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyElementTextEqual",
            "value2": "Texte",
            "acol2": "col-lg-3",
            "value1": "Chemin vers l'Element",
            "acol1": "col-lg-3",
            "value3": "[opt] Sensible à la Casse (Y/N)",
            "acol3": "col-lg-2",
            "fatal": ""
        },
        {
            "type": "verifyElementTextDifferent",
            "value2": "Texte",
            "acol2": "col-lg-3",
            "value1": "Chemin vers l'Element",
            "acol1": "col-lg-3",
            "value3": "[opt] Sensible à la Casse (Y/N)",
            "acol3": "col-lg-2",
            "fatal": ""
        },
        {
            "type": "verifyElementTextContains",
            "value2": "Texte",
            "acol2": "col-lg-3",
            "value1": "Chemin vers l'Element",
            "acol1": "col-lg-3",
            "value3": "[opt] Sensible à la Casse (Y/N)",
            "acol3": "col-lg-2",
            "fatal": ""
        },
        {
            "type": "verifyElementTextMatchRegex",
            "value2": "Regex",
            "acol2": "col-lg-4",
            "value1": "Chemin vers l'Element",
            "acol1": "col-lg-4",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyElementTextArrayContains",
            "value2": "Texte",
            "acol2": "col-lg-3",
            "value1": "Chemin vers le tableau",
            "acol1": "col-lg-3",
            "value3": "[opt] Sensible à la Casse (Y/N)",
            "acol3": "col-lg-2",
            "fatal": ""
        },
        {
            "type": "verifyElementNumericArrayContains",
            "value2": "Nombre",
            "acol2": "col-lg-3",
            "value1": "Chemin vers le tableau",
            "acol1": "col-lg-3",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyElementNumericDifferent",
            "value2": "Valeur numerique",
            "acol2": "col-lg-4",
            "value1": "Chemin vers l'Element",
            "acol1": "col-lg-4",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyElementNumericGreater",
            "value2": "Valeur numerique",
            "acol2": "col-lg-4",
            "value1": "Chemin vers l'Element",
            "acol1": "col-lg-4",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyElementNumericGreaterOrEqual",
            "value2": "Valeur numerique",
            "acol2": "col-lg-4",
            "value1": "Chemin vers l'Element",
            "acol1": "col-lg-4",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyElementNumericMinor",
            "value2": "Valeur numerique",
            "acol2": "col-lg-4",
            "value1": "Chemin vers l'Element",
            "acol1": "col-lg-4",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyElementNumericMinorOrEqual",
            "value2": "Valeur numerique",
            "acol2": "col-lg-4",
            "value1": "Chemin vers l'Element",
            "acol1": "col-lg-4",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyTextInPage",
            "value2": null,
            "value1": "Regex",
            "acol1": "col-lg-8",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyTextNotInPage",
            "value2": null,
            "value1": "Regex",
            "acol1": "col-lg-8",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyTitle",
            "value2": null,
            "value1": "Title",
            "acol1": "col-lg-6",
            "value3": "[opt] Sensible à la Casse (Y/N)",
            "acol3": "col-lg-2",
            "fatal": ""
        },
        {"type": "verifyUrl", "value2": null, "value1": "URL", "acol1": "col-lg-8", "value3": null, "fatal": ""},
        {
            "type": "verifyTextInDialog",
            "value2": null,
            "value1": "Text",
            "acol1": "col-lg-8",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyXmlTreeStructure",
            "value2": "Tree",
            "acol2": "col-lg-4",
            "value1": "XPath",
            "acol1": "col-lg-4",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "takeScreenshot",
            "value2": null,
            "value1": "[opt] Cadrer en pixel (gauche,droite,haut,bas)",
            "acol1": "col-lg-8",
            "value3": null,
            "fatal": null
        },
        {"type": "getPageSource", "value2": null, "value1": null, "value3": null, "fatal": null}
    ], "en": [
        {
            "type": "Unknown",
            "value1": null,
            "value2": null,
            "value3": null,
            "fatal": null
        },
        {
            "type": "verifyStringEqual",
            "value1": "String1", "acol1": "col-lg-3",
            "value2": "String2", "acol2": "col-lg-3",
            "value3": "[opt] case sensitive (Y/N)", "acol3": "col-lg-2",
            "fatal": ""
        },
        {
            "type": "verifyStringDifferent",
            "value1": "String1", "acol1": "col-lg-3",
            "value2": "String2", "acol2": "col-lg-3",
            "value3": "[opt] case sensitive (Y/N)", "acol3": "col-lg-2",
            "fatal": ""
        },
        {
            "type": "verifyStringGreater",
            "value1": "String1 (ex: ZZZ)", "acol1": "col-lg-4",
            "value2": "String2 (ex : AAA)", "acol2": "col-lg-4",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyStringMinor",
            "value1": "String1 (ex: AAA)", "acol1": "col-lg-4",
            "value2": "String2 (ex : ZZZ)", "acol2": "col-lg-4",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyStringContains",
            "value1": "String1 (ex : toto)", "acol1": "col-lg-4",
            "value2": "String2 (ex : ot)", "acol2": "col-lg-2",
            "value3": "[opt] case sensitive (Y/N)", "acol3": "col-lg-2",
            "fatal": ""
        },
        {
            "type": "verifyStringNotContains",
            "value1": "String1 (ex : toto)", "acol1": "col-lg-4",
            "value2": "String2 (ex : zot)", "acol2": "col-lg-2",
            "value3": "[opt] case sensitive (Y/N)", "acol3": "col-lg-2",
            "fatal": ""
        },
        {
            "type": "verifyStringArrayContains",
            "value2": "String",
            "acol2": "col-lg-3",
            "value1": "Array",
            "acol1": "col-lg-3",
            "value3": "[opt] Case Sensitive (Y/N)",
            "acol3": "col-lg-2",
            "fatal": ""
        },
        {
            "type": "verifyNumericEquals",
            "value1": "Integer1", "acol1": "col-lg-4",
            "value2": "Integer2", "acol2": "col-lg-4",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyNumericDifferent",
            "value1": "Integer1", "acol1": "col-lg-4",
            "value2": "Integer2", "acol2": "col-lg-4",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyNumericGreater",
            "value1": "Integer1 (ex : 20)", "acol1": "col-lg-4",
            "value2": "Integer2 (ex : 10)", "acol2": "col-lg-4",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyNumericGreaterOrEqual",
            "value1": "Integer1 (ex : 20)", "acol1": "col-lg-4",
            "value2": "Integer2 (ex : 10)", "acol2": "col-lg-4",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyNumericMinor",
            "value1": "Integer1 (ex : 10)", "acol1": "col-lg-4",
            "value2": "Integer2 (ex : 20)", "acol2": "col-lg-4",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyNumericMinorOrEqual",
            "value1": "Integer1 (ex : 10)", "acol1": "col-lg-4",
            "value2": "Integer2 (ex : 20)", "acol2": "col-lg-4",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyNumericArrayContains",
            "value2": "Number",
            "acol2": "col-lg-3",
            "value1": "Array",
            "acol1": "col-lg-3",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyElementPresent",
            "value1": "Element (ex : data-cerberus=fieldToto)", "acol1": "col-lg-8",
            "value2": null,
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyElementNotPresent",
            "value1": "Element (ex : data-cerberus=fieldToto)", "acol1": "col-lg-8",
            "value2": null,
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyElementVisible",
            "value1": "Element (ex : data-cerberus=fieldToto)", "acol1": "col-lg-8",
            "value2": null,
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyElementNotVisible",
            "value1": "Element (ex : data-cerberus=fieldToto)", "acol1": "col-lg-8",
            "value2": null,
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyElementEquals",
            "value1": "XPath of the element", "acol1": "col-lg-4",
            "value2": "Expected element", "acol2": "col-lg-4",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyElementDifferent",
            "value1": "XPath of the element", "acol1": "col-lg-4",
            "value2": "Not Expected element", "acol2": "col-lg-4",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyElementInElement",
            "value1": "Master Element", "acol1": "col-lg-4",
            "value2": "Sub Element", "acol2": "col-lg-4",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyElementClickable",
            "value1": "Element (ex : data-cerberus=fieldToto)", "acol1": "col-lg-8",
            "value2": null,
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyElementNotClickable",
            "value1": "Element (ex : data-cerberus=fieldToto)", "acol1": "col-lg-8",
            "value2": null,
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyElementTextEqual",
            "value2": "Text",
            "acol2": "col-lg-3",
            "value1": "Element Path",
            "acol1": "col-lg-3",
            "value3": "[opt] Case Sensitive (Y/N)",
            "acol3": "col-lg-2",
            "fatal": ""
        },
        {
            "type": "verifyElementTextDifferent",
            "value2": "Text",
            "acol2": "col-lg-3",
            "value1": "Element Path",
            "acol1": "col-lg-3",
            "value3": "[opt] Case Sensitive (Y/N)",
            "acol3": "col-lg-2",
            "fatal": ""
        },
        {
            "type": "verifyElementTextContains",
            "value2": "Text",
            "acol2": "col-lg-3",
            "value1": "Element Path",
            "acol1": "col-lg-3",
            "value3": "[opt] Case Sensitive (Y/N)",
            "acol3": "col-lg-2",
            "fatal": ""
        },
        {
            "type": "verifyElementTextMatchRegex",
            "value2": "Regex",
            "acol2": "col-lg-3",
            "value1": "Element Path",
            "acol1": "col-lg-5",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyElementTextArrayContains",
            "value2": "Text",
            "acol2": "col-lg-3",
            "value1": "Array path",
            "acol1": "col-lg-3",
            "value3": "[opt] Case Sensitive (Y/N)",
            "acol3": "col-lg-2",
            "fatal": ""
        },
        {
            "type": "verifyElementNumericArrayContains",
            "value2": "Numeric value",
            "acol2": "col-lg-3",
            "value1": "Array path",
            "acol1": "col-lg-3",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyElementNumericEqual",
            "value2": "Numeric value",
            "acol2": "col-lg-2",
            "value1": "Element Path",
            "acol1": "col-lg-6",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyElementNumericDifferent",
            "value2": "Numeric value",
            "acol2": "col-lg-2",
            "value1": "Element Path",
            "acol1": "col-lg-6",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyElementNumericGreater",
            "value2": "Numeric value",
            "acol2": "col-lg-2",
            "value1": "Element Path",
            "acol1": "col-lg-6",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyElementNumericGreaterOrEqual",
            "value2": "Numeric value",
            "acol2": "col-lg-2",
            "value1": "Element Path",
            "acol1": "col-lg-6",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyElementNumericMinor",
            "value2": "Numeric value",
            "acol2": "col-lg-2",
            "value1": "Element Path",
            "acol1": "col-lg-6",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyElementNumericMinorOrEqual",
            "value2": "Numeric value",
            "acol2": "col-lg-2",
            "value1": "Element Path",
            "acol1": "col-lg-6",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyTextInPage",
            "value2": null,
            "value1": "Regex",
            "acol1": "col-lg-8",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyTextNotInPage",
            "value2": null,
            "value1": "Regex",
            "acol1": "col-lg-8",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyTitle",
            "value2": null,
            "value1": "Title",
            "acol1": "col-lg-6",
            "value3": "[opt] Case Sensitive (Y/N)",
            "acol3": "col-lg-2",
            "fatal": ""
        },
        {
            "type": "verifyUrl",
            "value2": null,
            "acol2": null,
            "value1": "URL",
            "acol1": "col-lg-8",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyTextInDialog",
            "value2": null,
            "value1": "Text",
            "acol1": "col-lg-8",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "verifyXmlTreeStructure",
            "value2": "Tree",
            "acol2": "col-lg-4",
            "value1": "XPath",
            "acol1": "col-lg-4",
            "value3": null,
            "fatal": ""
        },
        {
            "type": "takeScreenshot",
            "value2": null,
            "value1": "[opt] Crop values in pixels (left,right,top,bottom)",
            "acol1": "col-lg-8",
            "value3": null,
            "fatal": null
        },
        {"type": "getPageSource", "value2": null, "value1": null, "value3": null, "fatal": null}
    ]
};
