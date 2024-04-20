package com.scriza;

import java.io.IOException;
//import org.jsoup:jsoup:1.17.2;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;


import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class OTPInputServlet extends HttpServlet {
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Get the OTP from the request parameter
        String otp = request.getParameter("otp");
 
        
        WebDriver driver = CustomDriver.webDriver;
//        Set<String> windowsOpened = driver.getWindowHandles();
        driver.findElement(By.name("otp")).sendKeys(otp);
        try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {

			e.printStackTrace();
		}
        
        driver.findElement(By.className("button_btn__A84dV")).click();
//        Alert alert = driver.switchTo().alert();	
//        String alert = driver.switchTo().alert().getText();
//        if(alert.contains("Do you want to clear the session and proceed? "))
        if(!driver.findElements(By.className("aadhaar-front")).isEmpty()) {
            // If Aadhaar front element is present, indicating successful login

            // Extract data from the website
        	WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        	WebElement profileElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='profile']")));
        	 profileElement.click();
        	 WebElement nameElement =  wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='name-local']")));
        	 JSONObject jsonData = scrapeDataFromWebsite(driver);

             // Further processing of the scraped data (if needed)
        	 if (jsonData != null) {
                 try {
                     // Set response type to JSON
                     response.setContentType("application/json");
                     response.setCharacterEncoding("UTF-8");

                     // Send JSON response
                     response.getWriter().write(jsonData.toString());
                 } catch (JSONException e) {
                     // Handle JSONException
                     e.printStackTrace();
                 }
             } else {
                 // Handle case where scraping failed
                 response.sendRedirect("index.html");
             }

            // Set response type to JSON
//            response.setContentType("application/json");
//            response.setCharacterEncoding("UTF-8");

            // Send JSON response
//            response.getWriter().write(jsonData.toString());
        } else {
            // Redirect to index.html if login fails
            response.sendRedirect("index.html");
        }
    }

    private JSONObject scrapeDataFromWebsite(WebDriver driver) {
    	 JSONObject jsonData = new JSONObject();

    	    try {
    	        // Get the HTML content of the page
    	        String htmlContent = driver.getPageSource();
    	        Document doc = Jsoup.parse(htmlContent);

    	        // Extract desired elements using JSoup selectors
    	        Element nameElement = doc.selectFirst("div.name-local");
    	        String name = nameElement.text();
    	        jsonData.put("Name", name);

    	        Element aadharContentElement = doc.selectFirst("div.aadhaar-front__aadhaar-content");
    	        String aadharContent = aadharContentElement.text();
    	        jsonData.put("AadharContent", aadharContent);

    	        Element ageElement = doc.selectFirst("div.name-english");
    	        String age = ageElement.text();
    	        jsonData.put("Age", age);

    	        Element dobElement = doc.selectFirst(".dob");
    	        String dob = dobElement.text();
    	        jsonData.put("DateOfBirth", dob);

    	        Element genderElement = doc.selectFirst(".gender");
    	        String gender = genderElement.text();
    	        jsonData.put("Gender", gender);

    	        Element aadharNumElement = doc.selectFirst(".aadhaar-front__aadhaar-number");
    	        String aadharNum = aadharNumElement.text();
    	        jsonData.put("AadharNumber", aadharNum);

    	        Element aadharAddressElement = doc.selectFirst(".aadhaar-back__address-english");
    	        String aadharAddress = aadharAddressElement.text();
    	        jsonData.put("AadharAddress", aadharAddress);

    	        // Convert JSONObject to string
//    	        String jsonResult = jsonData.toString();

    	        // Print JSON data
//    	        System.out.println(jsonResult);

    	        return jsonData;
    	    } catch (Exception e) {
    	        System.out.println("Error scraping data from website: " + e.getMessage());
    	        return null;
    	    }
//    	WebElement nameElement = driver.findElement(By.xpath("//div[@class='name-local']"));
//        String name = nameElement.getText();
//        WebElement aadharContentElement= driver.findElement(By.xpath("//div[@class='aadhaar-front__aadhaar-content']"));
//        String AddharContent = aadharContentElement.getText();
//        WebElement ageElement = driver.findElement(By.xpath("//div[@class= 'name-english']"));
//        String age = ageElement.getText();
//
//        WebElement dobElement = driver.findElement(By.className("dob"));
//        String dob = dobElement.getText();
//
//        WebElement genderElement = driver.findElement(By.className("gender"));
//        String gender = genderElement.getText();
//
//        WebElement aadharNumElement = driver.findElement(By.className("aadhaar-front__aadhaar-number"));
//        String aadharNum = aadharNumElement.getText();
//
//        WebElement aadharAddressElement = driver.findElement(By.className("aadhaar-back__address-english"));
//        String aadharAddress = aadharAddressElement.getText();
//
//        // Print scraped data to console
//        System.out.println("Name: " + name);
//        System.out.println("Age: " + age);
//        System.out.println("Date of Birth: " + dob);
//        System.out.println("Gender: " + gender);
//        System.out.println("Aadhar Number: " + aadharNum);
//        System.out.println("Aadhar Address: " + aadharAddress);
//        System.out.println("ALl contents in aadhar  "+ AddharContent);
    }

    private String convertToJson(List<String> data) {
        JSONObject jsonObject = new JSONObject();
        for (int i = 0; i < data.size(); i++) {
            jsonObject.put("data" + i, data.get(i));
        }
        return jsonObject.toString();
    }

    private String getAadharWithNullOTP() {
        String aadharNumber = null;
        try {
            Connection con = DBConnectionManager.getConnection();
            String sql = "SELECT aadhar_number FROM aadhar_data WHERE otp IS NULL";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                aadharNumber = rs.getString("aadhar_number");
            }
            rs.close();
            ps.close();
            con.close();
        } catch (SQLException e) {
            System.out.println("Error retrieving Aadhar number with null OTP: " + e.getMessage());
        }
        return aadharNumber;
    }
    public String retrieveOTPFromDatabase(String aadharNumber) throws SQLException, InterruptedException {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String retrievedOTP = null;

        try {
            // Establish a connection to the database
            con = DBConnectionManager.getConnection();
            String sql = "SELECT otp FROM aadhar_data WHERE aadhar_number = ? ORDER BY timestamp_column DESC LIMIT 1";
            ps = con.prepareStatement(sql);
            ps.setString(1, aadharNumber);
            
            // Continuously check if the OTP is not null
            while (retrievedOTP == null) {
                // Execute the query
                rs = ps.executeQuery();
                if (rs.next()) {
                    retrievedOTP = rs.getString("otp");
                } else {
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
    private void storeAadharAndOTPInDatabase(String aadharNumber, String otp) {
        try {
            // Establish a connection to the database
            Connection con = DBConnectionManager.getConnection();
            
            // Prepare SQL statement to update OTP for the retrieved Aadhar number
            String updateSql = "UPDATE aadhar_data SET otp = ? WHERE aadhar_number = ?";
            PreparedStatement updatePs = con.prepareStatement(updateSql);
                      
            // Set the new OTP and Aadhar number in the prepared statement
            updatePs.setString(1, otp);
            updatePs.setString(2, aadharNumber);
            
            // Execute the update SQL statement
            updatePs.executeUpdate();
            
            // Close the PreparedStatement and connection
            updatePs.close();
            con.close();
            
            System.out.println("Aadhar number and OTP stored in database successfully." +otp+ "\t"+aadharNumber);
        } catch (SQLException e) {
            System.out.println("Error storing Aadhar number and OTP in database: " + e.getMessage());
        }
    }

}

	/*
	 * // Get Aadhar number where OTP is null String aadharNumber =
	 * getAadharWithNullOTP();
	 * 
	 * // Store Aadhar number and OTP in the database
	 * storeAadharAndOTPInDatabase(aadharNumber, otp);
	 */
 
	/*
	 * try { String otps = retrieveOTPFromDatabase(aadharNumber);
	 * System.out.println("OTP retrived from Databse "+otps); } catch (SQLException
	 * | InterruptedException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); }
	 */

/*
 * String actualInvalidOTPError =
 * driver.findElement(By.className("login-section__error")).getText(); String
 * expectedInvalidOTPErrror = "Unable to Validate OTP! Please try again later";
 */
/*
* ProcessAadhar processAadhar = new ProcessAadhar(driver,aadharNumber,otp);
* processAadhar.processOTP(aadharNumber);
*/
//enterOTPAndVerify(otp);
// Redirect the user to another page or send a response
//if(expectedInvalidOTPErrror.equalsIgnoreCase(actualInvalidOTPError))