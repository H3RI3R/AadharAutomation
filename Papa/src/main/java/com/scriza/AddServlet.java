package com.scriza;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
public class AddServlet extends HttpServlet {

    public void init() throws ServletException {
        // Initialize the WebDriver here
        System.setProperty("webdriver.chrome.driver", "C:\\selenium WebDriver\\chromedriver-win64\\chromedriver.exe");
        CustomDriver.webDriver = new ChromeDriver();
    }

    public void destroy() {
        // Quit the WebDriver and perform cleanup here
        if (CustomDriver.webDriver != null) {
        	CustomDriver.webDriver.quit();
        }
    }

    public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        // Get Aadhar number from request parameter
        String aadhar = req.getParameter("ad");

        // Insert Aadhar number into the database
        // (Assuming DBConnectionManager and other methods are properly defined)
        try {
        	Connection connection = DBConnectionManager.getConnection();
            String sql = "select count(1) from aadhar_data where aadhar_number=?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, aadhar);
            ResultSet rs = preparedStatement.executeQuery();
            int count =0;
            while(rs.next()) {
            	count++;
            }
            if(count<=0){
                 sql = "INSERT INTO aadhar_data (aadhar_number, otp) VALUES (?, ?)";
                 preparedStatement = connection.prepareStatement(sql);
                 preparedStatement.setString(1, aadhar);
                 preparedStatement.setString(2, null);
            }
            else {
                sql = "UPDATE aadhar_data set otp=? where aadhar_number=?";
                preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, null);
                preparedStatement.setString(2, aadhar);
            }
            int rowsInserted = preparedStatement.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Aadhar number inserted/updated successfully");
            } else {
                System.out.println("Failed to insert Aadhar number into the database");
                // You can handle the failure scenario here
            }
        } catch (SQLException e) {
            System.out.println("Failed to establish database connection: " + e.getMessage());
            // You can handle the database connection failure here
        }

        // Create an instance of ProcessAadhar and pass the WebDriver instance to it
        ProcessAadhar processAadhar = new ProcessAadhar(CustomDriver.webDriver);

        // Get the WebDriver object after capturing the captcha
       processAadhar.getCaptcha(aadhar, res);
        System.out.println(aadhar);

        // Forward the request to otpinput.html
        RequestDispatcher dispatcher = req.getRequestDispatcher("otpinput.html");
        dispatcher.forward(req, res);

        // Send a response back to the client
        res.setContentType("text/html");
        res.setCharacterEncoding("UTF-8");
        PrintWriter out = res.getWriter();
        out.write("Aadhar number received: " + aadhar);
    }
}
