/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.distrib.actor

import it.unibo.scafi.distrib.actor.patterns.PeriodicBehavior

import akka.actor.{Props, ActorRef, Actor}

import scala.concurrent.duration._
import scala.collection.mutable.{ Map => MMap }
import scala.util.Random

trait PlatformSchedulers { self: Platform.Subcomponent =>

  /**
   * A generic scheduler. When it decides or someone decides its time to schedule
   *  (reception of a [[GoOn]] msg), it determines the next device to run
   *  ({{nextToRun: ()=>Option[ID]}}) and, if any, determines the destination mailbox
   *  ({{recipientForExecution: ID=>ActorRef}}) and sends to it a schedule tick ([[GoOn]] msg).
   */
  trait GenericScheduler { self: Actor =>
    var nextToRun: () => Option[UID]
    def recipientForExecution(id: UID): ActorRef
    def onDone() {}

    val logger = akka.event.Logging(context.system, this)

    def syncReceive: Receive = {
      case GoOn => nextToRun() match { // Someone has required a next scheduling cycle
        case Some(id) => {
          logger.debug(s"\nScheduling $id to run")
          recipientForExecution(id) ! GoOn
          onDone()
        }
        case None => {
          logger.debug("\nNo device to schedule.")
          onDone()
        }
      }
    }
  }

  /**
   * An autonomous scheduler decides how to schedule by applying some [[ExecStrategy]] {{strategy}}.
   * Notes:
   *   - If {{initialDelay}} is {{None}}, then the scheduler must be externally
   *     activated (only the first time). It can be seen as an autonomous self-reduction of autonomy.
   *   - This scheduler is not entirely autonomous, as it needs some external entity
   *     to tell it (via [[MsgWithDevices]] msg) what devices are schedulables
   *     and what their references are.
   *
   * @param strategy
   * @param initialDelay
   * @param workInterval
   */
  class AutonomousScheduler(val strategy: ExecStrategy,
                            override val initialDelay: Option[FiniteDuration],
                            override var workInterval: FiniteDuration) extends
  Actor with GenericScheduler with PeriodicBehavior {
    var ids = Vector[UID]()
    var map = MMap[UID,ActorRef]()
    var kNext = 0

    override def recipientForExecution(id: UID): ActorRef = map(id)

    logger.info("\nAUTONOMOUS SCHEDULER with strategy " + strategy)

    override var nextToRun: () => Option[UID] = strategy match {
      case RandomExecStrategy(seed) => {
        val random = new Random(seed)
        () => if(ids.isEmpty) None else Some(ids(random.nextInt(ids.size)))
      }
      case OrderedExecStrategy(next) => next
      case RoundRobinStrategy => () => if(ids.isEmpty) None else {
        val nextToRun = ids(kNext)
        kNext = (kNext + 1)%ids.size
        Some(nextToRun)
      }
    }

    def receive: Receive = setupBehavior orElse syncReceive

    def setupBehavior: Receive = {
      case MsgWithDevices(map) => {
        logger.info(s"Got devices to schedule: $map")
        this.ids ++= map.keys
        this.map ++= MMap(map.toSeq:_*)
      }
    }

    override def onDone(): Unit = { scheduleNextWorkingCycle() }
  }

  object AutonomousScheduler extends Serializable {
    def props(exec: ExecStrategy,
              wInterval: FiniteDuration = 1.millisecond,
              initialDelay: Option[FiniteDuration] = None): Props =
      Props(classOf[AutonomousScheduler], self, exec, initialDelay, wInterval)
  }

}
