lazy val akkaHttpVersion = "10.1.4"
lazy val akkaVersion    = "2.5.15"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "com.example",
      scalaVersion    := "2.12.6"
    )),
    name := "akka-http-file-upload",
    libraryDependencies ++= Seq(
      "com.typesafe.akka"  %% "akka-http"              % akkaHttpVersion,
      "com.typesafe.akka"  %% "akka-http-spray-json"   % akkaHttpVersion,
      "com.typesafe.akka"  %% "akka-stream"            % akkaVersion,
      "com.lightbend.akka" %% "akka-stream-alpakka-s3" % "0.20",
      "com.amazonaws"      %  "aws-java-sdk-s3"        % "1.11.396",

      "com.typesafe.akka" %% "akka-http-testkit"    % akkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-testkit"         % akkaVersion     % Test,
      "com.typesafe.akka" %% "akka-stream-testkit"  % akkaVersion     % Test,
      "org.scalatest"     %% "scalatest"            % "3.0.5"         % Test
    )
  )
