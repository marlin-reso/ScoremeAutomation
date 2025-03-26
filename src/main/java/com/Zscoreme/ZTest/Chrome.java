package com.Zscoreme.ZTest;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class Chrome {

	public static void main(String[] args) {
		
		System.setProperty("webdriver.chrome.driver", "C:\\Users\\SSPL-LT-SANJAY\\Documents\\Tools\\chromedriver-win64\\chromedriver.exe");

		WebDriver driver = new ChromeDriver();
		
		// TODO Auto-generated method stub
		driver.get("https://www.google.com/");

	}

}
