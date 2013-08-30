package com.redcats.tst.serviceEmail.impl;

import com.redcats.tst.entity.BatchInvariant;
import com.redcats.tst.entity.CountryEnvParam;
import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.service.IBatchInvariantService;
import com.redcats.tst.service.ICountryEnvParamService;
import com.redcats.tst.service.IParameterService;
import com.redcats.tst.serviceEmail.IEmailBodyGeneration;
import com.redcats.tst.serviceEmail.IEmailGeneration;
import com.redcats.tst.util.StringUtil;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import version.Version;

/**
 *
 * @author bcivel
 */
@Service
public class EmailGeneration implements IEmailGeneration {

    @Autowired
    private IParameterService parameterService;
    @Autowired
    private ICountryEnvParamService countryEnvParamService;
    @Autowired
    private IEmailBodyGeneration emailBodyGeneration;
    @Autowired
    private IBatchInvariantService batchInvariantService;

    @Override
    public String EmailGenerationRevisionChange(String country, String env, String build, String revision, Connection conn) {
        String result = "";
        try {
            CountryEnvParam myCountryEnvParam;
            myCountryEnvParam = countryEnvParamService.findCountryEnvParamByKey(country, env);

            /* Pick the datas from the database */
            String to = parameterService.findParameterByKey("integration_notification_newbuildrevision_to").getValue();
            String cc = parameterService.findParameterByKey("integration_notification_newbuildrevision_cc").getValue();
            String subject = parameterService.findParameterByKey("integration_notification_newbuildrevision_subject").getValue();
            String body = parameterService.findParameterByKey("integration_notification_newbuildrevision_body").getValue();

            if (!StringUtil.isNullOrEmptyOrNull(myCountryEnvParam.geteMailBodyRevision())) {
                body = myCountryEnvParam.geteMailBodyRevision();
            }
            if (!StringUtil.isNullOrEmptyOrNull(myCountryEnvParam.getDistribList())) {
                to = myCountryEnvParam.getDistribList();
            }

            /* Replace the Keywords from the fed text  */
            subject = subject.replaceAll("%COUNTRY%", country);
            subject = subject.replaceAll("%ENV%", env);
            subject = subject.replaceAll("%BUILD%", build);
            subject = subject.replaceAll("%REVISION%", revision);

            body = body.replaceAll("%COUNTRY%", country);
            body = body.replaceAll("%ENV%", env);
            body = body.replaceAll("%BUILD%", build);
            body = body.replaceAll("%REVISION%", revision);

            // Generate the Table Contented in the mail
            String content;

            String lastBuild = myCountryEnvParam.getBuild();
            String lastRev = myCountryEnvParam.getRevision();

            content = emailBodyGeneration.GenerateBuildContentTable(build, revision, lastBuild, lastRev, conn);
            body = body.replaceAll("%BUILDCONTENT%", content);

            content = emailBodyGeneration.GenerateTestRecapTable(build, revision, country, conn);
            body = body.replaceAll("%TESTRECAP%", content);

            content = emailBodyGeneration.GenerateTestRecapTable(build, revision, "ALL", conn);
            body = body.replaceAll("%TESTRECAPALL%", content);
            //End

            result = to + "///" + cc + "///" + subject + "///" + body + "///" + build + "///" + revision;

        } catch (CerberusException e) {
            Logger.getLogger(EmailGeneration.class.getName()).log(Level.SEVERE, Version.PROJECT_NAME_VERSION + " - Exception catched.", e);
        }

        return result;

    }

    @Override
    public String EmailGenerationDisableEnv(String country, String env) {
        String result = "";
        try {
            CountryEnvParam myCountryEnvParam;
            myCountryEnvParam = countryEnvParamService.findCountryEnvParamByKey(country, env);

            String to = parameterService.findParameterByKey("integration_notification_disableenvironment_to").getValue();
            String cc = parameterService.findParameterByKey("integration_notification_disableenvironment_cc").getValue();
            String subject = parameterService.findParameterByKey("integration_notification_disableenvironment_subject").getValue();
            String body = parameterService.findParameterByKey("integration_notification_disableenvironment_body").getValue();

            if (!StringUtil.isNullOrEmptyOrNull(myCountryEnvParam.geteMailBodyDisableEnvironment())) {
                body = myCountryEnvParam.geteMailBodyDisableEnvironment();
            }

            if (!StringUtil.isNullOrEmptyOrNull(myCountryEnvParam.getDistribList())) {
                to = myCountryEnvParam.getDistribList();
            }

            subject = subject.replaceAll("%COUNTRY%", country);
            subject = subject.replaceAll("%ENV%", env);
            subject = subject.replaceAll("%BUILD%", myCountryEnvParam.getBuild());
            subject = subject.replaceAll("%REVISION%", myCountryEnvParam.getRevision());

            body = body.replaceAll("%COUNTRY%", country);
            body = body.replaceAll("%ENV%", env);
            body = body.replaceAll("%BUILD%", myCountryEnvParam.getBuild());
            body = body.replaceAll("%REVISION%", myCountryEnvParam.getRevision());

            result = to + "///" + cc + "///" + subject + "///" + body;
            
        } catch (CerberusException e) {
            Logger.getLogger(EmailGeneration.class.getName()).log(Level.SEVERE, Version.PROJECT_NAME_VERSION + " - Exception catched.", e);
        }
        return result;
    }

    @Override
    public String EmailGenerationNewChain(String country, String env, String build, String revision, String chain) {

        String result = "";

        try {
            /* Page Display - START */

            CountryEnvParam myCountryEnvParam;
            myCountryEnvParam = countryEnvParamService.findCountryEnvParamByKey(country, env);

            BatchInvariant myBatchInvariant;
            myBatchInvariant = batchInvariantService.findBatchInvariantByKey(chain);
            String lastchain = myBatchInvariant.getBatch() + " (" + myBatchInvariant.getDescription() + ")";

            String to = parameterService.findParameterByKey("integration_notification_newchain_to").getValue();
            String cc = parameterService.findParameterByKey("integration_notification_newchain_cc").getValue();
            String subject = parameterService.findParameterByKey("integration_notification_newchain_subject").getValue();
            String body = parameterService.findParameterByKey("integration_notification_newchain_body").getValue();

            if (!StringUtil.isNullOrEmptyOrNull(myCountryEnvParam.geteMailBodyChain())) {
                body = myCountryEnvParam.geteMailBodyChain();
            }
            if (!StringUtil.isNullOrEmptyOrNull(myCountryEnvParam.getDistribList())) {
                to = myCountryEnvParam.getDistribList();
            }

            subject = subject.replaceAll("%COUNTRY%", country);
            subject = subject.replaceAll("%ENV%", env);
            subject = subject.replaceAll("%BUILD%", myCountryEnvParam.getBuild());
            subject = subject.replaceAll("%REVISION%", myCountryEnvParam.getRevision());
            subject = subject.replaceAll("%CHAIN%", lastchain);

            body = body.replaceAll("%COUNTRY%", country);
            body = body.replaceAll("%ENV%", env);
            body = body.replaceAll("%BUILD%", myCountryEnvParam.getBuild());
            body = body.replaceAll("%REVISION%", myCountryEnvParam.getRevision());
            body = body.replaceAll("%CHAIN%", lastchain);

            result = to + "///" + cc + "///" + subject + "///" + body + "///" + build + "///" + revision + "///" + chain;

        } catch (Exception e) {
            Logger.getLogger(EmailGeneration.class.getName()).log(Level.SEVERE, Version.PROJECT_NAME_VERSION + " - Exception catched.", e);
        }
        return result;

    }
}
