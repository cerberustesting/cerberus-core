(function(){
	"use strict";

	var root = this,
		Chart = root.Chart,
		helpers = Chart.helpers;

        // Notice now we're extending the particular Line chart type, rather than the base class.
        Chart.types.Bar.extend({
            // Passing in a name registers this chart in the Chart namespace in the same way
            name: "BarColors",
            initialize: function (data) {
                Chart.types.Bar.prototype.initialize.apply(this, arguments);
                this.eachBars(function (bar, index, datasetIndex) {
                    helpers.extend(bar, {
                        fillColor: data.datasets[datasetIndex].fillColors[index] || data.datasets[datasetIndex].fillColor,
                        strokeColor: data.datasets[datasetIndex].strokeColors[index] || data.datasets[datasetIndex].strokeColor,
                        highlightFill: data.datasets[datasetIndex].highlightFills[index] || data.datasets[datasetIndex].highlightFill,
                        highlightStroke: data.datasets[datasetIndex].highlightStrokes[index] || data.datasets[datasetIndex].highlightStroke
                    });

                    bar.save();
                }, this);
            }
        });
}).call(this);
