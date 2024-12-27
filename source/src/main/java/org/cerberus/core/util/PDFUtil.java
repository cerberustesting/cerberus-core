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

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.itextpdf.kernel.pdf.canvas.parser.listener.SimpleTextExtractionStrategy;
import com.itextpdf.signatures.PdfSignature;
import com.itextpdf.signatures.SignatureUtil;
import java.io.ByteArrayInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Utility class centralizing string utility methods
 *
 * @author Tiago Bernardes
 * @version 1.0, 10/01/2013
 * @since 2.0.0
 */
public final class PDFUtil {

    private static final Logger LOG = LogManager.getLogger(PDFUtil.class);

    /**
     * To avoid instantiation of utility class
     */
    private PDFUtil() {
    }

    public static boolean isPdf(byte[] bytes) {
        int pages;
        try {
            PdfDocument pdfDoc = new PdfDocument(new PdfReader(new ByteArrayInputStream(bytes)));
            pages = pdfDoc.getNumberOfPages();
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    public static int getNumberOfPages(byte[] bytes) throws IOException {
        LOG.debug("Getting nb of pdf pages.");
        int pages = 0;
        try {
            PdfDocument pdfDoc = new PdfDocument(new PdfReader(new ByteArrayInputStream(bytes)));
            pages = pdfDoc.getNumberOfPages();
        } catch (Exception ex) {
            LOG.error(ex, ex);
        }
        return pages;
    }
    // TEmporary version of method in order to retreive signature information from pdf file.

    public static JSONObject getTextFromPdf(byte[] bytes) throws IOException {
        LOG.debug("Getting text of pdf.");
        StringBuilder fullResult = new StringBuilder();
        JSONObject result = new JSONObject();
        JSONArray resultPages = new JSONArray();
        try {
            PdfDocument pdfDoc = new PdfDocument(new PdfReader(new ByteArrayInputStream(bytes)));
            int nbpages = pdfDoc.getNumberOfPages();
            LOG.debug("Pages " + nbpages);
            SimpleTextExtractionStrategy strategy = new SimpleTextExtractionStrategy();
            for (int i = 1; i <= nbpages; i++) {
                LOG.debug("Page " + i);
                String text = PdfTextExtractor.getTextFromPage(pdfDoc.getPage(i));
                LOG.debug(text);
                resultPages.put(text);
                fullResult.append(text);
            }
            result.put("pages", resultPages);
            result.put("allPages", fullResult.toString());
        } catch (Exception ex) {
            LOG.error(ex, ex);
        }
        return result;

    }

    public static JSONObject getSignatures(byte[] bytes) {
        LOG.debug("Getting cert details of pdf.");
        JSONObject result = new JSONObject();

        PdfDocument pdfDoc;
        try {
            pdfDoc = new PdfDocument(new PdfReader(new ByteArrayInputStream(bytes)));
            SignatureUtil signUtil = new SignatureUtil(pdfDoc);
            List<String> names = signUtil.getSignatureNames();

            List<String> names2 = signUtil.getBlankSignatureNames();

            SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd");

            String res_name = "";
            for (String name : names) {
                res_name += name + "|";
            }
            result.append("signatureNames", res_name);

            res_name = "";
            for (String name : names2) {
                res_name += name + "|";
            }
            result.append("blankSignatureNames", res_name);

            JSONArray signatureArray = new JSONArray();
            JSONObject signatureObject = new JSONObject();
            for (String name : names) {
                signatureObject.append("names", name);

                PdfSignature pdfSign = signUtil.getSignature(name);
                signatureObject.append("dates", pdfSign.getDate());
                signatureObject.append("contents", pdfSign.getContents().getValue());
                signatureObject.append("locations", pdfSign.getLocation());
                signatureObject.append("signatureNames", pdfSign.getName());
                signatureObject.append("reasons", pdfSign.getReason());
                signatureArray.put(signatureObject);
            }
            result.append("names", signatureObject);
            result.put("certNb", names.size());
            pdfDoc.close();
        } catch (Exception ex) {
            LOG.error(ex, ex);
        }
        return result;
    }

}
