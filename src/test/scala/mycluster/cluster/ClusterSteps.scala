package mycluster.cluster


import com.typesafe.config.ConfigFactory
import cucumber.api.scala.{EN, ScalaDsl}
import org.scalatest.Matchers
import org.scalatest.concurrent.{Eventually, ScalaFutures}
import org.scalatest.time.{Millis, Seconds, Span}

class ClusterSteps extends ScalaDsl with EN with Matchers with ScalaFutures with Eventually {

  var state = ClusterTestState()

  Given("""^I start node (.*) with configuration$""") { (name: String, config: String) =>
    state = state.nodeWithConfig(name, config)
  }

  Given("""^I start node (.*) on port (\d+) with roles? (.*)$""") { (name: String, port: Int, roleString: String) =>
    state = state.nodeWithPortAndRoles(name, port, asSet(roleString))
  }
  Given("""^I connect cluster client (.*) to node (.*)$""") { (clientName: String, clusterNodeName: String) =>
    state = state.connectClientToNode(clientName, clusterNodeName)
  }
  Then("^client (.*) can send a message$") { clientName: String =>
    val resp = state.sendMessage(clientName, "testing 123")
    resp should include("testing 123")
  }

  When("""^node (.*) member state should eventually contain members (.*)$""") { (name: String, nodesString: String) =>
    val expectedNames: Set[String] = asSet(nodesString)

    val c = state.clusterByName(name)
    eventually {
      val members = c.members(Set.empty).futureValue
      val ports = members.flatMap(_.address.port)
      val namesKnownToThisMember = ports.map { port =>
        state.namesByPort.getOrElse(port, sys.error("Found a cluster member on a different port than what was started in this test!"))
      }
      namesKnownToThisMember shouldBe expectedNames
    }
  }


  After { _ =>
    state = state.reset().futureValue
  }

  private def asSet(str: String) = str.replaceAllLiterally(" and ", ",").split(",", -1).map(_.trim).toSet

  implicit override val patienceConfig =
    PatienceConfig(timeout = scaled(Span(15, Seconds)), interval = scaled(Span(150, Millis)))
}
