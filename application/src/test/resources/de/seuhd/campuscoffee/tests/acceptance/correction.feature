Feature: Correcting a user's coffee count
  An admin corrects a wrong coffee count at any time and explains the correction in a note.

  Scenario: An admin corrects a coffee count
    Given the user "maxmustermann" has 3 coffees
    When the admin "jane_doe" corrects the coffee count of the user "maxmustermann" to 2 with the note "Double tap on 16.11."
    Then the correction succeeds
    And the coffee count of the user "maxmustermann" is 2
    And the corrections of the user "maxmustermann" are:
      | countBefore | countAfter | note                 | adminLoginName |
      | 3           | 2          | Double tap on 16.11. | jane_doe       |

  Scenario Outline: A rejected correction leaves the coffee count unchanged
    Given the user "maxmustermann" has 3 coffees
    When the admin "<admin>" corrects the coffee count of the user "maxmustermann" to <count> with the note <note>
    Then the correction is rejected with <status>
    And the coffee count of the user "maxmustermann" is 3
    And the corrections of the user "maxmustermann" are:
      | countBefore | countAfter | note | adminLoginName |

    Examples:
      | admin         | count | note                   | status |
      | maxmustermann | 2     | "Double tap on 16.11." | 403    |
      | jane_doe      | 2     | ""                     | 400    |
      | jane_doe      | 2     | "   "                  | 400    |
      | jane_doe      | -1    | "Double tap on 16.11." | 400    |

  Scenario: An undo after a correction removes the newest coffee
    Given the user "maxmustermann" has 2 coffees, the last one added at 09:00:00
    And the user adds a coffee at 10:00:00
    When the admin "jane_doe" corrects the coffee count of the user "maxmustermann" to 2 with the note "Double tap on 16.11."
    Then the correction succeeds
    When the user undoes the coffee at 10:03:00
    Then the response status is 409
    And the coffee count is 2
