# PlaywrightJavaTactive

UI automation suite for the Tactive web application (Frappe / ERPNext), built with **Java, Maven, JUnit Platform, Cucumber and Playwright for Java**.

This README covers setup and running on **Windows** with **VS Code and Maven**. The project is a Java project, so it needs no npm or Node.js.

---

## 1. Prerequisites

| Tool | Version | Check it with |
| --- | --- | --- |
| Git | Current | `git --version` |
| JDK | 25 (the project compiles with release 25) | `java -version` |
| Maven | 3.9 or newer | `mvn -version` |
| VS Code | Current | |

Set `JAVA_HOME` to the JDK folder and add `%JAVA_HOME%\bin` and the Maven `bin` folder to `PATH`. Open a new terminal afterwards and confirm that `java -version` and `mvn -version` both work.

### VS Code extensions

- **Extension Pack for Java** (Microsoft), which includes Maven for Java and the Test Runner
- **Cucumber (Gherkin) Full Support**, for feature file highlighting and step navigation

---

## 2. Get the Project

```powershell
git clone <repository-url>
cd PlaywrightJavaTactive
code .
```

VS Code imports the Maven project on first open. Wait until the Java status bar item shows it is ready.

---

## 3. Configure the Environment

The suite reads the target URL and login details from a git-ignored file: `env/.env.stage`.

Create it with these three keys:

```dotenv
TACTIVE_BASE_URL=https://<tenant>.frappe.cloud
TACTIVE_USERNAME=<login email>
TACTIVE_PASSWORD=<password>
```

- Give the base URL **without** a trailing path. The tests add `/desk/...` themselves.
- **Never commit** this file or `auth.json`. Both are git-ignored, so check that `git status` does not list them.
- In CI, provide the same three keys as CI/CD variables.

See [`docs/test-data-strategy.md`](docs/test-data-strategy.md) for details.

---

## 4. Install the Browsers

Playwright for Java downloads its browsers on the first run, so this step is optional. To install them ahead of time (Chromium is the standard browser):

```powershell
mvn exec:java -e "-Dexec.mainClass=com.microsoft.playwright.CLI" "-Dexec.args=install chromium"
```

---

## 5. Run the Tests

Run every command from the project root, in the VS Code terminal (PowerShell).

> **PowerShell tip:** wrap the whole `-D` argument in double quotes, exactly as shown. Without the quotes PowerShell splits the argument at the dot and Maven reports `LifecyclePhaseNotFoundException`.

### Run one scenario

```powershell
mvn clean test "-Dcucumber.filter.tags=@TS01-M1-05"
```

### Common runs

| Goal | Command |
| --- | --- |
| Create a lead, then change its status | `mvn test "-Dcucumber.filter.tags=@TS01-M1-02 or @TS01-M1-05"` |
| One module | `mvn clean test "-Dcucumber.filter.tags=@M1"` |
| Smoke (CI-safe) checks | `mvn clean test "-Dcucumber.filter.tags=@smoke and not @local-only"` |
| Data-creating scenarios | `mvn clean test "-Dcucumber.filter.tags=@createData"` |
| Validation scenarios | `mvn clean test "-Dcucumber.filter.tags=@validation"` |
| Login flow | `mvn clean test "-Dcucumber.filter.tags=@tactiveLogin"` |
| Everything | `mvn clean test` |

The full command catalogue, tag strategy and scenario map are in [`docs/execution-strategy.md`](docs/execution-strategy.md).

### Run from VS Code

1. Open the **Testing** panel (beaker icon) and run `TestRunner` (`com.tactive.runners`).
2. Use the terminal commands above whenever you need a specific tag filter.

### Order for dependent scenarios

`TS01-M1-05` works on the lead created by `TS01-M1-02`. On a new tenant, run M1-02 first, then M1-05.

### First run and login

On the first run the suite logs in once and saves the browser session to `auth.json`. Later runs reuse it. If the session expires, delete `auth.json` or run the `@tactiveLogin` scenario to refresh it.

---

## 6. Reports and Output

| Output | Location |
| --- | --- |
| Cucumber HTML report | `target/cucumber-reports/cucumber.html` |
| Surefire results | `target/surefire-reports/` |
| Failure screenshots | `target/screenshots/` |
| Created lead document IDs | `test-output/ts01-created-docs.txt` (`<doc id> | <organization>`) |

Open the HTML report in a browser after a run:

```powershell
start target\cucumber-reports\cucumber.html
```

Test data created in the tenant uses the `TS01-` prefix, so it is easy to find.

---

## 7. Project Layout

| Package or folder | Contents |
| --- | --- |
| `com.tactive.runners` | `TestRunner`, the JUnit Platform Cucumber suite |
| `com.tactive.stepDefinitions` | Step definitions by workflow |
| `com.tactive.pages` | Page objects for login, leads, projects and invoicing |
| `com.tactive.factory` | `DriverFactory`, which owns the browser, context and page |
| `com.tactive.hooks` | `AuthHooks` (suite-level login) and `Hooks` (scenario browser lifecycle and failure screenshots) |
| `com.tactive.utils` | `AuthStateManager` and `config/ConfigReader` |
| `com.tactive.baseLocator` | `basePage`, the shared locator helper base, ready for adoption |
| `src/test/resources/features` | Gherkin feature files |
| `src/test/resources/testdata` | Fixtures, such as `uploads/sample_dataset.csv` |
| `env/` | Environment file (git-ignored) |
| `docs/` | Project documentation |

---

## 8. Documentation

| Document | Covers |
| --- | --- |
| [`docs/architecture.md`](docs/architecture.md) | Components, lifecycle, Lead workflow, roadmap |
| [`docs/test-automation-strategy.md`](docs/test-automation-strategy.md) | Coverage, scenario design, execution profiles |
| [`docs/execution-strategy.md`](docs/execution-strategy.md) | Run commands, tags and combinations |
| [`docs/test-data-strategy.md`](docs/test-data-strategy.md) | Environment, credentials, data-driven CSV plan |
| [`docs/locator-strategy.md`](docs/locator-strategy.md) | Frappe data-attribute locators and page objects |

---

## 9. Git Workflow

```powershell
git checkout -b feature/<short-name>
git add .
git commit -m "TS01-M1-06: <what changed>"
git push -u origin feature/<short-name>
```

- Use the scenario ID in commit messages, for example `TS01-M2-02: add Costing tab scenario`.
- Before committing, run `git status` and confirm that `env/.env.*`, `auth.json`, `target/` and any credentials are not staged.

---

## 10. Troubleshooting

| Symptom | Likely cause | Fix |
| --- | --- | --- |
| `LifecyclePhaseNotFoundException` | PowerShell split the `-D` argument | Quote it: `"-Dcucumber.filter.tags=@TS01-M1-05"` |
| `invalid target release: 25` or a compiler error | JDK older than 25 | Install JDK 25 and point `JAVA_HOME` to it |
| Tests run, most are skipped | The tag filter excluded them | Expected, since only the tagged scenarios run |
| A scenario is not picked up | The tag is missing, or `TestRunner` sets its own tags | Check the feature file and `TestRunner.java` |
| Redirected to the login page | The saved session expired | Delete `auth.json` and rerun, or run `@tactiveLogin` |
| `TS01-M1-05` cannot find the lead | The `TS01-M1-02` lead is not in the tenant | Run `@TS01-M1-02` and update the organisation name in the M1-05 scenario |
| Browser not found | Browsers not downloaded yet | Run the install command in section 4 |
| Blank base URL or credentials | `env/.env.stage` missing or misnamed | Recreate it as in section 3 |
