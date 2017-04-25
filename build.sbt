enablePlugins(CucumberPlugin)

name := "mycluster"

version := "0.0.1"

scalaVersion := "2.11.8"

javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature", "-target:jvm-1.8")

mainClass in(Compile, run) := Some("mycluster.cluster.Main")

CucumberPlugin.glue := "classpath:mycluster.cluster"

CucumberPlugin.features := List("classpath:mycluster.cluster",
  "src/test/resources/mycluster/cluster")

//https://github.com/typesafehub/scala-logging
val logging = Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  "ch.qos.logback" % "logback-classic" % "1.1.7")

val cucumber = Seq("core", "jvm", "junit").map(suffix =>
  "info.cukes" % s"cucumber-$suffix" % "1.2.5" % "test") :+ ("info.cukes" %% "cucumber-scala" % "1.2.5" % "test")

val testDependencies = Seq(
  "org.scalactic" %% "scalactic" % "3.0.1" % "test",
  "org.scalatest" %% "scalatest" % "3.0.1" % ("test->*"),
  "junit" % "junit" % "4.12" % "test")

val circe = Seq("core", "generic", "parser", "optics").map(name => "io.circe" %% s"circe-$name" % "0.7.0")

val akkaHttp = List("http", "http-core", "http-testkit").map { suffix =>
  "com.typesafe.akka" %% s"akka-$suffix" % "10.0.5"
} :+ ("de.heikoseeberger" %% "akka-http-circe" % "1.14.0")

val akka = akkaHttp ++ List("cluster", "cluster-metrics", "cluster-tools").map { suffix =>
  "com.typesafe.akka" %% s"akka-$suffix" % "2.5.0"
}


libraryDependencies ++= logging ++ cucumber ++ testDependencies ++ circe ++ akka

lazy val copyDependencies = TaskKey[Unit]("copy-dependencies")

def copyDepTask = copyDependencies <<= (update, crossTarget, scalaVersion) map {
  (updateReport, out, scalaVer) =>
    updateReport.allFiles foreach { srcPath =>
      val destPath = out / "lib" / srcPath.getName
      IO.copyFile(srcPath, destPath, preserveLastModified = true)
    }
}


resolvers ++= List(
  "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases",
  Resolver.bintrayRepo("fcomb", "maven"),
  Opts.resolver.mavenLocalFile,
  DefaultMavenRepository,
  Resolver.defaultLocal,
  Resolver.mavenLocal,
  // Resolver.mavenLocal has issues - hence the duplication
  "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
  "Apache Staging" at "https://repository.apache.org/content/repositories/staging/",
  Classpaths.typesafeReleases,
  Classpaths.sbtPluginReleases,
  Resolver.typesafeRepo("releases"),
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  "Java.net Maven2 Repository" at "http://download.java.net/maven/2/",
  Resolver.bintrayRepo("hseeberger", "maven"), // for akka circe support
  "Eclipse repositories" at "https://repo.eclipse.org/service/local/repositories/egit-releases/content/",
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
)

///import scoverage.ScoverageKeys.{coverageFailOnMinimum, coverageMinimum}
coverageMinimum := 80

coverageFailOnMinimum := true

(testOptions in Test) += (Tests.Argument(TestFrameworks.ScalaTest, "-h", "target/scalatest-reports"))
