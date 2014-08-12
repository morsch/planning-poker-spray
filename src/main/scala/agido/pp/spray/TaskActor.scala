package agido.pp.spray

import akka.actor.Actor
import agido.pp.spray.tasks.Tasks

class TaskActor extends Actor {
  
  val repo = Tasks.repo
  
  override def preStart = {
    println("task actor starting")
  }

  def receive = {
    case team: String =>
      sender() ! repo.findTasks(team)
  }

}