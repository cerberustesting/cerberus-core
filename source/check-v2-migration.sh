#!/bin/bash
# =============================================================================
# check-v2-migration.sh - pre-swap checklist for a DataTables -> crbTable page.
#
# WHY THIS EXISTS
# Migrating TestCaseList to V2 silently broke every action button: the new page
# reproduced the table faithfully but dropped three <script> tags the old page
# carried (tinymce, bootstrap-treeview, TestCaseSimpleExecution). The shared
# modals those actions open need them at call time, so the buttons rendered
# perfectly and then threw "tinymce is not defined" on click - nothing visible
# until a human clicked. This script makes that class of omission impossible to
# miss BEFORE swapping a page.
#
# USAGE
#   ./check-v2-migration.sh <PageName>
#   e.g. ./check-v2-migration.sh TestCaseList
#
# Expects, in this directory:
#   <PageName>.jsp     the page currently served (the V2 candidate, or the V1
#                      still in place if you have not swapped yet)
#   <PageName>V1.jsp   the legacy page kept as fallback
# Run it from anywhere; it works in src/main/webapp itself.
#
# Deliberately NOT inside src/main/webapp: everything under that directory is
# packaged into the war and served, so a copy of this script was reachable at
# /check-v2-migration.sh on any deployed instance.
# =============================================================================

cd "$(dirname "$0")/src/main/webapp" || exit 2

set -u
PAGE="${1:-}"
if [ -z "$PAGE" ]; then
    echo "usage: $0 <PageName>   (e.g. TestCaseList)" >&2
    exit 2
fi

NEW="${PAGE}.jsp"
OLD="${PAGE}V1.jsp"
FAIL=0

for f in "$NEW" "$OLD"; do
    if [ ! -f "$f" ]; then
        echo "MISSING FILE: $f" >&2
        exit 2
    fi
done

# Strip the page's own controller script (it is legitimately replaced) and the
# cache-busting query so the two lists are comparable.
scripts_of() {
    grep -o 'src="[^"]*"' "$1" \
        | sed -e 's/^src="//' -e 's/"$//' -e 's/?v=.*$//' \
        | grep -v "js/pages/${PAGE}\.js$" \
        | grep -v "js/pages/${PAGE}V2\.js$" \
        | sort -u
}

echo "=== 1. Page-specific <script> tags present in V1 but NOT in the new page ==="
MISSING=$(comm -23 <(scripts_of "$OLD") <(scripts_of "$NEW"))
if [ -n "$MISSING" ]; then
    echo "$MISSING" | sed 's/^/  MISSING: /'
    echo
    echo "  ^ Each of these is a dependency of something the page can still do."
    echo "    A missing one usually shows up as an action button that renders but"
    echo "    throws on click. Add them to ${NEW} before swapping."
    FAIL=1
else
    echo "  OK - no script dropped."
fi

# Scripts are only half of it: the modal MARKUP (and the Alpine listeners inside
# it) arrives through jsp:include / <%@ include %>. Missing one of those is worse
# than a missing script, because nothing throws - the button fires an event that
# simply has no listener, and the click looks like it did nothing at all. That is
# exactly how Run / mass-Update / mass-Label were lost on the first TestCaseList
# swap while the console stayed clean.
includes_of() {
    grep -oE '(jsp:include page|@ include file)="[^"]*"' "$1" \
        | grep -oE '"[^"]*"' | tr -d '"' | sort -u
}

# Includes nest: a page pulls in modalInclusions.jsp, which itself pulls in ~19
# transversal modals. Resolving only the first level produces false alarms (it
# reports TestCaseSimpleCreation.html as missing when it arrives through
# modalInclusions.jsp), so follow them two levels deep - enough for this codebase.
# Include paths are written relative to the including file, so a transitive one
# comes back as "../transversal/Application.html" and does not exist from webapp/.
# Map it back to a real path, or print nothing.
resolve_include() {
    local inc="$1" candidate resolved
    for candidate in "$inc" "include/${inc#include/}" "include/${inc#../}" \
                     "include/global/${inc#../}"; do
        resolved=$(printf '%s' "$candidate" | sed 's#/\./#/#g')
        if [ -f "$resolved" ]; then
            printf '%s' "$resolved"
            return 0
        fi
    done
    return 0
}

includes_deep() {
    local page="$1"
    local direct
    direct=$(includes_of "$page")
    echo "$direct"
    local inc resolved
    for inc in $direct; do
        # include paths are written relative to the including file
        for candidate in "$inc" "include/${inc#include/}" "$(dirname "$page")/$inc" \
                         "include/global/${inc#../}" "include/${inc#../}"; do
            resolved=$(printf '%s' "$candidate" | sed 's#/\./#/#g')
            if [ -f "$resolved" ]; then
                includes_of "$resolved"
                break
            fi
        done
    done
    return 0
}

echo
echo "=== 1b. jsp:include / <%@ include %> present in V1 but NOT in the new page ==="
# Compare against the DEEP set for the new page: something V1 included directly
# may legitimately reach the new page through modalInclusions.jsp instead.
# Compare by FILE NAME, not path - the same file is written "../templates/x.html"
# from inside include/global/ and "include/templates/x.html" from a page, so a
# path-level comparison reports files that are in fact already there.
MISSING_INC=$(comm -23 <(includes_of "$OLD" | xargs -n1 basename 2>/dev/null | sort -u) \
                       <(includes_deep "$NEW" | xargs -n1 basename 2>/dev/null | sort -u))
if [ -n "$MISSING_INC" ]; then
    echo "$MISSING_INC" | sed 's/^/  MISSING: /'
    echo
    echo "  ^ These carry modal markup and Alpine @event listeners. A missing one"
    echo "    means a button dispatches an event nobody listens to - NO error is"
    echo "    logged, the click just does nothing. Add them to ${NEW}."
    FAIL=1
else
    echo "  OK - no include dropped."
fi

echo
echo "=== 1c. Events the new page dispatches - is anything listening? ==="
V2JS_EARLY="js/pages/${PAGE}V2.js"
if [ -f "$V2JS_EARLY" ]; then
    EVENTS=$(grep -oE "CustomEvent\(['\"][a-zA-Z0-9_-]+['\"]" "$V2JS_EARLY" \
             | sed -E "s/CustomEvent\(['\"]//" | tr -d "'\"" | sort -u)
    if [ -n "$EVENTS" ]; then
        for ev in $EVENTS; do
            # An Alpine listener looks like  @<event>.window="..."  in an include.
            LISTENER=$(grep -rl "@${ev}\.window" include/ 2>/dev/null | head -1)
            if [ -z "$LISTENER" ]; then
                echo "  '${ev}' -> !! no @${ev}.window listener found anywhere in include/"
                FAIL=1
            else
                # ...and that include has to reach this page, directly or through
                # a nested include such as modalInclusions.jsp.
                if includes_deep "$NEW" | grep -q "$(basename "$LISTENER")"; then
                    echo "  '${ev}' -> listener in $LISTENER, reachable  OK"
                else
                    echo "  '${ev}' -> listener in $LISTENER, but that file is NOT included in ${NEW} !!"
                    FAIL=1
                fi
            fi
        done
    else
        echo "  (page dispatches no CustomEvent)"
    fi
else
    echo "  (no ${V2JS_EARLY}; skipping)"
fi

echo
echo "=== 2. Global functions the new page's actions call ==="
V2JS="js/pages/${PAGE}V2.js"
if [ -f "$V2JS" ]; then
    # Function names invoked from action/toolbar handlers in the V2 controller.
    CALLED=$(grep -oE '\b(openModal[A-Za-z]+|initModal[A-Za-z]+)\b' "$V2JS" | sort -u)
    if [ -n "$CALLED" ]; then
        echo "$CALLED" | while read -r fn; do
            # Where is it defined? (transversal modal includes or a js file)
            # A modal entry point may be declared either as `function openModalX(`
            # or as `window.openModalX = function` (Invariant.html does the latter,
            # which used to be reported as "NOT FOUND" on every page using it).
            DEF=$(grep -rlE "(function ${fn}\b|window\.${fn}[[:space:]]*=)" include/ js/ 2>/dev/null | head -1)
            if [ -n "$DEF" ]; then
                echo "  $fn -> defined in $DEF"
            else
                echo "  $fn -> !! NOT FOUND in include/ or js/ - will throw on click"
                FAIL=1
            fi
        done
    else
        echo "  (no modal-opening calls detected)"
    fi
else
    echo "  (no ${V2JS}; skipping)"
fi

echo
echo "=== 2b. Functions the V1 controller defined that INCLUDES call back into ==="
# The shared modals call page-level functions by name (e.g.
# TestCaseListMassActionLabel.html calls massActionModalSaveHandler_addLabel()).
# Those live in the V1 controller, which a migrated page no longer loads - so the
# modal opens and its save button then throws. Nothing else catches this.
#
# A page may deliberately KEEP loading the V1 controller alongside the V2 one
# (Label.jsp does: its three tree tabs and every modal handler still live there,
# and copying ~500 lines into the V2 file would only let the two drift). When it
# does, those functions are still defined at runtime and this section has nothing
# to report - so detect that first instead of flagging every one of them.
V1JS_CB="js/pages/${PAGE}.js"
V1JS_STILL_LOADED=0
# Must be an actual <script src=...>, not a mention in a comment - TestCaseList.jsp
# names its V1 controller in the rollback note at the top of the file.
if grep -qE "<script[^>]+src=\"js/pages/${PAGE}\.js" "$NEW"; then
    V1JS_STILL_LOADED=1
fi
if [ "$V1JS_STILL_LOADED" -eq 1 ]; then
    echo "  (page still loads $V1JS_CB, so its functions remain defined - nothing to check)"
elif [ -f "$V1JS_CB" ] && [ -f "$V2JS" ]; then
    V1FUNCS=$(grep -oE '^(async )?function [A-Za-z_][A-Za-z0-9_]*' "$V1JS_CB" \
              | sed -E 's/^(async )?function //' | sort -u)
    FOUND_ANY=0
    for fn in $V1FUNCS; do
        # is this function called from any include the page pulls in?
        # A file that DECLARES the function matches "fn(" too, and would be
        # reported as calling it (Application.html declares its own
        # editEntryModalCloseHandler). Only count real call sites.
        CALLERS=$(grep -rlE "(^|[^A-Za-z0-9_.])${fn}\(" include/ 2>/dev/null \
                  | xargs -I{} sh -c 'grep -qE "(^|[^A-Za-z0-9_.])'"${fn}"'\(" "$1" && ! grep -qE "^[[:space:]]*(async )?function '"${fn}"'[[:space:]]*\(" "$1" && echo "$1"' _ {} \
                  | head -3)
        [ -z "$CALLERS" ] && continue
        REACHABLE=""
        for caller in $CALLERS; do
            if includes_deep "$NEW" | grep -q "$(basename "$caller")"; then
                REACHABLE="$caller"
                break
            fi
        done
        [ -z "$REACHABLE" ] && continue
        FOUND_ANY=1
        # Anchored at column 0: page controllers declare their functions at top
        # level, and an unanchored match also hits the name inside a comment.
        if grep -qE "^(async )?function ${fn}[[:space:]]*\(" "$V2JS" \
           || grep -qE "^(var|let|const)[[:space:]]+${fn}[[:space:]]*=" "$V2JS" \
           || grep -qE "^[[:space:]]*window\.${fn}[[:space:]]*=" "$V2JS"; then
            echo "  $fn  <- called by $(basename "$REACHABLE"), defined in V2  OK"
        else
            # A call sitting inside an Alpine attribute (@click / x-on:) is very
            # likely a method on that component's own x-data, not this page's
            # global function - report it, but do not fail the run on it.
            if grep -qE "(@[a-z]+(\.[a-z]+)*|x-on:[a-z]+)=\"[^\"]*\b${fn}\(" "$REACHABLE"; then
                echo "  $fn  <- called by $(basename "$REACHABLE") inside an Alpine attribute;"
                echo "         probably that component's own method, verify manually"
            else
                echo "  $fn  <- called by $(basename "$REACHABLE"), but NOT defined in $(basename "$V2JS") !!"
                FAIL=1
            fi
        fi
    done
    [ "$FOUND_ANY" -eq 0 ] && echo "  (no include calls back into the page controller)"
else
    echo "  (missing ${V1JS_CB} or ${V2JS}; skipping)"
fi

echo
echo "=== 2c. Modals that refresh the OLD table id without notifying the V2 one ==="
# The shared modals refresh the list after a save with
#   $("#<legacy table id>").dataTable().fnDraw()
# On a migrated page that id is gone, so the call hits an empty jQuery set and
# NOTHING happens: the user creates a record and it does not appear until they
# reload by hand. The modal must therefore also call crbNotifyDataChanged().
# This is the single most repeated defect of the migration - check it per page.
# The constructor call is sometimes wrapped across lines, e.g.
#   new TableConfigurationsServerSide(
#           "applicationObjectsTable", ...
# so flatten the file before matching rather than assuming one line.
LEGACY_TABLE_ID=$(tr '\n' ' ' < "js/pages/${PAGE}.js" 2>/dev/null \
                  | grep -oE 'TableConfigurations(ServerSide|ClientSide)\( *"[^"]+"' \
                  | head -1 | sed -E 's/.*"([^"]+)"/\1/')
if [ -n "$LEGACY_TABLE_ID" ]; then
    echo "  legacy table id: #${LEGACY_TABLE_ID}"
    OFFENDERS=$(grep -rln "#${LEGACY_TABLE_ID}\"\?)\?\.dataTable()" include/ 2>/dev/null)
    [ -z "$OFFENDERS" ] && OFFENDERS=$(grep -rl "#${LEGACY_TABLE_ID}" include/ 2>/dev/null \
                                       | xargs grep -l "fnDraw" 2>/dev/null)
    if [ -z "$OFFENDERS" ]; then
        echo "  (no shared modal refreshes this table)"
    else
        for m in $OFFENDERS; do
            # only care about modals this page actually pulls in
            if ! includes_deep "$NEW" | grep -q "$(basename "$m")"; then
                continue
            fi
            if grep -q "crbNotifyDataChanged" "$m"; then
                echo "  $(basename "$m")  refreshes #${LEGACY_TABLE_ID} AND notifies  OK"
            else
                echo "  $(basename "$m")  refreshes #${LEGACY_TABLE_ID} but NEVER calls"
                echo "        crbNotifyDataChanged() -> after create/edit the V2 table will"
                echo "        NOT update. Add the notify next to its fnDraw call."
                FAIL=1
            fi
        done
    fi
else
    echo "  (could not determine the legacy table id from js/pages/${PAGE}.js)"
fi

echo
echo "=== 2d. Page functions shadowed by a globally-included file ==="
# Every page pulls in include/transversal/*.html through modalInclusions.jsp, and
# several of those declare generic names (editEntryModalCloseHandler,
# addEntryModalCloseHandler...). They are parsed in the body, AFTER the head
# scripts, so their declaration wins the global name and the page's own handler
# never runs - silently, with no error. Section 2b cannot see this: the function
# IS defined, just not the one the page meant.
SHADOW_ANY=0
# Scan the V1 controller only when the page still loads it (Label.jsp does);
# otherwise a collision in a file that is no longer on the page is not a finding.
SHADOW_TARGETS="$V2JS"
if [ "${V1JS_STILL_LOADED:-0}" -eq 1 ]; then
    SHADOW_TARGETS="js/pages/${PAGE}.js $V2JS"
fi
for ctrl in $SHADOW_TARGETS; do
    [ -f "$ctrl" ] || continue
    PAGEFUNCS=$(grep -oE '^(async )?function [A-Za-z_][A-Za-z0-9_]*' "$ctrl" \
                | sed -E 's/^(async )?function //' | sort -u)
    for fn in $PAGEFUNCS; do
        for rawinc in $(includes_deep "$NEW"); do
            inc=$(resolve_include "$rawinc")
            [ -n "$inc" ] || continue
            case "$inc" in *.html) ;; *) continue ;; esac
            grep -qE "^[[:space:]]*(async )?function ${fn}[[:space:]]*\(" "$inc" || continue
            # A page that reclaims the name at document-ready (window.fn = ...),
            # which runs after every include has been parsed, has already fixed it.
            # `window.fn = fn` is a self-assignment: by then the bare name already
            # resolves to the include's declaration, so it fixes nothing. Only a
            # different right-hand side counts as reclaimed.
            if [ -f "$V2JS" ] && grep -qE "window\.${fn}[[:space:]]*=[[:space:]]*(${fn}[[:space:]]*;)" "$V2JS"; then
                echo "  $fn  \"reclaimed\" by window.${fn} = ${fn} in $(basename "$V2JS") - that is a"
                echo "         SELF-ASSIGNMENT and does nothing. Use a differently named function."
                SHADOW_ANY=1
                FAIL=1
            elif [ -f "$V2JS" ] && grep -qE "window\.${fn}[[:space:]]*=" "$V2JS"; then
                echo "  $fn  shadowed by $(basename "$inc"), reclaimed in $(basename "$V2JS")  OK"
                SHADOW_ANY=1
            else
                echo "  $fn  defined in $(basename "$ctrl") is SHADOWED by $(basename "$inc") !!"
                echo "         the include is parsed after the head scripts, so ITS version wins;"
                echo "         reclaim it at document-ready with a DIFFERENTLY NAMED function:"
                echo "         window.${fn} = myPageVersion;  (assigning the bare name to"
                echo "         itself is a silent no-op - it already resolves to the include's)"
                SHADOW_ANY=1
                FAIL=1
            fi
        done
    done
done
[ "$SHADOW_ANY" -eq 0 ] && echo "  (no page function is shadowed by an include)"

echo
echo "=== 3. Init calls the V1 made that the V2 may still need ==="
V1JS="js/pages/${PAGE}.js"
if [ -f "$V1JS" ]; then
    V1INIT=$(grep -oE '\b(initModal[A-Za-z]+|displayInvariantList|refreshPopoverDocumentation)\([^)]*\)' "$V1JS" | sed 's/(.*//' | sort -u)
    for fn in $V1INIT; do
        if [ -f "$V2JS" ] && grep -q "\b${fn}\b" "$V2JS"; then
            echo "  $fn -> also called in V2  OK"
        else
            echo "  $fn -> called in V1, NOT in V2  (check whether it is still required)"
            FAIL=1
        fi
    done
    [ -z "$V1INIT" ] && echo "  (V1 made no such init calls)"
else
    echo "  (no ${V1JS}; skipping)"
fi

echo
if [ "$FAIL" -eq 0 ]; then
    
# ---------------------------------------------------------------------------
# 4. Component methods called but never defined.
#
# Added after a real outage: the persistence rework renamed restoreColumnPrefs()
# to restoreState(), but crbTableSetColumns() still called the old name. It
# throws only on the runtime-column path - the campaign report grid and the
# homepage - so every other page looked fine while those two silently rendered
# an empty list, because the exception aborted the caller before it could push
# its rows. Nothing static was checking for it and no page-level test covered a
# runtime column set.
# ---------------------------------------------------------------------------
echo "=== 4. crbTable/crbLabelTree: methods called but not defined ==="
# NOTE: this script cd'd into src/main/webapp at the top, so paths are relative
# to THAT. Getting this wrong once already made the check report OK while
# reading nothing - a missing file is therefore a loud failure below, not a skip.
python3 - <<'PYEOF'
import io, re, sys, os
bad = False
for path in ["js/global/crbTable.js", "js/global/crbLabelTree.js"]:
    if not os.path.exists(path):
        print("  CHECK BROKEN - %s not found from %s" % (path, os.getcwd()))
        bad = True
        continue
    src = io.open(path, encoding="utf-8").read()
    defined = set(re.findall(r'^\s{8}([a-zA-Z_$][\w$]*): function', src, re.M))
    defined |= set(re.findall(r'^\s{8}get ([a-zA-Z_$][\w$]*)\(', src, re.M))
    called = set(re.findall(r'\b(?:table|tree|self|this)\.([a-zA-Z_$][\w$]*)\(', src))
    builtin = set("""forEach map filter indexOf slice splice push join some every sort concat
substring substr toLowerCase toUpperCase replace split trim getTime test removeChild appendChild
querySelector querySelectorAll addEventListener removeEventListener setItem getItem removeItem
focus select click scrollIntoView getBoundingClientRect matches contains then catch always
finally find findIndex keys toString charAt apply call bind $nextTick $watch $refs""".split())
    missing = sorted(c for c in called if c not in defined and c not in builtin)
    if missing:
        bad = True
        print("  %s -> calls undefined: %s" % (path.split('/')[-1], ", ".join(missing)))
    else:
        print("  %s OK (%d methods defined)" % (path.split('/')[-1], len(defined)))
sys.exit(0)
PYEOF
echo

echo "RESULT: no problem detected. Still click every action button once in the"
    echo "        browser - this script cannot prove a modal actually opens."
    exit 0
fi
echo "RESULT: issues above need attention before this page is swapped."
exit 1
