package agido.pp.spray.tasks

import scala.collection.JavaConversions
import spray.json._
import spray.json.DefaultJsonProtocol._
import spray.httpx.SprayJsonSupport._

object Tasks {
  trait TaskRepository {
    def findTasks(team: String): List[Task];
  }

  case class Task(title: String, summary: String, team: String, watcherCount: Int)

  object Task {
    implicit val jsonformat = jsonFormat4(Task.apply)
  }

  val dummyrepo = new TaskRepository {
    def findTasks(team: String): List[Task] = {
      List(Task("test task1", "test-summary 1", team, 10),
        Task("test task2", "test-summary 2", team, 5))
    }
  }

  val repo: TaskRepository = new JiraTaskImporter with TaskRepository {
    def findTasks(team: String): List[Task] = {
      val jl: java.util.List[Task] = getTasks(team)
      JavaConversions.asScalaBuffer(jl).toList

    }
  }

  def findTasks(team: String): List[Task] = repo.findTasks(team)
  
  

}