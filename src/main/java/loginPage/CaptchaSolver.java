package loginPage;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v131.network.Network;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

public class CaptchaSolver {

	public static void main(String[] args) throws InterruptedException {

		WebDriver driver = new ChromeDriver();
		driver.manage().window().maximize();
		driver.get("https://sm-quality.scoreme.in");

		// Setup DevTools to capture network requests
		DevTools devTools = ((ChromeDriver) driver).getDevTools();
		devTools.createSession();

		// Enable network domain
		devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));

		// Variable to store captured captcha image in base64
		AtomicReference<String> capturedImageData = new AtomicReference<>("");
		// Listener to capture base64 captcha data from network requests
		
		
		
		
		
		
		
		
		boolean isLoggedIn = false; // Flag to check if login is successful

		// Loop until the user is logged in successfully
		while (!isLoggedIn) {
			try {
				// Wait for the captcha to be captured
				Thread.sleep(5000); // Wait for network requests to complete

				String captchaBase64 = capturedImageData.get();
				if (captchaBase64 == null || captchaBase64.isEmpty()) {
					System.err.println("Failed to capture captcha image.");
					continue; // Retry the loop if captcha is not captured
				}

				// Decode and save the captcha image
				byte[] imageBytes = Base64.getDecoder().decode(captchaBase64);
				String outputFilePath = "output_image.png";

				try (FileOutputStream imageFile = new FileOutputStream(outputFilePath)) {
					imageFile.write(imageBytes);
					System.out.println("Captcha image saved to: " + outputFilePath);
				}

				// Send captcha image for solving
				String solvedCaptcha;
				try {
					HttpResponse<String> response = Unirest.post("http://3.109.228.234:5000/mca/captcha")
							.field("file", new File(outputFilePath))
							.field("payload", "{\"type\":\"LEGAL\",\"referenceId\":\"LEGAL\"}")
							.asString();

					JSONObject jsonResponse = new JSONObject(response.getBody());
					solvedCaptcha = jsonResponse.getString("data");

					System.out.println("Solved Captcha: " + solvedCaptcha);
				} catch (Exception e) {
					e.printStackTrace();
					continue; // Retry the loop if captcha solving fails
				}

				// Perform login
				try {
					WebElement emailField = driver.findElement(By.xpath("//input[@formcontrolname='email']"));
					emailField.clear();
					emailField.sendKeys("sanjay.giri@scoreme.in");

					WebElement passwordField = driver.findElement(By.id("password"));
					passwordField.clear();
					passwordField.sendKeys("Sa10057029@");

					WebElement captchaField = driver.findElement(By.xpath("//input[@placeholder='Enter Captcha']"));
					captchaField.clear();
					captchaField.sendKeys(solvedCaptcha);

					WebElement loginButton = driver.findElement(By.xpath("//button[@class='_btn _btn-primary w-100 mb-3']"));
					loginButton.click();

					// Wait for toast message or URL change to confirm login
					Thread.sleep(6000);

					try {
						WebElement toast = driver.findElement(By.className("toast-message"));
						String toastText = toast.getText();
						System.out.println("Toast Message: " + toastText);

						// Check if login was successful based on toast text
						if (toastText.contains("Invalid captcha")) {
							System.out.println("Captcha was incorrect. Retrying...");
							continue; // Retry the loop for incorrect captcha
						} else if (toastText.contains("Success")) {
							isLoggedIn = true; // Break the loop if login is successful
						}
					} catch (Exception e) {
						System.out.println("No toast message found. Retrying...");
						continue;
					}

					// Perform post-login actions if needed
					if (isLoggedIn) {
						System.out.println("Login successful.");
						driver.findElement(By.xpath("//div[@class='d-inline-block header-options tooltip-width']//span")).click();
						driver.findElement(By.xpath("//button[@class='mat-focus-indicator mat-menu-item ng-tns-c156-4']")).click();
						Thread.sleep(10000);
					}
				} catch (Exception e) {
					e.printStackTrace();
					continue; // Retry the loop if login fails due to other errors
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// Quit the driver
		//   driver.quit();
	}



}





