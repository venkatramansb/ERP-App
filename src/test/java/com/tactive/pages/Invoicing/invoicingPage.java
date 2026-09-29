package com.tactive.pages.Invoicing;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.tactive.utils.config.ConfigReader;

import java.util.List;

public class invoicingPage {

    private final Page page;

    public invoicingPage(Page page) {
        this.page = page;
    }

    // ---------- Navigation & Structure Locators ----------
    public Locator leftSidebarTop() {
        return page.locator(".body-sidebar-top");
    }

    public Locator receivablesCollapsedButton() {
        return page.locator("button[data-state='closed']");
    }

    public Locator receivablesMenuLink() {
        return page.locator("[data-id='Receivables']");
    }

    public Locator salesInvoiceMenuLink() {
        return page.locator("[data-id='Sales Invoice']");
    }

    // ---------- Target View Elements ----------
    public Locator salesInvoiceTitleHeader() {
        return page.locator("[title='Sales Invoice']").first();
    }

    // ---------- Core Page Operations ----------
    public void navigateToInvoicingModule() {
        String baseUrl = ConfigReader.get("TACTIVE_BASE_URL").replaceAll("/+$", "");
        page.navigate(baseUrl + "/desk/invoicing");
    }

    public List<String> getLeftMenuItemsTexts() {
        return leftSidebarTop().allInnerTexts();
    }
}
