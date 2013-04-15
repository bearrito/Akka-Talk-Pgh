name := "Akka-Talk-PGH"
     
version := "1.0"
     
version := "1.0"
 
scalaVersion := "2.10.0"
 
resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
 
libraryDependencies +="com.typesafe.akka" %% "akka-actor" % "2.1.0"

libraryDependencies +="com.typesafe.akka" %% "akka-testkit" % "2.1.0"

libraryDependencies +="com.typesafe.akka" %% "akka-transactor" % "2.1.0"

libraryDependencies += "org.scalatest" % "scalatest_2.10" % "1.9.1"
