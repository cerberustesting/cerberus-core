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
    /**
     * Class: $.jqplot.ciParser
     * Data Renderer function which converts a custom JSON data object into jqPlot data format.
     * Set this as a callable on the jqplot dataRenderer plot option:
     * 
     * > plot = $.jqplot('mychart', [data], { dataRenderer: $.jqplot.ciParser, ... });
     * 
     * Where data is an object in JSON format or a JSON encoded string conforming to the
     * City Index API spec.
     * 
     * Note that calling the renderer function is handled internally by jqPlot.  The
     * user does not have to call the function.  The parameters described below will
     * automatically be passed to the ciParser function.
     * 
     * Parameters:
     * data - JSON encoded string or object.
     * plot - reference to jqPlot Plot object.
     * 
     * Returns:
     * data array in jqPlot format.
     * 
     */
    $.jqplot.ciParser = function (data, plot) {
        var ret = [],
            line,
			temp,
            i, j, k, kk;
    
         if (typeof(data) == "string") {
             data =  $.jqplot.JSON.parse(data, handleStrings);
         }
 
         else if (typeof(data) == "object") {
             for (k in data) {
                 for (i=0; i<data[k].length; i++) {
                     for (kk in data[k][i]) {
                         data[k][i][kk] = handleStrings(kk, data[k][i][kk]);
                     }
                 }
             }
         }
 
         else {
             return null;
         }
 
         // function handleStrings
         // Checks any JSON encoded strings to see if they are
         // encoded dates.  If so, pull out the timestamp.
         // Expects dates to be represented by js timestamps.
 
         function handleStrings(key, value) {
            var a;
            if (value != null) {
                if (value.toString().indexOf('Date') >= 0) {
                    //here we will try to extract the ticks from the Date string in the "value" fields of JSON returned data
                    a = /^\/Date\((-?[0-9]+)\)\/$/.exec(value);
                    if (a) {
                        return parseInt(a[1], 10);
                    }
                }
                return value;
            }
         }
 
        for (var prop in data) {
            line = [];
            temp = data[prop];
            switch (prop) {
                case "PriceTicks":
                    for (i=0; i<temp.length; i++) {
                        line.push([temp[i]['TickDate'], temp[i]['Price']]);
                    }
                    break;
                case "PriceBars":
                    for (i=0; i<temp.length; i++) {
                        line.push([temp[i]['BarDate'], temp[i]['Open'], temp[i]['High'], temp[i]['Low'], temp[i]['Close']]);
                    }
                    break;
            }
            ret.push(line);
        }
        return ret;
    };
})(jQuery);