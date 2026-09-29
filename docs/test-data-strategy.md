# Test Data Strategy

Location: `docs/test-data-strategy.md`
Related docs: [`test-automation-strategy.md`](test-automation-strategy.md), [`execution-strategy.md`](execution-strategy.md), [`architecture.md`](architecture.md), [`locator-strategy.md`](locator-strategy.md)

---

## 1. Purpose

This strategy defines how test data is sourced, generated, isolated, used and retained by the Playwright Java and Cucumber suite. It supports repeatable validation of Tactive workflows, keeps credentials out of test code, and prepares the suite for data-driven runs from CSV files.

---

## 2. Environment and Credentials

The target environment is described by three values: **base URL, email (username) and password**. They are supplied from environment variables or from a git-ignored file, and are never committed.

### 2.1 How the values are used

| Key | Used for | Where it is read |
| --- | --- | --- |
| `TACTIVE_BASE_URL` | Building every page URL, for example `<base URL>/desk/lead` | `ConfigReader.getBaseUrl()`, called by the step definitions and page objects |
| `TACTIVE_USERNAME` | Login email | `LoginTactivePage.enterUserName()` and `AuthStateManager` |
| `TACTIVE_PASSWORD` | Login password | `LoginTactivePage.enterPassword()` and `AuthStateManager` |

Feature files and Java source contain no URLs or credentials. Steps build the address from the configured base URL plus the path, so pointing the suite at another environment is a configuration change only.

### 2.2 Where the values come from

| Context | Source |
| --- | --- |
| Local | `env/.env.stage`, a git-ignored file read by `ConfigReader` |
| CI (GitLab) | CI/CD variables with the same three keys, provided as environment variables (masked, and protected where the pipeline allows it) |

Example `env/.env.stage` (placeholders):

```dotenv
TACTIVE_BASE_URL=https://<tenant>.frappe.cloud
TACTIVE_USERNAME=<login email>
TACTIVE_PASSWORD=<password>
```

### 2.3 Handling rules

- **Never commit** the base URL, email or password, and never place them in feature files, fixtures or logs.
- Keep `env/.env.*` and `auth.json` in `.gitignore`. A template file with placeholder values can be committed so new team members know which keys to provide.
- Use a dedicated test account on the designated test tenant.
- Log the generated data keys and document IDs, and leave credentials and session contents out of the output.
- Keep the base URL without a trailing path, for example `https://<tenant>.frappe.cloud`, because the steps add `/desk/...` themselves.

---

## 3. Data Inventory

| Data class | Source or location | Handling standard |
| --- | --- | --- |
| Environment configuration | `env/.env.stage` (local) or CI variables | Supplied per environment, as described in section 2. |
| Authenticated browser state | Root `auth.json`, produced by `AuthStateManager` | Temporary sensitive state. It is recreated through the login flow when needed and does not replace verifying the login behaviour itself. |
| Scenario inputs | Gherkin parameters in the feature files | Explicit values such as first name, organisation, email and status, passed into the steps. |
| Data-driven inputs (planned) | CSV files under `src/test/resources/testdata/` | Version-controlled, small and deterministic. See section 5. |
| Stable upload input | `src/test/resources/testdata/uploads/sample_dataset.csv` | Versioned with the tests. It has an `id,username,email,role` header and two test-user rows. |
| Downloaded files | `src/test/resources/testdata/downloads/` | Reserved for known download fixtures. Runtime downloads go to a run-specific output directory. |
| Tenant business records | Created in the shared Tactive tenant by the lead and project workflows | Prefixed `TS01-` with a unique run value, and their identifiers are recorded. |
| Run evidence | Cucumber report and screenshots under `target/`, lead identifiers in `test-output/ts01-created-docs.txt` | Generated output tied to the run. It is not used as input. |

---

## 4. Data Lifecycle

1. **Configure.** Select the Tactive environment through the base URL, email and password, and confirm the account and tenant suit the selected profile.
2. **Prepare authentication.** The login flow creates or refreshes `auth.json`. Scenarios tagged `@freshLogin` use a clean browser context to exercise the credential login.
3. **Load inputs.** Steps take values from Gherkin parameters, and from CSV files as data-driven runs are introduced.
4. **Generate business records.** New tenant records are built from the input values plus a unique run value. The lead flow adds a timestamp and random suffix, and the project flow adds a timestamp to the project name.
5. **Verify and identify.** The scenario asserts the saved outcome and captures the application document ID where later inspection or cleanup needs it.
6. **Retain or remove.** Only intentional reference data stays in the tenant. Recorded document IDs and the `TS01-` prefix identify test-created records for approved cleanup, and reports stay as run artifacts.

---

## 5. Data-Driven Testing (planned)

The scenario steps already accept their data as parameters. For example, the lead-form step takes first name, organisation, email and status as `{string}` values. That makes the same steps ready to run many data sets, with CSV as the planned source.

### 5.1 Approach

| Option | Best for | How it works |
| --- | --- | --- |
| **Scenario Outline with an Examples table** | A handful of readable cases | Values sit in the feature file, and one scenario runs once per row. |
| **CSV file** | Larger or shared data sets | A small reader utility loads the rows and feeds each one into the same steps. |

CSV is the direction for volume and reuse, and Examples tables suit short lists where the data is part of the story.

### 5.2 Suggested layout

```
src/test/resources/testdata/
  uploads/sample_dataset.csv     existing upload fixture
  csv/leads.csv                  planned lead data set
  csv/projects.csv               planned project data set
```

Example `csv/leads.csv`:

```csv
caseId,firstName,organization,email,status
lead-valid,TS01-Asha,TS01-Asha-Org,ts01.asha@example.com,Lead
lead-dnc,TS01-DNC,TS01-DNC-Org,ts01.dnc@example.com,Do Not Contact
```

### 5.3 CSV standards

- The first row is a header, with one row per test case and a `caseId` column that names the case in reports.
- Values are base values. The unique timestamp and random suffix are added at run time by the step, so the CSV stays stable and repeated runs stay unique.
- Names keep the `TS01-` prefix.
- Validation cases add an expected-result column, for example `expectedMessage`, and change only the value under test.
- Files use UTF-8, are small enough to review at a glance, and are separated by intent (valid, boundary, invalid) when that helps.
- Credentials, the base URL, production customer data and personal information stay out of CSV files.

---

## 6. Data Design Standards

### 6.1 Tenant records

- Use the `TS01-` prefix for every record this suite creates, so it is searchable and attributable.
- Add a unique run or scenario suffix to record names, organisations and other uniqueness-constrained values.
- Keep values within application field limits, and use syntactically valid values for positive-path scenarios.
- For validation scenarios, change only the value under test and keep the other required fields valid.
- Capture the generated identifier with the human-readable key. The lead output format is `<doc id> | <organization>`, appended to `test-output/ts01-created-docs.txt` behind a lock.
- Draft-only rule for CI: documents such as Sales Invoices and Journal Entries are saved as drafts and never submitted.

### 6.2 Fixture data

- Keep upload fixtures deterministic and small.
- Preserve the required headers, column order and encoding.
- Give each row a stable identifier so an upload result traces back to its input.
- Use clear filenames that show the scenario purpose when several variants exist.

### 6.3 Shared reference data and dependencies

- Scenario-created data is preferred for workflows that change records. Shared tenant reference data is used when it is intentionally managed and stable.
- Some journeys intentionally span scenarios. The current one is **`TS01-M1-02` creating the lead and `TS01-M1-05` updating its status**:

| Item | Detail |
| --- | --- |
| Creator | `TS01-M1-02` |
| Consumer | `TS01-M1-05` |
| Shared key | The organisation name (`TS01-M1-02-<timestamp>-<random>`), passed from the feature file |
| Order | Run the creator first, then the consumer |
| Owner | The lead stays in the tenant as managed reference data, and its ID is in `test-output/ts01-created-docs.txt` |

  If that lead is removed from the tenant, run `TS01-M1-02` again and update the organisation name in the `TS01-M1-05` scenario.
- Read-only smoke checks, such as the invoicing workspace check, need no test records.

---

## 7. Tags and Data Policy

| Tag | Data meaning |
| --- | --- |
| `@createData` | The scenario creates or changes tenant data. |
| `@smoke` and `@ci` | Scenarios considered for routine runs. The CI profile includes only scenarios whose data behaviour suits its target tenant. |
| `@local-only` | Reserved for controlled local or designated non-CI runs. |
| `@TS01-M<module>-<nn>` | Identifies the scenario and connects generated data to its workflow. |
| `@tactiveLogin`, `@loginStorage`, `@freshLogin` | Login and session setup or behaviour. Browser storage remains separate, sensitive state. |

Select the target environment and check the profile against the scenarios' data behaviour before a run. Tags mark intent, and cleanup is handled by the process in section 9.

---

## 8. Isolation and Repeatability

- Every run has a recognisable identifier, used consistently for related records and output files.
- Scenarios use generated, unique data for repeated execution. The managed `TS01-M1-02` lead is the intentional reuse.
- Inputs are explicit in Gherkin or a controlled CSV, and only values that must be unique are generated.
- Browser storage, downloads, screenshots, reports and identifier logs are kept apart from committed fixtures.
- For parallel execution, use scenario-unique data keys and run-specific output paths, and keep the shared output file synchronised as it is today.
- Make data setup observable by reporting the generated key and document ID.

---

## 9. Cleanup and Retention

Tenant changes are identifiable, limited to the designated test tenant and traceable to a scenario or run. Recorded document IDs and `TS01-` values identify test-created data, and cleanup runs through an approved application or environment process, with ownership and retention set for each shared environment.

| Artifact | Location | Notes |
| --- | --- | --- |
| Stable inputs | `src/test/resources/testdata/` | Version-controlled |
| Build, report and screenshot output | `target/` | Rebuilt by `mvn clean` |
| Lead document references | `test-output/ts01-created-docs.txt` | Kept between builds, and retained or archived per the run policy |
| `auth.json` and environment config | Project root and `env/` | Protected sensitive state, with access limited to the test run |

---

## 10. Data Ownership by Workflow

| Workflow | Data inputs | Data produced or verified |
| --- | --- | --- |
| Login / session | `TACTIVE_BASE_URL`, `TACTIVE_USERNAME`, `TACTIVE_PASSWORD` | Authenticated browser storage in `auth.json`, and dashboard verification |
| Lead management (M1) | First name, organisation, email and status, from Gherkin or the planned `leads.csv` | Unique Lead, document ID, list visibility, status change, and validation outcomes |
| Project management (M2) | Project name (`TS01-EST-001` base), status and company (Cloud ERP (Demo)) | Unique project name and project save/view outcome |
| Invoicing (M4) | Environment configuration and an authenticated session | Invoicing navigation and Sales Invoice link verification. Later transaction scenarios use selected reference records and draft-safe data |
| Data import | `uploads/sample_dataset.csv` | Upload completion confirmation |

---

## 11. Review Checklist

For each new or changed data-driven scenario, confirm that:

- The target environment and session state are explicit, and the base URL, email and password come from the environment or the git-ignored file.
- Data is classified as fixture, CSV input, generated tenant data, shared reference data or run evidence.
- Created values are unique and follow the `TS01-` convention.
- Dependencies, execution order and cleanup ownership are documented when records are reused.
- Positive and validation cases isolate the rule being tested.
- Secrets and session data are absent from committed files and run logs.
- The output location and retention are known.
