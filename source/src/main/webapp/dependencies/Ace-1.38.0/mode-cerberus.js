ace.define("ace/mode/cerberus_highlight_rules", ["require", "exports", "module", "ace/lib/oop", "ace/mode/text_highlight_rules"], function (require, exports, module) {
    "use strict";

    var oop = require("../lib/oop");
    var TextHighlightRules = require("./text_highlight_rules").TextHighlightRules;

    var cerberusHighlightRules = function () {
        //default autocomplete
        var keywordMapper = this.createKeywordMapper({
            "tag": ""
        }, "identifier");
        //regex rule for number (can be deleted)
        this.$rules = {
            "start": [{
                    token: "constant.numeric", // hex| remove later ?
                    regex: "0[xX][0-9a-fA-F]+(L|l|UL|ul|u|U|F|f|ll|LL|ull|ULL)?\\b"
                }, {
                    token: "constant.numeric", // float| remove later ?
                    regex: "[+-]?\\d+(?:(?:\\.\\d*)?(?:[eE][+-]?\\d+)?)?(L|l|UL|ul|u|U|F|f|ll|LL|ull|ULL)?\\b"
                }]
        };

        var k = ["property", "system"];
        this.$rules["start"].push({
            token: "keyword",
            regex: "%" + createRegexHightlight(k) + ".",
            next: "p2-1"
        });
        this.$rules["p2-1"] = [];
        this.$rules["p2-1"].push({
            token: "keyword",
            regex: "[A-Za-z1-9]+" + "%",
            next: "start"
        });

        this.$rules["start"].push({
            token: "keyword",
            regex: "%" + "object" + ".",
            next: "p2-2"
        });
        this.$rules["p2-2"] = [];
        this.$rules["p2-2"].push({
            token: "keyword",
            regex: "[A-Za-z1-9]+" + "." + "[A-Za-z1-9]+" + "%",
            next: "start"
        });
    }
    oop.inherits(cerberusHighlightRules, TextHighlightRules);
    exports.cerberusHighlightRules = cerberusHighlightRules;
});

function createRegexHightlight(tab) {
    var regexString = "(?:";
    var firstLoop = true;
    for (var i in tab) {
        if (firstLoop) {
            firstLoop = false;
        } else {
            regexString += "|"
        }
        regexString += tab[i];
    }
    regexString += ")";

    return regexString;
}

ace.define("ace/mode/behaviour/cerberus", ["require", "exports", "module", "ace/lib/oop", "ace/mode/behaviour", "ace/token_iterator"], function (require, exports, module) {
    "use strict";

    var oop = require("../../lib/oop");
    var Behaviour = require("../behaviour").Behaviour;
    var TokenIterator = require("../../token_iterator").TokenIterator;

    function is(token, type) {
        return token.type.lastIndexOf(type + ".cerberus") > -1;
    }

    var cerberusBehaviour = function () {

        this.add("string_dquotes", "insertion", function (state, action, editor, session, text) {
            if (text == '"' || text == "'") {
                var quote = text;
                var selected = session.doc.getTextRange(editor.getSelectionRange());
                if (selected !== "" && selected !== "'" && selected != '"' && editor.getWrapBehavioursEnabled()) {
                    return {
                        text: quote + selected + quote,
                        selection: false
                    };
                }

                var cursor = editor.getCursorPosition();
                var line = session.doc.getLine(cursor.row);
                var rightChar = line.substring(cursor.column, cursor.column + 1);
                var iterator = new TokenIterator(session, cursor.row, cursor.column);
                var token = iterator.getCurrentToken();

                if (rightChar == quote && (is(token, "attribute-value") || is(token, "string"))) {
                    return {
                        text: "",
                        selection: [1, 1]
                    };
                }

                if (!token)
                    token = iterator.stepBackward();

                if (!token)
                    return;

                while (is(token, "tag-whitespace") || is(token, "whitespace")) {
                    token = iterator.stepBackward();
                }
                var rightSpace = !rightChar || rightChar.match(/\s/);
                if (is(token, "attribute-equals") && (rightSpace || rightChar == '>') || (is(token, "decl-attribute-equals") && (rightSpace || rightChar == '?'))) {
                    return {
                        text: quote + quote,
                        selection: [1, 1]
                    };
                }
            }
        });

        this.add("string_dquotes", "deletion", function (state, action, editor, session, range) {
            var selected = session.doc.getTextRange(range);
            if (!range.isMultiLine() && (selected == '"' || selected == "'")) {
                var line = session.doc.getLine(range.start.row);
                var rightChar = line.substring(range.start.column + 1, range.start.column + 2);
                if (rightChar == selected) {
                    range.end.column++;
                    return range;
                }
            }
        });

        this.add("autoclosing", "insertion", function (state, action, editor, session, text) {
            if (text == '>') {
                var position = editor.getCursorPosition();
                var iterator = new TokenIterator(session, position.row, position.column);
                var token = iterator.getCurrentToken() || iterator.stepBackward();
                if (!token || !(is(token, "tag-name") || is(token, "tag-whitespace") || is(token, "attribute-name") || is(token, "attribute-equals") || is(token, "attribute-value")))
                    return;
                if (is(token, "reference.attribute-value"))
                    return;
                if (is(token, "attribute-value")) {
                    var firstChar = token.value.charAt(0);
                    if (firstChar == '"' || firstChar == "'") {
                        var lastChar = token.value.charAt(token.value.length - 1);
                        var tokenEnd = iterator.getCurrentTokenColumn() + token.value.length;
                        if (tokenEnd > position.column || tokenEnd == position.column && firstChar != lastChar)
                            return;
                    }
                }
                while (!is(token, "tag-name")) {
                    token = iterator.stepBackward();
                }

                var tokenRow = iterator.getCurrentTokenRow();
                var tokenColumn = iterator.getCurrentTokenColumn();
                if (is(iterator.stepBackward(), "end-tag-open"))
                    return;

                var element = token.value;
                if (tokenRow == position.row)
                    element = element.substring(0, position.column - tokenColumn);

                if (this.voidElements.hasOwnProperty(element.toLowerCase()))
                    return;

                return {
                    text: '>' + '</' + element + '>',
                    selection: [1, 1]
                };
            }
        });

        this.add('autoindent', 'insertion', function (state, action, editor, session, text) {
            if (text == "\n") {
                var cursor = editor.getCursorPosition();
                var line = session.getLine(cursor.row);
                var rightChars = line.substring(cursor.column, cursor.column + 2);
                if (rightChars == '</') {
                    var next_indent = this.$getIndent(line);
                    var indent = next_indent + session.getTabString();

                    return {
                        text: '\n' + indent + '\n' + next_indent,
                        selection: [1, indent.length, 1, indent.length]
                    };
                }
            }
        });

    };

    oop.inherits(cerberusBehaviour, Behaviour);

    exports.cerberusBehaviour = cerberusBehaviour;
});

ace.define("ace/mode/folding/cerberus", ["require", "exports", "module", "ace/lib/oop", "ace/lib/lang", "ace/range", "ace/mode/folding/fold_mode", "ace/token_iterator"], function (require, exports, module) {
    "use strict";

    var oop = require("../../lib/oop");
    var lang = require("../../lib/lang");
    var Range = require("../../range").Range;
    var BaseFoldMode = require("./fold_mode").FoldMode;
    var TokenIterator = require("../../token_iterator").TokenIterator;

    var FoldMode = exports.FoldMode = function (voidElements, optionalEndTags) {
        BaseFoldMode.call(this);
        this.voidElements = oop.mixin(voidElements || {}, optionalEndTags || {});
    };

    oop.inherits(FoldMode, BaseFoldMode);

    var Tag = function () {
        this.tagName = "";
        this.closing = false;
        this.selfClosing = false;
        this.start = {row: 0, column: 0};
        this.end = {row: 0, column: 0};
    };

    function is(token, type) {
        return token.type.lastIndexOf(type + ".cerberus") > -1;
    }

//funtion needed to navigate inside ace
    (function () {
        this.getFoldWidget = function (session, foldStyle, row) {
            var tag = this._getFirstTagInLine(session, row);

            if (!tag)
                return "";

            if (tag.closing || (!tag.tagName && tag.selfClosing))
                return foldStyle == "markbeginend" ? "end" : "";

            if (!tag.tagName || tag.selfClosing || this.voidElements.hasOwnProperty(tag.tagName.toLowerCase()))
                return "";

            if (this._findEndTagInLine(session, row, tag.tagName, tag.end.column))
                return "";

            return "start";
        };
        this._getFirstTagInLine = function (session, row) {
            var tokens = session.getTokens(row);
            var tag = new Tag();

            for (var i = 0; i < tokens.length; i++) {
                var token = tokens[i];
                if (is(token, "tag-open")) {
                    tag.end.column = tag.start.column + token.value.length;
                    tag.closing = is(token, "end-tag-open");
                    token = tokens[++i];
                    if (!token)
                        return null;
                    tag.tagName = token.value;
                    tag.end.column += token.value.length;
                    for (i++; i < tokens.length; i++) {
                        token = tokens[i];
                        tag.end.column += token.value.length;
                        if (is(token, "tag-close")) {
                            tag.selfClosing = token.value == '/>';
                            break;
                        }
                    }
                    return tag;
                } else if (is(token, "tag-close")) {
                    tag.selfClosing = token.value == '/>';
                    return tag;
                }
                tag.start.column += token.value.length;
            }

            return null;
        };
        this._findEndTagInLine = function (session, row, tagName, startColumn) {
            var tokens = session.getTokens(row);
            var column = 0;
            for (var i = 0; i < tokens.length; i++) {
                var token = tokens[i];
                column += token.value.length;
                if (column < startColumn)
                    continue;
                if (is(token, "end-tag-open")) {
                    token = tokens[i + 1];
                    if (token && token.value == tagName)
                        return true;
                }
            }
            return false;
        };
        this._readTagForward = function (iterator) {
            var token = iterator.getCurrentToken();
            if (!token)
                return null;

            var tag = new Tag();
            do {
                if (is(token, "tag-open")) {
                    tag.closing = is(token, "end-tag-open");
                    tag.start.row = iterator.getCurrentTokenRow();
                    tag.start.column = iterator.getCurrentTokenColumn();
                } else if (is(token, "tag-name")) {
                    tag.tagName = token.value;
                } else if (is(token, "tag-close")) {
                    tag.selfClosing = token.value == "/>";
                    tag.end.row = iterator.getCurrentTokenRow();
                    tag.end.column = iterator.getCurrentTokenColumn() + token.value.length;
                    iterator.stepForward();
                    return tag;
                }
            } while (token = iterator.stepForward());

            return null;
        };
        this._readTagBackward = function (iterator) {
            var token = iterator.getCurrentToken();
            if (!token)
                return null;

            var tag = new Tag();
            do {
                if (is(token, "tag-open")) {
                    tag.closing = is(token, "end-tag-open");
                    tag.start.row = iterator.getCurrentTokenRow();
                    tag.start.column = iterator.getCurrentTokenColumn();
                    iterator.stepBackward();
                    return tag;
                } else if (is(token, "tag-name")) {
                    tag.tagName = token.value;
                } else if (is(token, "tag-close")) {
                    tag.selfClosing = token.value == "/>";
                    tag.end.row = iterator.getCurrentTokenRow();
                    tag.end.column = iterator.getCurrentTokenColumn() + token.value.length;
                }
            } while (token = iterator.stepBackward());

            return null;
        };
        this._pop = function (stack, tag) {
            while (stack.length) {

                var top = stack[stack.length - 1];
                if (!tag || top.tagName == tag.tagName) {
                    return stack.pop();
                } else if (this.voidElements.hasOwnProperty(tag.tagName)) {
                    return;
                } else if (this.voidElements.hasOwnProperty(top.tagName)) {
                    stack.pop();
                    continue;
                } else {
                    return null;
                }
            }
        };
        this.getFoldWidgetRange = function (session, foldStyle, row) {
            var firstTag = this._getFirstTagInLine(session, row);

            if (!firstTag)
                return null;

            var isBackward = firstTag.closing || firstTag.selfClosing;
            var stack = [];
            var tag;

            if (!isBackward) {
                var iterator = new TokenIterator(session, row, firstTag.start.column);
                var start = {
                    row: row,
                    column: firstTag.start.column + firstTag.tagName.length + 2
                };
                while (tag = this._readTagForward(iterator)) {
                    if (tag.selfClosing) {
                        if (!stack.length) {
                            tag.start.column += tag.tagName.length + 2;
                            tag.end.column -= 2;
                            return Range.fromPoints(tag.start, tag.end);
                        } else
                            continue;
                    }

                    if (tag.closing) {
                        this._pop(stack, tag);
                        if (stack.length == 0)
                            return Range.fromPoints(start, tag.start);
                    } else {
                        stack.push(tag);
                    }
                }
            } else {
                var iterator = new TokenIterator(session, row, firstTag.end.column);
                var end = {
                    row: row,
                    column: firstTag.start.column
                };

                while (tag = this._readTagBackward(iterator)) {
                    if (tag.selfClosing) {
                        if (!stack.length) {
                            tag.start.column += tag.tagName.length + 2;
                            tag.end.column -= 2;
                            return Range.fromPoints(tag.start, tag.end);
                        } else
                            continue;
                    }

                    if (!tag.closing) {
                        this._pop(stack, tag);
                        if (stack.length == 0) {
                            tag.start.column += tag.tagName.length + 2;
                            return Range.fromPoints(tag.start, end);
                        }
                    } else {
                        stack.push(tag);
                    }
                }
            }

        };
    }).call(FoldMode.prototype);

});

ace.define("ace/mode/cerberus", ["require", "exports", "module", "ace/lib/oop", "ace/lib/lang", "ace/mode/text", "ace/mode/cerberus_highlight_rules", "ace/mode/behaviour/cerberus", "ace/mode/folding/cerberus"], function (require, exports, module) {
    "use strict";

    var oop = require("../lib/oop");
    var lang = require("../lib/lang");
    var TextMode = require("./text").Mode;
    var cerberusHighlightRules = require("./cerberus_highlight_rules").cerberusHighlightRules;
    var cerberusBehaviour = require("./behaviour/cerberus").cerberusBehaviour;
    var cerberusFoldMode = require("./folding/cerberus").FoldMode;

    var Mode = function () {
        this.HighlightRules = cerberusHighlightRules;
        this.$behaviour = new cerberusBehaviour();
        this.foldingRules = new cerberusFoldMode();
        this.$keywordList = cerberusHighlightRules.$keywordList;
    };

    oop.inherits(Mode, TextMode);

    (function () {

        this.voidElements = lang.arrayToMap([]);

        // this.blockComment = {start: "<!--", end: "-->"};
        //this.blockComment = {start: "# ", end: ""};

        this.$id = "ace/mode/cerberus";
    }).call(Mode.prototype);

    exports.Mode = Mode;
});
