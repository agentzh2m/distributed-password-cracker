
name := "pwcracker-worker"

version := "1.0"

scalaVersion := "2.12.4"


resolvers += "Sonatype OSS Snapshots" at
  "https://oss.sonatype.org/content/repositories/snapshots"


// https://mvnrepository.com/artifact/commons-codec/commons-codec
libraryDependencies += "commons-codec" % "commons-codec" % "1.11"
libraryDependencies +=
  "com.storm-enroute" %% "scalameter-core" % "0.8.2"

val opRabbitVersion = "2.1.0"

libraryDependencies ++= Seq(
  "com.spingo" %% "op-rabbit-core"        % opRabbitVersion,
  "com.spingo" %% "op-rabbit-play-json"   % opRabbitVersion,
  "com.spingo" %% "op-rabbit-json4s"      % opRabbitVersion,
  "com.spingo" %% "op-rabbit-airbrake"    % opRabbitVersion,
  "com.spingo" %% "op-rabbit-akka-stream" % opRabbitVersion,
  "net.debasishg" %% "redisclient" % "3.5"
)

assemblyJarName in assembly := "cracker_worker.jar"

test in assembly := {}



        