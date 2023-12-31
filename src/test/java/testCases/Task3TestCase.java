package testCases;

import driver.DriverSingleton;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import pages.CalculatorPage;
import pages.GoogleCloudHomePage;
import pages.SearchResultPage;
import pages.YopMailPage;

import java.io.File;
import java.io.IOException;
import java.time.Duration;

public class Task3TestCase {
    public static WebDriver driver;
    public static WebDriverWait webDriverWait;

    @BeforeTest
    public static void init() {
        driver = DriverSingleton.getDriver();
        webDriverWait = new WebDriverWait(driver, Duration.ofSeconds(5));
    }

    @AfterTest
    public static void tearDown() {
        DriverSingleton.closeDriver();
    }

    @Test
    public static void test1() throws InterruptedException, IOException {
        try {
            // Step 1: Open Google Cloud Home Page
            driver.get("https://cloud.google.com/");
            driver.manage().window().maximize();
            GoogleCloudHomePage homePage = new GoogleCloudHomePage(driver);

            // Step 2-4: Search and go to calculator page
            homePage.searchForCalculator("Google Cloud Platform Pricing Calculator");
            SearchResultPage searchResultPage = new SearchResultPage(driver, webDriverWait);
            searchResultPage.goToCalculator();

            // Step 5-8: Fill out the form and check the price
            CalculatorPage calculatorPage = new CalculatorPage(driver, webDriverWait);
            calculatorPage.manageNotification();
            calculatorPage.addNumberOfInstances();
            calculatorPage.changeSeries();
            calculatorPage.changeMachineType();
            calculatorPage.addGPU();
            calculatorPage.localSSD();
            calculatorPage.addDataCenter();
            calculatorPage.addCommittedUsage();
            calculatorPage.addToEstimate();
            String totalCost = calculatorPage.getTotalCost();

            float cost = Float.parseFloat(totalCost.split("USD ", 2)[1].split(" per")[0].replace(",", ""));
            Assert.assertTrue(cost > 0);
            System.out.println("Total Estimated Cost: " + cost);

            // Step 9-15: Email estimate and validate
            calculatorPage.emailEstimate();
            YopMailPage yopMailPage = new YopMailPage(driver, webDriverWait);
            String firstWindow = driver.getWindowHandles().stream().findFirst().get();
            yopMailPage.openYopMailPage();
            String secondWindow = "";
            for (String window : driver.getWindowHandles()) {
                if (window != firstWindow)
                    secondWindow = window;
            }
            yopMailPage.switchToYopMailPage(secondWindow);
            yopMailPage.acceptCookiesAndContinue();
            String emailInbox = "epamTask2_email";
            yopMailPage.enterInbox(emailInbox);
            yopMailPage.checkInbox();

            calculatorPage.switchToCalculatorPage(firstWindow);
            calculatorPage.sendEmailEstimate(emailInbox + "@yopmail.com");
            yopMailPage.switchToYopMailPage(secondWindow);

            //Wait 30 seconds to receive the email
            Thread.sleep(10000);
            yopMailPage.refreshPage();
            float totalCostFromEmail = yopMailPage.getTotalCostFromEmail();
            Assert.assertEquals(cost, totalCostFromEmail);
        }
        catch (Exception e){
            TakesScreenshot screenshot = (TakesScreenshot)driver;
            File source = screenshot.getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(source, new File("./SeleniumScreenshots/Screen.png"));
            assert false;
        }
    }
}
