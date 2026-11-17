Feature: User balance and expenses
  A user records an expense that the user paid for the group, and the group owes the money back to the user.

  Scenario: A user records an expense
    Given the user with login name "maxmustermann"
    When the user records an expense of 15.00 euro
    Then the request succeeds and the balance is 15.00 euro

  Scenario: A user records two expenses
    Given the user with login name "maxmustermann"
    When the user records an expense of 2.50 euro
    And the user records an expense of 10.00 euro
    Then the request succeeds and the balance is 12.50 euro

  Scenario: A user records an expense of 0.00 euro
    Given the user with login name "maxmustermann"
    When the user records an expense of 0.00 euro
    Then the request fails with 400 and the balance stays 0.00 euro
