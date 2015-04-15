package org.cerberus.serviceEngine.impl;
/**
 * Enum that defines all system properties
 * @author FN
 */
public enum SystemPropertyEnum {
            SYS_SYSTEM("SYS_SYSTEM"),
            SYS_APPLI("SYS_APPLI"),
            SYS_APP_DOMAIN("SYS_APP_DOMAIN"),
            SYS_ENV("SYS_ENV"),
            SYS_ENVGP("SYS_ENVGP"),
            SYS_COUNTRY("SYS_COUNTRY"),
            SYS_COUNTRYGP1("SYS_COUNTRYGP1"),
            SYS_SSIP("SYS_SSIP"),
            SYS_SSPORT("SYS_SSPORT"),
            SYS_EXECUTIONID("SYS_EXECUTIONID"),
            SYS_TODAY_YYYY("SYS_TODAY-yyyy"),
            SYS_TODAY_MM("SYS_TODAY-MM"),
            SYS_TODAY_dd("SYS_TODAY-dd"),
            SYS_TODAY_doy("SYS_TODAY-doy"),
            SYS_TODAY_HH("SYS_TODAY-HH"),
            SYS_TODAY_mm("SYS_TODAY-mm"),
            SYS_TODAY_ss("SYS_TODAY-ss"),
            SYS_YESTERDAY_yyyy("SYS_YESTERDAY-yyyy"),
            SYS_YESTERDAY_MM("SYS_YESTERDAY-MM"),
            SYS_YESTERDAY_dd("SYS_YESTERDAY-dd"),
            SYS_YESTERDAY_doy("SYS_YESTERDAY-doy"),
            SYS_YESTERDAY_HH("SYS_YESTERDAY-HH"),
            SYS_YESTERDAY_mm("SYS_YESTERDAY-mm"),
            SYS_YESTERDAY_ss("SYS_YESTERDAY-ss"),
            SYS_ELAPSED_EXESTART("SYS_ELAPSED-EXESTART"),
            SYS_ELAPSED_STEPSTART("SYS_YESTERDAY-ss");
        private final String propertyName;

        public String getPropertyName() {
            return propertyName;
        }


        private SystemPropertyEnum(String propertyName) {
            this.propertyName = propertyName; 
        }
        
        /**
         * Verifies if the property name exists in the list of system properties
         * @param propertyName
         * @return true if property is defined in the enumeration, false if not
         */
        public static boolean contains(String propertyName){
            for(SystemPropertyEnum en : values()){
                if(en.getPropertyName().compareTo(propertyName) == 0){
                    return true;
                }
            }
            return false;
        }
}