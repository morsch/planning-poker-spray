package agido.pp.spray.estimates

import spray.json._
import spray.json.DefaultJsonProtocol._
import spray.httpx.SprayJsonSupport._

case class Estimate(username: String, `type`: String, amount: Option[Double])

object Estimate {
  implicit val jsonformat = jsonFormat3(Estimate.apply)
  val defaultTypes = List("A", "K", "U", "T")

  def sorted = (_: List[Estimate]) sortBy
    (estimate => Estimate.defaultTypes.indexOf(estimate.`type`))
}