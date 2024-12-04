package com.scriza;

import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class CustomDriver {
	/*
	 * private static final ThreadLocal<WebDriver> threadLocalDriver = new
	 * ThreadLocal<>();
	 * 
	 * public static WebDriver getDriver() { if (threadLocalDriver.get() == null) {
	 * System.setProperty("webdriver.chrome.driver",
	 * "C:\\selenium WebDriver\\chromedriver-win64\\chromedriver.exe");
	 * threadLocalDriver.set(new ChromeDriver()); // Initialize a new WebDriver
	 * instance for each thread } return threadLocalDriver.get(); }
	 * 
	 * public static void closeDriver() { WebDriver driver =
	 * threadLocalDriver.get(); if (driver != null) { driver.quit();
	 * threadLocalDriver.remove(); } }
	 */
	private static Map<String,WebDriver> webDriverMap = new HashMap();

	public static WebDriver getWebDriver(String aadhar) {
		return webDriverMap.get(aadhar);
	}

	public static void setWebDriverMap(String aadhar,WebDriver driver) {
		webDriverMap.put(aadhar, driver);
	}
}
