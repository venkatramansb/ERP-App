package com.tactive.baseLocator;

import com.microsoft.playwright.options.AriaRole;
//import com.tactive.utils.config.ConfigReader;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public abstract class basePage {

    protected final Page page;

    protected basePage(Page page) {
        this.page = page;
    }

    public Locator field(String fieldName) {
    return page.locator(
        String.format("[data-fieldname='%s']", fieldName)
    );
    }

    public Locator label(String fieldName) {
    return page.locator(
        String.format("[data-label='%s']", fieldName)
    );
    }


    /* 

    field("first_name").fill("TS01-QA");
    field("company_name").fill("TS01-ERP");
    
    protected Locator field(String fieldName) {
        return page.locator("[data-fieldname='" + fieldName + "']");
    }
    */
    protected void clickSave() {
        page.getByRole(
            AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName("Save")
        ).click();
    }

    protected void waitForPage() {
        page.waitForTimeout(5000);
        
    }
}