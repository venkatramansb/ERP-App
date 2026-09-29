package com.tactive.stepDefinitions;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.tactive.factory.DriverFactory;
import com.tactive.pages.LoginTactivePage;
import com.tactive.utils.config.ConfigReader;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
//import java.util.Map;

public class LoginTactive {

    private LoginTactivePage loginPage = new LoginTactivePage(DriverFactory.getPage());
    
    // COMPATIBLE FOR PARALLEL RUNS: Each thread gets its own isolated session data storage
    //private static ThreadLocal<Map<String, Object>> tlLocalStorageData = new ThreadLocal<>();

    @Given("I am on the Tactive login page")
    public void i_am_on_the_tactive_login_page() {

        
        String baseUrl = ConfigReader.getBaseUrl();
        DriverFactory.getPage().navigate(baseUrl);
        System.out.println("Landed on getLatest: " + DriverFactory.getPage().url());
    }

    @When("I enter valid username")
    public void i_enter_valid_username() {
        loginPage.enterUserName();
    }

    @When("I enter valid password")
    public void i_enter_valid_password() {
        loginPage.enterPassword();
    }

    @When("I click the Continue button")
    public void i_click_the_continue_button() {
        loginPage.clickLoginButton();
    }



    @Then("I should see the Title")
    public void i_should_see_the_title() {
    System.out.println("Current URL before wait: " + DriverFactory.getPage().url());
    loginPage.loggedInConfirmation().waitFor(
        new Locator.WaitForOptions()
            .setState(WaitForSelectorState.VISIBLE)
            .setTimeout(15000)
    );
    }

}
