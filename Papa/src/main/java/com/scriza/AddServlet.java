package com.scriza;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AddServlet extends HttpServlet{
	public void service(HttpServletRequest req,HttpServletResponse res) throws ServletException, IOException {
		String aadhar = req.getParameter("ad");
		ProcessAadhar processAadhar = new ProcessAadhar();
	    processAadhar.getCaptcha(aadhar);
	    System.out.println(aadhar);
	    // Send a response back to the client
	    res.setContentType("text/html");
	    res.setCharacterEncoding("UTF-8");
	    PrintWriter out = res.getWriter();
	    out.write("Aadhar number received: " + aadhar);

	    RequestDispatcher dispatcher = req.getRequestDispatcher("/otpinput.html");

	    dispatcher.forward(req, res);
//	    String oneTime = req.getParameter("otp");
//	    ProcessOTP generateOTP = new ProcessOTP();
//	    generateOTP.generateOtp(oneTime);
//	    System.out.println(oneTime);
//	    res.setContentType("text/html");
//	    res.setCharacterEncoding("UTF-8");
//	    PrintWriter ot = res.getWriter();
//	    ot.write("OTP number received: " + oneTime);

	    
	}

}
