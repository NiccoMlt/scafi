package it.unibo.scafi.simulation.gui.model.common.world

import it.unibo.scafi.simulation.gui.model.common.world.CommonWorldEvent.EventType
import it.unibo.scafi.simulation.gui.model.core.World
import it.unibo.utils.observer.Source

/**
  * describe a mutable world with the possibility to add or remove node
  */
trait ObservableWorld extends World with CommonConcept {
  self: ObservableWorld.Dependency =>
  override type O <: WorldObserver

  /**
    * method used to insert node produced by the instance passed
    * @param producer the produced of node
    * @return true if the node is legit (the position is in the world...) false otherwise
    */
  def insertNode(producer : NODE_PRODUCER) : Boolean

  /**
    * method used to remove a node by his id
    * @param id the id of node that the user want to remove
    * @return true if the node is in the world false otherwise
    */
  def removeNode(id : ID) : Boolean

  /**
    * remove all node in the world
    */
  def clear()
  /**
    * use strategy val to verify if the node is allowed in the world or not
    * @param n the node tested
    * @return true if the node is allowed false otherwise
    */
  protected def nodeAllowed(n:MUTABLE_NODE) : Boolean

  //simple factory
  def createObserver(listenEvent : Set[EventType]) : O
}

object ObservableWorld {
  type Dependency = Source
}