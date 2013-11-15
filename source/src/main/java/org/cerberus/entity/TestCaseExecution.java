/* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
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
package org.cerberus.entity;

import java.util.List;
import java.util.Map;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 18/Dez/2012
 * @since 2.0.0
 */
public class TestCaseExecution {
    private String environmentRun;
    private String tag;
    private String output;
    private int verbose;
    private long runID;
    private Country countryExecute;
    private TestCase testCase;
    private List<Property> properties;
    private List<TestCaseCountryProperties> prop;
    private List<Step> steps;
    private MessageGeneral result;
    private long start;
    private long end;
    private Environment environmentTest;
    private Map<String, String> devEnvironment;

    public String getEnvironmentRun() {
        return this.environmentRun;
    }

    public void setEnvironmentRun(String tempEnvironmentRun) {
        this.environmentRun = tempEnvironmentRun;
    }

    public String getTag() {
        return this.tag;
    }

    public void setTag(String tempTag) {
        this.tag = tempTag;
    }

    public String getOutput() {
        return this.output;
    }

    public void setOutput(String tempOutput) {
        this.output = tempOutput;
    }

    public int getVerbose() {
        return this.verbose;
    }

    public void setVerbose(int tempVerbose) {
        this.verbose = tempVerbose;
    }

    public long getRunID() {
        return this.runID;
    }

    public void setRunID(long tempRunID) {
        this.runID = tempRunID;
    }

    public Country getCountryExecute() {
        return this.countryExecute;
    }

    public void setCountryExecute(Country tempCountryExecute) {
        this.countryExecute = tempCountryExecute;
    }

    public TestCase getTestCase() {
        return this.testCase;
    }

    public void setTestCase(TestCase tempTestCase) {
        this.testCase = tempTestCase;
    }

    public List<Property> getProperties() {
        return this.properties;
    }

    public void setProperties(List<Property> tempProperties) {
        this.properties = tempProperties;
    }

    public List<Step> getSteps() {
        return this.steps;
    }

    public void setSteps(List<Step> tempSteps) {
        this.steps = tempSteps;
    }

    public MessageGeneral getResult() {
        return this.result;
    }

    public void setResult(MessageGeneral result) {
        this.result = result;
    }

    public long getStart() {
        return this.start;
    }

    public void setStart(long tempStart) {
        this.start = tempStart;
    }

    public long getEnd() {
        return this.end;
    }

    public void setEnd(long tempEnd) {
        this.end = tempEnd;
    }

    public Environment getEnvironmentTest() {
        return this.environmentTest;
    }

    public void setEnvironmentTest(Environment tempEnvironmentTest) {
        this.environmentTest = tempEnvironmentTest;
    }

    public Map<String, String> getDevEnvironment() {
        return this.devEnvironment;
    }

    public void setDevEnvironment(Map<String, String> devEnvironment) {
        this.devEnvironment = devEnvironment;
    }

    public Property getPropertyOfSequence(String propertyName) {
        for (Property property : this.properties) {
            if (property.getName().equalsIgnoreCase(propertyName)) {
                return property;
            }
        }
        return null;
    }
    
    public TestCaseCountryProperties getProperty(String propertyName){
        for (TestCaseCountryProperties property : this.prop) {
            if (property.getProperty().equalsIgnoreCase(propertyName)) {
                return property;
            }
        }
        return null;
    }

    public List<TestCaseCountryProperties> getProp() {
        return prop;
    }

    public void setProp(List<TestCaseCountryProperties> prop) {
        this.prop = prop;
    }
    
    
}
