package com.tactive.stepDefinitions;

import com.tactive.factory.DriverFactory;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

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
    public void quitBrowser() {

        DriverFactory.quitDriver();
    }
}