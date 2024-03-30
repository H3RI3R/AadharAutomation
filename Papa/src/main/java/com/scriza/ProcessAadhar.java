package com.scriza;
import java.io.File;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
//import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.io.FileHandler;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class ProcessAadhar {
public void getCaptcha(String aadhar){
  System.setProperty("webdriver.chrome.driver", "C:\\selenium WebDriver\\chromedriver-win64\\chromedriver.exe");
  WebDriver driver = new ChromeDriver();
  driver.manage().window().maximize();
  try {
      driver.manage().window().maximize();
      driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
      driver.get("https://myaadhaar.uidai.gov.in/genricDownloadAadhaar");
      WebElement aadhaarInput = driver.findElement(By.name("uid"));
      aadhaarInput.sendKeys(aadhar);
     performTesseractOCR(driver);
     System.out.println("Sending OTP");
//     driver.manage().window().setPosition(new Point(-2000, 0)); // Move the window off-screen
//      WebElement generateOTPButton = driver.findElement(By.className("button_btn__HeAxz"));
//     res.sendRedirect("./otpinput.html");
  } catch (Exception e) {
      System.out.println("Exception caught :" + e.getMessage());
  }
}

private static void performTesseractOCR(WebDriver driver) {
  try {
      WebElement imageelement = driver.findElement(By.className("auth-form__captcha-image"));
      File src = imageelement.getScreenshotAs(OutputType.FILE);
      String path = "C:\\Users\\H3RI3R\\git\\AadharAutomation\\Papa\\captchaImages\\captchaImage.png";
      FileHandler.copy(src, new File(path));
      Thread.sleep(3000);
      ITesseract image = new Tesseract();
      String str = image.doOCR(new File(path));
      System.out.println("Image OCR done");
      System.out.println(str);
       str = str.replaceAll("[^a-zA-Z0-9]", "");
      System.out.println(str);
      // Check if the captcha text is less than 6 characters
      if (str.length() < 6) {
          // Click on notFoundOTP button
          WebElement resetCaptcha = driver.findElement(By.xpath("//div[@class='auth-form__captcha']"));
          resetCaptcha.click();
          WebElement captchaInput = driver.findElement(By.name("captcha"));// Clear captcha input field
          captchaInput.clear();
          performTesseractOCR(driver);// Perform Tesseract OCR again
      } 
          // Fill captcha input field if captcha text is valid
      else if(str.length()>6){
      	 WebElement resetCaptcha = driver.findElement(By.xpath("//div[@class='auth-form__captcha']"));
           resetCaptcha.click();
           WebElement captchaInput = driver.findElement(By.name("captcha"));// Clear captcha input field
           captchaInput.clear();
           performTesseractOCR(driver); // Perform Tesseract OCR again
      
      }
      else if(str.length()==6){ 
      	WebElement captchaInput = driver.findElement(By.name("captcha"));
      	System.out.println("fuwfnn");
          captchaInput.sendKeys(str);
          Thread.sleep(2000);
          // Click on Send OTP button
          WebElement sendOTPButton = driver.findElement(By.xpath("//button[@class = 'button_btn__HeAxz']")); // Assuming this is the Send OTP button
          sendOTPButton.click();
          Thread.sleep(2000);
          WebElement alertElement = driver.findElement(By.xpath("//div[@role='alert' and contains(@class, 'Toastify__toast-body')]"));
          if (alertElement.getText().equals("Invalid Captcha")) {
              System.out.println("Invalid captcha. Retrying...");
              WebElement alertClose = driver.findElement(By.xpath("//button[@class='Toastify__close-button Toastify__close-button--error']"));
              alertClose.click();
              captchaInput.clear();
              performTesseractOCR(driver);
          }
         }
      } catch (Exception e) {
      System.out.println("Exception caught during Tesseract OCR: " + e.getMessage());
  }
}

}
