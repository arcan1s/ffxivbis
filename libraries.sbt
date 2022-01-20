val AkkaVersion = "2.6.18"
val AkkaHttpVersion = "10.2.7"
val ScalaTestVersion = "3.2.10"
val SlickVersion = "3.3.3"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.10"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4"

libraryDependencies += "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion
libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % AkkaVersion
libraryDependencies += "com.github.swagger-akka-http" %% "swagger-akka-http" % "2.6.0"
libraryDependencies += "jakarta.platform" % "jakarta.jakartaee-web-api" % "9.1.0"
libraryDependencies += "ch.megard" %% "akka-http-cors" % "1.1.2"

libraryDependencies += "io.spray" %%  "spray-json" % "1.3.6"

libraryDependencies += "org.playframework.anorm" %% "anorm" % "2.6.10"
libraryDependencies += "com.zaxxer" % "HikariCP" % "5.0.1" exclude("org.slf4j", "slf4j-api")
libraryDependencies += "org.flywaydb" % "flyway-core" % "8.4.1"
libraryDependencies += "org.xerial" % "sqlite-jdbc" % "3.36.0.3"
libraryDependencies += "org.postgresql" % "postgresql" % "42.3.1"

libraryDependencies += "org.mindrot" % "jbcrypt" % "0.4"
libraryDependencies += "com.google.guava" % "guava" % "31.0.1-jre"

// testing
libraryDependencies += "org.scalactic" %% "scalactic" % ScalaTestVersion % "test"
libraryDependencies += "org.scalatest" %% "scalatest" % ScalaTestVersion % "test"

libraryDependencies += "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % "test"
libraryDependencies += "com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion % "test"
libraryDependencies += "com.typesafe.akka" %% "akka-http-testkit" % AkkaHttpVersion % "test"
