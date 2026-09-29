@only-upload
Feature: Data Import Management

  Scenario: Successfully upload automation test data suite
    Given the user opens the portal page "https://herokuapp.com"
    When the user selects the test data file "src/test/resources/testdata/uploads/sample_dataset.csv"
    And confirms the data submission
    Then the portal should confirm completion with "File Uploaded!"
