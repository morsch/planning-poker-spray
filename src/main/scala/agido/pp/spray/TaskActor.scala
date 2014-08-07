package agido.pp.spray

import akka.actor.Actor
import agido.pp.spray.tasks.Tasks

class TaskActor extends Actor {
  
  val repo = Tasks.repo
  var i = 0;
  
  override def preStart = {
    println("task actor starting")
  }

  def receive = {
    case team: String =>
      i += 1
      println(s"i=$i")
      if (i%10==0)
        throw new Exception("failure")
      sender() ! repo.findTasks(team)
  }

}