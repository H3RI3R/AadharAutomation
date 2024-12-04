package com.scriza;

import jakarta.servlet.ServletException;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

/**
 * Servlet implementation class BatchAadharProcess
 */
public class BatchAadharProcess extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static List<String> aadhaarNumbers = new ArrayList<String>();

    /**
     * @see HttpServlet#HttpServlet()
     */
    public BatchAadharProcess() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		for(String str:request.getParameterValues("multiInput")) {
			aadhaarNumbers.add(str);
		}

	     ExecutorService executorService = Executors.newFixedThreadPool(aadhaarNumbers.size()); // Adjust the pool size based on your needs



		System.setProperty("webdriver.chrome.driver", "C:\\selenium WebDriver\\chromedriver-win64\\chromedriver.exe");
//        CustomDriver.webDriver = new ChromeDriver();
		for(String aadhar : aadhaarNumbers) {
			executorService.submit(()->{
				System.out.println("Started thread for aadharNumber : " + aadhar);
				 WebDriver driver =new ChromeDriver();
			        driver.manage().window().maximize();
			        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
			        driver.get("http://localhost:8080/Papa/add?ad=" + aadhar);
//			        driver.close();
			        });
			
		}
       
		executorService.shutdown();
		while(!executorService.isShutdown()) {
			try {
				executorService.awaitTermination(10, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
