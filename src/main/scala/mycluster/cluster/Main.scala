package mycluster.cluster

import scala.io.StdIn

object Main extends App {


  def conf(port: Int) = {
    val seedNodes = Seq(ClusterConfig.defaultHost -> 2551, ClusterConfig.defaultHost -> 2552)
    ClusterConfig(port, seedNodes)
  }

  def run(port: Int) = {

    val c = conf(port)

    val cluster = MyCluster(c)
    println("RUNNING ON " + port)
    () => cluster.terminate
  }

  val kill = run(args.headOption.map(_.toInt).getOrElse(2551))
  StdIn.readLine()
  kill()

}
