# Execution Strategy

How to run the TS01 Playwright + Cucumber suite, and how the tags are organised.

Location: `docs/execution-strategy.md`
Related: `test-data-strategy.md`, `locator-strategy.md`

---

## 1. Quick Start

Run everything from the project root (`PlaywrightJavaTactive`).

```powershell
# Run one scenario
mvn clean test "-Dcucumber.filter.tags=@TS01-M1-05"

# Run the CI smoke set
mvn clean test "-Dcucumber.filter.tags=@smoke"

# Run a whole module
mvn clean test "-Dcucumber.filter.tags=@M1"
```

> **PowerShell users:** always wrap the whole `-D` argument in double quotes, exactly as above.
> Without the quotes PowerShell splits at the dot and Maven fails with
> `LifecyclePhaseNotFoundException`.
> On Linux/macOS shells and in GitLab CI the same quoted form works.

---

## 2. Tag Strategy

Every scenario carries tags from four groups, so it can be selected by ID, by module, by suite, or by data behaviour.

### 2.1 Tag groups

| Group | Tag | Purpose | Status |
|---|---|---|---|
| **Scenario ID** | `@TS01-M1-01` ... `@TS01-M4-05` | Run exactly one scenario. One unique ID per scenario. | In use |
| **Module** | `@M1` `@M2` `@M3` `@M4` | Run a whole module. Put it once at Feature level. | Recommended |
| **Suite** | `@smoke` | Safe, fast, read-only checks that run in CI. | In use |
| | `@local-only` | Never runs in CI. Needs local state or creates data. | In use |
| **Data** | `@createData` | Scenario creates `TS01-` records in the tenant. | In use |
| **Setup** | `@tactiveLogin` | Login feature that produces the saved storage state. | In use |
| **Type** | `@validation` | Negative or mandatory-field checks (empty field, invalid value). | Recommended |

"Recommended" tags are not required for the suite to work. Add them to the feature files when you want module-level and type-level runs.

### 2.2 Rules

1. **Every scenario has exactly one ID tag** in the form `@TS01-M<module>-<nn>`.
2. **Every scenario is either `@smoke` or `@local-only`**, never both. This keeps the CI selection unambiguous.
3. **Anything that creates data is `@createData`** and is also `@local-only`, so CI never writes to the tenant.
4. **Module tags go on the `Feature:` line**, so they apply to every scenario in that file.
5. **Never click Submit in CI** (Sales Invoice, Journal Entry, and so on). Drafts only.
6. All test data is prefixed `TS01-` so it can be found and cleaned up.

### 2.3 Example

```gherkin
@M1
Feature: Lead management

  @TS01-M1-05 @local-only @createData
  Scenario: TS01-M1-05 Set Status to Do Not Contact
    Given I am on the lead list page
    When I open the lead "TS01-M1-02-1790670385399" from the list
    And I change the lead status to "Do Not Contact" and save
    Then the lead list should show status "Do Not Contact" for "TS01-M1-02-1790670385399"
```

### 2.4 Tag expression syntax

| Operator | Meaning | Example |
|---|---|---|
| `and` | Both tags present | `@M1 and @smoke` |
| `or` | Either tag present | `@TS01-M1-02 or @TS01-M1-05` |
| `not` | Exclude a tag | `@M1 and not @local-only` |
| `( )` | Grouping | `(@M1 or @M2) and not @createData` |

---

## 3. Command Reference

### 3.1 By scenario

```powershell
mvn clean test "-Dcucumber.filter.tags=@TS01-M1-02"
mvn clean test "-Dcucumber.filter.tags=@TS01-M1-05"
```

### 3.2 By combination of scenarios

```powershell
# Create the lead, then change its status
mvn clean test "-Dcucumber.filter.tags=@TS01-M1-02 or @TS01-M1-05"

# All Module 1 validation scenarios
mvn clean test "-Dcucumber.filter.tags=@TS01-M1-03 or @TS01-M1-04"
```

### 3.3 By module

```powershell
mvn clean test "-Dcucumber.filter.tags=@M1"
mvn clean test "-Dcucumber.filter.tags=@M2"
mvn clean test "-Dcucumber.filter.tags=@M3"
mvn clean test "-Dcucumber.filter.tags=@M4"

# Several modules
mvn clean test "-Dcucumber.filter.tags=@M1 or @M2"
```

### 3.4 By suite

```powershell
# CI selection: safe checks only
mvn clean test "-Dcucumber.filter.tags=@smoke and not @local-only"

# Local full run
mvn clean test "-Dcucumber.filter.tags=@local-only"

# Everything that writes data to the tenant
mvn clean test "-Dcucumber.filter.tags=@createData"

# Everything except data-creating scenarios
mvn clean test "-Dcucumber.filter.tags=not @createData"
```

### 3.5 By type

```powershell
# Only negative / mandatory-field checks
mvn clean test "-Dcucumber.filter.tags=@validation"

# Module 1 validation only
mvn clean test "-Dcucumber.filter.tags=@M1 and @validation"
```

### 3.6 By feature file

```powershell
mvn clean test "-Dcucumber.features=src/test/resources/features/LeadsOppertunity/leadCRUD.feature"

# Feature file plus tag filter
mvn clean test "-Dcucumber.features=src/test/resources/features/LeadsOppertunity/leadCRUD.feature" "-Dcucumber.filter.tags=@TS01-M1-05"
```

### 3.7 Login / setup

```powershell
# Refresh the saved storage state if it has expired
mvn clean test "-Dcucumber.filter.tags=@tactiveLogin"
```

---

## 4. Scenario Map and Status

### Module 1: Business development / lead tracking

| ID | Scenario | Status |
|---|---|---|
| `@TS01-M1-01` | Login and open New Lead, assert Series and Status default | Done |
| `@TS01-M1-02` | Create Lead (happy path), appears in list | Done |
| `@TS01-M1-03` | Save without First Name, mandatory validation | Done |
| `@TS01-M1-04` | Invalid email is not accepted | Done |
| `@TS01-M1-05` | Set Status to Do Not Contact | Done |
| `@TS01-M1-06` | Filter on list by `TS01-` | Covered in `@TS01-M1-02` (list check after creation) |
| `@TS01-M1-07` | Lead to Opportunity | Done |

Module 1 is complete. The check that a `TS01-` lead is returned by the list runs as part of `@TS01-M1-02`.

### Modules 2 to 4: foundation in place, remaining planned for later

| Module | Scenarios | Status | Suggested smoke candidates |
|---|---|---|---|
| **M2** Project estimation | `@TS01-M2-01` to `@TS01-M2-06` | `@TS01-M2-01` done, `M2-02` to `M2-06` planned | `@TS01-M2-06` Costing tab visible |
| **M3** Procurement, inventory | `@TS01-M3-01` to `@TS01-M3-05` | Planned for later | `@TS01-M3-05` Buying workspace |
| **M4** Accounts | `@TS01-M4-01` to `@TS01-M4-05` | `@TS01-M4-01` done, `M4-02` to `M4-05` planned | `@TS01-M4-01` Invoicing (done), `@TS01-M4-05` GST workspace |

The commands in section 3 work for these as soon as the tags are added to the feature files.

---

## 5. Order and Dependencies

Some scenarios reuse data created by an earlier one.

| Scenario | Depends on | Notes |
|---|---|---|
| `@TS01-M1-05` | Lead from `@TS01-M1-02` | The organization name is passed from the feature file. If the lead no longer exists, run M1-02 once and update the name. |
| `@TS01-M1-07` | A saved Lead | Party is the Lead. |
| `@TS01-M3-01`, `@TS01-M3-04` | Project `TS01-EST-001` from `@TS01-M2-01` | Only if the Project field exists on the form. |
| `@TS01-M4-03` | Customer, if created in the `TS01-` flow | Otherwise pick an existing Customer. |

When parallel execution is on, dependent scenarios can start before the scenario they depend on has finished. Run the creator first, then the dependent one.

```powershell
# Step 1: create
mvn clean test "-Dcucumber.filter.tags=@TS01-M1-02"

# Step 2: reuse
mvn test "-Dcucumber.filter.tags=@TS01-M1-05 or @TS01-M1-07"
```

---

## 6. CI (GitLab)

CI runs the safe set only:

```yaml
script:
  - mvn clean test "-Dcucumber.filter.tags=@smoke and not @local-only"
```

Everything tagged `@local-only` or `@createData` is run manually.

---

## 7. Environment and Output

- **Configuration:** base URL and credentials come from `env/.env.stage` (`TACTIVE_BASE_URL`, `TACTIVE_USERNAME`, `TACTIVE_PASSWORD`) through `ConfigReader`.
- **Created document IDs:** each run appends to `test-output/ts01-created-docs.txt` (`<doc id> | <organization>`), which is the list to use for cleanup.
- **Failure screenshots:** saved under `target/screenshots/`.
- **Clean builds:** `mvn clean` removes `target/`, but not `test-output/`.

---

## 8. Troubleshooting

| Symptom | Likely cause | Fix |
|---|---|---|
| `LifecyclePhaseNotFoundException` | PowerShell split the `-D` argument | Wrap it in double quotes: `"-Dcucumber.filter.tags=@TS01-M1-05"` |
| Tests run: N, Skipped: N-1 | Tag filter excluded the other scenarios | Expected, not a failure |
| Scenario not picked up | Tag not on the scenario, or `TestRunner` hardcodes tags | Check the feature file and `TestRunner.java` |
| Redirected to login | Saved storage state expired | Run `@tactiveLogin` |
| M1-05 cannot find the lead | The `TS01-M1-02-...` org no longer exists in the tenant | Run M1-02 and update the org name in the feature file |
