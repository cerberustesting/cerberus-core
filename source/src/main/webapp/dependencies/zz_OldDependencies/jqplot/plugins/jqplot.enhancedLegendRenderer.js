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
(function($) {
    // class $.jqplot.EnhancedLegendRenderer
    // Legend renderer which can specify the number of rows and/or columns in the legend.
    $.jqplot.EnhancedLegendRenderer = function(){
        $.jqplot.TableLegendRenderer.call(this);
    };
    
    $.jqplot.EnhancedLegendRenderer.prototype = new $.jqplot.TableLegendRenderer();
    $.jqplot.EnhancedLegendRenderer.prototype.constructor = $.jqplot.EnhancedLegendRenderer;
    
    // called with scope of legend.
    $.jqplot.EnhancedLegendRenderer.prototype.init = function(options) {
        // prop: numberRows
        // Maximum number of rows in the legend.  0 or null for unlimited.
        this.numberRows = null;
        // prop: numberColumns
        // Maximum number of columns in the legend.  0 or null for unlimited.
        this.numberColumns = null;
        // prop: seriesToggle
        // false to not enable series on/off toggling on the legend.
        // true or a fadein/fadeout speed (number of milliseconds or 'fast', 'normal', 'slow') 
        // to enable show/hide of series on click of legend item.
        this.seriesToggle = 'normal';
        // prop: disableIEFading
        // true to toggle series with a show/hide method only and not allow fading in/out.  
        // This is to overcome poor performance of fade in some versions of IE.
        this.disableIEFading = true;
        $.extend(true, this, options);
        
        if (this.seriesToggle) {
            $.jqplot.postDrawHooks.push(postDraw);
        }
    };
    
    // called with scope of legend
    $.jqplot.EnhancedLegendRenderer.prototype.draw = function() {
        var legend = this;
        if (this.show) {
            var series = this._series;
			var s;
            var ss = 'position:absolute;';
            ss += (this.background) ? 'background:'+this.background+';' : '';
            ss += (this.border) ? 'border:'+this.border+';' : '';
            ss += (this.fontSize) ? 'font-size:'+this.fontSize+';' : '';
            ss += (this.fontFamily) ? 'font-family:'+this.fontFamily+';' : '';
            ss += (this.textColor) ? 'color:'+this.textColor+';' : '';
            ss += (this.marginTop != null) ? 'margin-top:'+this.marginTop+';' : '';
            ss += (this.marginBottom != null) ? 'margin-bottom:'+this.marginBottom+';' : '';
            ss += (this.marginLeft != null) ? 'margin-left:'+this.marginLeft+';' : '';
            ss += (this.marginRight != null) ? 'margin-right:'+this.marginRight+';' : '';
            this._elem = $('<table class="jqplot-table-legend" style="'+ss+'"></table>');
            if (this.seriesToggle) {
                this._elem.css('z-index', '3');
            }
        
            var pad = false, 
                reverse = false,
                nr, nc;
            if (this.numberRows) {
                nr = this.numberRows;
                if (!this.numberColumns){
                    nc = Math.ceil(series.length/nr);
                }
                else{
                    nc = this.numberColumns;
                }
            }
            else if (this.numberColumns) {
                nc = this.numberColumns;
                nr = Math.ceil(series.length/this.numberColumns);
            }
            else {
                nr = series.length;
                nc = 1;
            }
                
            var i, j, tr, td1, td2, lt, rs, div, div0, div1;
            var idx = 0;
            // check to see if we need to reverse
            for (i=series.length-1; i>=0; i--) {
                if (nc == 1 && series[i]._stack || series[i].renderer.constructor == $.jqplot.BezierCurveRenderer){
                    reverse = true;
                }
            }    
                
            for (i=0; i<nr; i++) {
                tr = $(document.createElement('tr'));
                tr.addClass('jqplot-table-legend');
                if (reverse){
                    tr.prependTo(this._elem);
                }
                else{
                    tr.appendTo(this._elem);
                }
                for (j=0; j<nc; j++) {
                    if (idx < series.length && series[idx].show && series[idx].showLabel){
                        s = series[idx];
                        lt = this.labels[idx] || s.label.toString();
                        if (lt) {
                            var color = s.color;
                            if (!reverse){
                                if (i>0){
                                    pad = true;
                                }
                                else{
                                    pad = false;
                                }
                            }
                            else{
                                if (i == nr -1){
                                    pad = false;
                                }
                                else{
                                    pad = true;
                                }
                            }
                            rs = (pad) ? this.rowSpacing : '0';

                            td1 = $(document.createElement('td'));
                            td1.addClass('jqplot-table-legend jqplot-table-legend-swatch');
                            td1.css({textAlign: 'center', paddingTop: rs});

                            div0 = $(document.createElement('div'));
                            div0.addClass('jqplot-table-legend-swatch-outline');
                            div1 = $(document.createElement('div'));
                            div1.addClass('jqplot-table-legend-swatch');
                            div1.css({backgroundColor: color, borderColor: color});

                            td1.append(div0.append(div1));

                            td2 = $(document.createElement('td'));
                            td2.addClass('jqplot-table-legend jqplot-table-legend-label');
                            td2.css('paddingTop', rs);
                    
                            // td1 = $('<td class="jqplot-table-legend" style="text-align:center;padding-top:'+rs+';">'+
                            //     '<div><div class="jqplot-table-legend-swatch" style="background-color:'+color+';border-color:'+color+';"></div>'+
                            //     '</div></td>');
                            // td2 = $('<td class="jqplot-table-legend" style="padding-top:'+rs+';"></td>');
                            if (this.escapeHtml){
                                td2.text(lt);
                            }
                            else {
                                td2.html(lt);
                            }
                            if (reverse) {
                                if (this.showLabels) {td2.prependTo(tr);}
                                if (this.showSwatches) {td1.prependTo(tr);}
                            }
                            else {
                                if (this.showSwatches) {td1.appendTo(tr);}
                                if (this.showLabels) {td2.appendTo(tr);}
                            }
                            
                            if (this.seriesToggle) {

                                // add an overlay for clicking series on/off
                                // div0 = $(document.createElement('div'));
                                // div0.addClass('jqplot-table-legend-overlay');
                                // div0.css({position:'relative', left:0, top:0, height:'100%', width:'100%'});
                                // tr.append(div0);

                                var speed;
                                if (typeof(this.seriesToggle) == 'string' || typeof(this.seriesToggle) == 'number') {
                                    if (!$.jqplot.use_excanvas || !this.disableIEFading) {
                                        speed = this.seriesToggle;
                                    }
                                } 
                                if (this.showSwatches) {
                                    td1.bind('click', {series:s, speed:speed}, handleToggle);
                                    td1.addClass('jqplot-seriesToggle');
                                }
                                if (this.showLabels)  {
                                    td2.bind('click', {series:s, speed:speed}, handleToggle);
                                    td2.addClass('jqplot-seriesToggle');
                                }
                            }
                            
                            pad = true;
                        }
                    }
                    idx++;
                }
                
                td1 = td2 = div0 = div1 = null;   
            }
        }
        return this._elem;
    };

    var handleToggle = function (ev) {
        ev.data.series.toggleDisplay(ev);
        if (ev.data.series.canvas._elem.hasClass('jqplot-series-hidden')) {
            $(this).addClass('jqplot-series-hidden');
            $(this).next('.jqplot-table-legend-label').addClass('jqplot-series-hidden');
            $(this).prev('.jqplot-table-legend-swatch').addClass('jqplot-series-hidden');

        }
        else {
            $(this).removeClass('jqplot-series-hidden');
            $(this).next('.jqplot-table-legend-label').removeClass('jqplot-series-hidden');
            $(this).prev('.jqplot-table-legend-swatch').removeClass('jqplot-series-hidden');
        }
    };
    
    // called with scope of plot.
    var postDraw = function () {
        if (this.legend.renderer.constructor == $.jqplot.EnhancedLegendRenderer && this.legend.seriesToggle){
            var e = this.legend._elem.detach();
            this.eventCanvas._elem.after(e);
        }
    };
})(jQuery);