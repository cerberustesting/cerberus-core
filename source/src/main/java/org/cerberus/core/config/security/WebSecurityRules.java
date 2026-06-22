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
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

public class WebSecurityRules {

    // PathPatternRequestMatcher.withDefaults() only works for requests dispatched through
    // Spring MVC's DispatcherServlet. Cerberus @WebServlet servlets bypass DispatcherServlet,
    // so their paths would silently miss permitAll() rules and get redirected to login.
    // TODO: Replace AntPathRequestMatcher when Spring Security migration is finalized.
    @SuppressWarnings({"deprecation", "removal"})
    private static AntPathRequestMatcher m(String pattern) {
        return new AntPathRequestMatcher(pattern);
    }

    public static void applyRules(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(m("/api/public/**")).permitAll()
                // OAuth Protected Resource Metadata (RFC 9728) : public discovery for MCP clients
                .requestMatchers(m("/.well-known/oauth-protected-resource")).permitAll()
                // ── Public
                .requestMatchers(
                        m("/DatabaseMaintenance.jsp"),
                        m("/Documentation.jsp"),
                        m("/Login.jsp"),
                        m("/Logout.jsp"),
                        m("/Error.jsp"),
                        m("/dummy/**"),
                        m("/ChangePassword.jsp"),
                        m("/RunTestCase"), m("/RunTestCaseV001"),m("/RunTestCaseV002"),
                        m("/GetNumberOfExecutions"),
                        m("/ResultCI"), m("/ResultCIV001"),
                        m("/ResultCIV002"), m("/ResultCIV003"),
                        m("/ResultCIV004"),
                        m("/NewRelease"),
                        m("/AddToExecutionQueue"),
                        m("/AddToExecutionQueueV001"),
                        m("/AddToExecutionQueueV002"),
                        m("/AddToExecutionQueueV003"),
                        m("/NewBuildRevisionV000"),
                        m("/DisableEnvironmentV000"),
                        m("/NewEnvironmentEventV000"),
                        m("/GetTagExecutions"),
                        m("/GetTestCasesV001"),
                        m("/ManageV001"),
                        m("/ForgotPassword"),
                        m("/ForgotPasswordEmailConfirmation"),
                        m("/ChangeUserPassword"),
                        m("/ReadApplicationObjectImage"),
                        m("/DummyRESTCall"), m("/DummyRESTCallEmpty"),
                        m("/j_security_check")
                ).permitAll()

                // ── TestRO
                .requestMatchers(
                        m("/TestCaseExecution.jsp"),
                        m("/TestCaseExecutionList.jsp"),
                        m("/ReportingExecutionByTag.jsp"),
                        m("/ReportingExecutionOverTime.jsp"),
                        m("/ReportingCampaignOverTime.jsp"),
                        m("/ReportingCampaignStatistics.jsp"),
                        m("/TestCaseExecutionQueueList.jsp"),
                        m("/Test.jsp"),
                        m("/TestCaseScript.jsp"),
                        m("/TestCaseList.jsp"),
                        m("/CampaignList.jsp"),
                        m("/SwaggerUI.jsp"),
                        m("/ManageExecutionPool"), m("/ReadExecutionPool"),
                        m("/ReadExecutionPools"),
                        m("/GetTestCaseList"),
                        m("/ReadTestCaseExecutionMedia"),
                        m("/TestcaseList"),
                        m("/GetDataForTestCaseSearch"),
                        m("/TCEwwwDetail"),
                        m("/GenerateGraph"),
                        m("/TestCaseActionExecutionDetail"),
                        m("/ExecutionPerBuildRevision"),
                        m("/GetStepUsedAsLibraryInOtherTestCasePerApplication"),
                        m("/ExportTestCase"),
                        m("/FindTestImplementationStatusPerApplication"),
                        m("/GetCountryForTestCase"),
                        m("/GetPropertiesForTestCase"),
                        m("/GetReport"),
                        m("/GetStepInLibrary"),
                        m("/ReadTestCase"), m("/ReadTest"),
                        m("/ReadTestCaseStep"),
                        m("/GetReportTest"),
                        m("/ReadTestCaseExecutionByTag"),
                        m("/ReadExecutionStat"),
                        m("/ReadQueueStat"),
                        m("/ReadTagStat")
                ).hasRole("TestRO")

                // ── Test
                .requestMatchers(
                        m("/SqlLibrary.jsp"),
                        m("/TestDataLibList.jsp"),
                        m("/QuickStart.jsp"),
                        m("/UpdateTestCase"), m("/UpdateTestCaseMass"),
                        m("/CreateTestCaseCountry"), m("/DeleteTestCaseCountry"),
                        m("/CalculatePropertyForTestCase"),
                        m("/UpdateProperties"),
                        m("/CreateTestCase"),
                        m("/CreateTestCaseLabel"), m("/DeleteTestCaseLabel"),
                        m("/ImportSeleniumIDE"),
                        m("/ImportTestCaseStep"),
                        m("/ImportPropertyOfATestCaseToAnOtherTestCase"),
                        m("/CreateNotDefinedProperty"),
                        m("/DeleteTestData"), m("/UpdateTestData"),
                        m("/FindAllTestData"),
                        m("/ReadTestDataLib"), m("/ReadTestDataLibData"),
                        m("/ReadSqlLibrary"),
                        m("/PictureConnector"),
                        m("/UseTestCaseStep"),
                        m("/UpdateTestCaseWithDependencies"),
                        m("/UpdateTestCaseProperties"),
                        m("/Thumbnailer"),
                        m("/SaveTestCaseLabel"), m("/ReadTestCaseLabel")
                ).hasRole("Test")

                // ── TestStepLibrary
                .requestMatchers(m("/ZZZ")).hasRole("TestStepLibrary")

                // ── TestAdmin
                .requestMatchers(
                        m("/CreateTest"), m("/DeleteTest"),
                        m("/UpdateTest"),
                        m("/DeleteTestCase"), m("/DeleteTestCaseFromTestPage"),
                        m("/CreateSqlLibrary"), m("/DeleteSqlLibrary"),
                        m("/UpdateSqlLibrary")
                ).hasRole("TestAdmin")

                // ── Label
                .requestMatchers(
                        m("/Label.jsp"),
                        m("/CreateLabel"), m("/UpdateLabel"),
                        m("/DeleteLabel")
                ).hasRole("Label")

                // ── AS
                .requestMatchers(
                        m("/ReportingAutomateScore.jsp")
                ).hasRole("AS")

                // ── RunTest
                .requestMatchers(
                        m("/RunTests.jsp"),
                        m("/findEnvironmentByCriteria"),
                        m("/UpdateTestCaseExecution"),
                        m("/RunExecutionInQueue"),
                        m("/SetTagToExecution"),
                        m("/GetExecutionQueue"),
                        m("/UpdateTestCaseExecutionQueue"),
                        m("/ReadTestCaseExecutionQueue"),
                        m("/CreateTestCaseExecutionQueue"),
                        m("/CreateUpdateTestCaseExecutionFile"),
                        m("/DeleteTestCaseExecutionFile"),
                        m("/ReadCampaign"), m("/ReadCampaignParameter"),
                        m("/GetCampaign"), m("/UpdateCampaign"),
                        m("/CreateCampaign"), m("/DeleteCampaign"),
                        m("/CreateScheduleEntry"), m("/ReadScheduleEntry"),
                        m("/UpdateScheduleEntry"), m("/DeleteScheduleEntry"),
                        m("/AddToExecutionQueuePrivate")
                ).hasRole("RunTest")

                // ── TestDataManager
                .requestMatchers(
                        m("/CreateTestDataLib"),
                        m("/DuplicateTestDataLib"),
                        m("/ImportTestDataLib"),
                        m("/DeleteTestDataLib"),
                        m("/UpdateTestDataLibData"),
                        m("/UpdateTestDataLib"),
                        m("/BulkRenameDataLib")
                ).hasRole("TestDataManager")

                // ── IntegratorRO
                .requestMatchers(
                        m("/ApplicationList.jsp"),
                        m("/AppServiceList.jsp"),
                        m("/ApplicationObjectList.jsp"),
                        m("/BuildContent.jsp"),
                        m("/BuildRevDefinition.jsp"),
                        m("/Environment.jsp"),
                        m("/IntegrationStatus.jsp"),
                        m("/Project.jsp"),
                        m("/DeployType.jsp"),
                        m("/BatchInvariant.jsp"),
                        m("/GetShortTests"),
                        m("/GetInvariantsForTest"),
                        m("/GetEnvironmentAvailable"),
                        m("/FindBuildContent"),
                        m("/FindCountryEnvironmentDatabase"),
                        m("/GetCountryEnvParamList"),
                        m("/GetCountryEnvironmentParameterList"),
                        m("/FindEnvironments"),
                        m("/ReadDeployType"),
                        m("/GetNotification"),
                        m("/ReadBuildRevisionParameters"),
                        m("/ReadCountryEnvParam_log"),
                        m("/ReadBuildRevisionBatch"),
                        m("/ReadBatchInvariant"),
                        m("/ReadCountryEnvDeployType"),
                        m("/ReadCountryEnvironmentDatabase"),
                        m("/ReadCountryEnvironmentParameters"),
                        m("/ReadCountryEnvLink")
                ).hasRole("IntegratorRO")

                // ── Integrator
                .requestMatchers(
                        m("/CreateApplication"), m("/UpdateApplication"),
                        m("/CreateApplicationObject"), m("/UpdateApplicationObject"),
                        m("/DeleteApplicationObject"),
                        m("/UpdateCountryEnv"),
                        m("/CreateProject"), m("/DeleteProject"),
                        m("/UpdateProject"),
                        m("/CreateBuildRevisionInvariant"),
                        m("/UpdateBuildRevisionInvariant"),
                        m("/DeleteBuildRevisionInvariant"),
                        m("/CreateRobot"), m("/UpdateRobot"),
                        m("/DeleteRobot"),
                        m("/CreateCountryEnvParam"), m("/UpdateCountryEnvParam"),
                        m("/DeleteCountryEnvParam"),
                        m("/CreateCountryEnvironmentParameter"),
                        m("/UpdateCountryEnvironmentParameter"),
                        m("/DeleteCountryEnvironmentParameter"),
                        m("/CreateCountryEnvironmentDatabase"),
                        m("/UpdateCountryEnvironmentDatabase"),
                        m("/DeleteCountryEnvironmentDatabase"),
                        m("/CreateDeployType"), m("/UpdateDeployType"),
                        m("/DeleteDeployType"),
                        m("/CreateBuildRevisionParameters"),
                        m("/UpdateBuildRevisionParameters"),
                        m("/DeleteBuildRevisionParameters"),
                        m("/CreateBatchInvariant"), m("/DeleteBatchInvariant"),
                        m("/UpdateBatchInvariant"),
                        m("/CreateAppService"), m("/DeleteAppService"),
                        m("/UpdateAppService")
                ).hasRole("Integrator")

                // ── IntegratorNewChain
                .requestMatchers(m("/NewChain")).hasRole("IntegratorNewChain")

                // ── IntegratorDeploy
                .requestMatchers(
                        m("/DisableEnvironment"),
                        m("/NewBuildRev"),
                        m("/JenkinsDeploy")
                ).hasRole("IntegratorDeploy")

                // ── Administrator
                .requestMatchers(
                        m("/LogEvent.jsp"),
                        m("/Usage.jsp"),
                        m("/ParameterList.jsp"),
                        m("/UserManager.jsp"),
                        m("/EventHookList.jsp"),
                        m("/InvariantList.jsp"),
                        m("/CerberusInformation.jsp"),
                        m("/Prompt.jsp"),
                        m("/ReadUser"), m("/GetParameter"),
                        m("/UpdateParameter"),
                        m("/GetUsers"), m("/CreateUser"),
                        m("/UpdateUser"), m("/DeleteUser"),
                        m("/ReadLogEvent"),
                        m("/CreateInvariant"), m("/UpdateInvariant"),
                        m("/DeleteInvariant"),
                        m("/CreateEventHook"), m("/UpdateEventHook"),
                        m("/DeleteEventHook"),
                        m("/DeleteApplication"),
                        m("/ReadCerberusDetailInformation"),
                        m("/ChangeUserPasswordAdmin")
                ).hasRole("Administrator")

                // ── ANY : tout utilisateur authentifié
                .requestMatchers(
                        m("/"),
                        m("/Homepage.jsp"),
                        m("/ImpactAnalysis.jsp"),
                        m("/RobotList.jsp"),
                        m("/ReportingMonitor.jsp"),
                        m("/Homepage"),
                        m("/ReadMyUser"),
                        m("/ReadExecutionTagHistory"),
                        m("/GetInvariantList"),
                        m("/ReadCerberusInformation"),
                        m("/GeneratePerformanceString"),
                        m("/UpdateMyUser"), m("/UpdateMyUserSystem"),
                        m("/UpdateMyUserReporting"),
                        m("/UpdateMyUserReporting1"),
                        m("/UpdateMyUserRobotPreference"),
                        m("/ReadUserPublic"),
                        m("/FindInvariantByID"),
                        m("/ImportTestCaseFromJson"),
                        m("/ReadApplication"),
                        m("/ReadApplicationObject"),
                        m("/ReadBuildRevisionInvariant"),
                        m("/ReadProject"),
                        m("/ReadRobot"),
                        m("/ReadCountryEnvParam"),
                        m("/GetTestBySystem"),
                        m("/GetTestCaseForTest"),
                        m("/GetEnvironmentsPerBuildRevision"),
                        m("/GetEnvironmentsLastChangePerCountry"),
                        m("/ReadAppService"),
                        m("/ReadInvariant"),
                        m("/ReadTestCaseExecution"),
                        m("/ReadTag"),
                        m("/ReadParameter"),
                        m("/GetExecutionsInQueue"),
                        m("/ReadDocumentation"),
                        m("/ReadLabel")
                ).authenticated()

                .requestMatchers(m("/api/**")).authenticated()

                .anyRequest().authenticated()

        );
    }

}
