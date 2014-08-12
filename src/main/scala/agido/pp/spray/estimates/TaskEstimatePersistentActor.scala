package agido.pp.spray.estimates

import java.util.concurrent.TimeUnit

import scala.collection.mutable
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success

import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.pattern.ask
import akka.persistence.PersistentActor
import akka.util.Timeout

/**
 * Event: A user was added
 * @param user
 */
case class UserAdded(user: String)

/**
 * Message: Query all estimates for the given taskId
 */
case class GetAllEstimatesQuery(taskId: String)

/**
 * Return Message: Estimates for the original query
 */
case class GetAllEstimatesResponse(estimates: List[Estimate])

/**
 * Parent actor of the SingleEstimatePersistentActors
 *
 * Manages a set of known users
 *
 * Forwards certain commands and queries that are only relevant to a single task+user
 * to the right child actor
 *
 * Creates and processes subqueries for queries that combine multiple users
 *
 * @author schallab
 *
 */
class TaskEstimatePersistentActor(taskid: String) extends PersistentActor {

  override def persistenceId = s"estimates-for-$taskid"

  implicit val timeout = Timeout(5, TimeUnit.SECONDS)
  implicit val ec = context.dispatcher

  /**
   * The set of known users. Required to be able to combine all estimates
   * of all users.
   *
   * This is the mutable state of this persistent actor.
   */
  val knownUsers = mutable.Set[String]()

  /**
   * Look up the child actor for taskId+user or create it if it doesn't exist yet
   */
  def actorForEstimate(taskId: String, user: String): ActorRef = {
    val actorName = s"actor-$taskId-$user"
    println(s"looking up actor $actorName")

    context.child(actorName) match {
      case Some(actor) => actor // found existing
      case None =>
        // this is how you create an actor with constructor parameters
        val props = Props(classOf[SingleEstimatePersistentActor], taskId, user)
        context.actorOf(props, actorName);
    }
  }

  // persistent actor: receive journal
  def receiveRecover: Receive = {
    case UserAdded(user) => knownUsers += user
  }

  // persistent actor: receive message/commands, generate events where necessary
  def receiveCommand: Receive = {
    // receive command SetEstimate: generate UserAdded event if necessary,
    // and forward the command to the correct child actor
    case se: SetEstimate =>
      val SetEstimate(taskId, Estimate(user, _, _)) = se

      if (!knownUsers.contains(user)) {
        persist(UserAdded(user)) {
          case UserAdded(user) => knownUsers += user
        }
      }

      actorForEstimate(taskId, user).forward(se)

    // forward query to the correct child actor for known users
    case ge: GetEstimatesQuery if knownUsers(ge.user) =>
      actorForEstimate(ge.taskId, ge.user).forward(ge)

    // empty reply for unknown users
    case ge: GetEstimatesQuery =>
      sender() ! GetEstimatesResponse(List())

    // query all known users for their estimates, combine their answers into
    // a single list and reply to the sender
    case GetAllEstimatesQuery(taskId) =>
      val origsender = sender()

      // send a query to each actor and extract the relevant data from the response
      // this 
      val fEstimates = knownUsers.toList map { user =>
        // it's not enough to query all children! there might be persistent actors that we have
        // not created yet. this is why we need to store the knownUsers
        val response = (actorForEstimate(taskId, user) ? GetEstimatesQuery(taskId, user)).
          mapTo[GetEstimatesResponse]
        response map (_.estimates)
      }

      // sequence turns a List[Future[X]] into a Future[List[X]] 
      val sequenced = Future.sequence(fEstimates)

      // flatten the inner list (the X above is itself a List[Estimate])
      val flattened: Future[List[Estimate]] = sequenced.map { x => x.flatten }

      // handle the future asynchronously (this needs an implicit execution context)
      flattened onComplete ({
        case Success(e) =>
          println(s"found and combined estimates $e")
          // not 100% sure if sender() != origsender here
          origsender ! GetAllEstimatesResponse(e)
        case Failure(ex) =>
          println(ex)
          throw ex
      })
  }
}