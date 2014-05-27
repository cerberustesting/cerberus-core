package org.cerberus.servlet.reporting;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "GetReport", urlPatterns = {"/GetReport"})
public class GetReport extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JSONObject json = new JSONObject();
        JSONArray data = new JSONArray();
        try {

            JSONArray object = new JSONArray();
            object.put("Collection");
            object.put("0001A");
            object.put("VCCRM");
            object.put("Customer wants a small item collection.. no message");
            object.put("99");
            object.put("WORKING");
            object.put("OK");
            object.put("FA");
            object.put("");
            object.put("for 2013S2/R00");
            object.put("AUTOMATED");

            data.put(object);

            object = new JSONArray();
            object.put("ICS_Credit");
            object.put("0005A");
            object.put("VCCRM");
            object.put("Can I have my Order API");
            object.put("0");
            object.put("WORKING");
            object.put("");
            object.put("KO");
            object.put("");
            object.put("for 2013S2/R00");
            object.put("AUTOMATED");

            data.put(object);

            object = new JSONArray();
            object.put("ICS_Credit");
            object.put("0005A");
            object.put("VCCRM");
            object.put("Can I have my Order API");
            object.put("0");
            object.put("WORKING");
            object.put("");
            object.put("KO");
            object.put("");
            object.put("for 2013S2/R00");
            object.put("AUTOMATED");

            data.put(object);
            object = new JSONArray();
            object.put("ICS_Credit");
            object.put("0005A");
            object.put("VCCRM");
            object.put("Can I have my Order API");
            object.put("0");
            object.put("WORKING");
            object.put("");
            object.put("KO");
            object.put("");
            object.put("for 2013S2/R00");
            object.put("AUTOMATED");

            data.put(object);
            object = new JSONArray();
            object.put("ICS_Credit");
            object.put("0005A");
            object.put("VCCRM");
            object.put("Can I have my Order API");
            object.put("0");
            object.put("WORKING");
            object.put("");
            object.put("KO");
            object.put("");
            object.put("for 2013S2/R00");
            object.put("AUTOMATED");

            data.put(object);
            object = new JSONArray();
            object.put("ICS_Credit");
            object.put("0005A");
            object.put("VCCRM");
            object.put("Can I have my Order API");
            object.put("0");
            object.put("WORKING");
            object.put("");
            object.put("KO");
            object.put("");
            object.put("for 2013S2/R00");
            object.put("AUTOMATED");

            data.put(object);
            object = new JSONArray();
            object.put("ICS_Credit");
            object.put("0005A");
            object.put("VCCRM");
            object.put("Can I have my Order API");
            object.put("0");
            object.put("WORKING");
            object.put("");
            object.put("KO");
            object.put("");
            object.put("for 2013S2/R00");
            object.put("AUTOMATED");

            data.put(object);
            object = new JSONArray();
            object.put("ICS_Credit");
            object.put("0005A");
            object.put("VCCRM");
            object.put("Can I have my Order API");
            object.put("0");
            object.put("WORKING");
            object.put("");
            object.put("KO");
            object.put("");
            object.put("for 2013S2/R00");
            object.put("AUTOMATED");

            data.put(object);

            object = new JSONArray();
            object.put("ICS_Credit");
            object.put("0005A");
            object.put("VCCRM");
            object.put("Can I have my Order API");
            object.put("0");
            object.put("WORKING");
            object.put("");
            object.put("KO");
            object.put("");
            object.put("for 2013S2/R00");
            object.put("AUTOMATED");

            data.put(object);
            object = new JSONArray();
            object.put("ICS_Credit");
            object.put("0005A");
            object.put("VCCRM");
            object.put("Can I have my Order API");
            object.put("0");
            object.put("WORKING");
            object.put("");
            object.put("KO");
            object.put("");
            object.put("for 2013S2/R00");
            object.put("AUTOMATED");

            data.put(object);
            object = new JSONArray();
            object.put("ICS_Credit");
            object.put("0005A");
            object.put("VCCRM");
            object.put("Can I have my Order API");
            object.put("0");
            object.put("WORKING");
            object.put("");
            object.put("KO");
            object.put("");
            object.put("for 2013S2/R00");
            object.put("AUTOMATED");

            data.put(object);
            object = new JSONArray();
            object.put("ICS_Credit");
            object.put("0005A");
            object.put("VCCRM");
            object.put("Can I have my Order API");
            object.put("0");
            object.put("WORKING");
            object.put("");
            object.put("KO");
            object.put("");
            object.put("for 2013S2/R00");
            object.put("AUTOMATED");

            data.put(object);
            object = new JSONArray();
            object.put("ICS_Credit");
            object.put("0005A");
            object.put("VCCRM");
            object.put("Can I have my Order API");
            object.put("0");
            object.put("WORKING");
            object.put("");
            object.put("KO");
            object.put("");
            object.put("for 2013S2/R00");
            object.put("AUTOMATED");

            data.put(object);
            object = new JSONArray();
            object.put("ICS_Credit");
            object.put("0005A");
            object.put("VCCRM");
            object.put("Can I have my Order API");
            object.put("0");
            object.put("WORKING");
            object.put("");
            object.put("KO");
            object.put("");
            object.put("for 2013S2/R00");
            object.put("AUTOMATED");

            data.put(object);

            object = new JSONArray();
            object.put("ICS_Credit");
            object.put("0005A");
            object.put("VCCRM");
            object.put("Can I have my Order API");
            object.put("0");
            object.put("WORKING");
            object.put("");
            object.put("KO");
            object.put("");
            object.put("for 2013S2/R00");
            object.put("AUTOMATED");

            data.put(object);
            object = new JSONArray();
            object.put("ICS_Credit");
            object.put("0005A");
            object.put("VCCRM");
            object.put("Can I have my Order API");
            object.put("0");
            object.put("WORKING");
            object.put("");
            object.put("KO");
            object.put("");
            object.put("for 2013S2/R00");
            object.put("AUTOMATED");

            data.put(object);
            object = new JSONArray();
            object.put("ICS_Credit");
            object.put("0005A");
            object.put("VCCRM");
            object.put("Can I have my Order API");
            object.put("0");
            object.put("WORKING");
            object.put("");
            object.put("KO");
            object.put("");
            object.put("for 2013S2/R00");
            object.put("AUTOMATED");

            data.put(object);
            object = new JSONArray();
            object.put("ICS_Credit");
            object.put("0005A");
            object.put("VCCRM");
            object.put("Can I have my Order API");
            object.put("0");
            object.put("WORKING");
            object.put("");
            object.put("KO");
            object.put("");
            object.put("for 2013S2/R00");
            object.put("AUTOMATED");

            data.put(object);
            object = new JSONArray();
            object.put("ICS_Credit");
            object.put("0005A");
            object.put("VCCRM");
            object.put("Can I have my Order API");
            object.put("0");
            object.put("WORKING");
            object.put("");
            object.put("KO");
            object.put("");
            object.put("for 2013S2/R00");
            object.put("AUTOMATED");

            data.put(object);
            object = new JSONArray();
            object.put("ICS_Credit");
            object.put("0005A");
            object.put("VCCRM");
            object.put("Can I have my Order API");
            object.put("0");
            object.put("WORKING");
            object.put("");
            object.put("KO");
            object.put("");
            object.put("for 2013S2/R00");
            object.put("AUTOMATED");

            data.put(object);

            object = new JSONArray();
            object.put("ICS_Credit");
            object.put("0005A");
            object.put("VCCRM");
            object.put("Can I have my Order API");
            object.put("0");
            object.put("WORKING");
            object.put("");
            object.put("KO");
            object.put("");
            object.put("for 2013S2/R00");
            object.put("AUTOMATED");

            data.put(object);
            object = new JSONArray();
            object.put("ICS_Credit");
            object.put("0005A");
            object.put("VCCRM");
            object.put("Can I have my Order API");
            object.put("0");
            object.put("WORKING");
            object.put("");
            object.put("KO");
            object.put("");
            object.put("for 2013S2/R00");
            object.put("AUTOMATED");

            data.put(object);
            object = new JSONArray();
            object.put("ICS_Credit");
            object.put("0005A");
            object.put("VCCRM");
            object.put("Can I have my Order API");
            object.put("0");
            object.put("WORKING");
            object.put("");
            object.put("KO");
            object.put("");
            object.put("for 2013S2/R00");
            object.put("AUTOMATED");

            data.put(object);
            object = new JSONArray();
            object.put("ICS_Credit");
            object.put("0005A");
            object.put("VCCRM");
            object.put("Can I have my Order API");
            object.put("0");
            object.put("WORKING");
            object.put("");
            object.put("KO");
            object.put("");
            object.put("for 2013S2/R00");
            object.put("AUTOMATED");

            data.put(object);
            object = new JSONArray();
            object.put("ICS_Credit");
            object.put("0005A");
            object.put("VCCRM");
            object.put("Can I have my Order API");
            object.put("0");
            object.put("WORKING");
            object.put("");
            object.put("KO");
            object.put("");
            object.put("for 2013S2/R00");
            object.put("AUTOMATED");

            data.put(object);

            json.put("aaData", data);
            json.put("iTotalRecords", data.length());
            json.put("iTotalDisplayRecords", data.length());
            resp.setContentType("application/json");
            resp.getWriter().print(json.toString());

        } catch (JSONException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
