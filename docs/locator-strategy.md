# Locator Strategy

Location: `docs/locator-strategy.md`
Related docs: [`architecture.md`](architecture.md), [`test-automation-strategy.md`](test-automation-strategy.md), [`test-data-strategy.md`](test-data-strategy.md), [`execution-strategy.md`](execution-strategy.md)

---

## 1. Purpose

This strategy defines how Playwright locators are selected, organised and verified for the scenarios in `src/test/resources/features/`.

Tactive runs on Frappe / ERPNext. Element IDs and styling classes in a Frappe desk are generated and can change between pages and releases, but Frappe renders forms, lists and navigation from a **standardised DOM that carries the document type, field name and control type as `data-*` attributes**. Those attributes describe what a control *is* rather than how it looks, so they are the backbone of this suite's locators. Accessible roles and stable IDs complement them where they fit best.

---

## 2. Locator Priority

Use this order when identifying an element.

| # | Locator type | Use it for | Example |
| --- | --- | --- | --- |
| 1 | **Frappe data attributes** | ERP form fields, buttons, list cells, sidebar and workspace navigation | `select[data-doctype='Lead'][data-fieldname='status']` |
| 2 | **Accessible role and name** | Generic user-facing controls with a clear name, such as Continue | `getByRole(AriaRole.BUTTON, ...setName("Continue"))` |
| 3 | **Stable element IDs** | Established controls such as the login inputs | `#login_email`, `#login_password` |
| 4 | **Label or placeholder** | Editable inputs that expose them accessibly | `getByLabel(...)` |
| 5 | **Scoped CSS on Frappe structural classes** | Dialogs, messages, list rows, alerts | `.modal-content`, `.msgprint`, `.list-row-container` |
| 6 | **Visible text** | Business values and outcomes, with exact matching | `getByText(org, exact)` |

Keep selector definitions in the relevant page object. Feature files describe behaviour and outcomes and contain no CSS or XPath.

---

## 3. Frappe Data Attribute Reference

These are the attributes the suite relies on most.

| Attribute | Where it appears | Meaning | Used in the suite |
| --- | --- | --- | --- |
| `data-doctype` | Form controls | The DocType the control belongs to | `[data-doctype='Lead']`, `[data-doctype='Project']` |
| `data-fieldname` | Form controls, list cells | The field's internal name | `first_name`, `company_name`, `email_id`, `status`, `naming_series`, `title` |
| `data-fieldtype` | Form controls | The control type | `[data-fieldtype='Data']` |
| `data-label` | Buttons | The button's label | `button[data-label='Add Lead']`, `button[data-label='Save']` |
| `data-id` | Sidebar and workspace links | The navigation item | `a[data-id='Organization']`, `[data-id='Sales Invoice']` |
| `data-filter` | List row cells | The filter the cell represents | `[data-filter^='status']` |
| `data-sort-by` | List headers | The sort column | `span.level-item[data-sort-by='title']` |
| `data-dismiss` | Dialog controls | The dialog action | `button[data-dismiss='modal']` |

Frappe structural classes used with them: `.modal-content`, `.msgprint`, `.list-row-container`, `.level-left.ellipsis`, `label.reqd` (mandatory field label) and `#alert-container .alert-message` (toast).

### 3.1 Building a unique selector

Combine attributes until exactly one control matches. Many DocTypes share field names such as `status`, so the DocType makes the selector precise.

| Need | Selector |
| --- | --- |
| Field unique by name | `input[data-fieldname='email_id']` |
| Field shared across DocTypes | `select[data-doctype='Lead'][data-fieldname='status']` |
| Field also distinguished by control type | `input[data-fieldname='company_name'][data-fieldtype='Data'][data-doctype='Lead']` |
| Button by label | `button[data-label='Save']` |
| Mandatory indicator for one field | `div[data-fieldname="first_name"] label.reqd` |

Frappe desk can keep earlier pages in the DOM, so pair a data-attribute locator with a visibility assertion to be sure the active control is the one being used.

---

## 4. Feature-to-Locator Map

| Feature workflow | UI targets | Locator approach |
| --- | --- | --- |
| **Login and saved session** | Username, password, Continue, authenticated navigation | Login IDs `#login_email` and `#login_password`. Continue by button role and name. Confirm the session with `a[data-id='Organization']`. |
| **Lead defaults and creation** | Add Lead, naming series, status, first name, organisation, email, Save, lead list | `button[data-label='Add Lead']` and `button[data-label='Save']`. Fields by `data-fieldname`, adding `data-doctype='Lead'` and `data-fieldtype` where needed. Created leads by exact organisation text within `.level-left.ellipsis [data-fieldname='title']`. |
| **Lead validation** | Mandatory-fields dialog, invalid email message, First Name asterisk | Dialog scoped to `.modal-content`, messages read from `.msgprint`, close with `button[data-dismiss='modal']`. Asterisk found through `div[data-fieldname="first_name"] label.reqd`, checked through its `::after` content. Assertions cover the specific message text. |
| **Lead status update** | Existing lead, status selector, status in list | Lead opened by exact organisation text. Status through `select[data-doctype='Lead'][data-fieldname='status']`. Result read in the same row: `.list-row-container` filtered by the organisation, then `[data-filter^='status']`. |
| **Project creation** | Add Project, project form, project name, Save, company value | Add Project by accessible name. Fields and Save scoped to the active project dialog, using `data-fieldname` and `data-doctype='Project'`. Saved company value checked in the project view. |
| **Invoicing navigation** | Receivables and Sales Invoice navigation | Roles and names where available, otherwise `data-id='Receivables'` and `data-id='Sales Invoice'`. Destination verified by its heading or title attribute. |
| **CSV upload** | File selection, submission, completion message | `input[type='file']` with the fixture from `src/test/resources/testdata/uploads/`. Submit by role and name. Exact `File Uploaded!` message. |

---

## 5. Scoping and Dynamic Content

- Scope repeated controls to their nearest container: active dialog, form, navigation region or list row.
- For dynamic records, locate by scenario data such as the `TS01-` organisation or project name, then act on controls inside that record's row or form.
- Use exact text for known business values, and avoid picking the first match unless the scenario verifies ordering.
- Keep field selectors specific enough to tell DocTypes and field types apart.
- Keep changing record names out of selectors. Pass scenario values into page-object methods, for example `leadInList(title)`.

---

## 6. Waiting and Verification

Playwright locators auto-wait for actions and assertions, so waits are tied to the expected next state.

- **Visibility first.** Assert `isVisible` with a bounded timeout (15 seconds in the lead steps) before interacting with a form field, button or list item.
- **Form state for saves.** After Save, verify the Frappe form state (the document is no longer new and has no unsaved changes), then confirm the result in the list view.
- **Toast as a soft signal.** `#alert-container .alert-message` with the text "Saved" is logged when seen, while the form state is the authoritative check.
- **Specific validation checks.** For validation flows, assert the message text and field state named by the scenario, not only that a dialog appeared.
- **Readable failures.** Helpers such as `leadFromList` wait for the locator and raise a clear "not found in the list" message.

For each scenario, verify the visible postcondition named by the feature: the session is available after login, the created lead appears in the list, the requested status is displayed, the Sales Invoice view is visible, or the upload message appears.

Before relying on a selector that should match one control, confirm that its attributes and scope make it unambiguous. When the application shows repeated controls, narrow to a dialog, form or row instead of using positional selection.

---

## 7. Page-Object Ownership

- Locators are exposed as methods named after the control or business concept: `firstNameInput()`, `saveButton()`, `leadInList(title)`.
- Locators are grouped by page region. `leadPage` uses these groups:

| Group | Locators |
| --- | --- |
| Existing | `addLeadButton()`, `seriesSelect()`, `statusSelect()` |
| Form fields | `firstNameInput()`, `organizationNameInput()`, `emailInput()`, `saveButton()` |
| List page | `leadListTitles()`, `leadInList(title)` |
| Validation | `modalContent()`, `msgPrint()`, `dialogCloseButton()`, `firstNameMandatoryLabel()` |
| Status change | `listRow(org)` plus the actions built on the locators above |

- Locator strategy stays separate from test data. Values flow in as method parameters or exact-text locators.
- Reassess a locator when its feature changes, and update the page object and the scenario assertion together.

### 7.1 `basePage`

`baseLocator/basePage.java` is created and ready as the shared home for common locator helpers. The workflow page objects currently hold their own locators, which keeps each module easy to read while the patterns settle. As Modules 2 to 4 add more DocTypes, `basePage` is the natural place to consolidate helpers that repeat, for example:

| Candidate helper | Purpose | Selector pattern |
| --- | --- | --- |
| `fieldInput(doctype, fieldname)` | Input for any DocType field | `input[data-doctype='<doctype>'][data-fieldname='<fieldname>']` |
| `fieldSelect(doctype, fieldname)` | Select for any DocType field | `select[data-doctype='<doctype>'][data-fieldname='<fieldname>']` |
| `buttonByLabel(label)` | Any labelled button | `button[data-label='<label>']` |
| `fieldLabel(fieldname)` | A field's label, including the mandatory marker | `div[data-fieldname='<fieldname>'] label` |
| `listRow(text)` | A list row by its business value | `.list-row-container` filtered by text |
| `sidebarLink(id)` | Navigation by `data-id` | `[data-id='<id>']` |

Workflow-specific selectors stay in their owning page object, and shared helpers move into `basePage` once two or more page objects need them.

---

## 8. Selector Examples

```java
// Frappe button by label
page.locator("button[data-label='Save']");

// ERP field constrained by DocType, field name and control type
page.locator("input[data-fieldname='company_name'][data-fieldtype='Data'][data-doctype='Lead']");

// Field name shared across DocTypes, so the DocType is included
page.locator("select[data-doctype='Lead'][data-fieldname='status']");

// Navigation attribute
page.locator("a[data-id='Organization']");

// Exact dynamic business value in the list
page.locator(".level-left.ellipsis [data-fieldname='title']")
	.getByText(expectedOrganization, new Locator.GetByTextOptions().setExact(true));

// Status cell within the row of one record
page.locator(".list-row-container")
	.filter(new Locator.FilterOptions().setHasText(org))
	.first()
	.locator("[data-filter^='status']").first();

// Semantic action locator
page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Continue"));

// Container-scoped project form field
modalContent().locator("input[data-fieldname='project_name'][data-doctype='Project']");
```

---

## 9. Maintenance Standard

When adding a scenario:

1. Identify the user-visible control and the expected outcome.
2. Inspect the control for `data-doctype`, `data-fieldname`, `data-fieldtype`, `data-label` or `data-id`, and build the most specific selector from them.
3. Use a role and name or a stable ID where that reads better.
4. Encapsulate the locator in the page object for that workflow.
5. Pair it with a visibility assertion and an outcome assertion.

Favouring data attributes, roles, IDs and meaningful scope over generated styling classes and page position keeps locators resilient to layout changes.

### Next refinements

- Replace the short fixed pauses in the save steps with state waits (the form-state check or the "Saved" toast), so timing follows the application.
- Move repeated Frappe helpers into `basePage` as more DocTypes are covered.
- Add each new module's locator group to the map in section 4.
