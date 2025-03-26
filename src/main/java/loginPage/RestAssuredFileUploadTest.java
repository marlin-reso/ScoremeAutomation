package loginPage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.json.JSONObject;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v131.network.Network;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

public class RestAssuredFileUploadTest {
    public static void main(String[] args) {
       
        WebDriver driver = new ChromeDriver();

        // Attach DevTools
        DevTools devTools = ((ChromeDriver) driver).getDevTools();
        devTools.createSession();

        // Enable network domain
        devTools.send(Network.enable(Optional.of(100000), Optional.of(100000), Optional.of(100000)));
        AtomicReference<String> capturedImageData = new AtomicReference<>("");
        // Listen for 'requestWillBeSent' event to capture network requests
        devTools.addListener(Network.requestWillBeSent(), request -> {
            String url = request.getRequest().getUrl();
            String method = request.getRequest().getMethod();
            
            // Filter requests with the GET method and check if the URL starts with 'data:image/jpeg;base64,'
            if (method.equals("GET") && url.startsWith("data:image/jpeg;base64,")) {
                // Remove the 'GET data:image/jpeg;base64,' part and store the rest in a variable
                String remainingUrl = url.replaceFirst("data:image/jpeg;base64,", "");

                // Remove leading spaces from the remaining URL
                String cleanedUrl = remainingUrl.trim();

              //  System.out.println("Captured Image Request: " + cleanedUrl);

                // Store the cleaned URL (without leading spaces) in a variable for further processing if needed
                capturedImageData.getAndSet(cleanedUrl);

                // You can now use 'capturedImageData' as needed, for example, logging, saving, etc.
            }
        });

        // Navigate to the webpage
        driver.get("https://sm-quality.scoreme.in/#/start/sign-in");

        // Perform actions like clicking or scrolling on the webpage to trigger network requests

        // Allow some time for requests to be captured
        try {
            Thread.sleep(5000);  // Sleep for 5 seconds to capture network requests
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("check ="+capturedImageData.get());
        String captcha =capturedImageData.get();
    	byte[] imageBytes = Base64.getDecoder().decode(captcha);
		// Specify the output file path
		String outputFilePath = "output_image.png"; // Change file extension for different formats

		try (FileOutputStream imageFile = new FileOutputStream(outputFilePath)) {
			// Write the byte array to the file
			imageFile.write(imageBytes);
			System.out.println("Image saved to " + outputFilePath);
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			HttpResponse<String> response = Unirest.post("http://3.109.228.234:5000/mca/captcha")
					.field("file", new File("output_image.png"))
					.field("payload", "{\"type\":\"LEGAL\",\"referenceId\":\"LEGAL\"}")
					.asString();

			System.out.println(response.getBody());
			 String responseBody = response.getBody();
		        
		        // Parse the response body into a JSONObject
		        JSONObject jsonResponse = new JSONObject(responseBody);

		        // Extract individual values from the JSON response
		        String data = jsonResponse.getString("data");  // assuming 'captcha' key in JSON
		    
		        System.out.println("Data: " + data);
		    
	
			
		}catch (Exception e) {
			e.printStackTrace();
		}


        
       
    }
}