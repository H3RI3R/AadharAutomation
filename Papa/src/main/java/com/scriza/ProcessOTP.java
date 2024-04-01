
package com.scriza;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import jakarta.servlet.annotation.WebServlet;
@WebServlet("/processOTP")
public class ProcessOTP {
    public void process(String otp) {
        // Set ChromeDriver path
        System.setProperty("webdriver.chrome.driver", "C:\\selenium WebDriver\\chromedriver-win64\\chromedriver.exe");
        
        // Initialize ChromeDriver
        WebDriver driver = new ChromeDriver();
        driver.manage().window().maximize(); // Maximize the browser window

        try {
            // Navigate to the Aadhaar download page
        	 driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
            driver.get("https://myaadhaar.uidai.gov.in/genricDownloadAadhaar");
            
            // Enter OTP
            WebElement otpInput = driver.findElement(By.xpath("//input[@name='otp']"));
            otpInput.sendKeys(otp);
            
            // Click on Verify & Download button
            WebElement verifyDownloadButton = driver.findElement(By.xpath("//button[contains(text(),'Verify & Download')]"));
            verifyDownloadButton.click();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
