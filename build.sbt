name := "ffxivbis-scala"

version := "0.9.0"

scalaVersion := "2.13.1"

scalacOptions ++= Seq("-deprecation", "-feature")

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"

libraryDependencies += "io.spray" %%  "spray-json" % "1.3.5"

libraryDependencies += "com.typesafe.akka" %% "akka-http"   % "10.1.10"
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.10"
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.5.23"
libraryDependencies += "com.github.swagger-akka-http" %% "swagger-akka-http" % "2.0.4"
libraryDependencies += "javax.ws.rs" % "javax.ws.rs-api" % "2.1.1"

libraryDependencies += "com.typesafe.slick" %% "slick" % "3.3.2"
libraryDependencies += "com.typesafe.slick" %% "slick-hikaricp" % "3.3.2"
libraryDependencies += "org.flywaydb" % "flyway-core" % "6.0.6"
libraryDependencies += "org.xerial" % "sqlite-jdbc" % "3.28.0"
libraryDependencies += "org.postgresql" % "postgresql" % "9.3-1104-jdbc4"

libraryDependencies += "org.mindrot" % "jbcrypt" % "0.3m"

