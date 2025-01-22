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
package org.cerberus.core.service.appium.impl;

import io.appium.java_client.NoSuchContextException;
import io.appium.java_client.*;
import io.appium.java_client.touch.WaitOptions;
import io.appium.java_client.touch.offset.ElementOption;
import io.appium.java_client.touch.offset.PointOption;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.service.impl.ParameterService;
import org.cerberus.core.engine.entity.*;
import org.cerberus.core.engine.gwt.impl.ActionService;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.enums.MessageGeneralEnum;
import org.cerberus.core.exception.CerberusEventException;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.service.appium.IAppiumService;
import org.cerberus.core.service.appium.SwipeAction;
import org.cerberus.core.service.appium.SwipeAction.Direction;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.StringUtil;
import org.json.JSONException;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.geom.Line2D;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

/**
 * @author bcivel
 */
public abstract class AppiumService implements IAppiumService {

    private static final Logger LOG = LogManager.getLogger(AppiumService.class);

    /**
     * The Appium swipe duration parameter which is got thanks to the
     * {@link ParameterService}
     */
    private static final String CERBERUS_APPIUM_SWIPE_DURATION_PARAMETER = "cerberus_appium_swipe_duration";

    /**
     * The default Appium swipe duration if no
     * {@link AppiumService#CERBERUS_APPIUM_SWIPE_DURATION_PARAMETER} has been
     * defined
     */
    private static final int DEFAULT_CERBERUS_APPIUM_SWIPE_DURATION = 2000;

    @Autowired
    private ParameterService parameters;

    @Override
    public MessageEvent switchToContext(Session session, Identifier identifier) {
        MessageEvent message;
        AppiumDriver driver = session.getAppiumDriver();
        String newContext = "";

        @SuppressWarnings("unchecked")
        Set<String> contextNames = driver.getContextHandles();

        for (String contextName : contextNames) {
            LOG.error("Context : " + contextName);
            if (contextName.contains("WEBVIEW")) {
                driver.context(contextName);
                newContext = contextName;
                break;
            }
        }
        //driver.context("WEBVIEW_1");
        message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_SWITCHTOWINDOW);
        message.setDescription(message.getDescription().replace("%WINDOW%", newContext));
        return message;
    }

    @Override
    public MessageEvent switchToContext(Session session, String context) {
        MessageEvent message;
        AppiumDriver driver = session.getAppiumDriver();
        Set<String> contexts = driver.getContextHandles();

        try {
            if (context.isEmpty()) {
                context = "NATIVE_APP";
            }
            driver.context(context);
            message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_SWITCHTOCONTEXT);
            message.setDescription(message.getDescription().replace("%CONTEXT%", context));
        } catch (NoSuchContextException exception) {
            LOG.error("Impossible to change the context: ", exception);
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SWITCHTOCONTEXT_NO_SUCH_ELEMENT);
            message.setDescription(message.getDescription()
                    .replace("%CONTEXT%", context)
                    .replace("%CONTEXTS%", contexts.toString())
                    .replace("%ERROR%", exception.getMessage()));
        }
        return message;
    }

    @Override
    public MessageEvent wait(Session session, Identifier identifier) {
        MessageEvent message;
        try {
            WebElement elmt = this.getElement(session, identifier, false, false);
            message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_WAIT_ELEMENT);
            message.setDescription(message.getDescription().replace("%ELEMENT%", identifier.getIdentifier() + "=" + identifier.getLocator()));
            return message;

        } catch (NoSuchElementException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_WAIT_NO_SUCH_ELEMENT);
            message.setDescription(message.getDescription().replace("%ELEMENT%", identifier.getIdentifier() + "=" + identifier.getLocator()));
            LOG.debug(exception.toString());
            return message;

        } catch (WebDriverException exception) {
            LOG.error("Exception during Appium Wait Operation.", exception);
            return parseWebDriverException(exception);
        }
    }

    @Override
    public MessageEvent type(Session session, Identifier identifier, String valueToType, String propertyName) {
        MessageEvent message;
        MessageEvent foundElementMsg = new MessageEvent(MessageEventEnum.ACTION_FAILED_TYPE_NO_SUCH_ELEMENT);
        try {
            if (!StringUtil.isEmptyOrNULLString(valueToType)) {
                WebElement elmt = this.getElement(session, identifier, false, false);
                Integer numberOfElement = this.getNumberOfElements(session, identifier);
                foundElementMsg = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_FOUND_ELEMENT);
                foundElementMsg.resolveDescription("NUMBER", numberOfElement.toString());
                foundElementMsg.resolveDescription("ELEMENT", identifier.toString());
                if (elmt instanceof MobileElement) {
                    ((MobileElement) this.getElement(session, identifier, false, false)).setValue(valueToType);
                } else { // FIXME See if we can delete it ??
                    TouchAction action = new TouchAction(session.getAppiumDriver());
                    action.press(ElementOption.element(this.getElement(session, identifier, false, false))).release().perform();
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        LOG.error("Exception during Appium Type action.", e);
                    }
                    session.getAppiumDriver().getKeyboard().sendKeys(valueToType);
                }
            }
            message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_TYPE);
            message.setDescription(message.getDescription().replace("%ELEMENT%", identifier.getIdentifier() + "=" + identifier.getLocator()).replace("ELEMENTFOUND", foundElementMsg.getDescription()));
            if (!StringUtil.isEmptyOrNULLString(valueToType)) {
                message.setDescription(message.getDescription().replace("%DATA%", ParameterParserUtil.securePassword(valueToType, propertyName)));
            } else {
                message.setDescription(message.getDescription().replace("%DATA%", "No property"));
            }
            return message;
        } catch (NoSuchElementException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_TYPE_NO_SUCH_ELEMENT);
            message.setDescription(message.getDescription().replace("%ELEMENT%", identifier.getIdentifier() + "=" + identifier.getLocator()));
            LOG.debug(exception.toString());
            return message;
        } catch (WebDriverException exception) {
            LOG.error(exception.toString());
            return parseWebDriverException(exception);
        }
    }

    @Override
    public MessageEvent click(final Session session, final Identifier identifier, Integer hOffset, Integer vOffset) {
        try {
            MessageEvent foundElementMsg;
            final TouchAction action = new TouchAction(session.getAppiumDriver());

            if (identifier.isSameIdentifier(Identifier.Identifiers.COORDINATE)) {
                final Coordinates coordinates = getCoordinates(identifier);
                Point offset = new Point(coordinates.getX(), coordinates.getY());
                foundElementMsg = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_FOUND_ELEMENT);
                foundElementMsg.resolveDescription("NUMBER", "1");
                foundElementMsg.resolveDescription("ELEMENT", identifier.toString());
                action.tap(PointOption.point(offset)).perform();
            } else {
                WebElement element = getElement(session, identifier, false, false);
                Integer numberOfElement = this.getNumberOfElements(session, identifier);
                foundElementMsg = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_FOUND_ELEMENT);
                foundElementMsg.resolveDescription("NUMBER", numberOfElement.toString());
                foundElementMsg.resolveDescription("ELEMENT", identifier.toString());
                action.tap(ElementOption.element(element, hOffset, vOffset)).perform();
            }
            return new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CLICK).resolveDescription("ELEMENT", identifier.toString()).resolveDescription("ELEMENTFOUND", foundElementMsg.getDescription());
        } catch (NoSuchElementException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(e.getMessage());
            }
            return new MessageEvent(MessageEventEnum.ACTION_FAILED_CLICK_NO_SUCH_ELEMENT).resolveDescription("ELEMENT", identifier.toString());
        } catch (WebDriverException e) {
            LOG.warn(e.getMessage());
            return parseWebDriverException(e);
        }

    }

    /**
     * @param exception the exception need to be parsed by Cerberus
     * @return A new Event Message with selenium related description
     * @author vertigo17
     */
    private MessageEvent parseWebDriverException(WebDriverException exception) {
        MessageEvent mes;
        LOG.error(exception.toString(), exception);
        mes = new MessageEvent(MessageEventEnum.ACTION_FAILED_SELENIUM_CONNECTIVITY);
        mes.setDescription(mes.getDescription().replace("%ERROR%", exception.getMessage().split("\n")[0]));
        return mes;
    }

    /**
     * Get the {@link Coordinates} represented by the given {@link Identifier}
     *
     * @param identifier the {@link Identifier} to parse to get the
     *                   {@link Coordinates}
     * @return the {@link Coordinates} represented by the given
     * {@link Identifier}
     * @throws NoSuchElementException if no {@link Coordinates} can be found
     *                                inside the given {@link Identifier}
     */
    private Coordinates getCoordinates(final Identifier identifier) {
        if (identifier == null || !identifier.isSameIdentifier(Identifier.Identifiers.COORDINATE)) {
            throw new NoSuchElementException("Unable to get coordinates from a non coordinates identifier");
        }
        final Matcher coordinates = Identifier.Identifiers.COORDINATE_VALUE_PATTERN.matcher(identifier.getLocator());
        if (!coordinates.find()) {
            throw new NoSuchElementException("Bad coordinates format");
        }
        try {
            return new Coordinates(
                    Integer.valueOf(coordinates.group("xCoordinate")),
                    Integer.valueOf(coordinates.group("yCoordinate"))
            );
        } catch (NumberFormatException e) {
            throw new NoSuchElementException("Bad coordinates format", e);
        }
    }

    private By getBy(Identifier identifier) {

        LOG.debug("Finding selenium Element : " + identifier.getLocator() + " by : " + identifier.getIdentifier());

        if (identifier.getIdentifier().equalsIgnoreCase("id")) {
            return By.id(identifier.getLocator());

        } else if (identifier.getIdentifier().equalsIgnoreCase("name")) {
            return By.name(identifier.getLocator());

        } else if (identifier.getIdentifier().equalsIgnoreCase("class")) {
            return By.className(identifier.getLocator());

        } else if (identifier.getIdentifier().equalsIgnoreCase("css")) {
            return By.cssSelector(identifier.getLocator());

        } else if (identifier.getIdentifier().equalsIgnoreCase("xpath")) {
            return By.xpath(identifier.getLocator());

        } else if (identifier.getIdentifier().equalsIgnoreCase("link")) {
            return By.linkText(identifier.getLocator());

        } else if (identifier.getIdentifier().equalsIgnoreCase("data-cerberus")) {
            return By.xpath("//*[@data-cerberus='" + identifier.getLocator() + "']");

        } else if (identifier.getIdentifier().equalsIgnoreCase("accesibility-id")) {
            return MobileBy.AccessibilityId(identifier.getLocator());

        } else {
            throw new NoSuchElementException(identifier.getIdentifier());
        }
    }

    private WebElement getElement(Session session, Identifier identifier, boolean visible, boolean clickable) {
        AppiumDriver driver = session.getAppiumDriver();
        By locator = this.getBy(identifier);

        LOG.debug("Waiting for Element : " + identifier.getIdentifier() + "=" + identifier.getLocator());
        try {
            WebDriverWait wait = new WebDriverWait(driver, TimeUnit.MILLISECONDS.toSeconds(session.getCerberus_appium_wait_element()));
            if (visible) {
                if (clickable) {
                    wait.until(ExpectedConditions.elementToBeClickable(locator));
                } else {
                    wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
                }
            } else {
                wait.until(ExpectedConditions.presenceOfElementLocated(locator));
            }
        } catch (TimeoutException exception) {
            LOG.fatal("Exception waiting for element :" + exception.toString());
            throw new NoSuchElementException(identifier.getIdentifier() + "=" + identifier.getLocator());
        }
        LOG.debug("Finding Element : " + identifier.getIdentifier() + "=" + identifier.getLocator());
        return driver.findElement(locator);
    }

    /**
     * @param session
     * @param action
     * @return
     * @throws IllegalArgumentException
     */
    @Override
    public Direction getDirectionForSwipe(Session session, SwipeAction action) throws IllegalArgumentException {
        Dimension window = session.getAppiumDriver().manage().window().getSize();
        SwipeAction.Direction direction;
        switch (action.getActionType()) {
            case UP:
                direction = SwipeAction.Direction.fromLine(
                        new Line2D.Double(
                                window.getWidth() / 2,
                                2 * window.getHeight() / 3,
                                0,
                                -window.getHeight() / 3
                        )
                );
                break;
            case DOWN:
                direction = SwipeAction.Direction.fromLine(
                        new Line2D.Double(
                                window.getWidth() / 2,
                                window.getHeight() / 3,
                                0,
                                window.getHeight() / 3
                        )
                );
                break;
            case LEFT:
                direction = SwipeAction.Direction.fromLine(
                        new Line2D.Double(
                                2 * window.getWidth() / 3,
                                window.getHeight() / 2,
                                -window.getWidth() / 3,
                                0
                        )
                );
                break;
            case RIGHT:
                direction = SwipeAction.Direction.fromLine(
                        new Line2D.Double(
                                window.getWidth() / 3,
                                window.getHeight() / 2,
                                window.getWidth() / 3,
                                0
                        )
                );
                break;
            case CUSTOM:
                direction = action.getCustomDirection();
                break;
            default:
                throw new IllegalArgumentException("Unknown direction");
        }
        return direction;
    }

    @Override
    public MessageEvent scrollTo(TestCaseExecution testCaseExecution, Identifier element, String numberScrollDownMax, Integer hOffset, Integer vOffset) throws IllegalArgumentException {
        AppiumDriver driver = testCaseExecution.getSession().getAppiumDriver();
        MessageEvent message;
        try {
            message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_SCROLLTO);

            int numberOfScrollDown = 8;
            try {
                numberOfScrollDown = Integer.parseInt(numberScrollDownMax);
            } catch (NumberFormatException e) {
                // do nothing
            }

            // check text
            if (element.getIdentifier().equals("text")) {
                scrollDown(testCaseExecution, By.xpath("//*[contains(@text,'" + element.getLocator() + "')]"), numberOfScrollDown, hOffset, vOffset);
            } else {
                scrollDown(testCaseExecution, this.getBy(element), numberOfScrollDown, hOffset, vOffset);
            }

            message.setDescription(message.getDescription().replace("%VALUE%", element.toString()));

            return message;
        } catch (CerberusEventException e) {
            LOG.error("An error occured during scroll to (element:" + element + ",numberScrollDownMax:" + numberScrollDownMax + ")", e);
            return e.getMessageError();
        } catch (Exception e) {
            LOG.error("An error occured during scroll to (element:" + element + ",numberScrollDownMax:" + numberScrollDownMax + ")", e);
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_GENERIC);
            message.setDescription(message.getDescription().replace("%DETAIL%", e.getMessage()));
            return message;
        }
    }

    /**
     * Scroll down and stop when element is present
     *
     * @param driver
     * @param element
     * @return
     */
    private boolean scrollDown(TestCaseExecution testCaseExecution, By element, int numberOfScrollDown, Integer hOffset, Integer vOffset) throws CerberusEventException{

        AppiumDriver driver = testCaseExecution.getSession().getAppiumDriver();
        Dimension screenSize = driver.manage().window().getSize();

        float screenTopPercentage = parameters.getParameterFloatByKey("cerberus_appium_scroll_endTopScreenPercentageScreenHeight", null, 0.125f);
        float screenBottomPercentage = parameters.getParameterFloatByKey("cerberus_appium_scroll_startBottomPercentageScreenHeight", null, 0.8f);

        /**
         * Check if cerberus_appium_scroll_endTopScreenPercentageScreenHeight and cerberus_appium_scroll_startBottomPercentageScreenHeight parameters are float between 0 and 1
         */
        if (screenTopPercentage < 0 || screenTopPercentage > 1 ||  screenBottomPercentage < 0 || screenBottomPercentage > 1){
            MessageEvent me  =new MessageEvent(MessageEventEnum.ACTION_FAILED_SCROLL_INVALID_PARAMETER);
            throw new CerberusEventException(me);
        }

        int pressX = driver.manage().window().getSize().width / 2;

        int bottomY = (int) (screenSize.height * screenBottomPercentage);
        int topY = (int) (screenSize.height * screenTopPercentage);

        int i = 0;

        do {
            i++;
            boolean isPresent = driver.findElements(element).size() > 0;
            if (isPresent && driver.findElement(element).isDisplayed()) {

                //Element found, perform another scroll to put the element at the middle of the screen at the defined offset.
                int distanceFromMiddleOfTheScreen = (driver.manage().window().getSize().height / 2) - driver.findElement(element).getLocation().getY();
                testCaseExecution.addExecutionLog(ExecutionLog.STATUS_INFO, "[Action:ScrollTo] : Element " + element + " displayed. Coordinate : (" + driver.findElement(element).getLocation().getX() + ";" + driver.findElement(element).getLocation().getY() + "). Distance from the middle of the screen ("+distanceFromMiddleOfTheScreen+"px)");


                if (distanceFromMiddleOfTheScreen > 0) {
                    int targetCoordinateY = topY + distanceFromMiddleOfTheScreen + vOffset;
                    scroll(driver, pressX, topY, pressX, targetCoordinateY);
                    testCaseExecution.addExecutionLog(ExecutionLog.STATUS_INFO, "[Action:ScrollTo] : Element " + element + " displayed. Perform another scroll to get it at the middle of the screen after applied the offset (" + hOffset + ";" + vOffset + ") scrolling from coord("+pressX+";"+topY+") to coord("+pressX+";"+targetCoordinateY+")");
                } else if (distanceFromMiddleOfTheScreen < 0 ){
                    int targetCoordinateY = bottomY + distanceFromMiddleOfTheScreen + vOffset;
                    scroll(driver, pressX, bottomY, pressX, targetCoordinateY);
                    testCaseExecution.addExecutionLog(ExecutionLog.STATUS_INFO, "[Action:ScrollTo] : Element " + element + " displayed. Perform another scroll to get it at the middle of the screen after applied the offset (" + hOffset + ";" + vOffset + ") scrolling from coord("+pressX+";"+bottomY+") to coord("+pressX+";"+targetCoordinateY+")");
                }

                return true;
            } else {
                scroll(driver, pressX, bottomY, pressX, topY);
                testCaseExecution.addExecutionLog(ExecutionLog.STATUS_INFO, "[Action:ScrollTo] : Element "+element+" not displayed. Scrolled "+i+"/"+numberOfScrollDown+" from coord("+pressX+";"+bottomY+") to coord("+pressX+";"+topY+")");
            }

        } while (i < numberOfScrollDown);

        return false;
    }

    private void scroll(AppiumDriver driver, int fromX, int fromY, int toX, int toY) {

        TouchAction touchAction = new TouchAction(driver);
        touchAction.longPress(PointOption.point(fromX, fromY)).moveTo(PointOption.point(toX, toY)).release().perform();

    }

    public abstract String executeCommandString(Session session, String cmd, String args) throws IllegalArgumentException, JSONException;

    public String getElementPosition(Session session, Identifier identifier) {
        AppiumDriver driver = session.getAppiumDriver();

        MobileElement element = (MobileElement) driver.findElement(this.getBy(identifier));
        Point location = element.getLocation();

        return location.getX() + ";" + location.getY();
    }

    @Override
    public MessageEvent longPress(final Session session, final Identifier identifier, final Integer timeDuration) {
        try {
            final TouchAction action = new TouchAction(session.getAppiumDriver());
            if (identifier.isSameIdentifier(Identifier.Identifiers.COORDINATE)) {
                final Coordinates coordinates = getCoordinates(identifier);
                action.press(PointOption.point(coordinates.getX(), coordinates.getY())).waitAction(WaitOptions.waitOptions(Duration.ofMillis(timeDuration))).release().perform();
            } else {
                action.press(ElementOption.element(getElement(session, identifier, false, false))).waitAction(WaitOptions.waitOptions(Duration.ofMillis(timeDuration))).release().perform();
            }
            return new MessageEvent(MessageEventEnum.ACTION_SUCCESS_LONG_CLICK).resolveDescription("ELEMENT", identifier.toString());
        } catch (NoSuchElementException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(e.getMessage());
            }
            return new MessageEvent(MessageEventEnum.ACTION_FAILED_LONG_CLICK_NO_SUCH_ELEMENT).resolveDescription("ELEMENT", identifier.toString());
        } catch (WebDriverException e) {
            LOG.warn(e.getMessage());
            return parseWebDriverException(e);
        }

    }

    @Override
    public MessageEvent clearField(final Session session, final Identifier identifier) {
        try {
            final TouchAction action = new TouchAction(session.getAppiumDriver());
            if (identifier.isSameIdentifier(Identifier.Identifiers.COORDINATE)) {
                final Coordinates coordinates = getCoordinates(identifier);
                click(session, identifier,0, 0);
            } else {
                click(session, identifier, 0 ,0);
                this.getElement(session, identifier, false, false).clear();

            }
            return new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CLEAR).resolveDescription("ELEMENT", identifier.toString());
        } catch (NoSuchElementException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(e.getMessage());
            }
            return new MessageEvent(MessageEventEnum.ACTION_FAILED_CLEAR_NO_SUCH_ELEMENT).resolveDescription("ELEMENT", identifier.toString());
        } catch (WebDriverException e) {
            LOG.warn(e.getMessage());
            return parseWebDriverException(e);
        }

    }

    private Integer getNumberOfElements(Session session, Identifier identifier) {
        By locator = this.getBy(identifier);
        AppiumDriver driver = session.getAppiumDriver();
        return driver.findElements(locator).size();
    }
}
