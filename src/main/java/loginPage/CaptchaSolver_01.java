package loginPage;
import org.json.JSONObject;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v85.network.Network;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Optional;

public class CaptchaSolver_01 {

	public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {


		boolean loginSuccess = false;

		while (!loginSuccess) {
			loginSuccess = loginAction(); // Retry login until successful
			Thread.sleep(5000);
			if (!loginSuccess) {
				Thread.sleep(5000);

				System.out.println("❌ Login failed. Retrying...");
				Thread.sleep(5000); // Wait before retrying
			}
		}
	}


	public static boolean loginAction() {

		try {
			// Setup Chrome options
			//	ChromeOptions options = new ChromeOptions();
			//	options.addArguments("--remote-debugging-port=9222"); 
			WebDriver driver = new ChromeDriver();
			driver.manage().window().maximize(); 

			//	String  capturedImageData =  captureCaptchaData(driver);

			String captchaData = "";
			// Call the validateCaptcha method and store its return value
			captchaData = validateCaptcha(driver, captchaData);
			// Print the returned CAPTCHA data
			System.out.println("Captured CAPTCHA Data: " + captchaData);


			// Perform login

			WebElement emailField = driver.findElement(By.xpath("//input[@formcontrolname='email']"));
			emailField.clear();
			emailField.sendKeys("Enter User E-mail");

			WebElement passwordField = driver.findElement(By.id("password"));
			passwordField.clear();
			passwordField.sendKeys("Enter User password");

			WebElement captchaField = driver.findElement(By.xpath("//input[@placeholder='Enter Captcha']"));
			captchaField.clear();

			captchaField.sendKeys(captchaData);

			WebElement loginButton = driver.findElement(By.xpath("//button[@class='_btn _btn-primary w-100 mb-3']"));
			loginButton.click();


			String homepageUrl = driver.getCurrentUrl();

			System.out.println(homepageUrl);

			if(homepageUrl.contains("dashboard")) {
				return true;
			}else {
				driver.quit();
				return false;
			}



		} catch (Exception e) {
			e.printStackTrace();

			return false;

		}

	}

	/*
	 * Method to capture CAPTCHA data
	 */ 
	public static String captureCaptchaData(WebDriver driver) throws InterruptedException {
		// Get the DevTools interface for network monitoring
		DevTools devTools = ((ChromeDriver) driver).getDevTools();
		devTools.createSession();

		// Enable network tracking
		devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));

		// String to store the CAPTCHA data
		final String[] captchaData = {""};

		// Add listener for network requests
		devTools.addListener(Network.requestWillBeSent(), request -> {
			String url = request.getRequest().getUrl();
			String method = request.getRequest().getMethod();


			// Filter requests with the GET method and check if the URL starts with 'data:image/jpeg;base64,' (base64 CAPTCHA image)
			if (method.equals("GET") && url.startsWith("data:image/jpeg;base64,")) {
				// Remove the 'GET data:image/jpeg;base64,' part and store the rest in a variable
				String remainingUrl = url.replaceFirst("data:image/jpeg;base64,", "");

				// Remove leading spaces from the remaining URL
				String cleanedUrl = remainingUrl.trim();

				// Store the cleaned base64 string in the captchaData array
				captchaData[0] = cleanedUrl;
			}
		});
		driver.get("https://sm-quality.scoreme.in/#/start/sign-in");
		Thread.sleep(5000);
		return captchaData[0];
	}
	/*
	 * Method to read the captcha image
	 */

	public static String readCaptcha(WebDriver driver) throws FileNotFoundException, IOException, InterruptedException {
		// Call captureCaptchaData and print the result
		String captchaData = captureCaptchaData(driver);

		// Decode and save the captcha image
		byte[] imageBytes = Base64.getDecoder().decode(captchaData);
		Thread.sleep(5000);
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



		System.out.println("Captured CAPTCHA Data: " + solvedCaptcha);
		return solvedCaptcha;

	}

	public static String validateCaptcha(WebDriver driver, String captchaData) throws FileNotFoundException, IOException, InterruptedException {


		while(true) {
			Thread.sleep(5000);

			captchaData = readCaptcha(driver);
			if(captchaData.length() == 7) {
				System.out.println("Valid captcha found: " + captchaData);
				break;
			}else {
				System.out.println("Invalid captcha : " + captchaData);
				driver.quit();
			}

		}


		return captchaData;
	}



}




