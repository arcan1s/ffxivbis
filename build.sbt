name := "ffxivbis"

scalaVersion := "2.13.1"

scalacOptions ++= Seq("-deprecation", "-feature")

enablePlugins(JavaAppPackaging)

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case "application.conf" => MergeStrategy.concat
  case "module-info.class" => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}