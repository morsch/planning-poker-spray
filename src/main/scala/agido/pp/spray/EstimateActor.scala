package agido.pp.spray

import akka.actor.Actor
import akka.actor.actorRef2Scala
import agido.pp.spray.estimates.Estimates
import agido.pp.spray.estimates.Estimates.Estimate

class EstimateActor extends Actor {
  val repo = Estimates.repo
  
  override def preStart = {
    println("estimate actor started")
  }
  def receive = {
    case taskId: String =>
      sender() ! repo.getEstimates(taskId)

    case (taskId: String, estimate: Estimate) =>
      sender() ! repo.setEstimate(taskId, estimate)

    case (taskId: String, user: String) =>
      val estimates = repo.getEstimates(user, taskId)
      val missingTypes = Estimate.defaultTypes filter
        (defaulttype => !estimates.exists(_.`type` == defaulttype))

      val combinedEstimates = estimates ::: (missingTypes map (Estimate(user, _, None)))

      println(s"got estimates for $taskId/$user")

      sender() ! combinedEstimates
  }
}