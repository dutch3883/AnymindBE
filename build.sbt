lazy val basicSettings = Seq(
  organization in ThisBuild := "com.panuwach",
  scalaVersion in ThisBuild := "2.13.4",
  name := "btc-billionaire",
  libraryDependencies ++= Seq(
    "com.typesafe.akka"          %% "akka-http"                % Versions.akkaHttp,
    "com.typesafe.akka"          %% "akka-http-spray-json"     % Versions.akkaHttp,
    "de.heikoseeberger"          %% "akka-http-circe"          % "1.38.2",
    "com.typesafe.akka"          %% "akka-actor-typed"         % Versions.akka,
    "com.typesafe.akka"          %% "akka-stream"              % Versions.akka,
    "com.typesafe.scala-logging" %% "scala-logging"            % "3.9.3",
    "com.beachape"               %% "enumeratum"               % Versions.enumeratum,
    "org.tpolecat"               %% "doobie-core"              % Versions.doobie,
    "org.tpolecat"               %% "doobie-hikari"            % Versions.doobie,
    "io.circe"                   %% "circe-core"               % Versions.circe,
    "io.circe"                   %% "circe-generic"            % Versions.circe,
    "io.circe"                   %% "circe-parser"             % Versions.circe,
    "ch.qos.logback"              % "logback-classic"          % Versions.logback,
    "com.microsoft.sqlserver"     % "mssql-jdbc"               % "9.4.0.jre8",
    "joda-time"                   % "joda-time"                % Versions.joda,
    "org.tpolecat"               %% "doobie-scalatest"         % Versions.doobie          % Test,
    "com.typesafe.akka"          %% "akka-http-testkit"        % Versions.akkaHttp        % Test,
    "com.typesafe.akka"          %% "akka-actor-testkit-typed" % Versions.akka            % Test,
    "org.scalatest"              %% "scalatest"                % Versions.Tests.scalatest % Test,
    "org.mockito"                %% "mockito-scala-scalatest"  % Versions.Tests.mockitoScalaTest
  ),
  resolvers ++= Seq(
    "Typesafe Repository" at "https://repo.typesafe.com/typesafe/releases/",
    Resolver.mavenLocal,
    Resolver.mavenCentral
  ),
  mainClass in Runtime := Some("Boot"),
  scalacOptions ++= Seq(
    "-encoding",
    "utf8",             // Option and arguments on same line
    "-Xfatal-warnings", // New lines for each options
    "-deprecation",
    "-unchecked",
    "-language:implicitConversions",
    "-language:higherKinds",
    "-language:existentials",
    "-language:postfixOps"
  )
)

lazy val commonSettings = Defaults.coreDefaultSettings ++ basicSettings

lazy val root  = project.in(file(".")).settings(commonSettings)
lazy val model = project.in(file("model")).settings(commonSettings)
lazy val core  = project.in(file("core")).settings(commonSettings).dependsOn(model)
lazy val api   = project.in(file("api")).settings(commonSettings).dependsOn(model, core)
