package agido.pp.spray.estimates

import scala.collection.mutable
import spray.json._
import spray.json.DefaultJsonProtocol._
import spray.httpx.SprayJsonSupport._

object Estimates {

  case class Estimate(username: String, `type`: String, amount: Option[Double])

  object Estimate {
    implicit val jsonformat = jsonFormat3(Estimate.apply)
    val defaultTypes = List("A", "K", "U", "T")
  }

  trait EstimateRepo {
    def getEstimates(taskId: String): List[Estimate]

    def getEstimates(user: String, taskId: String): List[Estimate] =
      getEstimates(taskId) filter (_.username == user)

    def setEstimate(task: String, e: Estimate)
  }
  
  val dummy = new EstimateRepo {
    def getEstimates(taskId: String) = {
      List(Estimate("usera", "A", Some(14)),

        Estimate("usera", "K", Some(10)))
    }

    def setEstimate(task: String, e: Estimate) = ()
  }

  val inmem = new EstimateRepo {
    type TaskId = String
    type EstimateKey = Tuple2[String, String]
    type PerTaskStore = mutable.Map[EstimateKey, Estimate]

    val store = mutable.Map[String, PerTaskStore]()

    def estimatesForTask(task: String) = store.getOrElseUpdate(task, mutable.Map())

    def getEstimates(taskId: String) =
      estimatesForTask(taskId).values.toList

    def setEstimate(task: String, e: Estimate) = {
      val taskStore = estimatesForTask(task)

      taskStore.put((e.username, e.`type`), e)
    }
  }

  val repo = inmem

  def sorted = (_: List[Estimate]) sortBy
    (estimate => Estimate.defaultTypes.indexOf(estimate.`type`))

}