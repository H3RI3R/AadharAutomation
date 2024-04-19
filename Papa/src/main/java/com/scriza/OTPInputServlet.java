package com.scriza;

import java.io.IOException;
//import org.jsoup:jsoup:1.17.2;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        driver.findElement(By.className("button_btn__A84dV")).click();
//        Alert alert = driver.switchTo().alert();	
//        String alert = driver.switchTo().alert().getText();
//        if(alert.contains("Do you want to clear the session and proceed? "))
//        	
        if(!driver.findElements(By.className("aadhaar-front")).isEmpty()) {
            // If Aadhaar front element is present, indicating successful login

            // Extract data from the website
            String jsonData = scrapeDataFromWebsite(driver);

            // Set response type to JSON
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            // Send JSON response
            response.getWriter().write(jsonData);
        } else {
            // Redirect to index.html if login fails
            response.sendRedirect("index.html");
        }
    }

    private String scrapeDataFromWebsite(WebDriver driver) {
        // Load the webpage using JSoup
        Document doc = Jsoup.parse(driver.getPageSource());
        
        // Extract required elements using CSS selectors
        Element nameElement = doc.selectFirst(".name-local");
        String name = nameElement != null ? nameElement.text() : "";

        Element ageElement = doc.selectFirst(".name-english");
        String age = ageElement != null ? ageElement.text() : "";

        Element dobElement = doc.selectFirst(".dob");
        String dob = dobElement != null ? dobElement.text() : "";

        Element genderElement = doc.selectFirst(".gender");
        String gender = genderElement != null ? genderElement.text() : "";

        Element aadharNumElement = doc.selectFirst(".aadhaar-front__aadhaar-number");
        String aadharNum = aadharNumElement != null ? aadharNumElement.text() : "";

        Element aadharAddressElement = doc.selectFirst(".aadhaar-back__address-english");
        String aadharAddress = aadharAddressElement != null ? aadharAddressElement.text() : "";

        // Construct JSON object manually
        StringBuilder jsonData = new StringBuilder();
        jsonData.append("{");
        jsonData.append("\"name\": \"" + name + "\",");
        jsonData.append("\"age\": \"" + age + "\",");
        jsonData.append("\"dob\": \"" + dob + "\",");
        jsonData.append("\"gender\": \"" + gender + "\",");
        jsonData.append("\"aadharNumber\": \"" + aadharNum + "\",");
        jsonData.append("\"aadharAddress\": \"" + aadharAddress + "\"");
        jsonData.append("}");

        return jsonData.toString();
    }

    private String convertToJson(List<String> data) {
        // Convert the list of strings to JSON format
        JSONObject jsonObject = new JSONObject();
        for (int i = 0; i < data.size(); i++) {
            jsonObject.put("data" + i, data.get(i));
        }
        return jsonObject.toString();
    }

    private String getAadharWithNullOTP() {
        String aadharNumber = null;
        try {
            // Establish a connection to the database
            Connection con = DBConnectionManager.getConnection();
            
            // Prepare SQL statement to select Aadhar number where OTP is null
            String sql = "SELECT aadhar_number FROM aadhar_data WHERE otp IS NULL";
            PreparedStatement ps = con.prepareStatement(sql);

            // Execute the query
            ResultSet rs = ps.executeQuery();

            // Retrieve Aadhar number from the result set
            if (rs.next()) {
                aadharNumber = rs.getString("aadhar_number");
            }

            // Close the result set, prepared statement, and connection
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