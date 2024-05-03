package com.scriza;

import java.io.IOException;
import java.io.PrintWriter;
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
import org.openqa.selenium.By.ById;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;


import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import jakarta.servlet.annotation.MultipartConfig;
@MultipartConfig
public class OTPInputServlet extends HttpServlet {
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//    	 String otp = request.getParameter("otp");
    	 Part otpPart = request.getPart("otp");
    	 String aadhar =null;
 	    String orderId = request.getParameter("order_id");
 	   String otp = otpPart != null ? new String(otpPart.getInputStream().readAllBytes()) : null;
    	try {
    	    Connection connection = DBConnectionManager.getConnection();

    	        
    	        // Now update the `otp` field for the retrieved `aadhar_number`
    	        String updateOtpSql = "UPDATE aadhar_data SET otp = ? WHERE order_id = ?";
    	        PreparedStatement updatePreparedStatement = connection.prepareStatement(updateOtpSql);
    	        
    	        updatePreparedStatement.setString(1, otp);
    	        updatePreparedStatement.setString(2, orderId);
    	        String sql = "select * from aadhar_data where order_id=?";
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, orderId);
                ResultSet rs = preparedStatement.executeQuery();
                
                if(rs.absolute(1)) {
                	aadhar=rs.getString(1);
//                	System.out.printf("Aadhar has been captctured and now we will get the WebDriver using this Aadhar",aadhar );
                }
    	        // Execute the update
    	        updatePreparedStatement.executeUpdate();
    	        
    	        // Close resources
    	        updatePreparedStatement.close();
   
    	    connection.close();
    	    System.out.println("Aadhar has been captctured and now we will get the WebDriver using this Aadhar"+aadhar );
    	} catch (Exception e) {
    	    e.printStackTrace();
    	    System.out.println("Aadhar_data hasnot been updated and otp is not been stored");// Handle the exception
    	}
        WebDriver driver = CustomDriver.getWebDriver(aadhar);
//        Set<String> windowsOpened = driver.getWindowHandles();
        driver.findElement(By.name("otp")).sendKeys(otp);
        try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {

			e.printStackTrace();
		}
        
        driver.findElement(By.className("button_btn__A84dV")).click();
        try {
            WebElement errorElement = driver.findElement(By.className("login-section__error"));
            if (errorElement.isDisplayed() && errorElement.getText().contains("Invalid OTP")) {
                // If an error message is displayed indicating invalid OTP, return a custom response
                sendErrorResponse(response, "Invalid OTP. Please enter again.");
                return; // Stop further processing
            }
        } catch (NoSuchElementException e) {
            // If no error message is found, the OTP is valid
        }

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
//                     response.getWriter().write(jsonData.toString());
                     try {
                         Connection connection = DBConnectionManager.getConnection();
                         
                         // Prepare SQL insert statement
                         String sql = "INSERT INTO user_json_data (order_id, user_data_in_json) VALUES (?, ?)";
                         PreparedStatement preparedStatement = connection.prepareStatement(sql);
                         
                         preparedStatement.setString(1, orderId);
                         preparedStatement.setString(2, jsonData.toString()); // Convert JSONObject to string
                         
                         // Execute the insert statement
                         preparedStatement.executeUpdate();
                         
                         // Close the statement and connection
                         preparedStatement.close();
                         connection.close();
                         System.out.println("The database has been updated and stored all the data inside the database");
                         StringBuilder jsonResponse = new StringBuilder();
//                         JsonResponse.put("data", "successful");
                         jsonResponse.append("{\n");
                         jsonResponse.append("\"data\": Successful").append(",\n");
                         jsonResponse.append("\"link\": \"http://103.101.59.60/API/checkStatus?order_id=").append(orderId).append("\"\n");
                         jsonResponse.append("}");
                         PrintWriter out = response.getWriter();
                         out.print(jsonResponse.toString());
                         driver.findElement(By.className("header__log-out-button")).click();
                         
                         
                     } catch (Exception e) {
                         e.printStackTrace(); // Handle exceptions appropriately
                     }
                 } catch (JSONException e) {
                     // Handle JSONException
                     e.printStackTrace();
                 }
             } else {
            	 sendErrorResponse(response, "Invalid OTP. Please enter again.");
             }

        } else { 
        	sendErrorResponse(response, "Invalid OTP. Please enter again.");
        }
   
    }
    private void sendErrorResponse(HttpServletResponse res, String errorMessage) throws IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        PrintWriter out = res.getWriter();
        out.print("{\n  \"error\": \"" + errorMessage + "\"\n}");
        out.flush(); // Ensure the error response is sent
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


    	        return jsonData;
    	    } catch (Exception e) {
    	        System.out.println("Error scraping data from website: " + e.getMessage());
    	        return null;
    	    }

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

