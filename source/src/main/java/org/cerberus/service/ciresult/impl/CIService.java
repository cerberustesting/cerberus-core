/**
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
package org.cerberus.service.ciresult.impl;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.crud.service.ITestCaseExecutionService;
import org.cerberus.exception.CerberusException;
import org.cerberus.service.ciresult.ICIService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class CIService implements ICIService {

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(CIService.class);

    @Autowired
    private ITestCaseExecutionService testExecutionService;
    @Autowired
    private IParameterService parameterService;

    @Override
    public JSONObject getCIResult(String tag) {

        try {
            JSONObject jsonResponse = new JSONObject();

            List<TestCaseExecution> myList;

            int nbok = 0;
            int nbko = 0;
            int nbfa = 0;
            int nbpe = 0;
            int nbne = 0;
            int nbna = 0;
            int nbca = 0;
            int nbqu = 0;
            int nbqe = 0;
            int nbtotal = 0;

            int nbkop1 = 0;
            int nbkop2 = 0;
            int nbkop3 = 0;
            int nbkop4 = 0;

            long longStart = 0;
            long longEnd = 0;

            try {
                myList = testExecutionService.readLastExecutionAndExecutionInQueueByTag(tag);

                for (TestCaseExecution curExe : myList) {

                    if (longStart == 0) {
                        longStart = curExe.getStart();
                    }
                    if (curExe.getStart() < longStart) {
                        longStart = curExe.getStart();
                    }

                    if (longEnd == 0) {
                        longEnd = curExe.getEnd();
                    }
                    if (curExe.getEnd() > longEnd) {
                        longEnd = curExe.getEnd();
                    }

                    nbtotal++;

                    switch (curExe.getControlStatus()) {
                        case TestCaseExecution.CONTROLSTATUS_KO:
                            nbko++;
                            break;
                        case TestCaseExecution.CONTROLSTATUS_OK:
                            nbok++;
                            break;
                        case TestCaseExecution.CONTROLSTATUS_FA:
                            nbfa++;
                            break;
                        case TestCaseExecution.CONTROLSTATUS_NA:
                            nbna++;
                            break;
                        case TestCaseExecution.CONTROLSTATUS_CA:
                            nbca++;
                            break;
                        case TestCaseExecution.CONTROLSTATUS_PE:
                            nbpe++;
                            break;
                        case TestCaseExecution.CONTROLSTATUS_NE:
                            nbne++;
                            break;
                        case TestCaseExecution.CONTROLSTATUS_QU:
                            nbqu++;
                            break;
                        case TestCaseExecution.CONTROLSTATUS_QE:
                            nbqe++;
                            break;
                    }

                    if (!curExe.getControlStatus().equals("OK") && !curExe.getControlStatus().equals("NE")
                            && !curExe.getControlStatus().equals("PE") && !curExe.getControlStatus().equals("QU")) {
                        switch (curExe.getTestCaseObj().getPriority()) {
                            case 1:
                                nbkop1++;
                                break;
                            case 2:
                                nbkop2++;
                                break;
                            case 3:
                                nbkop3++;
                                break;
                            case 4:
                                nbkop4++;
                                break;
                        }
                    }
                }

            } catch (CerberusException ex) {
                LOG.warn(ex);
            } catch (ParseException ex) {
                LOG.warn(ex);
            }

            float pond1 = parameterService.getParameterFloatByKey("cerberus_ci_okcoefprio1", "", 0);
            float pond2 = parameterService.getParameterFloatByKey("cerberus_ci_okcoefprio2", "", 0);
            float pond3 = parameterService.getParameterFloatByKey("cerberus_ci_okcoefprio3", "", 0);
            float pond4 = parameterService.getParameterFloatByKey("cerberus_ci_okcoefprio4", "", 0);
            String result;
            float resultCal = (nbkop1 * pond1) + (nbkop2 * pond2) + (nbkop3 * pond3) + (nbkop4 * pond4);
            if ((nbtotal > 0) && nbqu + nbpe > 0) {
                result = "PE";
            } else if ((resultCal < 1) && (nbtotal > 0) && nbok > 0) {
                result = "OK";
            } else {
                result = "KO";
            }

            jsonResponse.put("messageType", "OK");
            jsonResponse.put("message", "CI result calculated with success.");
            jsonResponse.put("tag", tag);
            jsonResponse.put("CI_OK_prio1", pond1);
            jsonResponse.put("CI_OK_prio2", pond2);
            jsonResponse.put("CI_OK_prio3", pond3);
            jsonResponse.put("CI_OK_prio4", pond4);
            jsonResponse.put("CI_finalResult", resultCal);
            jsonResponse.put("NonOK_prio1_nbOfExecution", nbkop1);
            jsonResponse.put("NonOK_prio2_nbOfExecution", nbkop2);
            jsonResponse.put("NonOK_prio3_nbOfExecution", nbkop3);
            jsonResponse.put("NonOK_prio4_nbOfExecution", nbkop4);
            jsonResponse.put("status_OK_nbOfExecution", nbok);
            jsonResponse.put("status_KO_nbOfExecution", nbko);
            jsonResponse.put("status_FA_nbOfExecution", nbfa);
            jsonResponse.put("status_PE_nbOfExecution", nbpe);
            jsonResponse.put("status_NA_nbOfExecution", nbna);
            jsonResponse.put("status_CA_nbOfExecution", nbca);
            jsonResponse.put("status_NE_nbOfExecution", nbne);
            jsonResponse.put("status_QU_nbOfExecution", nbqu);
            jsonResponse.put("status_QE_nbOfExecution", nbqe);
            jsonResponse.put("TOTAL_nbOfExecution", nbtotal);
            jsonResponse.put("result", result);
            jsonResponse.put("ExecutionStart", String.valueOf(new Timestamp(longStart)));
            jsonResponse.put("ExecutionEnd", String.valueOf(new Timestamp(longEnd)));

            return jsonResponse;

        } catch (JSONException ex) {
            LOG.error(ex);
        }
        return null;
    }

}
