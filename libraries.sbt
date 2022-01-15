val AkkaVersion = "2.6.17"
val AkkaHttpVersion = "10.2.7"
val ScalaTestVersion = "3.2.10"
val SlickVersion = "3.3.3"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.9"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4"

libraryDependencies += "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion
libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % AkkaVersion
libraryDependencies += "com.github.swagger-akka-http" %% "swagger-akka-http" % "2.6.0"
libraryDependencies += "jakarta.platform" % "jakarta.jakartaee-web-api" % "9.1.0"

libraryDependencies += "io.spray" %%  "spray-json" % "1.3.6"

libraryDependencies += "com.typesafe.slick" %% "slick" % SlickVersion
libraryDependencies += "com.typesafe.slick" %% "slick-hikaricp" % SlickVersion
libraryDependencies += "org.flywaydb" % "flyway-core" % "8.2.2"
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
