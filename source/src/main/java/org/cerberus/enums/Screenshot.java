package org.cerberus.enums;

public enum Screenshot {

    NO_SCREENSHOTS_VIDEO (0),
    AUTOMATIC_SCREENSHOTS_ON_ERROR (1),
    SYSTEMATIC_SCREENSHOTS (2),
    AUTOMATIC_SCREENSHOTS_ON_ERROR_AND_VIDEO (3),
    SYSTEMATIC_SCREENSHOTS_AND_VIDEO (4);

    private int value;
    Screenshot(int value) {
        this.value=value;
    }

    public int getValue() {
        return value;
    }


    public static boolean recordVideo(int value) {
        return value == AUTOMATIC_SCREENSHOTS_ON_ERROR_AND_VIDEO.getValue() || value == SYSTEMATIC_SCREENSHOTS_AND_VIDEO.getValue();
    }
    public static boolean printScreenOnError(int value) {
        return value == AUTOMATIC_SCREENSHOTS_ON_ERROR.getValue() || value == AUTOMATIC_SCREENSHOTS_ON_ERROR_AND_VIDEO.getValue();
    }
    public static boolean printScreenSystematicaly(int value) {
        return value == SYSTEMATIC_SCREENSHOTS.getValue() || value == SYSTEMATIC_SCREENSHOTS_AND_VIDEO.getValue();
    }
}
