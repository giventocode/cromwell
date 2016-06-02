package cromwell.backend

import akka.actor.ActorLogging
import akka.event.LoggingReceive
import cromwell.backend.BackendLifecycleActor._
import cromwell.backend.BackendWorkflowFinalizationActor._

import scala.concurrent.Future

object BackendWorkflowFinalizationActor {

  // Commands
  sealed trait BackendWorkflowFinalizationActorCommand extends BackendWorkflowLifecycleActorCommand

  case object Finalize extends BackendWorkflowFinalizationActorCommand
  case object Abort extends BackendWorkflowFinalizationActorCommand

  // Responses
  sealed trait WorkflowBackendFinalizationActorResponse extends BackendWorkflowLifecycleActorResponse

  sealed trait FinalizationResponse extends WorkflowBackendFinalizationActorResponse
  case object FinalizationSuccess extends FinalizationResponse
  case class FinalizationFailed(reason: Throwable) extends Exception with FinalizationResponse
}

/**
  * Workflow-level actor for executing, recovering and aborting jobs.
  */
trait BackendWorkflowFinalizationActor extends BackendWorkflowLifecycleActor with ActorLogging {

  def receive: Receive = LoggingReceive {
    case Finalize => performActionThenRespond(afterAll, onFailure = FinalizationFailed)
    case Abort => abortFinalization
  }

  /**
    * Trigger an abort of all finalizations.
    *
    * Abort all finalizations.
    */
  def abortFinalization: Unit

  /**
    * Happens after everything else runs
    */
  def afterAll: Future[FinalizationResponse]

}
