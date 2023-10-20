val AkkaVersion = "2.8.0"
val AkkaHttpVersion = "10.5.0"
val ScalaTestVersion = "3.2.15"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.4.7"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5"

libraryDependencies += "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion
libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % AkkaVersion
libraryDependencies += "com.github.swagger-akka-http" %% "swagger-akka-http" % "2.10.0"
libraryDependencies += "jakarta.platform" % "jakarta.jakartaee-web-api" % "10.0.0"
libraryDependencies += "ch.megard" %% "akka-http-cors" % "1.2.0"

libraryDependencies += "io.spray" %%  "spray-json" % "1.3.6"

libraryDependencies += "org.playframework.anorm" %% "anorm" % "2.6.10"
libraryDependencies += "com.zaxxer" % "HikariCP" % "5.0.1" exclude("org.slf4j", "slf4j-api")
libraryDependencies += "org.flywaydb" % "flyway-core" % "9.16.0"
libraryDependencies += "org.xerial" % "sqlite-jdbc" % "3.40.1.0"
libraryDependencies += "org.postgresql" % "postgresql" % "42.5.4"

libraryDependencies += "org.mindrot" % "jbcrypt" % "0.4"
libraryDependencies += "com.google.guava" % "guava" % "31.0.1-jre"

// testing
libraryDependencies += "org.scalactic" %% "scalactic" % ScalaTestVersion % "test"
libraryDependencies += "org.scalatest" %% "scalatest" % ScalaTestVersion % "test"

libraryDependencies += "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % "test"
libraryDependencies += "com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion % "test"
libraryDependencies += "com.typesafe.akka" %% "akka-http-testkit" % AkkaHttpVersion % "test"
