package com.tactive.factory;

import com.microsoft.playwright.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DriverFactory {

    private static ThreadLocal<Playwright> tlPlaywright = new ThreadLocal<>();
    private static ThreadLocal<Browser> tlBrowser = new ThreadLocal<>();
    private static ThreadLocal<BrowserContext> tlContext = new ThreadLocal<>();
    private static ThreadLocal<Page> tlPage = new ThreadLocal<>();

    // Existing behaviour unchanged: always tries to reuse auth.json if present.
    public static Page initDriver(String browserName) {
        return initDriver(browserName, true);
    }

    // New: pass useSavedSession=false to force a clean, unauthenticated context
    // (used for scenarios that exercise the real login form, e.g. Scenario 1).
    public static Page initDriver(String browserName, boolean useSavedSession) {

        System.out.println("Launching browser: " + browserName);

        tlPlaywright.set(Playwright.create());

        switch (browserName.toLowerCase()) {
            case "chrome":
            case "chromium":
                tlBrowser.set(tlPlaywright.get().chromium().launch(new BrowserType.LaunchOptions().setHeadless(false)));
                break;
            case "firefox":
                tlBrowser.set(tlPlaywright.get().firefox().launch(new BrowserType.LaunchOptions().setHeadless(false)));
                break;
            case "webkit":
            case "safari":
                tlBrowser.set(tlPlaywright.get().webkit().launch(new BrowserType.LaunchOptions().setHeadless(false)));
                break;
            default:
                throw new IllegalArgumentException("Please provide a valid browser name!");
        }

        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                .setIgnoreHTTPSErrors(true);

        Path authStatePath = Paths.get("auth.json");
        if (useSavedSession && authStatePath.toFile().exists()) {
            System.out.println("[Thread " + Thread.currentThread().threadId() + "] Found saved session! Loading auth.json to bypass login.");
            contextOptions.setStorageStatePath(authStatePath);
        } else if (!useSavedSession) {
            System.out.println("[Thread " + Thread.currentThread().threadId() + "] Skipping saved session by request. Starting clean session.");
        } else {
            System.out.println("[Thread " + Thread.currentThread().threadId() + "] No saved session found. Starting clean session.");
        }

        tlContext.set(tlBrowser.get().newContext(contextOptions));

        tlPage.set(tlContext.get().newPage());

        return getPage();
    }

    public static Page getPage() {
        return tlPage.get();
    }

    public static BrowserContext getContext() {
        return tlContext.get();
    }

    public static void quitDriver() {
        if (tlPage.get() != null) tlPage.get().close();
        if (tlContext.get() != null) tlContext.get().close();
        if (tlBrowser.get() != null) tlBrowser.get().close();
        if (tlPlaywright.get() != null) tlPlaywright.get().close();

        tlPage.remove();
        tlContext.remove();
        tlBrowser.remove();
        tlPlaywright.remove();
    }
}