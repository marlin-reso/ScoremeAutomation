package loginPage;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v85.network.Network;
import org.openqa.selenium.devtools.v85.network.model.Response;
import java.util.Optional;
import org.openqa.selenium.chrome.ChromeOptions;
public class NetworkCaptureForGoogle {
	
	 public static void main(String[] args) {
	        // Set the path for your ChromeDriver
	       

	        // Initialize ChromeOptions
	     //   ChromeOptions options = new ChromeOptions();
	      //  options.setExperimentalOption("w3c", false);  // Enable CDP

	        // Create the Chrome WebDriver
	        WebDriver driver = new ChromeDriver();

	        // Get the DevTools interface for network monitoring
	        DevTools devTools = ((ChromeDriver) driver).getDevTools();
	        devTools.createSession();

	        // Enable network tracking
	        devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));

	        // Add a listener for network responses
	        devTools.addListener(Network.responseReceived(), response -> {
	            Response networkResponse = response.getResponse();
	            System.out.println("Response URL: " + networkResponse.getUrl());
	            System.out.println("Response Status: " + networkResponse.getStatus());
	          //  System.out.println("Response Body: " + networkResponse.getBody());
	        });

	        // Open a website (example)
	        driver.get("https://www.google.com");

	        // Perform actions or interact with the page as needed

	        // Close the driver
	        driver.quit();
	    }

}
