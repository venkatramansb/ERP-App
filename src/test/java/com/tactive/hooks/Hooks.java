package com.tactive.hooks;

import com.microsoft.playwright.Page;
import com.tactive.factory.DriverFactory;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

import java.nio.file.Paths;

public class Hooks {

    @Before(order = 0)
    public void launchBrowser(Scenario scenario) {
        // Scenarios tagged @freshLogin get a clean, unauthenticated context
        // (used for the real login-form flow). Everything else reuses
        // auth.json if it's present, to skip login.
        boolean useSavedSession = !scenario.getSourceTagNames().contains("@freshLogin");

        DriverFactory.initDriver("chrome", useSavedSession);
    }

    @After(order = 0)
    public void quitBrowser(Scenario scenario) {
        // Screenshot and cleanup live in one method, so the screenshot is always
        // taken from the scenario's own page before the browser is closed.
        try {
            Page page = DriverFactory.getPage();
            if (scenario.isFailed() && page != null) {
                // 1. Attach screenshot to the HTML/Cucumber report output stream
                byte[] screenshot = page.screenshot(
                        new Page.ScreenshotOptions().setFullPage(true));
                scenario.attach(screenshot, "image/png", "Failure Screenshot");

                // 2. Write the screenshot directly to a physical file so GitHub Actions
                // can capture it under the 'target/screenshots/' artifact directory path
                String cleanScenarioName = scenario.getName().replaceAll("[^a-zA-Z0-9]", "_");
                page.screenshot(new Page.ScreenshotOptions()
                        .setPath(Paths.get("target/screenshots/" + cleanScenarioName + ".png"))
                        .setFullPage(true));
                
                System.out.println("📸 Failure screenshot saved locally to target/screenshots/");
            }
        } catch (Exception e) {
            System.out.println("Could not capture failure screenshot: " + e.getMessage());
        } finally {
            DriverFactory.quitDriver();
        }
    }
}
