import ProjectPlugin._

lazy val protocol = project
  .in(file("protocol"))
  .settings(moduleName := "mu-smart-home-protocol")
  .settings(protocolSettings)

lazy val shared = project
  .in(file("shared"))
  .settings(moduleName := "mu-smart-home-shared")
  .settings(sharedSettings)

lazy val server = project
  .in(file("server"))
  .settings(moduleName := "mu-smart-home-server")
  .settings(serverSettings)
  .dependsOn(protocol)
  .dependsOn(shared)

lazy val client = project
  .in(file("client"))
  .settings(moduleName := "mu-smart-home-client")
  .settings(clientSettings)
  .dependsOn(protocol)
  .dependsOn(shared)

lazy val root = project
.in(file("."))
.settings(name := "mu-smart-home")
.settings(noPublishSettings)
.aggregate(protocol, shared, server, client)
.dependsOn(protocol, shared, server, client)


addCommandAlias("runServer", "server/runMain com.fortysevendeg.smarthome.server.app.ServerApp")
addCommandAlias("runClient", "client/runMain com.fortysevendeg.smarthome.client.app.ClientApp")