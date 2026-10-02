@TS01 @M1 @lead @loginStorage
Feature: TS01-M1 Lead creation and validation
  As a CRM user
  I want to create, validate, and filter Leads
  So that lead data is captured correctly

  # Session comes from saved storage state (@loginStorage), so no UI login step is needed.
  # All test data uses the "TS01-" prefix so it is easy to find, filter and clean up.

 @TS01-M1-01 @smoke @ci
  Scenario: TS01-M1-01 Open New Lead form and verify defaults
    Given i went on to click add lead
    Then assert Series CRM-LEAD-.YYYY.- and Status default.

 @TS01-M1-02 @smoke @ci @createData
  Scenario: TS01-M1-02 Create Lead (happy path)
    Given i went on to click add lead
    When I fill the lead form with first name "TS01-Fname", organization "TS01-Org", email "ts01.lead@example.com" and status "Lead"
    And I save the lead
    And I record the created lead document ID
    Then the lead should be saved successfully
    And the lead "TS01-Org" should appear in the lead list


   @TS01-M1-03 @local-only @negative
  Scenario: TS01-M1-03 Save without First Name
    Given I open the new lead form for the mandatory check
    When I leave First Name empty and click Save
    Then I should see the mandatory fields dialog
    When I close the mandatory fields dialog
    Then the First Name label should show the mandatory asterisk


  @TS01-M1-04 @local-only @negative
  Scenario: TS01-M1-04 Invalid email
    Given I open the new lead form for the mandatory check
    When I fill the lead form with first name "TS01-BadEmail", organization "TS01-BadEmailOrg", email "SDG.q44q2" and status "Lead"
    And I save the lead
    Then I should see the mandatory fields dialog
    And the dialog should report "SDG.q44q2" is not a valid Email Address
    When I close the mandatory fields dialog
    Then the lead should not be saved



 @TS01-M1-05 @local-only
Scenario: TS01-M1-05 Set Status to Do Not Contact
  Given I am on the lead list page
  When I open the lead "TS01-M1-02-1790670385399" from the list
  And I change the lead status to "Do Not Contact" and save
  Then the lead list should show status "Do Not Contact" for "TS01-M1-02-1790670385399"

  # ---- Enable these once their steps are written ----

  # @TS01-M1-06 @smoke @ci @createData
  # Scenario: TS01-M1-06 Filter Lead list by Organization Name
  #   When i went on to click add lead navgiated
  #   And I fill the lead form with first name "TS01-Filter", organization "TS01-FilterOrg", email "ts01.filter@example.com" and status "Lead"
  #   And I save the lead
  #   And I record the created lead document ID
  #   And I open the lead list
  #   And I filter the lead list by organization name containing "TS01-"
  #   Then the filtered list should contain the lead created in this scenario