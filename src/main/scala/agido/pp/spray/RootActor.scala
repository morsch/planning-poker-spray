package agido.pp.spray

import scala.concurrent.ExecutionContext
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import spray.http.MediaTypes._
import spray.httpx.SprayJsonSupport._
import spray.json._
import spray.json.DefaultJsonProtocol._
import spray.routing._
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.TimeUnit
import scala.util.Success
import scala.util.Failure
import agido.pp.spray.tasks.Tasks.Task
import agido.pp.spray.estimates.Estimates
import agido.pp.spray.estimates.Estimates.Estimate
import agido.pp.spray.teams.Teams
import spray.http.StatusCodes.Redirection
import spray.http.StatusCodes

class RootActor extends Actor with HttpService {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  var estimateActor: ActorRef = null
  var taskActor: ActorRef = null

  override def preStart = {
    println("root actor started")
    estimateActor = context.actorOf(Props[EstimateActor], "estimates")
    taskActor = context.actorOf(Props[TaskActor], "tasks")
  }

  implicit val ec = ExecutionContext.Implicits.global
  implicit val timeout = Timeout(5, TimeUnit.SECONDS)

  def receive = runRoute {
    (path("teams") & get) {
      complete(Teams.teams)
    } ~
      (path("tasks" / Rest) & get) { team =>
        val response = taskActor ? team
        val fTasks = response.mapTo[List[Task]]

        onSuccess(fTasks) { task => complete(task) }
      } ~
      path("estimates" / Segment / Segment) { (taskId, user) =>
        get {
          val response = estimateActor ? (taskId, user)
          val fEstimates = response.mapTo[List[Estimate]]

          onSuccess(fEstimates) { complete(_) }
        } ~
          post {
            entity(as[Estimate]) { estimate =>
              estimateActor ! (taskId, estimate)
              complete("ok") // fire+forget: sagt ok auch wenns schiefgeht
            }
          }
      } ~
      path("estimates" / Segment) { taskId =>
        get {
          val response = estimateActor ? taskId
          val fEstimates = response.mapTo[List[Estimate]]

          onSuccess(fEstimates) { estimates =>
            complete(Estimates.sorted(estimates))
          }
        }
      } ~
      path("") { redirect("/index.html", StatusCodes.PermanentRedirect)} ~
      getFromDirectory("/home/schallab/luna-workspace/spray-template/public/pp-client")
  }
}