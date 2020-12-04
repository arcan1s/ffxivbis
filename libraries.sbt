val AkkaVersion = "2.6.10"
val AkkaHttpVersion = "10.2.1"
val SlickVersion = "3.3.3"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"

libraryDependencies += "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion
libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % AkkaVersion
libraryDependencies += "com.github.swagger-akka-http" %% "swagger-akka-http" % "2.3.0"
libraryDependencies += "javax.ws.rs" % "javax.ws.rs-api" % "2.1.1"

libraryDependencies += "io.spray" %%  "spray-json" % "1.3.6"
libraryDependencies += "com.lihaoyi" %% "scalatags" % "0.9.2"

libraryDependencies += "com.typesafe.slick" %% "slick" % SlickVersion
libraryDependencies += "com.typesafe.slick" %% "slick-hikaricp" % SlickVersion
libraryDependencies += "org.flywaydb" % "flyway-core" % "6.0.6"
libraryDependencies += "org.xerial" % "sqlite-jdbc" % "3.32.3.2"
libraryDependencies += "org.postgresql" % "postgresql" % "42.2.18"

libraryDependencies += "org.mindrot" % "jbcrypt" % "0.3m"


// testing
libraryDependencies += "org.scalactic" %% "scalactic" % "3.1.4" % "test"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.1.4" % "test"

libraryDependencies += "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % "test"
libraryDependencies += "com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion % "test"
libraryDependencies += "com.typesafe.akka" %% "akka-http-testkit" % AkkaHttpVersion % "test"
