@tactiveLogin
Feature: Tactive Login Functionality

  # Scenario 1: Forces a clean context (no auth.json) so this actually
  # exercises the real login form, regardless of any saved session.
  @freshLogin
  Scenario: Successful login with valid credentials
    Given I am on the Tactive login page
    When I enter valid username
    And I enter valid password
    And I click the Continue button
    Then I should see the Title

  # Scenario 2: No login step needed — DriverFactory already loads the
  # saved session from auth.json into the browser context before this
  # scenario starts. Reaching the dashboard here is the proof it worked.
  Scenario: Bypass login on a subsequent test execution
    Given I am on the Tactive login page
    Then I should see the Title
