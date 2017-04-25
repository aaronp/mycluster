package mycluster.cluster.client

import scala.concurrent.Future

object Main extends App {

  def startClient = {

    val c = Client(ClientConfig())

    println(c.sendSync("Hello world!"))
    val all =c.membersSync()
    println("all members: " + all)
    println("foo members: " + c.membersSync("foo"))

  }

  def startServer(port: Int) = mycluster.cluster.Main.run(port)

  val first = startServer(2551)
  val second = startServer(2552)
  println("starting client")
  startClient

  println("Done!")
  first()
  second()

}
