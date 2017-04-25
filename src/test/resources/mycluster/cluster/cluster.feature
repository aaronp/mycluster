Feature: Connect to the cluster

  Scenario: Start up a cluster

    Given I start node A with configuration
    """
    my.cluster {
      port : 2600
      roles : ["foo"]
      name : "test"
      seed-nodes = [
        "akka.tcp://test@127.0.0.1:2600",
        "akka.tcp://test@127.0.0.1:2601"]
    }
    """

    And I start node B with configuration
    """
    my.cluster {
      port : 2601
      roles : ["bar"]
      name : "test"
      seed-nodes = [
        "akka.tcp://test@127.0.0.1:2600",
        "akka.tcp://test@127.0.0.1:2601"]
    }
    """

    Then node A member state should eventually contain members A and B

  Scenario: Start a cluster with multiple nodes

    Given I start node A on port 2551 with role foo
    And I start node B on port 2552 with role bar
    And I start node C on port 2553 with role fizz
    When I start node D on port 2554 with role fizz
    Then node A member state should eventually contain members A, B, C and D
    Then node C member state should eventually contain members A, B, C and D
    Then node D member state should eventually contain members A, B, C and D

  Scenario: Start a cluster client which doesn't join the cluster

    Given I start node A on port 2551 with role foo
    And I start node B on port 2552 with role bar
    Then node A member state should eventually contain members A and B
    When I connect cluster client C to node A
    Then client C can send a message
    And node A member state should eventually contain members A and B
    And node B member state should eventually contain members A and B

