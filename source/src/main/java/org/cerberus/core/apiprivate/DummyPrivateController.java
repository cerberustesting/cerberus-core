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
package org.cerberus.core.apiprivate;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.service.IAppServiceService;
import org.cerberus.core.util.servlet.ServletUtil;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author bcivel
 */
@RestController
@RequestMapping("/dummy")
public class DummyPrivateController {

    private static final Logger LOG = LogManager.getLogger(DummyPrivateController.class);
    private final PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

    @Autowired
    IAppServiceService appserviceService;

    @GetMapping("/testcsv")
    public String getnbByCriteria(
            HttpServletRequest request) {

        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);

        JSONObject jsonResponse = new JSONObject();

        try {
            return "id,model,code,openingDate,name,closingDate,cfReseau,brand,locationType,cfPays,activity,cfRefWebStore,cfManagement,cfLangues,region,phone,concept,email,thumbnailUrl,cfTailleMag,cfServices,mondayFirstOpening,mondaySecondOpening,tuesdayFirstOpening,tuesdaySecondOpening,wednesdayFirstOpening,wednesdaySecondOpening,thursdayFirstOpening,thursdaySecondOpening,fridayFirstOpening,fridaySecondOpening,saturdayFirstOpening,saturdaySecondOpening,sundayFirstOpening,sundaySecondOpening,cfFuseauH,lifecycleStep,latitude,longitude,zone,cfMarketPlace,creationDate,createdBy,lastModificationDate,lastModifiedBy,commercialLabel,cfFirstTillReceipt,street,streetComplement,zipCode,city,country,cfLocalAlphabetCommercialLabel,cfLocalAlphabetStreetComplement,cfLocalAlphabetStreet,cfLocalAlphabetZipcode,cfLocalAlphabetCity,cfLocalAlphabetCountry,currency,cfZoneFiscale,administrativeCode,company,cfNumClient,cfRealEstateCluster,cfCompetitiveIntensityCluster,cfDeliveryLocationAddress1,cfAdministrativeLocationAddress1,cfDeliveryLocationAddress2,cfAdministrativeLocationAddress2,cfDeliveryLocationAddress3,cfAdministrativeLocationAddress3,cfDeliveryLocationAddress4,cfAdministrativeLocationAddress4,cfDeliveryLocationAddress5,cfAdministrativeLocationAddress5,cfDeliveryLocationAddress6,cfAdministrativeLocationAddress6,cfDeliveryLocationAddress7,cfAdministrativeLocationAddress7,promoter,propertyManager,trustee,cfSurfVente,cfSurfNette,cfSurfRes,cfSurfCab,cfFacade,cfMetreRes,otherSurfaces,grossSurface,salesArea,cfEtages,cfMetreOrb,cfMetreCen,cfMetreTot,cfSurfTot,cenTableRonde90,cenTableRonde60,cenKit6Sellette,cfNbVitrines,cfPrimaryWindows,cfSecondaryWindows,cfWindowsNumber,cfWindowsSize,advertisingVersion,cfPLV49,cfPLV50,cfPLV51,cfPLV52,cfPLV53,cfPLV47,cfPLV72,cfPLV75,cfPLV01,cfPLV02,cfPLV03,cfPLV04,cfPLV05,cfPLV06,cfPLV07,cfPLV08,cfPLV09,cfPLV10,cfPLV11,cfPLV12,cfPLV13,cfPLV14,cfPLV15,cfPLV19,cfPLV20,cfPLV21,cfPLV25,cfPLV26,cfPLV28,cfPLV29,cfPLV31,cfPLV33,cfPLV35,cfPLV36,cfPLV37,cfPLV68,cfPLV69,cfPLV70,cfPLV73,cfPLV71,cfPLV64,cfPLV65,cfPLV66,cfPLV67,cfPLV60,cfPLV61,cfPLV62,cfPLV63,cfPLV48,cfPLV54,cfPLV55,cfPLV56,cfPLV57,cfPLV58,cfPLV59,cfPLV38,cfPLV39,cfPLV41,cfPLV44,cfPLV45,cfPLV46,cfPLV74,testhierarchique,regionTest,cfDecoupage,countrytest,archived,active,retailDriveId,scopeScoring,costCenter,excludeFromFinancialAnalysis,excludeFromSalesAnalysis,bic,iban,webSite,fax,mainSite,fullTimeEquivalent,state,monOpeningHours,analyticBreakdown,tueOpeningHours,wedOpeningHours,thuOpeningHours,friOpeningHours,satOpeningHours,sunOpeningHours,operatingMode,cfArchive,cfGroupCompany\n"
                    + "406,referential.site,10,04/30/1981 10:00:00,LILLE V2,,store,PROMOD,commercialCenter,,promod,,branch,,141,+33374493000,,mag0010@promod.fr,https://syn2storageprod.blob.core.windows.net/syn2-files-promod/thumbnail/86edd9ed-6ad5-474b-832b-062b4206554a?sv=2023-11-03&se=2024-05-22T19%3A12%3A51Z&sr=b&sp=r&sig=FFpctyQxiSZ3HJnOBt6%2BW1rqM9LEIB57Hpf8P9MsxCQ%3D,,\"webDelivery, eresa\",09:30:00 - 19:30:00,,09:30:00 - 19:30:00,,09:30:00 - 19:30:00,,09:30:00 - 19:30:00,,09:30:00 - 19:30:00,,09:30:00 - 19:30:00,,,,Europe/Paris,9266f62b-2694-4195-89c7-5a76d536d71c,50.41994,2.99452,NORD,False,12/27/2023 16:33:05,7bca1570-f079-4f8f-898d-cb85ea40bbfa,05/15/2024 13:33:06,david.thomas@promod.fr,PROMOD LILLE V2,,Boulevard de Valmy,\"Centre commercial V2, Premier étage\",59650,Villeneuve d'Ascq,FR,,,,,,,EUR,,,,,C1,I1,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,2,,,\"{\"\"shortText\"\":null,\"\"longText\"\":null,\"\"dateTime\"\":null,\"\"percent\"\":null,\"\"number\"\":1.00,\"\"yesNo\"\":null,\"\"email\"\":null,\"\"currencyId\"\":null,\"\"listItemId\"\":349}, {\"\"shortText\"\":null,\"\"longText\"\":null,\"\"dateTime\"\":null,\"\"percent\"\":null,\"\"number\"\":1.00,\"\"yesNo\"\":null,\"\"email\"\":null,\"\"currencyId\"\":null,\"\"listItemId\"\":350}\",\"{\"\"shortText\"\":null,\"\"longText\"\":null,\"\"dateTime\"\":null,\"\"percent\"\":null,\"\"number\"\":2.80,\"\"yesNo\"\":null,\"\"email\"\":null,\"\"currencyId\"\":null,\"\"listItemId\"\":356}, {\"\"shortText\"\":null,\"\"longText\"\":null,\"\"dateTime\"\":null,\"\"percent\"\":null,\"\"number\"\":3.55,\"\"yesNo\"\":null,\"\"email\"\":null,\"\"currencyId\"\":null,\"\"listItemId\"\":352}\",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,False,True,130013,,,False,False,,,,,False,,,,,,,,,,,,,";
        } catch (Exception ex) {
            LOG.warn(ex, ex);
            return "error " + ex.getMessage();
        }
    }

    @GetMapping("/empty")
    public String getEmpty(
            HttpServletRequest request) {

        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);

        try {
            return null;
        } catch (Exception ex) {
            LOG.warn(ex, ex);
            return "error " + ex.getMessage();
        }
    }

}
