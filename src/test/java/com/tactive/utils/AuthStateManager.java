package com.tactive.utils;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.tactive.pages.LoginTactivePage;
import com.tactive.utils.config.ConfigReader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AuthStateManager {

    // must match the path DriverFactory checks
    private static final Path STORAGE_STATE_PATH = Paths.get("auth.json");

    public static Path getStorageStatePath() {
        return STORAGE_STATE_PATH;
    }

    public static boolean storageStateExists() {
        return Files.exists(STORAGE_STATE_PATH);
    }

    /**
     * Launches its own short-lived Playwright/Browser (independent of
     * DriverFactory's thread-local instances, since this runs in @BeforeAll
     * before any scenario thread has called initDriver()), logs in once,
     * and saves the session to auth.json for every later context to reuse.
     */
    public static void generateStorageState() {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(true));

            BrowserContext setupContext = browser.newContext(
                    new Browser.NewContextOptions().setIgnoreHTTPSErrors(true));
            Page setupPage = setupContext.newPage();

            setupPage.navigate(ConfigReader.getBaseUrl());

            LoginTactivePage loginPage = new LoginTactivePage(setupPage);
            loginPage.enterUserName();
            loginPage.enterPassword();
            loginPage.clickLoginButton();

            loginPage.loggedInConfirmation().waitFor(
                    new Locator.WaitForOptions().setTimeout(30000));

            setupContext.storageState(new BrowserContext.StorageStateOptions()
                    .setPath(STORAGE_STATE_PATH));

            setupContext.close();
            browser.close();
        }
    }
}