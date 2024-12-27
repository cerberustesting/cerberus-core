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
package org.cerberus.core.servlet.crud.usermanagement;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.UserRole;
import org.cerberus.core.crud.entity.User;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.crud.service.IUserService;
import org.cerberus.core.crud.service.IUserSystemService;
import org.cerberus.core.util.StringUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.cerberus.core.crud.service.IUserRoleService;

/**
 * @author ip100003
 */
@WebServlet(name = "GetKeycloakImport", urlPatterns = {"/GetKeycloakImport"})
public class GetKeycloakImport extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(GetKeycloakImport.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String realm = request.getParameter("realm");

        JSONObject finalJSON = new JSONObject();

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        IUserService userService = appContext.getBean(IUserService.class);
        IUserSystemService userSystemService = appContext.getBean(IUserSystemService.class);
        IUserRoleService userGroupService = appContext.getBean(IUserRoleService.class);
        try {
            finalJSON.put("id", realm);
            finalJSON.put("realm", realm);

            JSONArray arrayRoles = new JSONArray();
            JSONObject emptyJSONObject = new JSONObject();
            JSONArray emptyJSONArray = new JSONArray();

            JSONObject roleJSON = new JSONObject();
            roleJSON.put("name", "IntegratorDeploy").put("composite", false).put("clientRole", false).put("containerId", realm).put("attributes", emptyJSONObject);
            arrayRoles.put(roleJSON);
            roleJSON = new JSONObject();
            roleJSON.put("name", "Administrator").put("composite", false).put("clientRole", false).put("containerId", realm).put("attributes", emptyJSONObject);
            arrayRoles.put(roleJSON);
            roleJSON = new JSONObject();
            roleJSON.put("name", "TestStepLibrary").put("composite", false).put("clientRole", false).put("containerId", realm).put("attributes", emptyJSONObject);
            arrayRoles.put(roleJSON);
            roleJSON = new JSONObject();
            roleJSON.put("name", "TestRO").put("composite", false).put("clientRole", false).put("containerId", realm).put("attributes", emptyJSONObject);
            arrayRoles.put(roleJSON);
            roleJSON = new JSONObject();
            roleJSON.put("name", "IntegratorRO").put("composite", false).put("clientRole", false).put("containerId", realm).put("attributes", emptyJSONObject);
            arrayRoles.put(roleJSON);
            roleJSON = new JSONObject();
            roleJSON.put("name", "Test").put("composite", false).put("clientRole", false).put("containerId", realm).put("attributes", emptyJSONObject);
            arrayRoles.put(roleJSON);
            roleJSON = new JSONObject();
            roleJSON.put("name", "IntegratorNewChain").put("composite", false).put("clientRole", false).put("containerId", realm).put("attributes", emptyJSONObject);
            arrayRoles.put(roleJSON);
            roleJSON = new JSONObject();
            roleJSON.put("name", "RunTest").put("composite", false).put("clientRole", false).put("containerId", realm).put("attributes", emptyJSONObject);
            arrayRoles.put(roleJSON);
            roleJSON = new JSONObject();
            roleJSON.put("name", "Integrator").put("composite", false).put("clientRole", false).put("containerId", realm).put("attributes", emptyJSONObject);
            arrayRoles.put(roleJSON);
            roleJSON = new JSONObject();
            roleJSON.put("name", "Label").put("composite", false).put("clientRole", false).put("containerId", realm).put("attributes", emptyJSONObject);
            arrayRoles.put(roleJSON);
            roleJSON = new JSONObject();
            roleJSON.put("name", "TestAdmin").put("composite", false).put("clientRole", false).put("containerId", realm).put("attributes", emptyJSONObject);
            arrayRoles.put(roleJSON);
            roleJSON = new JSONObject();
            roleJSON.put("name", "TestDataManager").put("composite", false).put("clientRole", false).put("containerId", realm).put("attributes", emptyJSONObject);
            arrayRoles.put(roleJSON);

            JSONObject rolesJSON = new JSONObject();
            rolesJSON.put("realm", arrayRoles);

            finalJSON.put("roles", rolesJSON);

            JSONArray usersArray = new JSONArray(); //data that will be shown in the table
//            JSONObject jsonResponse = new JSONObject();
            try {
//                long createdTimestamp = Long.valueOf("1544992910544");
                long createdTimestamp = Long.valueOf("0");

                JSONObject cred = new JSONObject();
                // Default password to : Cerberus2018
                cred.put("type", "password");
                cred.put("hashedSaltedValue", "Px1gKe9ehyjBrrWjEBdmBdhEhQssgW0+pW4qnJ5ILmd6uZX9CaSYljE7IKb8K/zYICa/UJ8e3sr/o2bMFtwlMg==");
                cred.put("salt", "MFcfOTQzzqh1KoGywz1p+A==");
                cred.put("hashIterations", 27500);
                cred.put("counter", 0);
                cred.put("algorithm", "pbkdf2-sha256");
                cred.put("digits", 0);
                cred.put("period", 0);
                cred.put("createdDate", createdTimestamp);
                cred.put("config", emptyJSONObject);
                JSONArray credArr = new JSONArray();
                credArr.put(cred);

                JSONArray disaCred = new JSONArray();
                disaCred.put("password");

                JSONArray reqActions = new JSONArray();
                reqActions.put("VERIFY_EMAIL");
                reqActions.put("UPDATE_PASSWORD");
                reqActions.put("UPDATE_PROFILE");

                JSONArray account = new JSONArray();
                account.put("manage-account");
                account.put("view-profile");
                JSONObject acc = new JSONObject();
                acc.put("account", account);

                for (User myUser : userService.findallUser()) {
                    if (!StringUtil.isEmptyOrNull(myUser.getEmail())) {

                    JSONArray roles = new JSONArray();
                    roles.put("uma_authorization");
                    roles.put("offline_access");
                    for (UserRole myUserGroup : userGroupService.findRoleByKey(myUser.getLogin())) {
                        roles.put(myUserGroup.getRole());
                    }

                    JSONObject u = new JSONObject();
                    u.put("createdTimestamp", createdTimestamp);
                    u.put("username", myUser.getLogin());
                    u.put("enabled", true);
                    u.put("totp", false);
                    u.put("emailVerified", false);
                    if (myUser.getName() == null) {
                        u.put("firstName", myUser.getLogin());
                    } else {
                        u.put("firstName", myUser.getName());
                    }
                    if (myUser.getEmail() == null) {
                        u.put("email", myUser.getLogin());
                    } else {
                        u.put("email", myUser.getEmail());
                    }

                    u.put("credentials", credArr);

                    u.put("disableableCredentialTypes", disaCred);

                    u.put("requiredActions", reqActions);
                    u.put("realmRoles", roles);
                    u.put("clientRoles", acc);

                    u.put("notBefore", createdTimestamp);

                    u.put("groups", emptyJSONArray);

                    usersArray.put(u);
                    }
                }
            } catch (CerberusException ex) {
                response.setContentType("text/html");
                response.getWriter().print(ex.getMessageError().getDescription());
            }

            finalJSON.put("users", usersArray);

            response.setContentType("application/json");
            response.getWriter().print(finalJSON.toString());
        } catch (JSONException e) {
            LOG.warn(e);
            response.setContentType("text/html");
            response.getWriter().print(e.getMessage());
        }
    }
}
