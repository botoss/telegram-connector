name := "telegram-connector"

version := "1.0"

scalaVersion := "2.12.1"

libraryDependencies ++= Seq(
  "info.mukel" %% "telegrambot4s" % "3.0.1",
  "org.apache.kafka" % "kafka-clients" % "0.11.0.0",
  "io.spray" %%  "spray-json" % "1.3.3",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe" % "config" % "1.3.1",
  "org.scalaz" %% "scalaz-core" % "7.2.14",
  "com.jsuereth" %% "scala-arm" % "2.0",
  "org.scalatest" %% "scalatest" % "3.0.1" % Test,
  "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0" % Test,
  "com.typesafe.akka" %% "akka-testkit" % "2.5.3" % Test,
  "net.manub" %% "scalatest-embedded-kafka" % "0.16.0" % Test
)

enablePlugins(DockerPlugin)

dockerfile in docker := {
  val jarFile: File = sbt.Keys.`package`.in(Compile, packageBin).value
  val classpath = (managedClasspath in Compile).value
  val mainclass = mainClass.in(Compile, packageBin).value.getOrElse(
    sys.error("Expected exactly one main class"))
  val jarTarget = s"/app/${jarFile.getName}"
  // Make a colon separated classpath with the JAR file
  val classpathString = classpath.files.map("/app/" + _.getName).mkString(":") + ":" + jarTarget
  new Dockerfile {
    // Base image
    from("java")
    // Add all files on the classpath
    add(classpath.files, "/app/")
    // Add the JAR file
    add(jarFile, jarTarget)
    // On launch run Java with the classpath and the main class
    entryPoint("java", "-cp", classpathString, mainclass)
  }
}
