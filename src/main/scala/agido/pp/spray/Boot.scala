package agido.pp.spray

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http
import akka.event.Logging

object Boot extends App {

  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("pp")

  // create and start our service actor
  val service = system.actorOf(Props[RootActor], "root")

  // start a new HTTP server on port 8080 with our service actor as the handler
  IO(Http) ! Http.Bind(service, interface = "localhost", port = 8080)
  
//  system.eventStream.setLogLevel(Logging.DebugLevel)
  
}