@TS01 @M4 @invoicing @loginStorage
Feature: TS01-M4 Invoicing and Accounting management
  As an Accounting user
  I want to verify invoicing workflows and accounting balances
  So that financial entries are processed accurately

  @TS01-M4-01 @smoke @ci
  Scenario: TS01-M4-01 Invoicing smoke — Open invoicing module and verify links
    Given I navigate to the invoicing desk page
    Then I should assert that the Sales Invoice links are visible

  # @TS01-M4-02 @negative
  # Scenario: TS01-M4-02 Sales Invoice without Customer
  #   Given I open the new sales invoice form
  #   When I leave the Customer field empty and click Save
  #   Then the form should fail validation and display an error

  # @TS01-M4-03 @smoke @createData
  # Scenario: TS01-M4-03 Sales Invoice draft creation
  #   Given I open the new sales invoice form
  #   When I select a Customer, set Posting Date to today, and add required Items
  #   And I save the sales invoice
  #   Then the sales invoice should be saved as a draft with status "Not Submitted"

  # @TS01-M4-04 @negative
  # Scenario: TS01-M4-04 Journal Entry Dr = Cr mismatch validation
  #   Given I open the new journal entry form
  #   When I add two accounting rows where Debit does not equal Credit and click Save
  #   Then the form should display a difference error and block the save operation

  # @TS01-M4-05 @smoke @ci
  # Scenario: TS01-M4-05 GST workspace reachable check
  #   Given I navigate to the GST India desk page
  #   Then the GST workspace page should load successfully
