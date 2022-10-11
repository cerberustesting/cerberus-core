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

var conditionNewUIList = {
    "always":{"value":"always","label":{"en":"Always","fr":"Toujours"}},
    "ifPropertyExist":{"value":"ifPropertyExist","label":{"en":"If Property exists","fr":"Si la Propriété existe"},"field1":{"label":{"en": "Property name  (ex : PROP1)", "fr": "Propriété (ex : PROP1)"}}},
    "ifPropertyNotExist":{"value":"ifPropertyNotExist","label":{"en":"If Property not exists","fr":"Si la Propriété n'existe pas"},"field1":{"label":{"en": "Property name  (ex : PROP1)", "fr": "Propriété (ex : PROP1)"}}},

    "ifElementPresent":{"value":"ifElementPresent","label":{"en":"If Element Present","fr":"Si l'élément est présent"},"field1":{"label":{"en": "Element Path", "fr": "Chemin de l'élément"}}},
    "ifElementNotPresent":{"value":"ifElementNotPresent","label":{"en":"If Element not Present","fr":"Si l'élément n'est pas présent"},"field1":{"label":{"en": "Element Path", "fr": "Chemin de l'élément"}}},
    "ifElementVisible":{"value":"ifElementVisible","label":{"en":"If Element Visible","fr":"Si l'élément est visible'"},"field1":{"label":{"en": "Element Path", "fr": "Chemin de l'élément"}}},
    "ifElementNotVisible":{"value":"ifElementNotVisible","label":{"en":"If Element not Visible","fr":"Si l'élément n'est pas visible"},"field1":{"label":{"en": "Element Path", "fr": "Chemin de l'élément"}}},
    "ifTextInElement":{"value":"ifTextInElement","label":{"en":"If Text in Element","fr":"Si l'élément contient le texte"},"field1":{"label":{"en": "Element Path", "fr": "Chemin de l'élément"}},"field2":{"label":{"en": "Text", "fr": "Texte"}}},
    "ifTextNotInElement":{"value":"ifTextNotInElement","label":{"en":"If Text not in Element","fr":"Si l'élément ne contient pas le texte"},"field1":{"label":{"en": "Element Path", "fr": "Chemin de l'élément"}},"field2":{"label":{"en": "Text", "fr": "Texte"}}},

    "ifNumericEqual":{"value":"ifNumericEqual","label":{"en":"If Numeric Equals","fr":"Si la valeur numérique égale"},"field1":{"label":{"en": "Integer 1", "fr": "Entier 1"}},"field2":{"label":{"en": "Integer 2", "fr": "Entier 2"}}},
    "ifNumericDifferent":{"value":"ifNumericDifferent","label":{"en":"If Numeric Different","fr":"Si la valeur numérique est différente"},"field1":{"label":{"en": "Integer 1", "fr": "Entier 1"}},"field2":{"label":{"en": "Integer 2", "fr": "Entier 2"}}},
    "ifNumericGreater":{"value":"ifNumericGreater","label":{"en":"If Numeric Greater","fr":"Si la valeur numérique est supérieure"},"field1":{"label":{"en": "Integer 1", "fr": "Entier 1"}},"field2":{"label":{"en": "Integer 2", "fr": "Entier 2"}}},
    "ifNumericGreaterOrEqual":{"value":"ifNumericGreaterOrEqual","label":{"en":"If Numeric Greater or Equal","fr":"Si la valeur numérique est supérieure ou égale"},"field1":{"label":{"en": "Integer 1", "fr": "Entier 1"}},"field2":{"label":{"en": "Integer 2", "fr": "Entier 2"}}},
    "ifNumericMinor":{"value":"ifNumericMinor","label":{"en":"If Numeric Minor","fr":"Si la valeur numérique est inférieure"},"field1":{"label":{"en": "Integer 1", "fr": "Entier 1"}},"field2":{"label":{"en": "Integer 2", "fr": "Entier 2"}}},
    "ifNumericMinorOrEqual":{"value":"ifNumericMinorOrEqual","label":{"en":"If Numeric Minor or Equals","fr":"Si la valeur numérique est inférieure ou égale"},"field1":{"label":{"en": "Integer 1", "fr": "Entier 1"}},"field2":{"label":{"en": "Integer 2", "fr": "Entier 2"}}},

    "ifStringEqual":{"value":"ifStringEqual","label":{"en":"If String Equals","fr":"Si le texte est egal"},"field1":{"label":{"en": "String 1", "fr": "Texte 1"}},"field2":{"label":{"en": "String 2", "fr": "Texte 2"}}},
    "ifStringDifferent":{"value":"ifStringDifferent","label":{"en":"If String Different","fr":"Si le texte est différent"},"field1":{"label":{"en": "String 1", "fr": "Texte 1"}},"field2":{"label":{"en": "String 2", "fr": "Texte 2"}}},
    "ifStringGreater":{"value":"ifStringGreater","label":{"en":"If String Greater","fr":"Si le texte est supérieur"},"field1":{"label":{"en": "String 1", "fr": "Texte 1"}},"field2":{"label":{"en": "String 2", "fr": "Texte 2"}}},
    "ifStringMinor":{"value":"ifStringMinor","label":{"en":"If String Minor","fr":"Si le texte est inférieur"},"field1":{"label":{"en": "String 1", "fr": "Texte 1"}},"field2":{"label":{"en": "String 2", "fr": "Texte 2"}}},
    "ifStringContains":{"value":"ifStringContains","label":{"en":"If String Contains","fr":"Si le texte contient"},"field1":{"label":{"en": "String 1", "fr": "Texte 1"}},"field2":{"label":{"en": "String 2", "fr": "Texte 2"}}},
    "ifStringNotContains":{"value":"ifStringNotContains","label":{"en":"If String not Contains","fr":"Si le texte ne contient pas"},"field1":{"label":{"en": "String 1", "fr": "Texte 1"}},"field2":{"label":{"en": "String 2", "fr": "Texte 2"}}},
    "never":{"value":"never","label":{"en":"Never","fr":"Jamais"}},

}
