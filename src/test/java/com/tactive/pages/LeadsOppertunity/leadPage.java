package com.tactive.pages.LeadsOppertunity;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.tactive.utils.config.ConfigReader;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class leadPage {

    private static final double LIST_TIMEOUT_MS = 15000;

    private final Page page;

    public leadPage(Page page) {
        this.page = page;
    }

    // ---------- existing ----------
    public Locator addLeadButton() {
        return page.locator("button[data-label='Add Lead']");
    }

    public Locator seriesSelect() {
        return page.locator("select[data-fieldname='naming_series']");
    }

    public Locator statusSelect() {
        return page.locator("select[data-doctype='Lead'][data-fieldname='status']");
    }

    // ---------- form fields ----------
    public Locator firstNameInput() {
        return page.locator("input[data-fieldtype='Data'][data-fieldname='first_name']");
    }

    public Locator organizationNameInput() {
        return page.locator("input[data-fieldname='company_name'][data-fieldtype='Data'][data-doctype='Lead']");
    }

    public Locator emailInput() {
        return page.locator("input[data-fieldname='email_id']");
    }

    public Locator saveButton() {
        return page.locator("button[data-label='Save']");
    }

    // ---------- list page ----------
    public Locator leadListTitles() {
        return page.locator("span.level-item[data-sort-by='title']");
    }

    public Locator leadInList(String title) {
        return page.locator(".level-left.ellipsis [data-fieldname='title']")
                .getByText(title, new Locator.GetByTextOptions().setExact(true));
    }

    // ---------- TS01-M1-03: mandatory validation ----------
    public Locator modalContent() {
        return page.locator(".modal-content");
    }

    public Locator msgPrint() {
        return page.locator(".msgprint");
    }

    public Locator dialogCloseButton() {
        return page.locator("button[data-dismiss='modal']");
    }

    public Locator firstNameMandatoryLabel() {
        return page.locator("div[data-fieldname=\"first_name\"] label.reqd");
    }

    // ---------- TS01-M1-05: change status on an existing lead ----------
    public Locator listRow(String org) {
        return page.locator(".list-row-container")
                .filter(new Locator.FilterOptions().setHasText(org))
                .first();
    }

    public void openLeadList() {
        String baseUrl = ConfigReader.get("TACTIVE_BASE_URL").replaceAll("/+$", "");
        page.navigate(baseUrl + "/desk/lead");
        leadListTitles().first().waitFor();
    }

    // was LeadFromList in the TS: waits up to 15s and returns the locator
    public Locator leadFromList(String expectedLead) {
        Locator lead = leadInList(expectedLead);
        try {
            assertThat(lead).isVisible(
                    new LocatorAssertions.IsVisibleOptions().setTimeout(LIST_TIMEOUT_MS));
        } catch (AssertionError | PlaywrightException e) {
            throw new AssertionError("Lead \"" + expectedLead + "\" was not found in the list!", e);
        }
        return lead;
    }

    public void openLeadFromList(String expectedLead) {
        leadFromList(expectedLead).click();
    }

    public void changeStatusAndSave(String status) {
        assertThat(statusSelect()).isVisible();
        statusSelect().selectOption(status);
        assertThat(statusSelect()).hasValue(status);

        assertThat(saveButton()).isVisible();
        saveButton().click();
        page.waitForTimeout(2000);   // same as the TS; replace with the "Saved" alert check if re-enabled
    }

    public String getListStatusFor(String org) {
        openLeadList();
        String raw = listRow(org).locator("[data-filter^='status']").first().textContent();
        return raw == null ? "" : raw.trim();
    }
}