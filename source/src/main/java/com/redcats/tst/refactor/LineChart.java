/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.refactor;


import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.util.ShapeUtilities;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;

public class LineChart {

    public BufferedImage bi(CategoryDataset categorydataset, String name, int count) {

        BufferedImage bi = null;


        JFreeChart jfreechart = ChartFactory.createLineChart(name, "Category", "Count", categorydataset, PlotOrientation.VERTICAL, true, true, false);
        CategoryPlot categoryplot = (CategoryPlot) jfreechart.getPlot();
        NumberAxis numberaxis = (NumberAxis) categoryplot.getRangeAxis();
        numberaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        Shape point = ShapeUtilities.createDiagonalCross(1, 1);
        //TODO check this - seriesColors never used
        String[] seriesColors = {"#FF0000", "#D7D6F6", "#0F07F3", "#EEFFBD", "#75C53E", "#FED7BA", "#FE6F01"};
        String[] seriesColors2 = {"#D7D6F6", "#0F07F3", "#EEFFBD", "#75C53E", "#FED7BA", "#FE6F01"};


        LineAndShapeRenderer lineandshaperenderer = (LineAndShapeRenderer) categoryplot.getRenderer();
        lineandshaperenderer.setBaseShapesVisible(true);
        lineandshaperenderer.setBaseShapesFilled(true);
        for (int a = 0; a < count; a++) {
            lineandshaperenderer.setSeriesShapesVisible(a, true);
            lineandshaperenderer.setSeriesLinesVisible(a, true);
            lineandshaperenderer.setSeriesStroke(a, new BasicStroke(1.0F));
            lineandshaperenderer.setSeriesShape(a, point);
        }

        lineandshaperenderer.setDrawOutlines(true);
        lineandshaperenderer.setUseFillPaint(true);
        lineandshaperenderer.setBaseFillPaint(Color.white);

        //DateAxis dateaxis = (DateAxis)xyplot.getDomainAxis();
        //dateaxis.setDateFormatOverride(new SimpleDateFormat("hh:mm"));

        bi = jfreechart.createBufferedImage(500, 270);


        return bi;

    }

    public BufferedImage bi(TimeSeriesCollection timeseriescollection, String xname, String name, int count) {

        BufferedImage bi = null;
        boolean fc = false;
        XYDataset xydataset = timeseriescollection;

        JFreeChart jfreechart = ChartFactory.createTimeSeriesChart(name, xname, name, xydataset, true, true, false);
        XYPlot xyplot = (XYPlot) jfreechart.getPlot();
        xyplot.setDomainCrosshairVisible(true);
        xyplot.setRangeCrosshairVisible(false);
        XYItemRenderer xyitemrenderer = xyplot.getRenderer();
        if (xyitemrenderer instanceof XYLineAndShapeRenderer) {
            Shape point = ShapeUtilities.createDiagonalCross(1, 1);
            String[] seriesColors = {"#FF0000", "#D7D6F6", "#0F07F3", "#EEFFBD", "#75C53E", "#FED7BA", "#FE6F01"};
            String[] seriesColors2 = {"#D7D6F6", "#0F07F3", "#EEFFBD", "#75C53E", "#FED7BA", "#FE6F01"};
            XYLineAndShapeRenderer xylineandshaperenderer = (XYLineAndShapeRenderer) xyitemrenderer;
            xylineandshaperenderer.setBaseShapesVisible(true);
            xylineandshaperenderer.setBaseShapesFilled(true);
            for (int a = 0; a < count; a++) {
                xylineandshaperenderer.setSeriesShape(a, point);
                xyitemrenderer.setSeriesStroke(a, new BasicStroke(1.0F));
                //TODO check this - fc is always false
                if (fc) {
                    xylineandshaperenderer.setSeriesPaint(a, Color.decode(seriesColors[count - a - 1]));
                } else {
                    xylineandshaperenderer.setSeriesPaint(a, Color.decode(seriesColors2[count - a - 1]));
                }
            }

        }
        DateAxis dateaxis = (DateAxis) xyplot.getDomainAxis();
        dateaxis.setDateFormatOverride(new SimpleDateFormat("hh:mm"));

        bi = jfreechart.createBufferedImage(500, 270);


        return bi;

    }
}