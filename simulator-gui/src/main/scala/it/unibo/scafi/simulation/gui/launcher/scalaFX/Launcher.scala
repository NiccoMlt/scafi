package it.unibo.scafi.simulation.gui.launcher.scalaFX

import javafx.embed.swing.JFXPanel

import it.unibo.scafi.simulation.gui.controller.synchronization.Scheduler.scheduler
import it.unibo.scafi.simulation.gui.controller.{BasicPresenter, SimpleInputController}
import it.unibo.scafi.simulation.gui.demos.Simple
import it.unibo.scafi.simulation.gui.incarnation.scafi.Actions.ACTION
import it.unibo.scafi.simulation.gui.incarnation.scafi.ScafiLikeWorld.{in, out}
import it.unibo.scafi.simulation.gui.incarnation.scafi._
import it.unibo.scafi.simulation.gui.launcher.SensorName._
import it.unibo.scafi.simulation.gui.launcher.WorldConfig._
import it.unibo.scafi.simulation.gui.model.graphics2D.BasicShape2D.Rectangle
import it.unibo.scafi.simulation.gui.view.scalaFX.common.{FXSelectionArea, KeyboardManager}
import it.unibo.scafi.simulation.gui.view.scalaFX.drawer.{FXDrawer, StandardFXDrawer}
import it.unibo.scafi.simulation.gui.view.scalaFX.pane.FXSimulationPane
import it.unibo.scafi.simulation.gui.view.scalaFX.{RichPlatform, SimulationWindow}

import scala.util.Random
import scalafx.application.Platform
import scalafx.scene.layout.HBox
object Launcher {
  val r = new Random()
  new JFXPanel()
  //WORLD DEFINITION
  val world = SimpleScafiWorld
  var drawer : FXDrawer = StandardFXDrawer
  val shape = Rectangle(3,3)
  var boundary : Option[Rectangle] = None
  val ticked = 33
  var radius = 70.0
  var nodes = 1000
  var actions : List[ACTION] = List();
  var maxPoint = 1000
  var neighbourRender = false
  var program : Class[_] = classOf[Simple]
  devs = Set(
    dev(sens1,false,in),
    dev(sens2,false,in),
    dev(sens3,false,in),
    dev(gsensor,false,out),
    dev(gsensor1,"",out)
  )
  nodeProto = NodePrototype(shape)
  //SHOW THE WINDOW IMMEDIATLY
  val inputLogic = new SimpleInputController[ScafiLikeWorld](world)

  implicit val scafi = ScafiBridge(world)

  def launch(): Unit = {
    if(boundary.isDefined) {
      putBoundary(boundary.get)
    }
    import it.unibo.scafi.simulation.gui.view.AbstractKeyboardManager._
    val pane = new FXSimulationPane[world.type](inputLogic,drawer) with KeyboardManager[world.type] with FXSelectionArea[world.type]

    pane.addCommand(Code1, (ids : Set[Int]) => inputLogic.DeviceOnCommand(ids,sens1.name))
    pane.addCommand(Code2, (ids : Set[Int]) => inputLogic.DeviceOnCommand(ids,sens2.name))
    pane.addCommand(Code3, (ids : Set[Int]) => inputLogic.DeviceOnCommand(ids,sens3.name))
    pane.addMovementAction((ids : Map[Int,world.P]) => inputLogic.MoveCommand(ids))
    var window : Option[SimulationWindow] = None
    Platform.runLater {
      window = Some(new SimulationWindow(new HBox{}, pane, true))
      window.get.show
    }

    randomize2D(nodes,boundary)
    //gridLike2D(400,200,radius)
    actions.foreach(scafi.addAction(_))
    scafi.setProgramm(program)
    scafi.simulationPrototype = Some(ScafiBridge.createRadiusPrototype(radius))
    val x = System.currentTimeMillis()
    scafi.init()
    println("creation time = " + (System.currentTimeMillis() - x))
    val render = new BasicPresenter(world,neighbourRender)
    render.out = Some(pane)
    RichPlatform.thenRunLater{
      pane.outNode(world.nodes)
      if(neighbourRender) {
        pane.outNeighbour(world.network.neighbours() map{x => world(x._1).get -> world(x._2)})
      }
      //window.get.renderSimulation()
    } {
      scafi.start()
      val movement = new MovementSyncController(0.01f,world,500)
      movement.start()
      scheduler <-- inputLogic <-- movement <-- scafi <-- render
      scheduler.delta_=(ticked)
      scheduler.start()
    }
  }
}