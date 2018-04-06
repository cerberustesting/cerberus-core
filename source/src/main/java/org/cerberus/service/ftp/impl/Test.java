package org.cerberus.service.ftp.impl;

import java.net.MalformedURLException;
import java.net.URL;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.TouchAction;
import org.openqa.selenium.By;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.remote.AppiumCommandExecutor;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.cerberus.engine.execution.impl.MyHttpClientFactory;
import org.openqa.selenium.remote.CommandInfo;
import org.openqa.selenium.remote.DesiredCapabilities;

public class Test {

	public static void main(String[] args) throws MalformedURLException {
		/**
		String hubUrl = "http://127.0.0.1:4723/wd/hub";
		URL url;
		url = new URL(hubUrl);
		DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability("device","Android");
        caps.setCapability("platformName","Android");
        caps.setCapability("deviceName","testadv");
        caps.setCapability("appActivity","com.android.calculator2.Calculator");
        caps.setCapability("appPackage","com.android.calculator2");

		AppiumCommandExecutor executor = null;
		String proxyHost = "127.0.0.1";
		int proxyPort = 4723;
		HttpClientBuilder builder = HttpClientBuilder.create();
		HttpHost proxy = new HttpHost(proxyHost, proxyPort);
		builder.setProxy(proxy);
		MyHttpClientFactory factory = new MyHttpClientFactory(builder);
		executor = new AppiumCommandExecutor(new HashMap<String, CommandInfo>(), url, factory);
		AppiumDriver appiumDriver = new AndroidDriver(executor, caps);
		TouchAction action = new TouchAction(appiumDriver);
		action.tap(appiumDriver.findElement(By.xpath("//android.widget.Button[1]"))).perform();
		**/

	}

}
