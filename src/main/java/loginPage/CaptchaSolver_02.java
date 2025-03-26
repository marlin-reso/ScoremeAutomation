package loginPage;
import org.json.JSONObject;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v85.network.Network;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Optional;


public class CaptchaSolver_02 {
	

	    public static void main(String[] args) throws IOException, InterruptedException {
	        // Setup Chrome options
	        ChromeOptions options = new ChromeOptions();
	        options.addArguments("--remote-debugging-port=9222"); 
	        WebDriver driver = new ChromeDriver(options);
	        driver.manage().window().maximize(); 

	        // Retry logic for captcha validation and login until success
	        while (true) {
	            String captchaData = validateCaptcha(driver);
	            boolean loginSuccess = performLogin(driver, captchaData);

	            if (loginSuccess) {
	                System.out.println("Successfully logged in!");
	                System.out.println("Home Page Title: " + driver.getTitle());
	                System.out.println("Home Page URL: " + driver.getCurrentUrl());
	                break;
	            } else {
	                System.out.println("Login failed, retrying...");
	                driver.navigate().refresh();
	                Thread.sleep(5000); // Wait before retrying
	            }
	        }
	    }

	    // Method to capture CAPTCHA data from the browser's network requests
	    public static String captureCaptchaData(WebDriver driver) throws InterruptedException {
	        DevTools devTools = ((ChromeDriver) driver).getDevTools();
	        devTools.createSession();
	        devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));

	        final String[] captchaData = {""};

	        devTools.addListener(Network.requestWillBeSent(), request -> {
	            String url = request.getRequest().getUrl();
	            String method = request.getRequest().getMethod();

	            if (method.equals("GET") && url.startsWith("data:image/jpeg;base64,")) {
	                captchaData[0] = url.replaceFirst("data:image/jpeg;base64,", "").trim();
	            }
	        });

	        driver.get("https://sm-quality.scoreme.in/#/start/sign-in");
	        Thread.sleep(5000);
	        return captchaData[0];
	    }

	    // Method to read and decode CAPTCHA
	    public static String readCaptcha(WebDriver driver) throws IOException, InterruptedException {
	        String captchaData = captureCaptchaData(driver);

	        // Decode Base64 CAPTCHA image
	        byte[] imageBytes = Base64.getDecoder().decode(captchaData);
	        String outputFilePath = "output_image.png";

	        try (FileOutputStream imageFile = new FileOutputStream(outputFilePath)) {
	            imageFile.write(imageBytes);
	            System.out.println("Captcha image saved to: " + outputFilePath);
	        }

	        // Send captcha image for solving
	        String solvedCaptcha = null;
	        try {
	            HttpResponse<String> response = Unirest.post("http://3.109.228.234:5000/mca/captcha")
	                    .field("file", new File(outputFilePath))
	                    .field("payload", "{\"type\":\"LEGAL\",\"referenceId\":\"LEGAL\"}")
	                    .asString();

	            JSONObject jsonResponse = new JSONObject(response.getBody());
	            solvedCaptcha = jsonResponse.getString("data");
	        } catch (Exception e) {
	            e.printStackTrace();
	        }

	        System.out.println("Decoded CAPTCHA: " + solvedCaptcha);
	        return solvedCaptcha;
	    }

	    // Method to validate the CAPTCHA (Retry until a valid 7-character captcha is obtained)
	    public static String validateCaptcha(WebDriver driver) throws IOException, InterruptedException {
	        String captchaData;
	        while (true) {
	            captchaData = readCaptcha(driver);
	            if (captchaData != null && captchaData.length() == 7) {
	                System.out.println("Valid CAPTCHA found: " + captchaData);
	                break;
	            } else {
	                System.out.println("Invalid CAPTCHA: " + captchaData);
	                driver.navigate().refresh();
	                Thread.sleep(5000);
	            }
	        }
	        return captchaData;
	    }

	    // Perform login and validate response
	    public static boolean performLogin(WebDriver driver, String captchaData) {
	        try {
	            driver.findElement(By.xpath("//input[@formcontrolname='email']")).sendKeys("sanjay.giri@scoreme.in");
	            driver.findElement(By.id("password")).sendKeys("Sa10057029@");
	            driver.findElement(By.xpath("//input[@placeholder='Enter Captcha']")).sendKeys(captchaData);
	            driver.findElement(By.xpath("//button[@class='_btn _btn-primary w-100 mb-3']")).click();

	            // Check network response for login validation
	            return validateLogin(driver);
	        } catch (Exception e) {
	            System.out.println("Error during login: " + e.getMessage());
	            return false;
	        }
	    }

	    // Capture login response from network and validate login success
	    public static boolean validateLogin(WebDriver driver) {
	        DevTools devTools = ((ChromeDriver) driver).getDevTools();
	        devTools.createSession();

	        final String[] loginResponse = {""};

	        devTools.addListener(Network.responseReceived(), response -> {
	            if (response.getResponse().getUrl().contains("/login")) {
	                loginResponse[0] = response.getResponse().getStatusText();
	            }
	        });

	        // Wait a few seconds for network response to be captured
	        try {
	            Thread.sleep(5000);
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        }

	        System.out.println("Login Response: " + loginResponse[0]);
	        return loginResponse[0].equalsIgnoreCase("OK");
	    }
	}

	

	  

	
	  
	


	  
	



