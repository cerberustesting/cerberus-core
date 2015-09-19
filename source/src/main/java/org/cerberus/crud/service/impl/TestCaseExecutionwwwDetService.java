/*
 * Cerberus  Copyright (C) 2013  vertigo17
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
package org.cerberus.crud.service.impl;

import edu.umass.cs.benchlab.har.HarEntry;
import edu.umass.cs.benchlab.har.HarLog;
import edu.umass.cs.benchlab.har.tools.HarFileReader;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import org.apache.log4j.Level;
import org.cerberus.crud.dao.ITestCaseExecutionwwwDetDAO;
import org.cerberus.crud.entity.StatisticDetail;
import org.cerberus.crud.entity.TestCase;
import org.cerberus.crud.entity.TestCaseExecutionwwwDet;
import org.cerberus.crud.entity.TestCaseExecutionwwwSumHistoric;
import org.cerberus.log.MyLogger;
import org.cerberus.crud.service.ITestCaseExecutionwwwDetService;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.util.ShapeUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class TestCaseExecutionwwwDetService implements ITestCaseExecutionwwwDetService {

    @Autowired
    private ITestCaseExecutionwwwDetDAO testCaseExecutionWWWDetDAO;

    @Override
    public void registerDetail(long runId, String file, String page) {
        try {
            JsonFactory f = new JsonFactory();
            JsonParser jp = f.createJsonParser(file);

            HarFileReader r = new HarFileReader();
            HarLog log = r.readHarFile(jp, null);
            StatisticDetail detail;

            for (HarEntry entry : log.getEntries().getEntries()) {
                detail = new StatisticDetail();
                detail.setStart(entry.getStartedDateTime().getTime());
                detail.setTime(entry.getTime());
                detail.setEnd(entry.getStartedDateTime().getTime() + entry.getTime());
                detail.setPageRes(page);
                detail.setUrl(entry.getRequest().getUrl());
                detail.setExt(entry.getResponse().getContent().getMimeType().split("/")[1]);
                detail.setStatus(entry.getResponse().getStatus());
                detail.setMethod(entry.getRequest().getMethod());
                detail.setBytes(entry.getRequest().getHeadersSize() + entry.getRequest().getBodySize());
                detail.setHostReq(entry.getRequest().getUrl().split("/")[2]);
                detail.setContentType();

                this.testCaseExecutionWWWDetDAO.register(runId, detail);
            }
        } catch (IOException exception) {
            MyLogger.log(TestCaseExecutionwwwDetService.class.getName(), Level.WARN, exception.toString() + "\nrunID: " + runId);
        }
    }

    @Override
    public List<TestCaseExecutionwwwDet> getListOfDetail(int id) {
        return testCaseExecutionWWWDetDAO.getListOfDetail(id);
    }

    @Override
    public BufferedImage getHistoricOfParameter(TestCase testcase, String parameter) {
        List<TestCaseExecutionwwwSumHistoric> historic = testCaseExecutionWWWDetDAO.getHistoricForParameter(testcase, parameter);
        BufferedImage result = null;
        DefaultCategoryDataset defaultcategorydataset = new DefaultCategoryDataset();
        TimeSeriesCollection timeseriescollection = new TimeSeriesCollection();
        /*Create timeseries with the data*/
        String timeseriesname = parameter;
        TimeSeries timeseries = new TimeSeries(timeseriesname, Minute.class);
        for (TestCaseExecutionwwwSumHistoric ep : historic) {
            defaultcategorydataset.addValue(Float.valueOf(ep.getParameter()), parameter, ep.getStart());
            if (!ep.getStart().equals("2011-01-01 00:00")) {
                String tims = ep.getStart();
                int year = Integer.valueOf(tims.substring(0, 4));
                int month = Integer.valueOf(tims.substring(5, 7));
                int day = Integer.valueOf(tims.substring(8, 10));
                int hour = Integer.valueOf(tims.substring(11, 13));
                int min = Integer.valueOf(tims.substring(14, 16));
                float value = Float.valueOf(ep.getParameter());
                timeseries.addOrUpdate(new Minute(min, hour, day, month, year), value);
            }
        }
        timeseriescollection.addSeries(timeseries);
        result = this.bi(timeseriescollection, "test", parameter, 1);
        return result;
    }

    private BufferedImage bi(TimeSeriesCollection timeseriescollection, String xname, String name, int count) {
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
