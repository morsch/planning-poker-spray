organization  := "com.example"

resolvers += "spray repo" at "http://repo.spray.io"

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

resolvers += "JIRA Kram" at "https://maven.atlassian.com/repository/public"

version       := "0.1"

scalaVersion  := "2.10.4"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")


libraryDependencies ++= {
  val akkaV = "2.4-SNAPSHOT" //"2.3.4"
  val sprayV = "1.3.1"
  Seq(
    "io.spray"            %%  "spray-can"     % sprayV,
    "io.spray"            %%  "spray-routing" % sprayV,
    "io.spray"            %%  "spray-json"    % "1.2.6",
    "com.typesafe.akka"   %%  "akka-actor"    % akkaV,
    "com.typesafe.akka"   %%  "akka-testkit"  % akkaV   % "test",
    "com.typesafe.akka" %% "akka-persistence-experimental" % akkaV,
    "org.specs2"          %%  "specs2"        % "2.4" % "test",
    "com.atlassian.jira" % "jira-rest-java-client-api" % "2.0.0-m30",
    "com.atlassian.jira" % "jira-rest-java-client-core" % "2.0.0-m30"
  )
}

libraryDependencies += "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.7"

EclipseKeys.withSource := true

EclipseKeys.withBundledScalaContainers := false

fork := true


