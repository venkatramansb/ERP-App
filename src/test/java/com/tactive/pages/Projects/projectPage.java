package com.tactive.pages.Projects;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
//import com.microsoft.playwright.options.AriaRole;
import com.tactive.utils.config.ConfigReader;

//import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class projectPage {

    private final Page page;

    public projectPage(Page page) {
        this.page = page;
    }

    // ---------- List view / Nav Locators ----------
    public Locator addProjectButton() {
        return page.locator("button[data-label='Add Project']");
    }

    // ---------- Modal / Form Containers ----------
    public Locator modalContent() {
        return page.locator(".modal-content");
    }

    // ---------- Form Fields (Contextual to Modal Popup) ----------
    public Locator seriesSelect() {
        return modalContent().locator("select[data-fieldname='naming_series']");
    }

    public Locator projectNameInput() {
        return modalContent().locator("input[data-fieldname='project_name'][data-doctype='Project']");
    }

    public Locator saveButton() {
        return modalContent().locator("button.btn-modal-primary").filter(new Locator.FilterOptions().setHasText("Save"));
    }

    public Locator savedMessageAlert() {
        return modalContent().locator("#alert-container .alert-message").filter(new Locator.FilterOptions().setHasText("Saved"));
    }

    // ---------- Core Page Operations ----------
    public void navigateToProjectModule() {
        String baseUrl = ConfigReader.get("TACTIVE_BASE_URL").replaceAll("/+$", "");
        page.navigate(baseUrl + "/desk/project");
    }

    public void waitForModal() {
        page.waitForSelector(".modal-content");
    }
}
