package com.tactive.hooks;

//import com.microsoft.playwright.options.AriaRole;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
//import com.tactive.utils.config.ConfigReader;
//import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

public class hooks {

    private static Playwright playwright;
    private static Browser browser;
    private static BrowserContext context;
    private static Page page;

    @Before
    public void setUp() {
        playwright = Playwright.create();

        browser = playwright.chromium().launch(
            new BrowserType.LaunchOptions()
                .setHeadless(true)
        );

        context = browser.newContext();
        page = context.newPage();
    }

    @After
    public void tearDown(Scenario scenario) {

        if (scenario.isFailed()) {
            byte[] screenshot = page.screenshot(
                new Page.ScreenshotOptions().setFullPage(true)
            );

            scenario.attach(
                screenshot,
                "image/png",
                "Failure Screenshot"
            );
        }

        context.close();
        browser.close();
        playwright.close();
    }

    public static Page getPage() {
        return page;
    }
}