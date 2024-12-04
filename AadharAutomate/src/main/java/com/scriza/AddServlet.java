package com.scriza;

import java.io.IOException;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

//@WebServlet(value = "/add", asyncSupported = true) // Enable async support
public class AddServlet extends HttpServlet {
    private static final Random random = new Random(); // Random generator

    @Override
    public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        String aadhar = req.getParameter("ad");
        if (aadhar == null || aadhar.length() != 12) {
            // If not, send an error response
            sendErrorResponse(res, "Invalid Aadhar number. It must be 12 characters long.");
            return; // Stop further processing
        }
        String orderId = generateUniqueOrderId();

        // Create AsyncContext to handle background processing
        AsyncContext asyncContext = req.startAsync();

        // Initial response before starting background processing
        sendInitialResponse(res, orderId); 

        // Asynchronous processing with CompletableFuture
        CompletableFuture.runAsync(() -> {
            try {
                processAadharTasks(asyncContext, aadhar, orderId , req, res); // Background tasks
            } catch (Exception e) {
                e.printStackTrace(); // Handle exceptions
            } finally {
                asyncContext.complete(); // Complete the async context
            }
        });
    }
    private void sendErrorResponse(HttpServletResponse res, String errorMessage) throws IOException {
        PrintWriter out = res.getWriter();
        out.print("{\n  \"error\": \"" + errorMessage + "\"\n}");
        out.flush(); // Ensure response is sent
        out.close(); // Complete the response
    }
    private void sendInitialResponse(HttpServletResponse res, String orderId) throws IOException {
        PrintWriter out = res.getWriter();
        String responseText = String.format(
            "{\n  \"order_id\": \"%s\",\n  \"link\": \"http://103.101.59.60/API/OTPInputServlet?order_id=%s\"\n}",
            orderId, orderId
        );
        out.print(responseText);
        out.flush(); // Ensure response is sent
        out.close(); // Complete the response
    }

    private void processAadharTasks(AsyncContext asyncContext, String aadhar, String orderId, HttpServletRequest req,HttpServletResponse res) {
        try {
            // Database operations
            Connection connection = DBConnectionManager.getConnection();

            String sql = "SELECT COUNT(1) FROM otp.aadhar_data WHERE aadhar_number = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, aadhar);
            ResultSet rs = preparedStatement.executeQuery();
            rs.first();

            int count = rs.getInt(1);

            // Insert or update depending on the existing record
            if (count == 0) {
                sql = "INSERT INTO otp.aadhar_data (aadhar_number, otp, order_id) VALUES (?, ?, ?)";
                PreparedStatement insertStatement = connection.prepareStatement(sql);
                insertStatement.setString(1, aadhar);
                insertStatement.setNull(2, java.sql.Types.VARCHAR); // Null OTP
                insertStatement.setString(3, orderId);
                insertStatement.executeUpdate();
            } else {
                sql = "UPDATE otp.aadhar_data SET otp = ?, order_id = ? WHERE aadhar_number = ?";
                PreparedStatement updateStatement = connection.prepareStatement(sql);
                updateStatement.setNull(1, java.sql.Types.VARCHAR); // Null OTP
                updateStatement.setString(2, orderId);
                updateStatement.setString(3, aadhar);
                updateStatement.executeUpdate();
            }

            ChromeOptions options = new ChromeOptions();
            options.setBinary("C:/Users/H3RI3R/Downloads/chrome-win/chrome-win/chrome.exe");
//            options.addArguments( "--disable-gpu"); 
//            options.setAcceptInsecureCerts(true);
//            options.addArguments(
//                    "--blink-settings=imagesEnabled=false",
//                    "--renderer",
//                    "--no-sandbox",
//                    "--no-service-autorun",
//                    "--no-experiments",
//                    "--no-default-browser-check",
//                    "--disable-webgl",
//                    "--disable-threaded-animation",
//                    "--disable-threaded-scrolling",
//                    "--disable-in-process-stack-traces",
//                    "--disable-histogram-customizer",
//                    "--disable-gl-extensions",
//                    "--disable-extensions",
//                    "--disable-composited-antialiasing",
//                    "--disable-canvas-aa",
//                    "--disable-3d-apis",
//                    "--disable-accelerated-2d-canvas",
//                    "--disable-accelerated-jpeg-decoding",
//                    "--disable-accelerated-mjpeg-decode",
//                    "--disable-app-list-dismiss-on-blur",
//                    "--disable-accelerated-video-decode",
//                    "--num-raster-threads=1",
//                    "--window-size=375,812",
//                    "--headless=new"// Set window size
//                );
            		// Necessary configurations
            WebDriver webDriver = new ChromeDriver(options);

            // Store in WebDriverManager
            req.setAttribute("webdriver"+aadhar, webDriver);
            ProcessAadhar processAadhar = new ProcessAadhar();
            processAadhar.getCaptcha(aadhar, req); // Continue Selenium tasks
        } catch (SQLException e) {
            e.printStackTrace(); // Handle database exceptions
        } catch (Exception e) {
            e.printStackTrace(); // Handle other exceptions
        }
    }

    private String generateUniqueOrderId() {
        return String.valueOf(random.nextInt(9000) + 1000); // Generate unique 4-digit order ID
    }
}