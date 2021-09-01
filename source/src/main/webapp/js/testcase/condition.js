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

var conditionUIList = {
    "fr": [
        {"type": "always", "object": null, "property": null, "condValue3": null},
        {"type": "ifPropertyExist", "object": "Propriété (ex : PROP1)", "property": null, "condValue3": null},
        {"type": "ifPropertyNotExist", "object": "Propriété (ex : PROP1)", "property": null, "condValue3": null},
        {"type": "ifElementPresent", "object": "Element", "property": null, "condValue3": null},
        {"type": "ifElementNotPresent", "object": "Element (ex : data-cerberus=fieldTest)", "property": null, "condValue3": null},
        {"type": "ifElementVisible", "object": "Element", "property": null, "condValue3": null},
        {"type": "ifElementNotVisible", "object": "Element (ex : data-cerberus=fieldTest)", "property": null, "condValue3": null},
        {"type": "ifTextInElement", "object": "Element", "property": "Texte", "condValue3": "[opt] Sensible à la Casse (Y/N)"},
        {"type": "ifTextNotInElement", "object": "Element", "property": "Texte", "condValue3": "[opt] Sensible à la Casse (Y/N)"},
        {"type": "ifNumericEqual", "object": "Integer1", "property": "Integer2", "condValue3": null},
        {"type": "ifNumericDifferent", "object": "Integer1", "property": "Integer2", "condValue3": null},
        {"type": "ifNumericGreater", "object": "Integer1 (ex : 20)", "property": "Integer2 (ex : 10)", "condValue3": null},
        {"type": "ifNumericGreaterOrEqual", "object": "Integer1 (ex : 20)", "property": "Integer2 (ex : 10)", "condValue3": null},
        {"type": "ifNumericMinor", "object": "Integer1 (ex : 10)", "property": "Integer2 (ex : 20)", "condValue3": null},
        {"type": "ifNumericMinorOrEqual", "object": "Integer1 (ex : 10)", "property": "Integer2 (ex : 20)", "condValue3": null},
        {"type": "ifStringEqual", "object": "String1", "property": "String2", "condValue3": "[opt] Sensible à la Casse (Y/N)"},
        {"type": "ifStringDifferent", "object": "String1", "property": "String2", "condValue3": "[opt] Sensible à la Casse (Y/N)"},
        {"type": "ifStringGreater", "object": "String1 (ex : ZZZ)", "property": "String2 (ex : AAA)", "condValue3": null},
        {"type": "ifStringMinor", "object": "String2 (ex : AAA)", "property": "String2 (ex : ZZZ)", "condValue3": null},
        {"type": "ifStringContains", "object": "String1 (ex : toto)", "property": "String2 (ex : ot)", "condValue3": "[opt] Sensible à la Casse (Y/N)"},
        {"type": "ifStringNotContains", "object": "String1 (ex : toto)", "property": "String2 (ex : zot)", "condValue3": "[opt] Sensible à la Casse (Y/N)"},
        {"type": "Never", "object": null, "property": null, "condValue3": null}
    ], "en": [
        {"type": "always", "object": null, "property": null, "condValue3": null},
        {"type": "ifPropertyExist", "object": "Property name  (ex : PROP1)", "property": null, "condValue3": null},
        {"type": "ifPropertyNotExist", "object": "Property name  (ex : PROP1)", "property": null, "condValue3": null},
        {"type": "ifElementPresent", "object": "Element", "property": null, "condValue3": null},
        {"type": "ifElementNotPresent", "object": "Element (ex : data-cerberus=fieldTest)", "property": null, "condValue3": null},
        {"type": "ifElementVisible", "object": "Element", "property": null, "condValue3": null},
        {"type": "ifElementNotVisible", "object": "Element (ex : data-cerberus=fieldTest)", "property": null, "condValue3": null},
        {"type": "ifTextInElement", "object": "Element", "property": "Text", "condValue3": "[opt] case sensitive (Y/N)"},
        {"type": "ifTextNotInElement", "object": "Element", "property": "Text", "condValue3": "[opt] case sensitive (Y/N)"},
        {"type": "ifNumericEqual", "object": "Integer1", "property": "Integer2", "condValue3": null},
        {"type": "ifNumericDifferent", "object": "Integer1", "property": "Integer2", "condValue3": null},
        {"type": "ifNumericGreater", "object": "Integer1 (ex : 20)", "property": "Integer2 (ex : 10)", "condValue3": null},
        {"type": "ifNumericGreaterOrEqual", "object": "Integer1 (ex : 20)", "property": "Integer2 (ex : 10)", "condValue3": null},
        {"type": "ifNumericMinor", "object": "Integer1 (ex : 10)", "property": "Integer2 (ex : 20)", "condValue3": null},
        {"type": "ifNumericMinorOrEqual", "object": "Integer1 (ex : 10)", "property": "Integer2 (ex : 20)", "condValue3": null},
        {"type": "ifStringEqual", "object": "String1", "property": "String2", "condValue3": "[opt] case sensitive (Y/N)"},
        {"type": "ifStringDifferent", "object": "String1", "property": "String2", "condValue3": "[opt] case sensitive (Y/N)"},
        {"type": "ifStringGreater", "object": "String1 (ex : ZZZ)", "property": "String2 (ex : AAA)", "condValue3": null},
        {"type": "ifStringMinor", "object": "String1 (ex : AAA)", "property": "String (ex : ZZZ)", "condValue3": null},
        {"type": "ifStringContains", "object": "String1 (ex : toto)", "property": "String2 (ex : ot)", "condValue3": "[opt] case sensitive (Y/N)"},
        {"type": "ifStringNotContains", "object": "String1 (ex : toto)", "property": "String2 (ex : zot)", "condValue3": "[opt] case sensitive (Y/N)"},
        {"type": "never", "object": null, "property": null, "condValue3": null}
    ]
};
