@TS01 @M2 @project @loginStorage
Feature: TS01-M2 Project and Quotation management
  As an ERP user
  I want to manage Projects and Quotations
  So that project costing and quotation metrics are captured correctly

  @TS01-M2-01 @smoke @ci @createData
  Scenario: TS01-M2-01 New Project — Details — Open New Project
    Given I open the new project form
    When I fill the project form with name "TS01-EST-001", status "Open", and company "Cloud ERP (Demo)"
    And I save the project
    Then the project should be saved successfully
    And the company name "Cloud ERP (Demo)" should be visible with view difference type

  # @smoke @ci @TS01-M2-02
  # Scenario: TS01 M2 02 Costing tab data retention
  #   Given I open the project "TS01 EST 001"
  #   When I navigate to the Costing tab
  #   And I set the Estimated Cost to 100000 and save
  #   And I reload the project page
  #   Then the Estimated Cost value 100000 should be retained after reload

  # @local @only @negative @TS01-M2-03
  # Scenario: TS01 M2 03 Project Name required validation
  #   Given I open the new project form
  #   When I leave the Project Name empty and click Save
  #   Then I should see the project validation error

  # @smoke @createData @TS01-M2-04
  # Scenario: TS01 M2 04 Quotation with item
  #   Given I navigate to the Quotation URL "https://frappe.cloud" and click new quotation
  #   When I select Quotation To, set Date to today, and add at least one Item row
  #   And I save the quotation as draft with title prefix TS01
  #   Then the quotation should be saved successfully

  # @negative @TS01-M2-05
  # Scenario: TS01 M2 05 Quotation without Items validation
  #   Given I navigate to the Quotation URL "https://frappe.cloud" and click new quotation
  #   When I leave the Items table empty and click Save
  #   Then the form should fail validation and display an error

  # @smoke @ci @TS01-M2-06
  # Scenario: TS01 M2 06 Costing tab visible smoke check
  #   Given I open the new project form
  #   Then I should assert that the Details and Costing tabs are visible
