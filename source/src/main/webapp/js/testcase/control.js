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
var operatorOptList = {
    "unknown":{"value":"unknown","label":{"en":"---","fr":"---"},"control_type":["unknown","title","url","text_in_page","text_not_in_page","text_in_dialog","take_screenshot","get_page_source"]},
    "equals":{"value":"equals","label":{"en":"=","fr":"="},"control_type":["string_comparison","numeric_comparison","element","element_text","element_numeric"]},
    "different":{"value":"different","label":{"en":"!=","fr":"!="},"control_type":["string_comparison","numeric_comparison","element","element_numeric","element_text"]},
    "greater":{"value":"greater","label":{"en":">","fr":">"},"control_type":["string_comparison","numeric_comparison","element_numeric"]},
    "greaterOrEqual":{"value":"greaterOrEqual","label":{"en":">=","fr":">="},"control_type":["numeric_comparison","element_numeric"]},
    "minor":{"value":"minor","label":{"en":"<","fr":"<"},"control_type":["string_comparison","numeric_comparison","element_numeric"]},
    "minorOrEqual":{"value":"minorOrEqual","label":{"en":"<=","fr":"<="},"control_type":["numeric_comparison","element_numeric"]},
    "contains":{"value":"contains","label":{"en":"Contains","fr":"Contient"},"control_type":["string_comparison","element_text"]},
    "notContains":{"value":"notContains","label":{"en":"Not Contains","fr":"Ne Contient Pas"},"control_type":["string_comparison"]},
    "matchRegex":{"value":"matchRegex","label":{"en":"Match Regex","fr":"Match Regex"},"control_type":["element_text"]},
    "isPresent":{"value":"isPresent","label":{"en":"isPresent","fr":"isPresent"},"control_type":["element"]},
    "isNotPresent":{"value":"isNotPresent","label":{"en":"isNotPresent","fr":"isNotPresent"},"control_type":["element"]},
    "isVisible":{"value":"isVisible","label":{"en":"isVisible","fr":"isVisible"},"control_type":["element"]},
    "isNotVisible":{"value":"isNotVisible","label":{"en":"isNotVisible","fr":"isNotVisible"},"control_type":["element"]},
    "isClickable":{"value":"isClickable","label":{"en":"isClickable","fr":"isClickable"},"control_type":["element"]},
    "isNotClickable":{"value":"isNotClickable","label":{"en":"isNotClickable","fr":"isNotClickable"},"control_type":["element"]},
    "isArrayString":{"value":"isArrayString","label":{"en":"isArrayString","fr":"isArrayString"},"control_type":["array"]},
    "isArrayNumeric":{"value":"isArrayNumeric","label":{"en":"isArrayNumeric","fr":"isArrayNumeric"},"control_type":["array"]},
    "isElementArrayString":{"value":"isElementArrayString","label":{"en":"isElementArrayString","fr":"isElementArrayString"},"control_type":["array"]},
    "isElementArrayNumeric":{"value":"isElementArrayNumeric","label":{"en":"isElementArrayNumeric","fr":"isElementArrayNumeric"},"control_type":["array"]}
};



var convertToGui = {
    "unknown":{"control":"unknown","operator":"unknown"},

    "verifyStringEquals":{"control":"string_comparison","operator":"equals","field1":{"label":{"en": "String 1", "fr": "Chaine 1"},"picto":"images/action-font.png","class": "col-lg-5"},"field2":{"label":{"en": "String 2", "fr": "Chaine 2"},"picto":"images/action-font.png","class": "col-lg-5"},"field3":{"label":{"en": "Case sensitive (Y/N)", "fr": "[opt] Sensible à la Casse (Y/N)"},"picto":"images/action-vote.png","class": "col-lg-5"}},
    "verifyStringDifferent":{"control":"string_comparison","operator":"different","field1":{"label":{"en": "String 1", "fr": "Chaine 1"},"picto":"images/action-font.png","class": "col-lg-5"},"field2":{"label":{"en": "String 2", "fr": "Chaine 2"},"picto":"images/action-font.png","class": "col-lg-5"},"field3":{"label":{"en": "Case sensitive (Y/N)", "fr": "[opt] Sensible à la Casse (Y/N)"},"picto":"images/action-vote.png","class": "col-lg-5"}},
    "verifyStringGreater":{"control":"string_comparison","operator":"greater","field1":{"label":{"en": "String 1", "fr": "Chaine 1"},"picto":"images/action-font.png","class": "col-lg-5"},"field2":{"label":{"en": "String 2", "fr": "Chaine 2"},"picto":"images/action-font.png","class": "col-lg-5"},"field3":{"label":{"en": "Case sensitive (Y/N)", "fr": "[opt] Sensible à la Casse (Y/N)"},"picto":"images/action-vote.png","class": "col-lg-5"}},
    "verifyStringMinor":{"control":"string_comparison","operator":"minor","field1":{"label":{"en": "String 1", "fr": "Chaine 1"},"picto":"images/action-font.png","class": "col-lg-5"},"field2":{"label":{"en": "String 2", "fr": "Chaine 2"},"picto":"images/action-font.png","class": "col-lg-5"}},
    "verifyStringContains":{"control":"string_comparison","operator":"contains","field1":{"label":{"en": "String 1 (ex : toto)", "fr": "Chaine 1 (ex : toto)"},"picto":"images/action-font.png","class": "col-lg-5"},"field2":{"label":{"en": "String 2 (ex : ot)", "fr": "Chaine 2 (ex : ot)"},"picto":"images/action-font.png","class": "col-lg-5"}},
    "verifyStringNotContains":{"control":"string_comparison","operator":"notContains","field1":{"label":{"en": "String 1 (ex : toto)", "fr": "Chaine 1 (ex : toto)"},"picto":"images/action-font.png","class": "col-lg-5"},"field2":{"label":{"en": "String 2 (ex : zot)", "fr": "Chaine 2 (ex : zot)"},"picto":"images/action-font.png","class": "col-lg-5"}},

    "verifyNumericEquals":{"control":"numeric_comparison","operator":"equals","field1":{"label":{"en": "Integer 1 (ex : 1234)", "fr": "Entier 1 (ex : 1234)"},"picto":"images/action-numeric.png","class": "col-lg-5"},"field2":{"label":{"en": "Integer 2 (ex : 1234)", "fr": "Entier 2 (ex : 1234)"},"picto":"images/action-numeric.png","class": "col-lg-5"}},
    "verifyNumericDifferent":{"control":"numeric_comparison","operator":"different","field1":{"label":{"en": "Integer 1 (ex : 1234)", "fr": "Entier 1 (ex : 123)"},"picto":"images/action-numeric.png","class": "col-lg-5"},"field2":{"label":{"en": "Integer 2 (ex : 1234)", "fr": "Entier 2 (ex : 123)"},"picto":"images/action-numeric.png","class": "col-lg-5"}},
    "verifyNumericGreater":{"control":"numeric_comparison","operator":"greater","field1":{"label":{"en": "Integer 1 (ex : 12)", "fr": "Entier 1 (ex : 12)"},"picto":"images/action-numeric.png","class": "col-lg-5"},"field2":{"label":{"en": "Integer 2 (ex : 10)", "fr": "Entier 2 (ex : 10)"},"picto":"images/action-numeric.png","class": "col-lg-5"}},
    "verifyNumericGreaterOrEqual":{"control":"numeric_comparison","operator":"greaterOrEqual","field1":{"label":{"en": "Integer 1 (ex : 12)", "fr": "Entier 1 (ex : 12)"},"picto":"images/action-numeric.png","class": "col-lg-5"},"field2":{"label":{"en": "Integer 2 (ex : 10)", "fr": "Entier 2 (ex : 10)"},"picto":"images/action-numeric.png","class": "col-lg-5"}},
    "verifyNumericMinor":{"control":"numeric_comparison","operator":"minor","field1":{"label":{"en": "Integer 1 (ex : 10)", "fr": "Entier 1 (ex : 10)"},"picto":"images/action-numeric.png","class": "col-lg-5"},"field2":{"label":{"en": "Integer 2 (ex : 12)", "fr": "Entier 2 (ex : 12)"},"picto":"images/action-numeric.png","class": "col-lg-5"}},
    "verifyNumericMinorOrEqual":{"control":"numeric_comparison","operator":"minorOrEqual","field1":{"label":{"en": "Integer 1 (ex : 10)", "fr": "Entier 1 (ex : 10)"},"picto":"images/action-numeric.png","class": "col-lg-5"},"field2":{"label":{"en": "Integer 2 (ex : 12)", "fr": "Entier 2 (ex : 12)"},"picto":"images/action-numeric.png","class": "col-lg-5"}},

    "verifyElementPresent":{"control":"element","operator":"isPresent","field1":{"label":{"en": "Element (ex : xpath=/path/to/element)", "fr": "Element (ex : xpath=/path/to/element)"},"picto":"images/action-html.png","class": "col-lg-8"}},
    "verifyElementNotPresent":{"control":"element","operator":"isNotPresent","field1":{"label":{"en": "Element (ex : xpath=/path/to/element)", "fr": "Element (ex : xpath=/path/to/element)"},"picto":"images/action-html.png","class": "col-lg-8"}},
    "verifyElementVisible":{"control":"element","operator":"isVisible","field1":{"label":{"en": "Element (ex : xpath=/path/to/element)", "fr": "Element (ex : xpath=/path/to/element)"},"picto":"images/action-html.png","class": "col-lg-8"}},
    "verifyElementNotVisible":{"control":"element","operator":"isNotVisible","field1":{"label":{"en": "Element (ex : xpath=/path/to/element)", "fr": "Element (ex : xpath=/path/to/element)"},"picto":"images/action-html.png","class": "col-lg-8"}},
    "verifyElementClickable":{"control":"element","operator":"isClickable","field1":{"label":{"en": "Element (ex : xpath=/path/to/element)", "fr": "Element (ex : xpath=/path/to/element)"},"picto":"images/action-html.png","class": "col-lg-8"}},
    "verifyElementNotClickable":{"control":"element","operator":"isNotClickable","field1":{"label":{"en": "Element (ex : xpath=/path/to/element)", "fr": "Element (ex : xpath=/path/to/element)"},"picto":"images/action-html.png","class": "col-lg-8"}},
    "verifyElementEquals":{"control":"element","operator":"equals","field1":{"label":{"en": "Path of the element", "fr": "Chemin de l'élément"},"picto":"images/action-settings.png","class": "col-lg-5"},"field2":{"label":{"en": "Expected element", "fr": "Element"},"picto":"images/action-html.png","class": "col-lg-5"},"field3":{"label":{"en": "Case sensitive (Y/N)", "fr": "[opt] Sensible à la Casse (Y/N)"},"picto":"images/action-vote.png","class": "col-lg-5"}},
    "verifyElementDifferent":{"control":"element","operator":"different","field1":{"label":{"en": "Path of the element", "fr": "Chemin de l'élément"},"picto":"images/action-settings.png","class": "col-lg-5"},"field2":{"label":{"en": "Expected element", "fr": "Element"},"picto":"images/action-html.png","class": "col-lg-5"},"field3":{"label":{"en": "Case sensitive (Y/N)", "fr": "[opt] Sensible à la Casse (Y/N)"},"picto":"images/action-vote.png","class": "col-lg-5"}},


    "verifyStringArrayContains":{"control":"array","operator":"isArrayString","field1":{"label":{"en": "Array (ex : [\"a\",\"b\",\"c\"])", "fr": "Tableau (ex : [\"a\",\"b\",\"c\"])"},"picto":"images/action-font.png","class": "col-lg-5"},"field2":{"label":{"en": "Text (ex: a)", "fr": "Texte (ex: a)"},"picto":"images/action-font.png","class": "col-lg-5"},"field3":{"label":{"en": "Case sensitive (Y/N)", "fr": "[opt] Sensible à la Casse (Y/N)"},"picto":"images/action-vote.png","class": "col-lg-5"}},
    "verifyNumericArrayContains":{"control":"array","operator":"isArrayNumeric","field1":{"label":{"en": "Array (ex : [1,2,3])", "fr": "Tableau (ex : [1,2,3])"},"picto":"images/action-font.png","class": "col-lg-5"},"field2":{"label":{"en": "Integer (ex: 1)", "fr": "Entier (ex: 1)"},"picto":"images/action-numeric.png","class": "col-lg-5"}},
    "verifyElementTextArrayContains":{"control":"array","operator":"isElementArrayString","field1":{"label":{"en": "Element (ex : xpath=/path/to/element)", "fr": "Element (ex : xpath=/chemin/vers/element)"},"picto":"images/action-html.png","class": "col-lg-5"},"field2":{"label":{"en": "Text (ex: a)", "fr": "Texte (ex: a)"},"picto":"images/action-font.png","class": "col-lg-5"},"field3":{"label":{"en": "Case sensitive (Y/N)", "fr": "[opt] Sensible à la Casse (Y/N)"},"picto":"images/action-vote.png","class": "col-lg-5"}},
    "verifyElementNumericArrayContains":{"control":"array","operator":"isElementArrayNumeric","field1":{"label":{"en": "Element (ex : xpath=/path/to/element)", "fr": "Element (ex : xpath=/chemin/vers/element)"},"picto":"images/action-html.png","class": "col-lg-5"},"field2":{"label":{"en": "Integer (ex: 1)", "fr": "Entier (ex: 1)"},"picto":"images/action-font.png","class": "col-lg-5"}},

    "verifyElementTextEqual":{"control":"element_text","operator":"equals","field1":{"label":{"en": "Element (ex : xpath=/path/to/element)", "fr": "Element (ex : xpath=/chemin/vers/element)"},"picto":"images/action-html.png","class": "col-lg-5"},"field2":{"label":{"en": "Text", "fr": "Texte"},"picto":"images/action-font.png","class": "col-lg-5"},"field3":{"label":{"en": "Case sensitive (Y/N)", "fr": "[opt] Sensible à la Casse (Y/N)"},"picto":"images/action-vote.png","class": "col-lg-5"}},
    "verifyElementTextDifferent":{"control":"element_text","operator":"different","field1":{"label":{"en": "Element (ex : xpath=/path/to/element)", "fr": "Element (ex : xpath=/chemin/vers/element)"},"picto":"images/action-html.png","class": "col-lg-5"},"field2":{"label":{"en": "Text", "fr": "Texte"},"picto":"images/action-font.png","class": "col-lg-5"},"field3":{"label":{"en": "Case sensitive (Y/N)", "fr": "[opt] Sensible à la Casse (Y/N)"},"picto":"images/action-vote.png","class": "col-lg-5"}},
    "verifyElementTextContains":{"control":"element_text","operator":"contains","field1":{"label":{"en": "Element (ex : xpath=/path/to/element)", "fr": "Element (ex : xpath=/chemin/vers/element)"},"picto":"images/action-html.png","class": "col-lg-5"},"field2":{"label":{"en": "Text", "fr": "Texte"},"picto":"images/action-font.png","class": "col-lg-5"},"field3":{"label":{"en": "Case sensitive (Y/N)", "fr": "[opt] Sensible à la Casse (Y/N)"},"picto":"images/action-vote.png","class": "col-lg-5"}},
    "verifyElementTextMatchRegex":{"control":"element_text","operator":"matchRegex","field1":{"label":{"en": "Element (ex : xpath=/path/to/element)", "fr": "Element (ex : xpath=/chemin/vers/element)"},"picto":"images/action-html.png","class": "col-lg-5"},"field2":{"label":{"en": "Regex", "fr": "Regex"},"picto":"images/action-font.png","class": "col-lg-5"}},

    "verifyElementNumericEqual":{"control":"element_numeric","operator":"equals","field1":{"label":{"en": "Element (ex : xpath=/path/to/element)", "fr": "Element (ex : xpath=/chemin/vers/element)"},"picto":"images/action-html.png","class": "col-lg-5"},"field2":{"label":{"en": "Numeric", "fr": "Valeur Numérique"},"picto":"images/action-numeric.png","class": "col-lg-5"}},
    "verifyElementNumericDifferent":{"control":"element_numeric","operator":"different","field1":{"label":{"en": "Element (ex : xpath=/path/to/element)", "fr": "Element (ex : xpath=/chemin/vers/element)"},"picto":"images/action-html.png","class": "col-lg-5"},"field2":{"label":{"en": "Numeric", "fr": "Valeur Numérique"},"picto":"images/action-numeric.png","class": "col-lg-5"}},
    "verifyElementNumericGreater":{"control":"element_numeric","operator":"greater","field1":{"label":{"en": "Element (ex : xpath=/path/to/element)", "fr": "Element (ex : xpath=/chemin/vers/element)"},"picto":"images/action-html.png","class": "col-lg-5"},"field2":{"label":{"en": "Numeric", "fr": "Valeur Numérique"},"picto":"images/action-numeric.png","class": "col-lg-5"}},
    "verifyElementNumericGreaterOrEqual":{"control":"element_numeric","operator":"greaterOrEqual","field1":{"label":{"en": "Element (ex : xpath=/path/to/element)", "fr": "Element (ex : xpath=/chemin/vers/element)"},"picto":"images/action-html.png","class": "col-lg-5"},"field2":{"label":{"en": "Numeric", "fr": "Valeur Numérique"},"picto":"images/action-numeric.png","class": "col-lg-5"}},
    "verifyElementNumericMinor":{"control":"element_numeric","operator":"minor","field1":{"label":{"en": "Element (ex : xpath=/path/to/element)", "fr": "Element (ex : xpath=/chemin/vers/element)"},"picto":"images/action-html.png","class": "col-lg-5"},"field2":{"label":{"en": "Numeric", "fr": "Valeur Numérique"},"picto":"images/action-numeric.png","class": "col-lg-5"}},
    "verifyElementNumericMinorOrEqual":{"control":"element_numeric","operator":"minorOrEqual","field1":{"label":{"en": "Element (ex : xpath=/path/to/element)", "fr": "Element (ex : xpath=/chemin/vers/element)"},"picto":"images/action-html.png","class": "col-lg-5"},"field2":{"label":{"en": "Numeric", "fr": "Valeur Numérique"},"picto":"images/action-numeric.png","class": "col-lg-5"}},

    "verifyTitle":{"control":"title","operator":"unknown","field1":{"label":{"en": "Title (ex : welcome to site)", "fr": "Titre (ex : welcome to site)"},"picto":"images/action-font.png","class": "col-lg-12"}},
    "verifyUrl":{"control":"url","operator":"unknown","field1":{"label":{"en": "URL", "fr": "URL"},"picto":"images/action-link.png","class": "col-lg-12"}},
    "verifyTextInPage":{"control":"text_in_page","operator":"unknown","field1":{"label":{"en": "Text to find", "fr": "Texte à rechercher"},"picto":"images/action-font.png","class": "col-lg-12"}},
    "verifyTextNotInPage":{"control":"text_not_in_page","operator":"unknown","field1":{"label":{"en": "Text not to find)", "fr": "Texte à ne pas trouver"},"picto":"images/action-font.png","class": "col-lg-12"}},
    "verifyTextInDialog":{"control":"text_in_dialog","operator":"unknown","field1":{"label":{"en": "Text to find", "fr": "Texte à rechercher"},"picto":"images/action-font.png","class": "col-lg-12"}},
    "takeScreenshot":{"control":"take_screenshot","operator":"unknown","field1":{"label":{"en": "Crop values in pixels (left,right,top,bottom)", "fr": "Cadrer en pixel (gauche,droite,haut,bas)"},"picto":"images/action-font.png","class": "col-lg-12"}},
    "getPageSource":{"control":"get_page_source","operator":"unknown"}
};

var newControlOptList = {
    "unknown":{"value":"unknown","label":{"en":"Define a control","fr":"Choisir un control"},"application_types":["GUI","SRV","IPA","APK","BAT","FAT","NONE"]},
    "string_comparison":{"value":"string_comparison","label":{"en":"String Comparison","fr":"Comparaison de Textes"},"application_types":["GUI","SRV","IPA","APK","BAT","FAT","NONE"],"equals":"verifyStringEquals","different":"verifyStringDifferent","greater":"verifyStringGreater","minor":"verifyStringMinor", "contains":"verifyStringContains", "notContains":"verifyStringNotContains" },
    "numeric_comparison":{"value":"numeric_comparison","label":{"en":"Numeric Comparison","fr":"Comparaison de Numériques"},"application_types":["GUI","SRV","IPA","APK","BAT","FAT","NONE"],"equals":"verifyNumericEquals","different":"verifyNumericDifferent","greater":"verifyNumericGreater","greaterOrEqual":"verifyNumericGreaterOrEqual","minor":"verifyNumericMinor", "minorOrEqual":"verifyNumericMinorOrEqual"},
    "element":{"value":"element","label":{"en":"Verify Element","fr":"Vérifier l'élément"},"application_types":["GUI","SRV","IPA","APK","BAT","FAT","NONE"],"isPresent":"verifyElementPresent","isNotPresent":"verifyElementNotPresent","isVisible":"verifyElementVisible","isNotVisible":"verifyElementNotVisible","isClickable":"verifyElementClickable","isNotClickable":"verifyElementNotClickable","equals":"verifyElementEquals", "different":"verifyElementDifferent"},
    "element_text":{"value":"element_text","label":{"en":"Verify Text in Element","fr":"Vérifier le texte de l'élément"},"application_types":["GUI","SRV","IPA","APK","BAT","FAT","NONE"],"equals":"verifyElementTextEqual","matchRegex":"verifyElementTextMatchRegex","different":"verifyElementTextDifferent", "contains":"verifyElementTextContains"},
    "element_numeric":{"value":"element_numeric","label":{"en":"Verify Numeric in Element","fr":"Vérifier la valeur numérique de l'élément"},"application_types":["GUI","SRV","IPA","APK","BAT","FAT","NONE"],"equals":"verifyElementNumericEqual","different":"verifyElementNumericDifferent","greater":"verifyElementNumericGreater","greaterOrEqual":"verifyElementNumericGreaterOrEqual","minor":"verifyElementNumericMinor", "minorOrEqual":"verifyElementNumericMinorOrEqual"},
    "array":{"value":"array","label":{"en":"Verify Array Content","fr":"Vérifier le contenu d'un tableau"},"application_types":["GUI","SRV","IPA","APK","BAT","FAT","NONE"],"isArrayString":"verifyStringArrayContains","isArrayNumeric":"verifyNumericArrayContains","isElementArrayString":"verifyElementTextArrayContains", "isElementArrayNumeric":"verifyElementNumericArrayContains"},
    "title":{"value":"title","label":{"en":"Verify Title","fr":"Vérifier la balise title"},"application_types":["GUI"],"unknown":"verifyTitle"},
    "url":{"value":"url","label":{"en":"Verify URL","fr":"Vérifier l'URL"},"application_types":["GUI"],"unknown":"verifyUrl"},
    "text_in_page":{"value":"text_in_page","label":{"en":"Verify Text in Page","fr":"Vérifier que la page contient le texte"},"application_types":["GUI"],"unknown":"verifyTextInPage"},
    "text_not_in_page":{"value":"text_not_in_page","label":{"en":"Verify Text not in Page","fr":"Vérifier que la page ne contient pas le texte"},"application_types":["GUI"],"unknown":"verifyTextNotInPage"},
    "text_in_dialog":{"value":"text_in_dialog","label":{"en":"Verify Text in Dialog","fr":"Vérifier que la popup contient"},"application_types":["GUI"],"unknown":"verifyTextInDialog"},
    "take_screenshot":{"value":"take_screenshot","label":{"en":"Take a Screenshot","fr":"Prendre un Screenshot"},"application_types":["GUI"],"unknown":"takeScreenshot"},
    "get_page_source":{"value":"get_page_source","label":{"en":"Record Page Source","fr":"Enregistrer les sources de la page"},"application_types":["GUI"],"unknown":"getPageSource"}
};


var controlOptList = {
    "unknown":{"value":"unknown","label":{"en":"Define a control","fr":"Choisir un control"},"application_types":["GUI","SRV","IPA","APK","BAT","FAT","NONE"]},
    "string_comparison":{"value":"string_comparison","label":{"en":"String Comparison","fr":"Comparaison de Chaine de Caractère"},"application_types":["GUI","SRV","IPA","APK","BAT","FAT","NONE"],"equals":"verifyStringEquals","different":"verifyStringDifferent","greater":"verifyStringGreater","minor":"verifyStringMinor"}
};

