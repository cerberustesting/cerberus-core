package com.redcats.tst.refactor;


public class StatusMessage {
    public static String OK = "OK";

    public static String ERROR_PASSWORDEQUALTOOLDPASSOWRD = "The new password is the same to the previous! Please use different password.";
    public static String ERROR_PASSWORDDONTMATCH = "The current password don't match with the password stored!";
    public static String ERROR_PASSWORDERRORUPDATE = "An error occur trying update the password! Please try again.";
    public static String SUCCESS_PASSWORDUPDATE = "The password was successfully updated.";

    public static String ERROR_USERCREATED = "An error occur trying create the user! Please try again.";
    public static String ERROR_USERGROUPCREATED = "An error occur trying create the group of the user! Please try again.";
    public static String SUCCESS_USERCREATED = "The user was successfully created";

    public static String ERROR_USERREMOVED = "An error occur trying delete the user! Please try again.";
    public static String ERROR_USERGROUPREMOVED = "An error occur trying delete the group of the user! Please try again.";
    public static String SUCCESS_USERREMOVED = "The user was successfully removed";

    public static String ERROR_NAMEERRORUPDATE = "An error occur trying update the name of the user! Please try again.";
    public static String SUCCESS_NAMEUPDATE = "The name of user was successfully update";

    public static String ERROR_USERGROUPUPDATE = "An error occur trying update the group of the user! Please try again.";
    public static String SUCCESS_USERGROUPUPDATE = "The group of user was successfully update";

    public static String ERROR_REQUESTERRORUPDATE = "An error occur trying update the request password of the user! Please try again.";
    public static String SUCCESS_REQUESTUPDATE = "The request password of user was successfully update";

    public static String ERROR_LOGINERRORUPDATE = "An error occur trying update the login of the user! Please try again.";
    public static String SUCCESS_LOGINUPDATE = "The login of user was successfully update";
    
    public static String ERROR_TESTCASEERRORUPDATE = "An error occur trying update the test case! Please try again.";
    public static String SUCCESS_TESTCASEUPDATE = "The test case was successfully update";
}
