package it.unibo.scafi.js.view.static

import it.unibo.scafi.js.view.static.CssSettings._
import org.scalajs.dom.html.{Div, Select, TextArea}
import org.scalajs.dom.raw.HTMLStyleElement
import scalacss.ScalatagsCss._
import scalatags.JsDom.{TypedTag, _}
import scalatags.JsDom.all._

/**
 * the skeleton page of this web site. it contains the main dom element (editor, visualization, configuration)
 */
object SkeletonPage {
  //TODO find a good way to model bootstrap class (with a dedicated module?)
  /**
   * The root style of the web page.
   */
  def renderedStyle(style : RootStyle) : TypedTag[HTMLStyleElement] = style.render[TypedTag[HTMLStyleElement]]
  /**
   * Textarea associated with the editor section.
   */
  lazy val editorSection : TextArea = textarea(id := "editor").render
  /**
   * Select part to choose an aggregate program.
   */
  lazy val selectionProgram : Select = select(id := "select-program", cls := "form-control bg-dark text-white").render
  /**
    * Advanced and easy mode selection
    */
  lazy val modeSelection : Div = div(cls := "d-flex pl-2 pr-2 align-items-center",
    input(`type` := "checkbox", id := "modeSelection"),
    label(`for` := "modeSelection", cls := "form-check-label text-white ml-1", "advanced")
  ).render
  /**
   * Section that contains the controls to manage the backend, it is support specific.
   */
  lazy val controlsDiv : Div = div(id := "controls").render
  /**
    * Editor header that contains the program and mode selector
    */
  lazy val editorHeader : Div = div(id := "editor-header", cls := "input-group input-group-sm pt-1 pb-1", selectionProgram, modeSelection).render
  /**
    * Section that contains the controls to manage the visualization, it is support specific.
    */
  lazy val visualizationOptionDiv : Div = div(id := "visualization-option").render
  /**
   * Section in which is rendered the graph that represent the aggregate system.
   */
  lazy val visualizationSection : Div = div(id := "visualization-pane", cls := "border border-secondary", tabindex := 0).render
  /**
   * Section used to configure the backend (it is support specific)
   */
  lazy val backendConfig : Div = div(cls := "col-2 bg-dark", id := "backend-config-section").render
  /**
   * The entirely page content.
   */
  val fullPage : TypedTag[Div] = div(
    cls:= "container-fluid d-flex flex-column p-0 bg-dark",
    navBar,
    pageContainer
  )
  /**
    * The main section of the page.
    */
  val contentOnly : TypedTag[Div] = div(
    cls:= "container-fluid d-flex flex-column p-0 bg-dark",
    pageContainer
  )

  private def navBar : Tag = tag("nav")(
    cls := "navbar navbar-dark flex-shrink-0 bg-secondary",
    span(
      cls := "navbar-brand",
      h1(cls := "text-light", "Scafi")
    ),
    form(cls := "form-inline",
      button(cls := "btn btn-outline-light mr-2 my-sm-0", "FAQ"),
      button(cls := "btn btn-outline-light my-2 my-sm-0", "Repo"),
      span(cls := "navbar-text ml-2", "Discover the power of the collective")
    )
  )

  private def pageContainer : TypedTag[Div] = div(
    cls := "row flex-grow-1 m-0 bg-dark pt-3",
    id := "page-container",
    backendConfig,
    editor,
    visualization
  )

  private def editor : TypedTag[Div] = div(
    cls := "col-4 bg-dark",
    id := "editor-section",
    editorHeader,
    editorSection
  )

  private def visualization : TypedTag[Div] = div(
    cls := "col-6 bg-dark",
    id := "visualization-section",
    controlsDiv,
    visualizationOptionDiv,
    visualizationSection
  )
}
