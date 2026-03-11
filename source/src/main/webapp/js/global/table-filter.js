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
/***************************SEARCH/FILTERS**************************************/

/**
 * Function that allow to reset the filter selected
 * @param {type} oTable datatable object
 * @param {int} columnNumber If empty, reset all the column's filter
 * @param {boolean} clearGlobalSearch true if global search should be cleared.
 * @param {boolean} doRedraw true if draw should be triggeres.
 * @returns {undefined}
 */
function resetFilters(oTable, columnNumber, clearGlobalSearch = false, doRedraw = true) {
    var oSettings = oTable.fnSettings();
    for (iCol = 0; iCol < oSettings.aoPreSearchCols.length; iCol++) {
        /**
         * if columnNumber defined, clear that column search
         */
        if (columnNumber !== undefined) {
            if (parseInt(columnNumber) === iCol) {
                oSettings.aoPreSearchCols[iCol].sSearch = '';
            }
            /**
             * else clear all columns
             */
        } else {
            oSettings.aoPreSearchCols[iCol].sSearch = '';
        }
    }
    /**
     * if clearGlobalSearch, clear search
     */
    if (clearGlobalSearch) {
        oSettings.oPreviousSearch.sSearch = '';
    }
    if (doRedraw) {
        oTable.fnDraw();
}
}

function resetTooltip() {
    $(".tooltip.fade").remove();
}


/**
 * Function that allow to filter column on specific value
 * @param {type} tableId >Id of the datatable
 * @param {type} column > Name of the column
 * @param {type} value > Value to filter
 * @returns {undefined}
 */
function filterOnColumn(tableId, column, value) {
    var oTable = $('#' + tableId).dataTable();
    resetFilters(oTable, undefined, undefined, false);
    var oSettings = oTable.fnSettings();
    for (iCol = 0; iCol < oSettings.aoPreSearchCols.length; iCol++) {
        if (oSettings.aoColumns[iCol].data === column) {
            oTable.api().column(iCol).search(value);
        }
    }
    oTable.fnDraw();
}

/**
 * Function that apply filters on given datatable's columns
 *
 * Values can either be contained into the given columns, or retrieved from the current URL.
 *
 * @param tableId the datatable from which filter columns
 * @param searchColumns the array of columns to filter. Column can be either an object {param, values}, or simply the name of the column (param)
 * @param fromURL if values are to be retrived from the current URL
 */
function applyFiltersOnMultipleColumns(tableId, searchColumns, fromURL) {
    // Get or create the search array
    var searchArray = searchColumns;
    if (fromURL) {
        searchArray = [];
        for (var searchColumn = 0; searchColumn < searchColumns.length; searchColumn++) {
            var param = GetURLParameters(searchColumns[searchColumn]);
            var searchObject = {
                param: searchColumns[searchColumn],
                values: param};
            searchArray.push(searchObject);
        }
    }

    // Apply filter on table
    var oTable = $('#' + tableId).dataTable();
    resetFilters(oTable, undefined, undefined, false);
    var oSettings = oTable.fnSettings();
    for (iCol = 0; iCol < oSettings.aoPreSearchCols.length; iCol++) {
        for (sCol = 0; sCol < searchArray.length; sCol++) {
            if (oSettings.aoColumns[iCol].data === searchArray[sCol].param) {
                oTable.api().column(iCol).search(searchArray[sCol].values);
            }
        }
    }
    oTable.fnDraw();
}

/**
 * Function that allow to clear filter individually
 * @param {type} tableId >> ID of the datatable
 * @param {type} columnNumber >> Number of the column
 * @param {type} clearGlobalSearch >> boolean. true if global search should be cleared.
 * @returns {undefined}
 */
function clearIndividualFilter(tableId, columnNumber, clearGlobalSearch) {
    var oTable = $('#' + tableId).dataTable();
    resetFilters(oTable, columnNumber, clearGlobalSearch);
}

/**
 * Function that allow to clear filter individually
 * @param {type} tableId >> ID of the datatable
 * @param {type} columnNumber >> Number of the column
 * @param {type} clearGlobalSearch >> boolean. true if global search should be cleared.
 * @returns {undefined}
 */
function clearIndividualFilterForClientSide(tableId, columnNumber, clearGlobalSearch) {
    columnSearchValuesForClientSide[columnNumber] = [];//reset the search value for the column
    clearIndividualFilter(tableId, columnNumber, clearGlobalSearch);
}

/**
 * Function that allow display of individual column when the table is created client side
 * @param {type} tableId >> ID of the datatable
 * @param {type} oSettings >> datatable settings
 * @returns {undefined}
 */
var columnSearchValuesForClientSide = [];//global var that take the role of the ajax function
function displayColumnSearchForClientSideTable(tableData, contentUrl, oSettings) {
    privateDisplayColumnSearch(tableData, contentUrl, oSettings, true); // table data not use ?
}

/**
 * Function that allow display of individual column searching on datatable
 * @param {type} tableId >> ID of the datatable
 * @param {type} contentUrl >> URL of the service to get content of each columns
 * @param {type} oSettings >> datatable settings
 * @returns {undefined}
 */
function displayColumnSearch(tableId, contentUrl, oSettings) {
    privateDisplayColumnSearch(tableId, contentUrl, oSettings, false);
}


/**
 * privateDisplayColumnSearch — réécriture compatible Tailwind + Alpine.js
 *
 * Problèmes corrigés :
 *  1. Popover arraché du flux DOM (teleport vers <body>) pour contourner
 *     l'overflow:hidden des wrappers DataTables.
 *  2. Positionnement recalculé à chaque ouverture via getBoundingClientRect
 *     + repositionnement sur scroll/resize pour éviter le décalage.
 *  3. Gestion des événements sur le champ search refactorisée : un seul
 *     binding stable, pas de fuites après re-render.
 *  4. Fermeture du popover actif avant d'en ouvrir un autre (une seule
 *     instance à la fois).
 */

var firstclickOnShowHide = true;

// ─── Gestionnaire de popover global ──────────────────────────────────────────
var ColumnSearchPopover = (function () {
    var $active = null;          // popover actuellement visible
    var $anchor = null;          // bouton qui l'a ouvert
    var resizeScrollHandler = null;

    /** Téléporte un popover vers <body> et le positionne sous son ancre */
    function _position($pop, $btn) {
        // Sécurité : si l'ancre n'est plus dans le DOM, ne pas positionner en 0,0
        if (!$btn || !$btn[0] || !document.contains($btn[0])) return;

        var rect = $btn[0].getBoundingClientRect();
        // getBoundingClientRect renvoie {top:0,left:0} si l'élément est hors viewport ou détaché
        if (rect.width === 0 && rect.height === 0) return;

        var popW = $pop.outerWidth() || 260;
        var viewW = $(window).width();

        var left = rect.left;
        // Ne pas dépasser la fenêtre à droite
        if (left + popW > viewW - 8) {
            left = viewW - popW - 8;
        }

        $pop.css({
            position : 'fixed',
            top      : (rect.bottom + 6) + 'px',
            left     : left + 'px',
            zIndex   : 99999
        });
    }

    /** Ferme le popover actif */
    function close() {
        if ($active) {
            $active.css('display', 'none').detach(); // .hide() ne suffit pas si Tailwind 'hidden' est présent
            $active = null;
            $anchor = null;
        }
        if (resizeScrollHandler) {
            $(window).off('resize scroll', resizeScrollHandler);
            resizeScrollHandler = null;
        }
    }

    /** Ouvre (ou ferme si déjà ouvert) un popover */
    function open($pop, $btn) {
        // Toggle : clic sur le même bouton referme
        if ($active && $active.is($pop)) {
            close();
            return;
        }
        close();

        $active = $pop;
        $anchor = $btn;

        // Téléporter vers <body> pour sortir de tout overflow:hidden
        $('body').append($pop);
        $pop.css('display', 'flex'); // forcer display:flex — évite le !important de Tailwind 'hidden'
        _position($pop, $btn);

        // Repositionner sur scroll ou resize
        resizeScrollHandler = function () {
            if ($active) _position($active, $anchor);
        };
        $(window).on('resize scroll', resizeScrollHandler);
    }

    // Clic en dehors → fermer
    $(document).on('click.colSearch', function (e) {
        if (!$active) return;
        if (
            !$active.is(e.target) &&
            $active.find(e.target).length === 0 &&
            !$anchor.is(e.target) &&
            $anchor.find(e.target).length === 0
        ) {
            close();
        }
    });

    return { open: open, close: close, reposition: function () { if ($active && $anchor) _position($active, $anchor); } };
})();


// ─── Fonction principale ──────────────────────────────────────────────────────
function privateDisplayColumnSearch(tableId, contentUrl, oSettings, clientSide) {

    var fctClearIndividualFilter = clientSide
        ? 'clearIndividualFilterForClientSide'
        : 'clearIndividualFilter';

    // ── Zone d'alerte "Filtered by" ──────────────────────────────────────────
    var showFilteredColumnsAlertMessage =
        "<br><div id='filterAlertDiv' class='marginBottom10 border-gray-200 dark:border-gray-800'>" +
        "<div id='activatedFilters'></div></div>";

    $('#filterAlertDiv').remove();
    if ($('#' + tableId + '_filterresult').length > 0) {
        $(showFilteredColumnsAlertMessage)
            .appendTo($('#' + tableId + '_filterresult'))
            .hide();
    } else {
        $('#' + tableId + '_filter').after($(showFilteredColumnsAlertMessage).hide());
    }

    // ── Init DataTable ────────────────────────────────────────────────────────
    var table  = $('#' + tableId).dataTable().api();
    var doc    = new Doc();
    var columnVisibleIndex = 0;

    // ── Colonnes ordonnées ────────────────────────────────────────────────────
    var orderedColumns = [];
    $.each(oSettings.aoColumns, function (i, col) {
        if (clientSide) {
            if (col.sName !== '') {
                orderedColumns.push(col.sName.split('.')[1] || col.sName);
            } else {
                orderedColumns.push('labels');
            }
        } else {
            orderedColumns.push(col.sName);
        }
    });

    // ── Ligne de filtres dans le thead ────────────────────────────────────────
    $('#' + tableId + '_wrapper #filterHeader').remove();
    $('#' + tableId + '_wrapper .dataTables_scrollBody').find('#filterHeader').remove();
    $('#' + tableId + '_wrapper').attr('style', 'position: relative');
    $('#' + tableId + '_wrapper [class="dataTables_scrollHead ui-state-default"]')
        .attr('style', 'overflow: hidden; border: 0px; width: 100%;');
    $('#' + tableId + '_wrapper .dataTables_scrollHeadInner table thead')
        .append('<tr id="filterHeader"></tr>');
    $('#' + tableId + '_wrapper .dataTables_scrollHeadInner table thead tr th').each(function () {
        $('#' + tableId + '_wrapper #filterHeader').append("<th name='filterColumnHeader'></th>");
    });

    // ── Accumulation des valeurs de filtre ────────────────────────────────────
    var allcolumnSearchValues = {};
    var filteredInformationWrapper = [];

    // ── Template de la barre "Filtered by" ───────────────────────────────────
    var filteredInformation = [`
        <div class="flex flex-wrap items-center gap-2 mt-2 w-full">
            <span class="font-semibold text-gray-600 dark:text-gray-300">Filtered by:</span>
            ${table.search() !== '' ? `
            <span class="inline-flex items-center px-3 h-8 border border-gray-200 dark:border-gray-700 rounded-full text-sm truncate"
                  title="${table.search()}">[ ${table.search()} ]</span>` : ''}
            %WRAPPER%
            <span id="clearFilterButtonGlobal"
                  class="ml-auto font-semibold text-gray-400 dark:text-gray-500 cursor-pointer hover:text-gray-600 dark:hover:text-gray-300 text-xs">
                Clear Filters
            </span>
        </div>`];

    // ── Tooltips ──────────────────────────────────────────────────────────────
    var emptyFilter    = doc.getDocLabel('page_global', 'tooltip_column_filter_empty');
    var selectedFilter = doc.getDocLabel('page_global', 'tooltip_column_filter_filtered');

    // ─────────────────────────────────────────────────────────────────────────
    // Boucle sur chaque colonne
    // ─────────────────────────────────────────────────────────────────────────
    $.each(orderedColumns, function (index, value) {

        // Valeurs de filtre actives pour cette colonne
        var columnSearchValues;
        if (clientSide) {
            columnSearchValues = columnSearchValuesForClientSide[index];
        } else {
            var json_obj = JSON.stringify(table.ajax.params());
            columnSearchValues = JSON.parse(json_obj)['sSearch_' + index].split(',');
        }

        if (columnSearchValues != null) {
            allcolumnSearchValues[value] = (columnSearchValues[0] === '' || columnSearchValues[0] === undefined)
                ? undefined
                : columnSearchValues;
        } else {
            allcolumnSearchValues[value] = undefined;
        }

        var title        = value;
        var valueFiltered = [];

        // ── Badge "filtre actif" dans la barre Filtered by ───────────────────
        if (columnSearchValues !== undefined &&
            columnSearchValues.length > 0 &&
            columnSearchValues[0] !== '') {

            var filteredColumnInformation = [];
            var filteredTooltip = '<div>';

            $(columnSearchValues).each(function (i) {
                var safeVal = $('<p>' + columnSearchValues[i] + '</p>').text();
                valueFiltered[i]  = safeVal;
                filteredTooltip  += '<br><span>' + safeVal + '</span> ';
                filteredColumnInformation.push(columnSearchValues[i]);
                filteredColumnInformation.push(' | ');
            });
            filteredColumnInformation.pop();
            filteredTooltip += '</div>';

            filteredInformationWrapper.push(`
                <div class="h-8 inline-flex items-center px-3 border border-gray-200 dark:border-gray-700 rounded-full">
                    <div class="truncate whitespace-nowrap overflow-hidden inline-flex items-center space-x-1">
                        ${oSettings.aoColumns[index].like
                ? `<strong>${title}</strong><span> LIKE </span>`
                : `<strong>${title}</strong><span> IN </span>`}
                        <span title="${filteredTooltip}" id="alertFilteredValues${index}">
                            [ ${filteredColumnInformation} ]
                        </span>
                    </div>
                    <span id="clearFilterButton${index}"
                          onclick='${fctClearIndividualFilter}("${tableId}", "${index}", false)'
                          title="Clear filter ${title}"
                          class="ml-2 cursor-pointer text-blue-600 dark:text-blue-200 hover:text-blue-800">✕</span>
                </div>`);
        }

        // ── Colonne visible → créer la cellule de filtre ─────────────────────
        if (!oSettings.aoColumns[index].bVisible) return; // continue

        var data = [];
        if (title !== 'labels') {
            table.column(index).data().unique().sort().each(function (d) { data.push(d); });
        } else {
            oSettings.aoColumns[index].bSearchable = false;
        }

        var tableCell = $($('#' + tableId + '_wrapper [name="filterColumnHeader"]')[columnVisibleIndex])[0];
        $(tableCell).removeClass().addClass('filterHeader');

        var isSearchable = clientSide
            ? oSettings.aoColumns[index].bSearchable
            : table.ajax.params()['bSearchable_' + index];

        if (isSearchable) {
            _buildColumnFilter({
                tableId              : tableId,
                contentUrl           : contentUrl,
                oSettings            : oSettings,
                clientSide           : clientSide,
                index                : index,
                value                : value,
                title                : title,
                data                 : data,
                tableCell            : tableCell,
                table                : table,
                allcolumnSearchValues: allcolumnSearchValues,
                columnSearchValues   : columnSearchValues,
                valueFiltered        : valueFiltered,
                emptyFilter          : emptyFilter,
                selectedFilter       : selectedFilter,
                columnVisibleIndex   : columnVisibleIndex
            });
        }

        columnVisibleIndex++;
    }); // fin boucle colonnes

    // ── Injecter la barre "Filtered by" ──────────────────────────────────────
    filteredInformation[0] = filteredInformation[0].replace('%WRAPPER%', filteredInformationWrapper.join(''));

    var focusOnNextSearchInputBool = false;

    if (table.search() !== '' || filteredInformationWrapper.length > 0) {
        $('#' + tableId + '_wrapper #activatedFilters').html(filteredInformation.join(''));

        $('#' + tableId + '_wrapper #clearFilterButtonGlobal').off('click').on('click', function () {
            if (clientSide) columnSearchValuesForClientSide = [];
            resetFilters($('#' + tableId).dataTable(), undefined, true);
        });

        $('#' + tableId + '_wrapper #filterAlertDiv').show();
        focusOnNextSearchInputBool = true;
    }

    // ── Show/Hide colonnes ────────────────────────────────────────────────────
    $('#' + tableId + '_wrapper #showHideColumnsButton').off('click.colSearchInit').on('click.colSearchInit', function () {
        if (firstclickOnShowHide) {
            $('.dt-button.buttons-columnVisibility').each(function (i, el) {
                $(el).find('a').text(oSettings.aoColumns[i].nTh.innerText);
            });
            firstclickOnShowHide = false;
            $('#' + tableId + '_wrapper #showHideColumnsButton').click();
        }
        $('ul[class="dt-button-collection dropdown-menu"] li').off('click.colSearch').on('click.colSearch', function () {
            privateDisplayColumnSearch(tableId, contentUrl, oSettings, clientSide);
        });
    });

    if (focusOnNextSearchInputBool) {
        focusOnNextSearchInput(lastSearchInput);
    }

    resetTooltip();
}


// ─── Construction d'un filtre colonne (popover maison téléporté) ──────────────
function _buildColumnFilter(opts) {
    var tableId               = opts.tableId;
    var contentUrl            = opts.contentUrl;
    var oSettings             = opts.oSettings;
    var clientSide            = opts.clientSide;
    var index                 = opts.index;
    var value                 = opts.value;
    var title                 = opts.title;
    var data                  = opts.data;
    var tableCell             = opts.tableCell;
    var table                 = opts.table;
    var allcolumnSearchValues = opts.allcolumnSearchValues;
    var columnSearchValues    = opts.columnSearchValues;
    var valueFiltered         = opts.valueFiltered;
    var emptyFilter           = opts.emptyFilter;
    var selectedFilter        = opts.selectedFilter;

    var isLike    = !!oSettings.aoColumns[index].like;
    var hasValues = columnSearchValues !== undefined &&
        columnSearchValues.length > 0 &&
        columnSearchValues[0] !== '';

    // ── Icône funnel discrète, injectée à côté de l'icône sort ───────────────
    var svgFunnel = '<svg xmlns="http://www.w3.org/2000/svg" width="12" height="17"'
        + ' viewBox="0 0 24 24" fill="none" stroke="currentColor"'
        + ' stroke-width="2" stroke-linecap="round" stroke-linejoin="round">'
        + '<line x1="3" y1="6" x2="21" y2="6"/>'
        + '<line x1="7" y1="12" x2="17" y2="12"/>'
        + '<line x1="10" y1="18" x2="14" y2="18"/>'
        + '</svg>';

    var $triggerBtn = $('<span>')
        .attr({ 'data-col-index': index, title: hasValues ? valueFiltered.join(', ') : '' })
        .addClass(
            'col-filter-trigger inline-flex items-center justify-end ml-auto cursor-pointer rounded transition-colors duration-150 ' +
            (hasValues
                ? 'text-blue-500 dark:text-blue-400'
                : 'text-gray-300 dark:text-gray-600 hover:text-gray-500 dark:hover:text-gray-400')
        )
        .html(svgFunnel);

    // Injecter l'icône dans le th principal (à côté du label de colonne).
    // On cible le <th> de la première ligne de headers via columnVisibleIndex.
    var $th = $($('#' + tableId + '_wrapper .dataTables_scrollHeadInner table thead tr:first th')[opts.columnVisibleIndex]);
    $th.find('.col-filter-trigger').remove(); // éviter les doublons au refresh
    $th.append($triggerBtn);

    // filterHeader reste vide — c'est juste un spacer structurel
    $(tableCell).empty();

    // ── Construire le popover (pas encore dans le DOM) ────────────────────────
    var $popover = _buildPopover(opts, $triggerBtn);

    // ── Ouvrir/fermer au clic sur l'icône ─────────────────────────────────────
    $triggerBtn.on('click', function (e) {
        e.stopPropagation();

        ColumnSearchPopover.open($popover, $triggerBtn);

        if (!clientSide && !isLike && $popover.data('sourcesLoaded') !== true) {
            // Async : _applyCheckedState est appelé dans le callback de _loadServerSources
            _loadServerSources(opts, $popover, allcolumnSearchValues);
        } else {
            // Lire allcolumnSearchValues courant (mis à jour par _applyFilter à chaque Apply)
            _applyCheckedState($popover, allcolumnSearchValues[value], isLike);
        }

        $popover.find('.col-filter-search').focus();
    });
}


// ─── Construction du DOM du popover ──────────────────────────────────────────
function _buildPopover(opts, $triggerBtn) {
    var tableId               = opts.tableId;
    var oSettings             = opts.oSettings;
    var clientSide            = opts.clientSide;
    var index                 = opts.index;
    var value                 = opts.value;
    var title                 = opts.title;
    var data                  = opts.data;
    var table                 = opts.table;
    var allcolumnSearchValues = opts.allcolumnSearchValues;
    var isLike                = !!oSettings.aoColumns[index].like;

    // Wrapper du popover — hors DOM jusqu'à l'ouverture.
    // IMPORTANT: pas de classe Tailwind 'hidden' ici car son display:none !important
    // empêche jQuery .css('display','flex') de fonctionner.
    var $pop = $('<div>')
        .addClass([
            'col-filter-popover',
            'bg-white', 'dark:bg-gray-900',
            'border', 'border-gray-200', 'dark:border-gray-700',
            'rounded-lg', 'shadow-xl',
            'w-64',
            'flex', 'flex-col', 'gap-2', 'p-3'
        ].join(' '))
        .attr('data-col-index', index)
        .css({ display: 'none', zIndex: 99999 }); // masqué via inline style seulement

    // ── Header : titre + boutons All/None ───────────────────────────────────────
    var $header = $('<div>').addClass('flex items-center justify-between gap-2 border-b border-gray-200 dark:border-gray-700 pb-2 mb-1');
    $header.append(
        $('<span>').addClass('font-semibold text-sm text-gray-700 dark:text-gray-200 truncate').text(title)
    );

    if (!isLike) {
        var $quickBtns = $('<div>').addClass('flex gap-1 items-center shrink-0');
        var $selAll = $('<button>').attr('type', 'button')
            .addClass('text-[10px] px-2 py-0.5 bg-slate-200 hover:bg-slate-300 dark:bg-slate-700 dark:text-slate-200 dark:hover:bg-slate-600 rounded cursor-pointer')
            .text('All');
        var $unselAll = $('<button>').attr('type', 'button')
            .addClass('text-[10px] px-2 py-0.5 bg-slate-200 hover:bg-slate-300 dark:bg-slate-700 dark:text-slate-200 dark:hover:bg-slate-600 rounded cursor-pointer')
            .text('None');

        $selAll.on('click', function () {
            $pop.find('.col-filter-item:visible input[type="checkbox"]').prop('checked', true);
        });
        $unselAll.on('click', function () {
            $pop.find('.col-filter-item:visible input[type="checkbox"]').prop('checked', false);
        });
        $quickBtns.append($selAll, $unselAll);
        $header.append($quickBtns);
    }
    $pop.append($header);

    // ── Champ de recherche dans la liste ─────────────────────────────────────
    var $searchInput = $('<input>')
        .addClass([
            'col-filter-search',
            'w-full', 'text-sm',
            'border', 'border-gray-300', 'dark:border-gray-600',
            'rounded', 'px-2', 'py-1',
            'text-slate-700', 'dark:text-slate-200',
            'bg-white', 'dark:bg-gray-800',
            'focus:outline-none', 'focus:ring-1', 'focus:ring-blue-500'
        ].join(' '))
        .attr({ placeholder: 'Search...', autocomplete: 'off', id: 'inputsearch_' + index, name: 'searchField' });

    $pop.append($searchInput);

    // Liste de valeurs (checklist ou champ texte libre pour LIKE)
    var $list = $('<div>').addClass('col-filter-list max-h-48 overflow-y-auto flex flex-col gap-1 mt-1');

    if (isLike) {
        // Mode LIKE : un seul champ texte, pas de checklist
        // (le champ de recherche fait office d'input LIKE)
        $searchInput.attr('placeholder', 'Like filter…');
        if (allcolumnSearchValues[opts.value] !== undefined) {
            $searchInput.val(allcolumnSearchValues[opts.value][0] || '');
        }
    } else {
        // Mode checklist
        $.each(data, function (i, d) {
            var safeVal = $('<p>' + d + '</p>').text();
            var $item   = $('<label>').addClass('col-filter-item flex items-center gap-2 text-sm cursor-pointer px-1 py-0.5 rounded hover:bg-gray-100 dark:hover:bg-gray-800');
            var $cb     = $('<input type="checkbox">').val(safeVal).addClass('accent-blue-600');
            var $lbl    = $('<span>').addClass('truncate text-gray-700 dark:text-gray-200').text(safeVal);
            $item.append($cb, $lbl);
            $list.append($item);
        });
    }
    $pop.append($list);

    // Boutons footer : Clear (si filtre actif) + Cancel + Apply
    var $footer = $('<div>').addClass('flex items-center gap-2 pt-2 border-t border-gray-200 dark:border-gray-700');

    // Bouton Clear — visible seulement si un filtre est déjà appliqué sur cette colonne
    var hasActiveFilter = !isLike && allcolumnSearchValues[opts.value] !== undefined;
    var $clear = $('<button>').attr('type', 'button')
        .addClass('text-[10px] px-2 py-0.5 rounded bg-red-50 hover:bg-red-100 dark:bg-red-900/30 dark:hover:bg-red-900/50 text-red-500 dark:text-red-400 cursor-pointer mr-auto')
        .text('Clear')
        .css('display', hasActiveFilter ? '' : 'none');

    var $cancel = $('<button>').attr('type', 'button')
        .addClass('text-xs px-3 py-1 rounded border border-gray-300 dark:border-gray-600 text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800 cursor-pointer')
        .text('Cancel');
    var $apply = $('<button>').attr('type', 'button')
        .addClass('text-xs px-3 py-1 rounded bg-blue-600 text-white hover:bg-blue-700 cursor-pointer ml-auto')
        .text('Apply');

    $clear.on('click', function () {
        // Décocher tout + appliquer → efface le filtre et ferme
        $pop.find('.col-filter-item input[type="checkbox"]').prop('checked', false);
        _applyFilter({ tableId, oSettings, clientSide, index, value: opts.value, table, isLike, allcolumnSearchValues, $pop, $searchInput, $triggerBtn });
        ColumnSearchPopover.close();
    });

    $cancel.on('click', function () { ColumnSearchPopover.close(); });

    $apply.on('click', function () {
        _applyFilter({ tableId, oSettings, clientSide, index, value: opts.value, table, isLike, allcolumnSearchValues, $pop, $searchInput, $triggerBtn });
        ColumnSearchPopover.close();
    });

    $footer.append($clear, $cancel, $apply);
    $pop.append($footer);

    // Exposer $clear pour le mettre à jour après _applyFilter
    $pop.data('clearBtn', $clear);

    // ── Recherche dans la liste ──────────────────────────────────────────────
    // Dès le premier caractère saisi : uncheck tout sauf les items matchants,
    // puis recheck uniquement les items visibles (comportement "filtre rapide").
    if (!isLike) {
        var _searchDirty = false; // true dès que l'user a commencé à taper

        $searchInput.on('input', function () {
            var q = $(this).val().toLowerCase();

            if (q === '') {
                // Champ vidé → restaurer l'état coché précédent
                _searchDirty = false;
                $pop.find('.col-filter-item').show();
                // On ne re-coche pas tout : on laisse l'état tel qu'il était avant la saisie
                // (géré par _applyCheckedState à l'ouverture du popover)
            } else {
                _searchDirty = true;
                // Décocher + masquer tout, puis cocher + afficher les matchs
                $pop.find('.col-filter-item').each(function () {
                    var txt = $(this).find('span').text().toLowerCase();
                    var matches = txt.indexOf(q) >= 0;
                    $(this).find('input[type="checkbox"]').prop('checked', matches);
                    $(this).toggle(matches);
                });
            }
        });

        // Réinitialiser le flag quand le popover se ferme (via clearBtn ou cancel)
        $pop.data('resetSearchDirty', function () { _searchDirty = false; });
    }

    // ── TAB / ENTER : valider et ouvrir le filtre de la colonne suivante ──────
    $searchInput.on('keydown', function (e) {
        var key = e.keyCode || e.which;
        if (key === 13 || key === 9) { // ENTER ou TAB
            // 1. Appliquer le filtre
            _applyFilter({ tableId: opts.tableId, oSettings: opts.oSettings, clientSide: opts.clientSide,
                index: opts.index, value: opts.value, table: opts.table, isLike: isLike,
                allcolumnSearchValues: allcolumnSearchValues, $pop: $pop,
                $searchInput: $searchInput, $triggerBtn: $triggerBtn });

            // 2. Identifier le trigger suivant — sauter les colonnes masquées
            //    On trie tous les triggers visibles par data-col-index et on prend
            //    le premier dont l'index est strictement supérieur au courant.
            var currentColIdx = parseInt($triggerBtn.attr('data-col-index'), 10);
            var candidates = [];
            $('.col-filter-trigger').each(function () {
                var idx = parseInt($(this).attr('data-col-index'), 10);
                if (!isNaN(idx) && idx > currentColIdx) {
                    candidates.push({ idx: idx, el: this });
                }
            });
            candidates.sort(function (a, b) { return a.idx - b.idx; });
            var $next = candidates.length ? $(candidates[0].el) : $();

            // 3. Fermer le popover courant
            ColumnSearchPopover.close();

            // 4. Ouvrir le suivant — requestAnimationFrame garantit que le close est peint
            //    avant qu'on calcule la position du prochain anchor
            if ($next.length) {
                requestAnimationFrame(function () {
                    $next.trigger('click');
                });
            }

            return false;
        }
        if (key === 27) { // ESC
            ColumnSearchPopover.close();
            return false;
        }
    });

    return $pop;
}


// ─── Pré-cocher les valeurs actives ──────────────────────────────────────────
function _applyCheckedState($pop, activeValues, isLike) {
    if (isLike) return; // rien à cocher en mode LIKE

    var $items = $pop.find('.col-filter-item input[type="checkbox"]');

    if (activeValues === undefined) {
        // Aucun filtre actif → tout coché
        $items.prop('checked', true);
    } else {
        $items.prop('checked', false);
        $.each(activeValues, function (i, v) {
            $items.filter('[value="' + v + '"]').prop('checked', true);
        });
    }
}


// ─── Chargement serveur des valeurs distinctes ────────────────────────────────
function _loadServerSources(opts, $pop, allcolumnSearchValues) {
    var oSettings  = opts.oSettings;
    var index      = opts.index;
    var title      = opts.title;
    var contentUrl = opts.contentUrl;
    var table      = opts.table;
    var data       = opts.data;

    var urlSeparator = contentUrl.indexOf('?') > -1 ? '&' : '?';
    var url = './' + contentUrl + urlSeparator + 'columnName=' + title + getUser().defaultSystemsQuery;

    var params = table.ajax.params();
    var like   = '';
    $.each(oSettings.aoColumns, function (i, col) {
        if (col.like) like += col.sName + ',';
    });
    params['sLike'] = like.slice(0, -1);

    var $list = $pop.find('.col-filter-list');
    $list.html('<span class="text-xs text-gray-400 p-2">Loading…</span>');

    $.ajax({
        type     : 'POST',
        async    : true,
        url      : url,
        data     : params,
        success  : function (responseObject) {
            var result;
            if (responseObject.distinctValues !== undefined) {
                result = responseObject.distinctValues;
            } else {
                try {
                    var parsed = JSON.parse(responseObject);
                    result = parsed.distinctValues || data;
                } catch (e) {
                    result = data;
                }
            }
            $list.empty();
            $.each(result, function (i, d) {
                var safeVal = $('<p>' + d + '</p>').text();
                var $item   = $('<label>').addClass('col-filter-item flex items-center gap-2 text-sm cursor-pointer px-1 py-0.5 rounded hover:bg-gray-100 dark:hover:bg-gray-800');
                var $cb     = $('<input type="checkbox">').val(safeVal).addClass('accent-blue-600');
                var $lbl    = $('<span>').addClass('truncate text-gray-700 dark:text-gray-200').text(safeVal);
                $item.append($cb, $lbl);
                $list.append($item);
            });
            $pop.data('sourcesLoaded', true);
            // Utiliser allcolumnSearchValues courant (pas opts qui est figé à l'init)
            var activeVals = allcolumnSearchValues ? allcolumnSearchValues[opts.value] : opts.allcolumnSearchValues[opts.value];
            _applyCheckedState($pop, activeVals, false);
            ColumnSearchPopover.reposition();
        },
        error: function () {
            $list.html('<span class="text-xs text-red-400 p-2">Error loading values</span>');
        }
    });
}


// ─── Appliquer le filtre au DataTable ────────────────────────────────────────
function _applyFilter(params) {
    var tableId               = params.tableId;
    var oSettings             = params.oSettings;
    var clientSide            = params.clientSide;
    var index                 = params.index;
    var value                 = params.value;
    var table                 = params.table;
    var isLike                = params.isLike;
    var allcolumnSearchValues = params.allcolumnSearchValues;
    var $pop                  = params.$pop;
    var $searchInput          = params.$searchInput;
    var $triggerBtn           = params.$triggerBtn;

    if (isLike) {
        // Mode LIKE : filtre sur la valeur du champ texte
        var likeVal = [$searchInput.val()];
        allcolumnSearchValues[value] = likeVal[0] !== '' ? likeVal : undefined;
        $('#' + tableId).dataTable().fnFilter(likeVal, index);
    } else {
        // Mode checklist
        var newValue = [];
        $pop.find('.col-filter-item input[type="checkbox"]:checked').each(function () {
            newValue.push($(this).val());
        });

        allcolumnSearchValues[value] = newValue.length > 0 ? newValue : undefined;

        if (clientSide) {
            columnSearchValuesForClientSide[index] = newValue;
            var filterStr = newValue.map(function (v) { return v; }).join('|');
            $('#' + tableId).dataTable().fnFilter(filterStr ? '(' + filterStr + ')' : '', index, true);
        } else {
            $('#' + tableId).dataTable().fnFilter(newValue, index);
        }
    }

    // ── Mettre à jour l'icône trigger ───────────────────────────────────────
    var hasActive  = allcolumnSearchValues[value] !== undefined && allcolumnSearchValues[value].length > 0;
    var activeVals = hasActive ? allcolumnSearchValues[value] : [];

    $triggerBtn
        .attr('title', activeVals.join(', '))
        .removeClass('text-blue-500 dark:text-blue-400 text-gray-300 dark:text-gray-600 hover:text-gray-500 dark:hover:text-gray-400')
        .addClass(hasActive
            ? 'text-blue-500 dark:text-blue-400'
            : 'text-gray-300 dark:text-gray-600 hover:text-gray-500 dark:hover:text-gray-400');

    // Afficher/masquer le bouton Clear dans le footer selon l'état du filtre
    var $clearBtn = $pop.data('clearBtn');
    if ($clearBtn) {
        $clearBtn.css('display', hasActive ? '' : 'none');
    }
}

var lastSearchInput = null;
var handlerSearchInputClick = [];

function  focusOnNextSearchInput(startElement) {
    lastSearchInput = null;
    if (startElement === null)
        return;

    startElement = $("#" + startElement.id);

    if (startElement[0].name !== "searchField")
        return; // if start element is not an input "searchField", do nothing

    //get all text inputs
    var inputs = $('input[name="searchField"]');

    //search inputs for one that comes after starting element
    for (var i = 0; i < inputs.length; i++) {
        if (isAfter(inputs[i], startElement)) {
            var nextInput = inputs[i];
            if ($(':focus') == $(nextInput)) { // prevent infinite loop
                break;
            }
            $(nextInput).click();
            $(nextInput).click(); // double click is important !!!
            break;
        }
    }
}

//is element before or after
function isAfter(elA, elB) {
    return ($('*').index($(elA).last()) > $('*').index($(elB).first()));
}