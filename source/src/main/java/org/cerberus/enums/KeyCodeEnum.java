/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
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
package org.cerberus.enums;

import java.awt.event.KeyEvent;
import org.openqa.selenium.Keys;

/**
 * Stores the AWT codes for each key in order to perform those. Keys stored are the same defined in class: org.openqa.selenium.Keys 
 * BACK_SPACE, CANCEL, HELP, TAB, CLEAR, RETURN, ENTER, SHIFT, CONTROL, 
 * ALT,  PAUSE, ESCAPE, SPACE, PAGE_UP, 
 * PAGE_DOWN, END, HOME, LEFT, ARROW_LEFT, UP, ARROW_UP, RIGHT, 
 * ARROW_RIGHT, DOWN, ARROW_DOWN, INSERT, DELETE, SEMICOLON, 
 * EQUALS, NUMPAD0, NUMPAD1, NUMPAD2, NUMPAD3, NUMPAD4, NUMPAD5, 
 * NUMPAD6, NUMPAD7, NUMPAD8, NUMPAD9, MULTIPLY, ADD, SEPARATOR, 
 * SUBTRACT, DECIMAL, DIVIDE, F1, F2, F3, F4, F5, F6, F7, F8, F9, 
 * F10, F11, F12, META, 
 * Selenium's Keys that don't have match for AWT:
 //NULL,  LEFT_SHIFT, LEFT_CONTROL, LEFT_ALT, COMMAND, ZENKAKU_HANKAKU
 * @author FNogueira
 */
public enum KeyCodeEnum {
    NOT_VALID(0, "NOT_VALID", -1),
    BACK_SPACE(1, "BACK_SPACE", KeyEvent.VK_BACK_SPACE),
    CANCEL(2, "CANCEL", KeyEvent.VK_CANCEL),
    HELP(3, "HELP", KeyEvent.VK_HELP),
    TAB(4, "TAB", KeyEvent.VK_TAB),
    CLEAR(5, "CLEAR", KeyEvent.VK_CLEAR),
    RETURN(6, "RETURN", KeyEvent.VK_ENTER),
    ENTER(7, "ENTER", KeyEvent.VK_ENTER),
    SHIFT(8, "SHIFT", KeyEvent.VK_SHIFT),    
    CONTROL(9, "CONTROL", KeyEvent.VK_CONTROL),    
    ALT(10, "ALT", KeyEvent.VK_ALT),
    PAUSE(11, "PAUSE", KeyEvent.VK_PAUSE),
    ESCAPE(12, "ESCAPE", KeyEvent.VK_ESCAPE),
    SPACE(13, "SPACE", KeyEvent.VK_SPACE),
    PAGE_UP(14, "PAGE_UP", KeyEvent.VK_PAGE_UP),
    PAGE_DOWN(15, "PAGE_DOWN", KeyEvent.VK_PAGE_DOWN),
    END(16, "END", KeyEvent.VK_END),
    HOME(17, "HOME", KeyEvent.VK_HOME),
    LEFT(18, "LEFT", KeyEvent.VK_LEFT),
    ARROW_LEFT(19, "ARROW_LEFT", KeyEvent.VK_KP_LEFT),
    UP(20, "UP", KeyEvent.VK_UP),
    ARROW_UP(21, "ARROW_UP", KeyEvent.VK_KP_UP),
    RIGHT(22, "RIGHT", KeyEvent.VK_RIGHT),
    ARROW_RIGHT(23, "ARROW_RIGHT", KeyEvent.VK_KP_RIGHT),
    DOWN(24, "DOWN", KeyEvent.VK_DOWN),
    ARROW_DOWN(25, "ARROW_DOWN", KeyEvent.VK_KP_DOWN),
    INSERT(26, "INSERT", KeyEvent.VK_INSERT),
    DELETE(27, "DELETE", KeyEvent.VK_DELETE),
    SEMICOLON(28, "SEMICOLON", KeyEvent.VK_SEMICOLON), 
    EQUALS(29, "EQUALS", KeyEvent.VK_EQUALS),
    NUMPAD0(30, "NUMPAD0", KeyEvent.VK_NUMPAD0),
    NUMPAD1(31, "NUMPAD1", KeyEvent.VK_NUMPAD1),
    NUMPAD2(32, "NUMPAD2", KeyEvent.VK_NUMPAD2),
    NUMPAD3(33, "NUMPAD3", KeyEvent.VK_NUMPAD3),
    NUMPAD5(34, "NUMPAD5", KeyEvent.VK_NUMPAD5),
    NUMPAD6(35, "NUMPAD6", KeyEvent.VK_NUMPAD6),
    NUMPAD7(36, "NUMPAD7", KeyEvent.VK_NUMPAD7),
    NUMPAD8(37, "NUMPAD8", KeyEvent.VK_NUMPAD8),
    NUMPAD9(38, "NUMPAD9", KeyEvent.VK_NUMPAD9),
    MULTIPLY(39, "MULTIPLY", KeyEvent.VK_MULTIPLY),
    ADD(40, "ADD", KeyEvent.VK_ADD),
    SEPARATOR(41, "SEPARATOR", KeyEvent.VK_SEPARATOR),
    SUBTRACT(42, "SUBTRACT", KeyEvent.VK_SUBTRACT),
    DECIMAL(43, "DECIMAL", KeyEvent.VK_DECIMAL),
    DIVIDE(44, "DIVIDE", KeyEvent.VK_DIVIDE),
    F1(45, "F1", KeyEvent.VK_F1),
    F2(46, "F2", KeyEvent.VK_F2),
    F3(47, "F3", KeyEvent.VK_F3),
    F4(48, "F4", KeyEvent.VK_F4),
    F5(49, "F5", KeyEvent.VK_F5),
    F6(50, "F6", KeyEvent.VK_F6),
    F7(51, "F7", KeyEvent.VK_F7),
    F8(52, "F8", KeyEvent.VK_F8),
    F9(53, "F9", KeyEvent.VK_F9),
    F10(54, "F10", KeyEvent.VK_F10),
    F11(55, "F11", KeyEvent.VK_F11),
    F12(56, "F12", KeyEvent.VK_F12),
    META(57, "META", KeyEvent.VK_META),
       
    //TODO: check the best approach to implement
    //NULL(, "NULL", KeyEvent.),
    //LEFT_SHIFT(, "LEFT_SHIFT", KeyEvent.VK_SHIFT), 
    //LEFT_CONTROL(, "LEFT_CONTROL", KeyEvent.VK_CONTROL),
    //LEFT_ALT(, "LEFT_ALT", KeyEvent.VK_ALT),
    //ZENKAKU_HANKAKU(, "ZENKAKU_HANKAKU", KeyEvent.VK_),
    //COMMAND(8, "COMMAND", KeyEvent.vk_c),
    ;
    
    
    
    private final int code;
    private final String keyName;
    private final int keyCode;
    
    private KeyCodeEnum(int code, String keyName, int keyCode) {
        this.code = code;
        this.keyName = keyName; 
        this.keyCode = keyCode;
    }
     
    /**
    * Gets the AWT key code with basis on the name of the key
    * @param keyName - name of the key
    * @return keyCode if property is defined in the enumeration, false if not
    */
    public static int getAWTKeyCode(String keyName){
       for(KeyCodeEnum en : values()){
           if(en.getKeyName().compareTo(keyName) == 0){
               return en.getKeyCode();
           }
       }
       return NOT_VALID.getKeyCode();
    }
    
    /**
    * Gets the Selenium's key code with basis on the name of the key
    * @param keyName - name of the key
    * @return keyCode if property is defined in the enumeration, false if not
    */
    public static Keys getSeleniumKeyCode(String keyName){
        return Keys.valueOf(keyName);       
    }
   
    public int getCode() {
        return code;
    }

    public String getKeyName() {
        return keyName;
    }

    public int getKeyCode() {
        return keyCode;
    }

}
