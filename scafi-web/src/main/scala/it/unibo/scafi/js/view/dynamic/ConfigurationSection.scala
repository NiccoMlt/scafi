package it.unibo.scafi.js.view.dynamic

import it.unibo.scafi.js.controller.AggregateSystemSupport
import it.unibo.scafi.js.controller.local.{DeviceConfiguration, GridLikeNetwork, RandomNetwork, SimulationSeeds, SimulationSupport, SpatialRadius, SupportConfiguration}
import org.scalajs.dom.html.{Div, Select}

import scala.scalajs.js
import scala.util.{Failure, Success, Try}

class ConfigurationSection(configuration : Div, support : AggregateSystemSupport[_, SupportConfiguration, _]) {
  import ConfigurationSection._
  import scalatags.JsDom.all._

  val container : Div = div(cls:= "pt-1, pb-1").render
  configuration.appendChild(container)

  val selectMode = select (cls := "form-control", option(Random.toString), option(Grid.toString)).render
  val loadButton = button( cls := "btn btn-primary btn-sm",`type` := "button", "load config").render
  val mainDiv = div(cls := "input-group input-group-sm", selectMode, loadButton).render
  selectMode.onchange = _ => init(getModeFromSelect(selectMode))
  loadButton.onclick = _ => load(getModeFromSelect(selectMode))

  private val (cols, rows, stepX, stepY, tolerance) = (InputText("cols", 10), InputText("rows", 10), InputText("stepX", 60), InputText("stepY", 60), InputText("tolerance", 0))
  private val gridValue = List(cols, rows, stepX, stepY, tolerance)

  private val (min, max, howMany) = (InputText("min", 0), InputText("max", 1000), InputText("howMany", 100))
  private val randomValue = List(min, max, howMany)

  private var sensors = List(SensorInputText("source", "false"), SensorInputText("obstacle", "false"))
  private val radius = InputText("radius", 100)

  private val addSensorButton = button( cls := "btn btn-primary btn-sm",`type` := "button", "add sensor").render
  addSensorButton.onclick = ev => {
    val newElement = SensorInputText()
    sensors = sensors ::: List(newElement)
    container.appendChild(newElement.render)
  }

  def init(mode : Mode) : Unit = {
    container.textContent = ""
    container.appendChild(mainDiv)
    val elements = mode match {
      case Grid => gridValue
      case Random => randomValue
    }

    elements foreach { (input) => container.appendChild(input.render) }
    container.appendChild(radius.render)
    container.appendChild(addSensorButton)
    sensors foreach { input => container.appendChild(input.render) }
  }

  def load(mode : Mode) : Unit = {
    val netSettings = mode match {
      case Grid => GridLikeNetwork(rows.intValue, cols.intValue, stepX.intValue, stepY.intValue, tolerance.intValue)
      case Random => RandomNetwork(min.intValue, max.intValue, howMany.intValue)
    }
    val sensorMap = js.Dictionary[Any](sensors.map(_.nameAndValue):_*)
    support.evolve(SupportConfiguration(netSettings, SpatialRadius(radius.intValue), DeviceConfiguration(sensorMap), SimulationSeeds()))
  }

  init(Random)
}

object ConfigurationSection {
  import scalatags.JsDom.all._
  private case class SensorInputText(name : String = "", default : String = "") {
    private val nameTag = input (`type` := "text", placeholder := "name", cls := "form-control", value := name).render
    private val valueTag = input (`type` := "text", placeholder := "value", cls := "form-control", value := default).render
    private def booleanFromString(value : String) : Try[Boolean] = value match {
      case "true" => Success(true)
      case "false" => Success(false)
      case _ => Failure(new IllegalArgumentException)
    }
    def nameAndValue : (String, Any) = (nameTag.value, parseValue)

    def parseValue : Any = Try(valueTag.value.toInt)
      .recoverWith { case _ => Try(valueTag.value.toDouble) }
      .recoverWith { case _ => (booleanFromString(valueTag.value)) }
      .getOrElse(valueTag.value)
    val render =  div (
      cls := "input-group input-group-sm mb-2 mt-2",
      div(cls := "input-group-prepend", span (cls := "input-group-text", "sensor"), nameTag, valueTag),
    ).render
  }

  private case class InputText(label : String, defaultValue : Int) {
    private val inputSection = input(`type` := "number", cls := "form-control", value := defaultValue).render
    def intValue : Int = inputSection.value.toInt
    val render = div (
      cls := "input-group input-group-sm mb-2 mt-2",
      div(cls := "input-group-prepend", span (cls := "input-group-text", label)),
      inputSection
    ).render
  }

  private trait Mode
  private case object Random extends Mode { override def toString: String = "random" }
  private case object Grid extends Mode { override def toString: String = "grid" }
  private def modeFromString(mode : String) : Mode = mode match {
    case "random" => Random
    case "grid" => Grid
  }

  private def getModeFromSelect(select : Select) : Mode = {
    val option = select.selectedIndex
    modeFromString(select.children.item(option).textContent)
  }
}