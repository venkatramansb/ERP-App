package com.tactive.pages;

import com.microsoft.playwright.options.AriaRole;
import com.tactive.utils.config.ConfigReader;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class LoginTactivePage {

    private Page page;

    public LoginTactivePage(Page page) {
        this.page = page;
    }

    public void enterUserName() {
        // NOTE: If ConfigReader.get() fails, see the ConfigReader section below
        String username = ConfigReader.get("TACTIVE_USERNAME");
        page.locator("#login_email").fill(username);
    }

    public void enterPassword() {
        String password = ConfigReader.get("TACTIVE_PASSWORD");
        page.locator("#login_password").fill(password);
    }
    public void clickLoginButton() {
        page.getByRole(
            AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName("Continue")
        ).click();
}
    public Locator loggedInConfirmation() {
        return page.locator("a[data-id='Organization']");
    }
}
