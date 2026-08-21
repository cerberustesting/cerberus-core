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
package org.cerberus.core.util;

import com.iab.gpp.encoder.section.TcfEuV2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import com.iab.gpp.encoder.GppModel;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class IabGppDecoder {

    private static final Logger LOG = LogManager.getLogger(IabGppDecoder.class);

    public Map<String, Object> decodeTcfEuV2(String iabgpp) {
        Map<String, Object> result = new HashMap<>();
        result.put("gppString", iabgpp);

        try {
            GppModel gppModel = new GppModel(iabgpp);
            TcfEuV2 tcfEuV2Section = (TcfEuV2) gppModel.getSection(TcfEuV2.NAME);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            Map<String, Object> tcfeuV2Data = new HashMap<>();
            if (tcfEuV2Section != null) {
                tcfeuV2Data.put("version", tcfEuV2Section.getVersion());
                tcfeuV2Data.put("created", tcfEuV2Section.getCreated());
                tcfeuV2Data.put("lastUpdated",tcfEuV2Section.getLastUpdated().format(formatter));
                tcfeuV2Data.put("cmpId", tcfEuV2Section.getCmpId());
                tcfeuV2Data.put("cmpVersion", tcfEuV2Section.getCmpVersion());
                tcfeuV2Data.put("consentString", tcfEuV2Section.getConsentScreen());
                tcfeuV2Data.put("consentLanguage", tcfEuV2Section.getConsentLanguage());
                tcfeuV2Data.put("vendorListVersion", tcfEuV2Section.getVendorListVersion());
                tcfeuV2Data.put("policyVersion", tcfEuV2Section.getPolicyVersion());
                tcfeuV2Data.put("isServiceSpecific", tcfEuV2Section.getIsServiceSpecific());
                tcfeuV2Data.put("useNonStandardStacks", tcfEuV2Section.getUseNonStandardStacks());
                tcfeuV2Data.put("specialFeatureOptins", tcfEuV2Section.getSpecialFeatureOptins());
                tcfeuV2Data.put("purposeConsents", tcfEuV2Section.getPurposeConsents());
                tcfeuV2Data.put("purposeLegitimateInterests", tcfEuV2Section.getPurposeLegitimateInterests());
                tcfeuV2Data.put("purposeOneTreatment", tcfEuV2Section.getPurposeOneTreatment());
                tcfeuV2Data.put("publisherCountryCode", tcfEuV2Section.getPublisherCountryCode());
                tcfeuV2Data.put("vendorConsents", tcfEuV2Section.getVendorConsents());
                tcfeuV2Data.put("vendorLegitimateInterests", tcfEuV2Section.getVendorLegitimateInterests());
                tcfeuV2Data.put("publisherRestrictions", tcfEuV2Section.getPublisherRestrictions());
                tcfeuV2Data.put("publisherPurposesSegmentType", tcfEuV2Section.getPublisherPurposesSegmentType());
                tcfeuV2Data.put("publisherConsents", tcfEuV2Section.getPublisherConsents());
                tcfeuV2Data.put("publisherLegitimateInterests", tcfEuV2Section.getPublisherLegitimateInterests());
                tcfeuV2Data.put("numCustomPurposes", tcfEuV2Section.getNumCustomPurposes());
                tcfeuV2Data.put("publisherCustomConsents", tcfEuV2Section.getPublisherCustomConsents());
                tcfeuV2Data.put("publisherCustomLegitimateInterests", tcfEuV2Section.getPublisherCustomLegitimateInterests());
                tcfeuV2Data.put("vendorsAllowedSegmentType", tcfEuV2Section.getVendorsAllowedSegmentType());
                tcfeuV2Data.put("vendorsAllowed", tcfEuV2Section.getVendorsAllowed());
                tcfeuV2Data.put("vendorsDisclosedSegmentType", tcfEuV2Section.getVendorsDisclosedSegmentType());
                tcfeuV2Data.put("vendorsDisclosed", tcfEuV2Section.getVendorsDisclosed());
            } else {
                tcfeuV2Data.put("error", "TCFEuV2 not present");
            }

            result.put("tcfeuV2", tcfeuV2Data);
        } catch (IllegalArgumentException | ClassCastException e) {
            LOG.error("Failed to decode TCFEuV2 from GPP string: {}", iabgpp, e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Invalid GPP string or TCFEuV2 section");
            result.put("tcfeuV2", error);
        } catch (Exception e) {
            LOG.error("Unexpected error decoding TCFEuV2", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Unexpected error decoding TCFEuV2");
            result.put("tcfeuV2", error);
        }

        return result;
    }
}
