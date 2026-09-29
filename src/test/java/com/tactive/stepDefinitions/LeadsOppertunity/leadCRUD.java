package com.tactive.stepDefinitions.LeadsOppertunity;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.tactive.factory.DriverFactory;
import com.tactive.pages.LeadsOppertunity.leadPage;
import com.tactive.utils.config.ConfigReader;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class leadCRUD {

    private final leadPage leadPage = new leadPage(DriverFactory.getPage());

    // Per-scenario state (Cucumber creates one instance per scenario)
    private String lastCreatedOrganization;
    private String lastCreatedDocId;

    // TS01-M1-03 / M1-04 state
    private List<String> dialogsMandate;

    // Shared output file, so writes are locked
    private static final Object FILE_LOCK = new Object();
    private static final Path CREATED_DOCS_FILE = Paths.get("test-output", "ts01-created-docs.txt");

    // ======================= EXISTING STEPS (unchanged) =======================

    @When("i went on to click add lead navgiated")
    public void i_went_on_to_click_add_lead_navgiated() {
        String baseUrl = ConfigReader.getBaseUrl();
        DriverFactory.getPage().navigate(baseUrl + "/desk/lead");

        assertThat(leadPage.addLeadButton())
                .isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(15000));
        leadPage.addLeadButton().click();
    }

    @Then("assert Series CRM-LEAD-.YYYY.- and Status default.")
    public void assert_series_and_status_default() {
        assertThat(leadPage.seriesSelect())
                .isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(15000));
        String seriesValue = leadPage.seriesSelect().textContent();
        Assertions.assertEquals("CRM-LEAD-.YYYY.-", seriesValue.trim(),
                "Naming series did not match expected default!");

        assertThat(leadPage.statusSelect())
                .isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(15000));
        String statusValue = leadPage.statusSelect().inputValue();
        Assertions.assertEquals("Lead", statusValue,
                "Status did not default to 'Lead'!");
    }

    // ======================= TS01-M1-02 =======================

    @When("I fill the lead form with first name {string}, organization {string}, email {string} and status {string}")
    public void i_fill_the_lead_form(String firstName, String organization, String email, String status) {
        String ts = System.currentTimeMillis() + "-"
                + ThreadLocalRandom.current().nextInt(1000, 10000);

        assertThat(leadPage.firstNameInput())
                .isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(15000));
        if (!firstName.isBlank()) {
            leadPage.firstNameInput().fill(firstName + ts);
        }

        String uniqueOrg = organization + "-" + ts;
        assertThat(leadPage.organizationNameInput())
                .isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(15000));
        leadPage.organizationNameInput().fill(uniqueOrg);
        lastCreatedOrganization = uniqueOrg;

        String finalEmail = email;
        int at = email.indexOf('@');
        if (at > 0) {
            finalEmail = email.substring(0, at) + "." + ts + email.substring(at);
        }
        assertThat(leadPage.emailInput())
                .isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(15000));
        leadPage.emailInput().fill(finalEmail);

        assertThat(leadPage.statusSelect())
                .isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(15000));
        leadPage.statusSelect().selectOption(status);
        Assertions.assertEquals(status, leadPage.statusSelect().inputValue(),
                "Status was not set to '" + status + "'!");
    }

    @When("I save the lead")
    public void i_save_the_lead() {
        Page page = DriverFactory.getPage();

        assertThat(leadPage.saveButton())
                .isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(15000));

        // Let Frappe's field change handlers finish before saving
        page.waitForTimeout(500);
        leadPage.saveButton().click();

        // No URL involved: wait for Frappe to mark the doc as no longer new.
        // If the click was ignored, fall back to Frappe's Ctrl+S shortcut.
        try {
            page.waitForFunction(
                    "() => window.cur_frm && cur_frm.doc && !cur_frm.doc.__islocal",
                    null,
                    new Page.WaitForFunctionOptions().setTimeout(5000));
        } catch (TimeoutError e) {
            System.out.println("Save click was ignored, retrying with Ctrl+S.");
            page.keyboard().press("Control+s");
        }
    }

    @Then("the lead should be saved successfully")
    public void the_lead_should_be_saved_successfully() throws IOException {
        Page page = DriverFactory.getPage();

        // Soft toast signal, as in the TS test: log if it doesn't show, don't fail
        try {
            assertThat(page.locator("#alert-container .alert-message",
                    new Page.LocatorOptions().setHasText("Saved")))
                    .isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(5000));
        } catch (AssertionError e) {
            System.out.println("Saved toast not seen.");
        }

        // Real check: Frappe form is no longer new and has no unsaved changes (no URL involved)
        try {
            page.waitForFunction(
                    "() => window.cur_frm && cur_frm.doc && !cur_frm.doc.__islocal && !cur_frm.is_dirty()",
                    null,
                    new Page.WaitForFunctionOptions().setTimeout(15000));
        } catch (TimeoutError e) {
            Path shot = Paths.get("target", "screenshots",
                    "lead-not-saved-" + System.currentTimeMillis() + ".png");
            Files.createDirectories(shot.getParent());
            page.screenshot(new Page.ScreenshotOptions().setPath(shot).setFullPage(true));

            Object state = page.evaluate("() => window.cur_frm ? ({"
                    + "name: cur_frm.doc.name, islocal: cur_frm.doc.__islocal, dirty: cur_frm.is_dirty(),"
                    + "indicator: (document.querySelector('.page-head .indicator-pill') || {}).innerText"
                    + "}) : 'cur_frm not available'");
            throw new AssertionError("Lead not saved. FormState=" + state + " | Screenshot=" + shot, e);
        }

        Object name = page.evaluate("() => cur_frm.doc.name");
        lastCreatedDocId = String.valueOf(name);
    }

    @Then("I record the created lead document ID")
    public void i_record_the_created_lead_document_id() throws IOException {
        Assertions.assertNotNull(lastCreatedDocId, "No lead document ID available to record!");
        String line = lastCreatedDocId + " | " + lastCreatedOrganization + System.lineSeparator();
        synchronized (FILE_LOCK) {
            Files.createDirectories(CREATED_DOCS_FILE.getParent());
            Files.writeString(CREATED_DOCS_FILE, line,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        }
    }

    @Then("the lead {string} should appear in the lead list")
    public void the_lead_should_appear_in_the_lead_list(String expectedOrganization) {
        Page page = DriverFactory.getPage();
        page.navigate(ConfigReader.getBaseUrl() + "/desk/lead");

        String toFind = (lastCreatedOrganization != null && lastCreatedOrganization.startsWith(expectedOrganization))
                ? lastCreatedOrganization
                : expectedOrganization;

        assertThat(leadPage.leadListTitles().first())
                .isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(15000));
        assertThat(leadPage.leadInList(toFind))
                .isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(15000));
    }

    // ======================= TS01-M1-03 =======================

    @Given("I open the new lead form for the mandatory check")
    public void i_open_the_new_lead_form_for_the_mandatory_check() {
        DriverFactory.getPage().navigate(ConfigReader.getBaseUrl() + "/desk/lead");

        assertThat(leadPage.addLeadButton())
                .isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(15000));
        leadPage.addLeadButton().click();
    }

    @When("I leave First Name empty and click Save")
    public void i_leave_first_name_empty_and_click_save() {
        // Same as the TS test: First Name is only clicked, never filled
        assertThat(leadPage.firstNameInput())
                .isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(15000));
        leadPage.firstNameInput().click();

        // Organization Name is only checked for visibility, as in the TS test
        assertThat(leadPage.organizationNameInput())
                .isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(15000));

        assertThat(leadPage.saveButton())
                .isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(15000));
        leadPage.saveButton().click();
    }

    @Then("I should see the mandatory fields dialog")
    public void i_should_see_the_mandatory_fields_dialog() {
        assertThat(leadPage.modalContent().first())
                .isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(15000));

        dialogsMandate = leadPage.msgPrint().allInnerTexts();
        System.out.println("M1-03 mandatory dialog text: " + dialogsMandate);

        Assertions.assertFalse(dialogsMandate.isEmpty(),
                "Mandatory fields dialog had no message!");
    }

    @When("I close the mandatory fields dialog")
    public void i_close_the_mandatory_fields_dialog() {
        leadPage.dialogCloseButton().click();
    }

    @Then("the First Name label should show the mandatory asterisk")
    public void the_first_name_label_should_show_the_mandatory_asterisk() {
        assertThat(leadPage.firstNameMandatoryLabel())
                .isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(15000));

        Object contentAstericIcon = leadPage.firstNameMandatoryLabel().evaluate(
                "el => window.getComputedStyle(el, '::after').getPropertyValue('content')");

        Assertions.assertNotNull(contentAstericIcon, "No ::after content found on the First Name label!");
        Assertions.assertTrue(String.valueOf(contentAstericIcon).contains("*"),
                "First Name label has no mandatory asterisk. ::after content=" + contentAstericIcon);
    }

    // ======================= TS01-M1-04 =======================

    @Then("the dialog should report {string} is not a valid Email Address")
    public void the_dialog_should_report_email_is_not_a_valid_email_address(String badEmail) {
        Assertions.assertNotNull(dialogsMandate,
                "No dialog text captured. Run 'I should see the mandatory fields dialog' first!");

        String dialogText = String.join(" ", dialogsMandate).replaceAll("\\s+", " ").trim();
        System.out.println("M1-04 dialog text: " + dialogText);

        Assertions.assertTrue(dialogText.toLowerCase().contains(badEmail.toLowerCase()),
                "Dialog does not mention the entered email '" + badEmail + "'. Dialog=" + dialogText);
        Assertions.assertTrue(dialogText.toLowerCase().contains("not a valid email address"),
                "Dialog does not say 'not a valid Email Address'. Dialog=" + dialogText);
    }

    @Then("the lead should not be saved")
    public void the_lead_should_not_be_saved() {
        // Value not accepted: the document is still a new, unsaved Lead
        Boolean stillNew = (Boolean) DriverFactory.getPage().evaluate(
                "() => !!(window.cur_frm && cur_frm.doc && cur_frm.doc.__islocal)");
        Assertions.assertTrue(stillNew, "Lead was saved even though the email is invalid!");
    }

    // ======================= TS01-M1-05 =======================
    // Reuses the lead created in TS01-M1-02 (org name is passed from the feature file)

    @Given("I am on the lead list page")
    public void i_am_on_the_lead_list_page() {
        leadPage.openLeadList();
    }

    @When("I open the lead {string} from the list")
    public void i_open_the_lead_from_the_list(String org) {
        leadPage.openLeadFromList(org);
    }

    @When("I change the lead status to {string} and save")
    public void i_change_the_lead_status_and_save(String status) {
        leadPage.changeStatusAndSave(status);
    }

    @Then("the lead list should show status {string} for {string}")
    public void the_lead_list_should_show_status_for(String expectedStatus, String org) {
        String actual = leadPage.getListStatusFor(org);
        System.out.println("M1-05 cleaned status value: \"" + actual + "\"");
        Assertions.assertEquals(expectedStatus, actual,
                "Status in the lead list did not match for '" + org + "'!");
    }
}