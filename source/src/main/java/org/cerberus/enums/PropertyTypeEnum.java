package org.cerberus.enums;
/**
 * Enumeration that defines all property types
 * @author FNogueira
 */
public enum PropertyTypeEnum {
    GET_FROM_DATALIB("getFromDataLib_BETA"),
    EXECUTE_SQL("executeSql"),
    EXECUTE_SQL_FROM_LIB("executeSqlFromLib"), 
    ACCESS_SUBDATA("accessSubData"), 
    TEXT("text"), 
    GET_FROM_HTML_VISIBLE("getFromHtmlVisible"), 
    GET_FROM_HTML("getFromHtml"), 
    GET_FROM_JS("getFromJS"), 
    GET_FROM_TEST_DATA("getFromTestData"), 
    GET_ATTRIBUTE_FROM_HTML("getAttributeFromHtml"), 
    GET_FROM_COOKIE("getFromCookie"), 
    GET_FROM_XML("getFromXml"), 
    GET_FROM_JSON("getFromJson"), 
    EXECUTE_SOAP_FROM_LIB("executeSoapFromLib"), 
    GET_DIFFERENCES_FROM_XML("getDifferencesFromXml");
    
    private final String propertyName;

    public String getPropertyName() {
        return propertyName;
    }


    private PropertyTypeEnum(String propertyName) {
        this.propertyName = propertyName; 
    }

    /**
     * Verifies if the property name exists in the list of system properties
     * @param propertyName
     * @return true if property is defined in the enumeration, false if not
     */
    public static boolean contains(String propertyName){
        for(PropertyTypeEnum en : values()){
            if(en.getPropertyName().compareTo(propertyName) == 0){
                return true;
            }
        }
        return false;
    }
    
    @Override
    public String toString(){
        return propertyName;
    }
}