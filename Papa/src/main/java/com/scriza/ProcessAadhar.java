package com.scriza;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Point;
//import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.io.FileHandler;
//import org.openqa.selenium.support.ui.ExpectedConditions;
//import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;


import org.openqa.selenium.support.ui.ExpectedConditions;


import jakarta.servlet.http.HttpServletResponse;
//import net.sourceforge.tess4j.ITesseract;
//import net.sourceforge.tess4j.Tesseract;
//import net.sourceforge.tess4j.TesseractException;

public class ProcessAadhar {
//    private WebDriver driver;
	   private WebDriver driver;
	    private Connection con;
    public ProcessAadhar(WebDriver driver) {
    	this.driver = driver;
        try {
            con = DBConnectionManager.getConnection();
        } catch (SQLException e) {
            System.out.println("Failed to establish database connection: " + e.getMessage());
        }
    }
//    public ProcessAadhar(WebDriver driver2) {
//		// TODO Auto-generated constructor stub
//	}
	public void processOTP(String aadharNumber) {
        try {
            // Retrieve OTP from the database
            String otp = retrieveOTPFromDatabase(aadharNumber);

            // Enter OTP and verify
            enterOTPAndVerify(otp);
        } catch (SQLException | InterruptedException e) {
            // Handle exceptions
            e.printStackTrace();
        }
    }

    public void getCaptcha(String aadhar, HttpServletResponse response) {
        // Assign the local driver to the class-level driver
        driver.manage().window().maximize();
        try {
            driver.manage().window().maximize();
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
            driver.get("https://myaadhaar.uidai.gov.in/");
            WebElement login = driver.findElement(By.xpath("//button[@class='button_btn__HeAxz']"));
            login.click();
            Thread.sleep(3000);
            WebElement aadhaarInput = driver.findElement(By.name("uid"));
            aadhaarInput.sendKeys(aadhar);
            System.out.println("Sending OTP");
            String captchaImageUrl = extractCaptchaImageUrl(driver);
            insertCaptchaUrlToDatabase(captchaImageUrl);
            String captchaTexxt = searchCaptchaImage();
            enterCaptcha(driver, captchaTexxt, response);
            
        } catch (Exception e) {
           e.printStackTrace();
        }
    }

    private void enterCaptcha(WebDriver driver, String captchaText, HttpServletResponse response) {
        int maxAttempts = 6; // Maximum number of attempts to enter captcha
        int attempts = 0; // Initialize attempts counter
        while (attempts < maxAttempts) {
            try {
                if (captchaText != null) {
                    WebElement captchaInput = driver.findElement(By.name("captcha"));
                    captchaInput.sendKeys(captchaText);
                    Thread.sleep(2000);
                    // Click on Send OTP button
                    WebElement sendOTPButton = driver.findElement(By.xpath("//button[@class='button_btn__A84dV']"));
                    sendOTPButton.click();
                    Thread.sleep(2000);

                    // Check if error message indicates captcha value doesn't match
                    WebElement errorMessage = driver.findElement(By.xpath("//div[@class='login-section__error']"));
                    if (errorMessage.getText().contains("Captcha value doesn't match")) {
                        // Click on refresh icon to refresh captcha
                        WebElement refreshIcon = driver.findElement(By.xpath("//img[@src='./static/media/RefreshIcon.874efff6da316d5687c409d5d2763bbe.svg']"));
                        refreshIcon.click();
                        Thread.sleep(2000);
                        String captchaImageUrl = extractCaptchaImageUrl(driver);
                        insertCaptchaUrlToDatabase(captchaImageUrl);
                        captchaText = searchCaptchaImage(); // Retrieve new captcha text
                        attempts++; // Increment attempts counter
                        continue; // Retry entering captcha with new text
                    }

                    return; // Exit method
                } else {
                    Thread.sleep(5000); // Wait for 5 seconds before checking again
                }
            } catch (Exception e) {
                System.out.println("Exception caught while entering captcha: " + e.getMessage());
            }
            attempts++; // Increment attempts counter
        }
    }

	private void insertCaptchaUrlToDatabase(String captchaImageUrl) throws SQLException {
		 String sql = "INSERT INTO captcha_data (captcha_url) VALUES (?)";
	        PreparedStatement ps = con.prepareStatement(sql);
	        ps.setString(1, captchaImageUrl);
	        ps.executeUpdate();
	        ps.close();
	}
	private String extractCaptchaImageUrl(WebDriver driver) {
        WebElement imageElement = driver.findElement(By.xpath("//img[@alt='captcha']"));
        return imageElement.getAttribute("src");
    }

public String retrieveFromDatabase() throws ClassNotFoundException,SQLException {
	 Connection con = DBConnectionManager.getConnection();

    String sql = "SELECT captcha_text FROM captcha_data ORDER BY id DESC LIMIT 1;"; // Assuming 'id' is a unique identifier column
    PreparedStatement ps = con.prepareStatement(sql);
    ResultSet rs = ps.executeQuery();

    String retrievedOTP = null;
    if (rs.next()) {
        retrievedOTP = rs.getString("otp");
    }

    rs.close();
    ps.close();
    con.close();

    return retrievedOTP;
}
private String retrieveCaptchaImageUrlFromDatabase() {
    try {
        String sql = "SELECT captcha_url FROM captcha_data ORDER BY id DESC LIMIT 1"; // Assuming 'id' is a unique identifier column
        PreparedStatement ps = con.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        String captchaImageUrl = null;
        if (rs.next()) {
            captchaImageUrl = rs.getString("captcha_url");
        }
        rs.close();
        ps.close();
        return captchaImageUrl;
    } catch (SQLException e) {
        System.out.println("Exception caught while retrieving CAPTCHA image URL from database: " + e.getMessage());
        return null;
    }
}
public String searchCaptchaImage() {
	System.setProperty("webdriver.chrome.driver", "C:\\selenium WebDriver\\chromedriver-win64\\chromedriver.exe");
    WebDriver driver = new ChromeDriver();
//    googleDriver.get("https://images.google.com/");
    driver.manage().window().maximize();
    
    try {
    	driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
    	driver.get("https://images.google.com/");

        // Click on the "Upload an image" button
        WebElement uploadButton = driver.findElement(By.xpath("//div[@class='nDcEnd']"));
        uploadButton.click();
        System.out.println("Pressed Image to uplaod");

        // Enter the captcha image URL
        WebElement searchInput = driver.findElement(By.xpath("//input[@class='cB9M7']"));
        String captchaImageUrl = retrieveCaptchaImageUrlFromDatabase();
        searchInput.sendKeys(captchaImageUrl);
        System.out.println("Entered the link ");

        // Click on the "Search" button
        WebElement searchButton = driver.findElement(By.xpath("//div[@class='Qwbd3']"));
        searchButton.click();
       
        // Click on the text button to get the captcha text
        WebElement textButton = driver.findElement(By.xpath("//span[@id='text']"));
        textButton.click();
        Thread.sleep(2000);
        WebElement selectAllText = driver.findElement(By.xpath("//span[contains(text(), 'Select all text')]"));
        selectAllText.click();

        // Wait for the captcha text to appear
        WebElement captchaHeaderText =driver.findElement(By.xpath("//h1[@class='wCgoWb']"));
        String captchaText = captchaHeaderText.getText();
        
        return captchaText;
    } catch (Exception e) {
        System.out.println("Exception caught while searching captcha image: " + e.getMessage());
        
    } 
    
    return null;
}


//public void enterOTPAndVerify( String otp) throws InterruptedException {
//    if (otp != null && !otp.isEmpty()) {
//        WebElement otpInput = driver.findElement(By.xpath("//input[@name='otp']"));
//        otpInput.sendKeys(otp);
//        Thread.sleep(2000);
//        
//        // Click on Verify & Download button
//        WebElement verifyDownloadButton = driver.findElement(By.xpath("//button[@class='button_btn__A84dV']"));
//        verifyDownloadButton.click();
//    } else {
//        System.out.println("OTP retrieved from the database is null or empty.");
//    }
//}
public String retrieveOTPFromDatabase(String aadharNumber) throws SQLException, InterruptedException {
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    String retrievedOTP = null;

    try {
        // Establish a connection to the database
        con = DBConnectionManager.getConnection();
        
        // Prepare SQL statement to select the OTP for the given Aadhar number
        String sql = "SELECT otp FROM aadhar_data WHERE aadhar_number = ? ORDER BY timestamp_column DESC LIMIT 1";
        ps = con.prepareStatement(sql);
        
        // Set the Aadhar number as a parameter in the prepared statement
        ps.setString(1, aadharNumber);
        
        // Continuously check if the OTP is not null
        while (retrievedOTP == null) {
            // Execute the query
            rs = ps.executeQuery();

            // Retrieve the OTP from the result set
            if (rs.next()) {
                retrievedOTP = rs.getString("otp");
            } else {
                // Sleep for a short duration before checking again
                Thread.sleep(1000); // Sleep for 1 second
            }

            // Close the result set
            if (rs != null) {
                rs.close();
            }
        }
    } catch (SQLException e) {
        System.out.println("Error retrieving OTP from database: " + e.getMessage());
    } finally {
        // Close the prepared statement and connection in a finally block to ensure they are closed even if an exception occurs
        if (ps != null) {
            ps.close();
        }
        if (con != null) {
            con.close();
        }
    }

    return retrievedOTP;
}


public void enterOTPAndVerify(String otp) throws InterruptedException {
	 if (otp != null && !otp.isEmpty()) {
	        // Find the OTP input field
		 WebElement otpInput = driver.findElement(By.xpath("//input[@name='otp']"));
	        
	        // Loop until the OTP field is filled
	        while (otpInput.getAttribute("value") == null || otpInput.getAttribute("value").isEmpty()) {
	            try {
	                Thread.sleep(1000); // Wait for 1 second before checking again
	            } catch (InterruptedException e) {
	                e.printStackTrace();
	            }
	        }
	        
	        // Once the OTP field is filled, enter the OTP
	        otpInput.sendKeys(otp);
	    } else {
	        System.out.println("OTP is null or empty.");
	    }
}
}
