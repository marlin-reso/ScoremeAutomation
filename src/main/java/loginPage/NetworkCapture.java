package loginPage;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v131.network.Network;
import org.openqa.selenium.devtools.v131.network.model.RequestId;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class NetworkCapture {

	
	public static void main(String[] args) {

		// Initialize WebDriver
		ChromeDriver driver = new ChromeDriver();
		driver.manage().window().maximize();

		// Open login page
		

		// Initialize DevTools and create a session
		DevTools devTools = ((ChromeDriver) driver).getDevTools();
		devTools.createSession();

		// Enable Network domain
		devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));

		// Variable to store CAPTCHA value
		AtomicReference<String> captchaBase64 = new AtomicReference<>("");

		// Listener to capture the CAPTCHA image data from network responses
		devTools.addListener(Network.responseReceived(), response -> {
			// Check if the response URL contains "captcha" (customize based on your app)
			 String url = response.getResponse().getUrl();
			    System.out.println("Intercepted URL: " + url);
		});
		
		driver.get("https://sm-quality.scoreme.in");

		// Add a wait to capture the CAPTCHA network request
		try {
			Thread.sleep(5000); // Wait for network requests to load
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Check if CAPTCHA data is captured
//		if (captchaBase64.get().isEmpty()) {
//			System.err.println("Failed to capture CAPTCHA data from network.");
//			driver.quit();
//			return;
//		}

		// Log the CAPTCHA data for verification
		System.out.println("Captured CAPTCHA Data (Base64): " + captchaBase64.get());

		// Perform login
		try {
			// Enter email
			WebElement emailField = driver.findElement(By.xpath("//input[@formcontrolname='email']"));
			emailField.clear();
			emailField.sendKeys("sanjay.giri@scoreme.in");

			// Enter password
			WebElement passwordField = driver.findElement(By.id("password"));
			passwordField.clear();
			passwordField.sendKeys("Sa10057029@");

			// Enter the solved CAPTCHA (use captchaBase64 for decoding/solving logic if needed)
			WebElement captchaField = driver.findElement(By.xpath("//input[@placeholder='Enter Captcha']"));
			captchaField.clear();
			captchaField.sendKeys(solveCaptcha(captchaBase64.get())); // Solve and send CAPTCHA

			// Click the login button
			WebElement loginButton = driver.findElement(By.xpath("//button[@class='_btn _btn-primary w-100 mb-3']"));
			loginButton.click();

			// Add post-login actions or verifications as needed
			Thread.sleep(5000); // Wait to verify login
			System.out.println("Login operation performed.");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// Close the browser
			driver.quit();
		}
	}

	// Method to simulate solving the CAPTCHA (you can replace this with an actual API call to solve it)
	private static String solveCaptcha(String base64Captcha) {
		// For now, return a placeholder or call an external service to decode the CAPTCHA
		// Example: Send `base64Captcha` to an API to decode the text
		System.out.println("Solve the CAPTCHA here using an external service or manual approach.");
		return "solvedCaptchaText";






	}
}
