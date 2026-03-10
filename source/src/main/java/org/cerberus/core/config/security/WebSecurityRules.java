/**
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
package org.cerberus.core.config.security;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

public class WebSecurityRules {

    public static void applyRules(HttpSecurity http) throws Exception {
        var m = PathPatternRequestMatcher.withDefaults();

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(m.matcher("/api/public/**")).permitAll()
                // ── Public
                .requestMatchers(
                        m.matcher("/DatabaseMaintenance.jsp"),
                        m.matcher("/Documentation.jsp"),
                        m.matcher("/Login.jsp"),
                        m.matcher("/Logout.jsp"),
                        m.matcher("/Error.jsp"),
                        m.matcher("/index1.jsp"), m.matcher("/index2.jsp"),
                        m.matcher("/index3.jsp"), m.matcher("/index4.jsp"),
                        m.matcher("/ChangePassword.jsp"),
                        m.matcher("/RunTestCase"), m.matcher("/RunTestCaseV001"),
                        m.matcher("/GetNumberOfExecutions"),
                        m.matcher("/ResultCI"), m.matcher("/ResultCIV001"),
                        m.matcher("/ResultCIV002"), m.matcher("/ResultCIV003"),
                        m.matcher("/ResultCIV004"),
                        m.matcher("/NewRelease"),
                        m.matcher("/AddToExecutionQueue"),
                        m.matcher("/AddToExecutionQueueV001"),
                        m.matcher("/AddToExecutionQueueV002"),
                        m.matcher("/AddToExecutionQueueV003"),
                        m.matcher("/NewBuildRevisionV000"),
                        m.matcher("/DisableEnvironmentV000"),
                        m.matcher("/NewEnvironmentEventV000"),
                        m.matcher("/GetTagExecutions"),
                        m.matcher("/GetTestCasesV001"),
                        m.matcher("/ManageV001"),
                        m.matcher("/ForgotPassword"),
                        m.matcher("/ForgotPasswordEmailConfirmation"),
                        m.matcher("/ChangeUserPassword"),
                        m.matcher("/ReadApplicationObjectImage"),
                        m.matcher("/DummyRESTCall"), m.matcher("/DummyRESTCallEmpty"),
                        m.matcher("/j_security_check")
                ).permitAll()

                // ── TestRO
                .requestMatchers(
                        m.matcher("/TestCaseExecution.jsp"),
                        m.matcher("/TestCaseExecutionList.jsp"),
                        m.matcher("/ReportingExecutionByTag.jsp"),
                        m.matcher("/ReportingExecutionOverTime.jsp"),
                        m.matcher("/ReportingCampaignOverTime.jsp"),
                        m.matcher("/ReportingCampaignStatistics.jsp"),
                        m.matcher("/TestCaseExecutionQueueList.jsp"),
                        m.matcher("/Test.jsp"),
                        m.matcher("/TestCaseScript.jsp"),
                        m.matcher("/TestCaseList.jsp"),
                        m.matcher("/CampaignList.jsp"),
                        m.matcher("/SwaggerUI.jsp"),
                        m.matcher("/ManageExecutionPool"), m.matcher("/ReadExecutionPool"),
                        m.matcher("/ReadExecutionPools"),
                        m.matcher("/GetTestCaseList"),
                        m.matcher("/ReadTestCaseExecutionMedia"),
                        m.matcher("/TestcaseList"),
                        m.matcher("/GetDataForTestCaseSearch"),
                        m.matcher("/TCEwwwDetail"),
                        m.matcher("/GenerateGraph"),
                        m.matcher("/TestCaseActionExecutionDetail"),
                        m.matcher("/ExecutionPerBuildRevision"),
                        m.matcher("/GetStepUsedAsLibraryInOtherTestCasePerApplication"),
                        m.matcher("/ExportTestCase"),
                        m.matcher("/FindTestImplementationStatusPerApplication"),
                        m.matcher("/GetCountryForTestCase"),
                        m.matcher("/GetPropertiesForTestCase"),
                        m.matcher("/GetReport"),
                        m.matcher("/GetStepInLibrary"),
                        m.matcher("/ReadTestCase"), m.matcher("/ReadTest"),
                        m.matcher("/ReadTestCaseStep"),
                        m.matcher("/GetReportTest"),
                        m.matcher("/ReadTestCaseExecutionByTag"),
                        m.matcher("/ReadExecutionStat"),
                        m.matcher("/ReadQueueStat"),
                        m.matcher("/ReadTagStat")
                ).hasRole("TestRO")

                // ── Test
                .requestMatchers(
                        m.matcher("/SqlLibrary.jsp"),
                        m.matcher("/TestDataLibList.jsp"),
                        m.matcher("/QuickStart.jsp"),
                        m.matcher("/UpdateTestCase"), m.matcher("/UpdateTestCaseMass"),
                        m.matcher("/CreateTestCaseCountry"), m.matcher("/DeleteTestCaseCountry"),
                        m.matcher("/CalculatePropertyForTestCase"),
                        m.matcher("/UpdateProperties"),
                        m.matcher("/CreateTestCase"),
                        m.matcher("/CreateTestCaseLabel"), m.matcher("/DeleteTestCaseLabel"),
                        m.matcher("/ImportSeleniumIDE"),
                        m.matcher("/ImportTestCaseStep"),
                        m.matcher("/ImportPropertyOfATestCaseToAnOtherTestCase"),
                        m.matcher("/CreateNotDefinedProperty"),
                        m.matcher("/DeleteTestData"), m.matcher("/UpdateTestData"),
                        m.matcher("/FindAllTestData"),
                        m.matcher("/ReadTestDataLib"), m.matcher("/ReadTestDataLibData"),
                        m.matcher("/ReadSqlLibrary"),
                        m.matcher("/PictureConnector"),
                        m.matcher("/UseTestCaseStep"),
                        m.matcher("/UpdateTestCaseWithDependencies"),
                        m.matcher("/UpdateTestCaseProperties"),
                        m.matcher("/Thumbnailer"),
                        m.matcher("/SaveTestCaseLabel"), m.matcher("/ReadTestCaseLabel")
                ).hasRole("Test")

                // ── TestStepLibrary
                .requestMatchers(m.matcher("/ZZZ")).hasRole("TestStepLibrary")

                // ── TestAdmin
                .requestMatchers(
                        m.matcher("/CreateTest"), m.matcher("/DeleteTest"),
                        m.matcher("/UpdateTest"),
                        m.matcher("/DeleteTestCase"), m.matcher("/DeleteTestCaseFromTestPage"),
                        m.matcher("/CreateSqlLibrary"), m.matcher("/DeleteSqlLibrary"),
                        m.matcher("/UpdateSqlLibrary")
                ).hasRole("TestAdmin")

                // ── Label
                .requestMatchers(
                        m.matcher("/Label.jsp"),
                        m.matcher("/CreateLabel"), m.matcher("/UpdateLabel"),
                        m.matcher("/DeleteLabel")
                ).hasRole("Label")

                // ── AS
                .requestMatchers(
                        m.matcher("/ReportingAutomateScore.jsp")
                ).hasRole("AS")

                // ── RunTest
                .requestMatchers(
                        m.matcher("/RunTests.jsp"),
                        m.matcher("/findEnvironmentByCriteria"),
                        m.matcher("/UpdateTestCaseExecution"),
                        m.matcher("/RunExecutionInQueue"),
                        m.matcher("/SetTagToExecution"),
                        m.matcher("/GetExecutionQueue"),
                        m.matcher("/UpdateTestCaseExecutionQueue"),
                        m.matcher("/ReadTestCaseExecutionQueue"),
                        m.matcher("/CreateTestCaseExecutionQueue"),
                        m.matcher("/CreateUpdateTestCaseExecutionFile"),
                        m.matcher("/DeleteTestCaseExecutionFile"),
                        m.matcher("/ReadCampaign"), m.matcher("/ReadCampaignParameter"),
                        m.matcher("/GetCampaign"), m.matcher("/UpdateCampaign"),
                        m.matcher("/CreateCampaign"), m.matcher("/DeleteCampaign"),
                        m.matcher("/CreateScheduleEntry"), m.matcher("/ReadScheduleEntry"),
                        m.matcher("/UpdateScheduleEntry"), m.matcher("/DeleteScheduleEntry"),
                        m.matcher("/AddToExecutionQueuePrivate")
                ).hasRole("RunTest")

                // ── TestDataManager
                .requestMatchers(
                        m.matcher("/CreateTestDataLib"),
                        m.matcher("/DuplicateTestDataLib"),
                        m.matcher("/ImportTestDataLib"),
                        m.matcher("/DeleteTestDataLib"),
                        m.matcher("/UpdateTestDataLibData"),
                        m.matcher("/UpdateTestDataLib"),
                        m.matcher("/BulkRenameDataLib")
                ).hasRole("TestDataManager")

                // ── IntegratorRO
                .requestMatchers(
                        m.matcher("/ApplicationList.jsp"),
                        m.matcher("/AppServiceList.jsp"),
                        m.matcher("/ApplicationObjectList.jsp"),
                        m.matcher("/BuildContent.jsp"),
                        m.matcher("/BuildRevDefinition.jsp"),
                        m.matcher("/Environment.jsp"),
                        m.matcher("/IntegrationStatus.jsp"),
                        m.matcher("/Project.jsp"),
                        m.matcher("/DeployType.jsp"),
                        m.matcher("/BatchInvariant.jsp"),
                        m.matcher("/GetShortTests"),
                        m.matcher("/GetInvariantsForTest"),
                        m.matcher("/GetEnvironmentAvailable"),
                        m.matcher("/FindBuildContent"),
                        m.matcher("/FindCountryEnvironmentDatabase"),
                        m.matcher("/GetCountryEnvParamList"),
                        m.matcher("/GetCountryEnvironmentParameterList"),
                        m.matcher("/FindEnvironments"),
                        m.matcher("/ReadDeployType"),
                        m.matcher("/GetNotification"),
                        m.matcher("/ReadBuildRevisionParameters"),
                        m.matcher("/ReadCountryEnvParam_log"),
                        m.matcher("/ReadBuildRevisionBatch"),
                        m.matcher("/ReadBatchInvariant"),
                        m.matcher("/ReadCountryEnvDeployType"),
                        m.matcher("/ReadCountryEnvironmentDatabase"),
                        m.matcher("/ReadCountryEnvironmentParameters"),
                        m.matcher("/ReadCountryEnvLink")
                ).hasRole("IntegratorRO")

                // ── Integrator
                .requestMatchers(
                        m.matcher("/CreateApplication"), m.matcher("/UpdateApplication"),
                        m.matcher("/CreateApplicationObject"), m.matcher("/UpdateApplicationObject"),
                        m.matcher("/DeleteApplicationObject"),
                        m.matcher("/UpdateCountryEnv"),
                        m.matcher("/CreateProject"), m.matcher("/DeleteProject"),
                        m.matcher("/UpdateProject"),
                        m.matcher("/CreateBuildRevisionInvariant"),
                        m.matcher("/UpdateBuildRevisionInvariant"),
                        m.matcher("/DeleteBuildRevisionInvariant"),
                        m.matcher("/CreateRobot"), m.matcher("/UpdateRobot"),
                        m.matcher("/DeleteRobot"),
                        m.matcher("/CreateCountryEnvParam"), m.matcher("/UpdateCountryEnvParam"),
                        m.matcher("/DeleteCountryEnvParam"),
                        m.matcher("/CreateCountryEnvironmentParameter"),
                        m.matcher("/UpdateCountryEnvironmentParameter"),
                        m.matcher("/DeleteCountryEnvironmentParameter"),
                        m.matcher("/CreateCountryEnvironmentDatabase"),
                        m.matcher("/UpdateCountryEnvironmentDatabase"),
                        m.matcher("/DeleteCountryEnvironmentDatabase"),
                        m.matcher("/CreateDeployType"), m.matcher("/UpdateDeployType"),
                        m.matcher("/DeleteDeployType"),
                        m.matcher("/CreateBuildRevisionParameters"),
                        m.matcher("/UpdateBuildRevisionParameters"),
                        m.matcher("/DeleteBuildRevisionParameters"),
                        m.matcher("/CreateBatchInvariant"), m.matcher("/DeleteBatchInvariant"),
                        m.matcher("/UpdateBatchInvariant"),
                        m.matcher("/CreateAppService"), m.matcher("/DeleteAppService"),
                        m.matcher("/UpdateAppService")
                ).hasRole("Integrator")

                // ── IntegratorNewChain
                .requestMatchers(m.matcher("/NewChain")).hasRole("IntegratorNewChain")

                // ── IntegratorDeploy
                .requestMatchers(
                        m.matcher("/DisableEnvironment"),
                        m.matcher("/NewBuildRev"),
                        m.matcher("/JenkinsDeploy")
                ).hasRole("IntegratorDeploy")

                // ── Administrator
                .requestMatchers(
                        m.matcher("/LogEvent.jsp"),
                        m.matcher("/Usage.jsp"),
                        m.matcher("/ParameterList.jsp"),
                        m.matcher("/UserManager.jsp"),
                        m.matcher("/EventHookList.jsp"),
                        m.matcher("/InvariantList.jsp"),
                        m.matcher("/CerberusInformation.jsp"),
                        m.matcher("/Prompt.jsp"),
                        m.matcher("/ReadUser"), m.matcher("/GetParameter"),
                        m.matcher("/UpdateParameter"),
                        m.matcher("/GetUsers"), m.matcher("/CreateUser"),
                        m.matcher("/UpdateUser"), m.matcher("/DeleteUser"),
                        m.matcher("/ReadLogEvent"),
                        m.matcher("/CreateInvariant"), m.matcher("/UpdateInvariant"),
                        m.matcher("/DeleteInvariant"),
                        m.matcher("/CreateEventHook"), m.matcher("/UpdateEventHook"),
                        m.matcher("/DeleteEventHook"),
                        m.matcher("/DeleteApplication"),
                        m.matcher("/ReadCerberusDetailInformation"),
                        m.matcher("/ChangeUserPasswordAdmin")
                ).hasRole("Administrator")

                // ── ANY : tout utilisateur authentifié
                .requestMatchers(
                        m.matcher("/"),
                        m.matcher("/Homepage.jsp"),
                        m.matcher("/ImpactAnalysis.jsp"),
                        m.matcher("/RobotList.jsp"),
                        m.matcher("/ReportingMonitor.jsp"),
                        m.matcher("/Homepage"),
                        m.matcher("/ReadMyUser"),
                        m.matcher("/ReadExecutionTagHistory"),
                        m.matcher("/GetInvariantList"),
                        m.matcher("/ReadCerberusInformation"),
                        m.matcher("/GeneratePerformanceString"),
                        m.matcher("/UpdateMyUser"), m.matcher("/UpdateMyUserSystem"),
                        m.matcher("/UpdateMyUserReporting"),
                        m.matcher("/UpdateMyUserReporting1"),
                        m.matcher("/UpdateMyUserRobotPreference"),
                        m.matcher("/ReadUserPublic"),
                        m.matcher("/FindInvariantByID"),
                        m.matcher("/ImportTestCaseFromJson"),
                        m.matcher("/ReadApplication"),
                        m.matcher("/ReadApplicationObject"),
                        m.matcher("/ReadBuildRevisionInvariant"),
                        m.matcher("/ReadProject"),
                        m.matcher("/ReadRobot"),
                        m.matcher("/ReadCountryEnvParam"),
                        m.matcher("/GetTestBySystem"),
                        m.matcher("/GetTestCaseForTest"),
                        m.matcher("/GetEnvironmentsPerBuildRevision"),
                        m.matcher("/GetEnvironmentsLastChangePerCountry"),
                        m.matcher("/ReadAppService"),
                        m.matcher("/ReadInvariant"),
                        m.matcher("/ReadTestCaseExecution"),
                        m.matcher("/ReadTag"),
                        m.matcher("/ReadParameter"),
                        m.matcher("/GetExecutionsInQueue"),
                        m.matcher("/ReadDocumentation"),
                        m.matcher("/ReadLabel")
                ).authenticated()

                .requestMatchers(m.matcher("/api/**")).authenticated()

                .anyRequest().authenticated()

        );
    }

}
