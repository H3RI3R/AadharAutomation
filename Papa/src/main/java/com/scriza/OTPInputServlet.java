package com.scriza;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class OTPInputServlet extends HttpServlet {
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Get the OTP from the form
        String otps = request.getParameter("ot");
        
        // Process the OTP (You can redirect it to another servlet like ProcessOTP)
        ProcessOTP processOTP = new ProcessOTP();
        processOTP.process(otps);
        System.out.println(otps);
        response.setContentType("text/html");
	    response.setCharacterEncoding("UTF-8");
	    PrintWriter out = response.getWriter();
	    out.write("Aadhar otp received: " + otps);
        // Minimize the browser window using JavaScript
//        PrintWriter out = response.getWriter();
//        out.println("<script>window.blur();</script>");
        
        // Display a message to the user
//        response.setContentType("text/html");
        out.println("<html><body><h2>Please wait while we process your OTP...</h2></body></html>");
    }
}
