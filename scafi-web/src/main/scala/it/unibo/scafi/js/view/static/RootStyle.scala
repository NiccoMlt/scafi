package it.unibo.scafi.js.view.static
import CssSettings._
import it.unibo.scafi.js.view.static.RootStyle.Measure
import scalacss.internal.{Attr, Length}
import scalacss.internal.ValueT.TypedAttrBase

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

/**
  * The css main file of the page, it contains the main tag and id that need to be styled
  */
case class RootStyle(measures : Measure) extends StyleSheet.Standalone {
  import dsl._
  import measures._

  "html, body" - (height(100 %%))
  "nav" -(height(navHeight))

  "#editor" - (
    height(100 %%),
    paddingBottom(10 px),
    flex := "1 1 auto",
    position.relative,
    overflow.auto
  )
  "#editor-header" - (height(demoSelectionHeight))
  "#select-program, #select-mode" - (height(100 %%))

  ".CodeMirror" - (height(editorHeight).important)

  "#visualization-section, #editor-section, #backend-config-section" - (height(contentHeight))

  "backendConfig" -(overflow.auto)

  "#controls, #visualization-option" -(
    height(utilsVisualizationHeight),
    whiteSpace.nowrap
  )

  "#console" -(height(utilsVisualizationHeight))

  "#visualization-pane" - (
    height(visualizationHeight),
    outlineWidth(0 px)
  )

  "#page-container" -(height(pageContentHeight))

  ".simplebar-scrollbar::before" -(backgroundColor(gray))

  ".carousel-control" -(filter := "invert(1);")
}

object RootStyle extends StyleSheet.Standalone {
  import dsl._
  val maxVh = 100
  val standardNavHeight = 10
  case class Measure(navHeight :  Length[Int],
                     pageContentHeight : Length[Int],
                     contentHeight : Length[Int],
                     visualizationHeight : Length[Int], editorHeight : Length[Int],
                     demoSelectionHeight : Length[Int], utilsVisualizationHeight : Length[Int])

  def withNav(nav : Int = standardNavHeight) : RootStyle = {
    val measure = Measure(
      navHeight = nav vh,
      pageContentHeight = (maxVh - nav) vh,
      contentHeight = 86 vh,
      visualizationHeight = 76 vh,
      editorHeight = 83 vh,
      demoSelectionHeight = 3 vh,
      utilsVisualizationHeight = 5 vh
    )
    RootStyle(measure)
  }

  lazy val withoutNav : RootStyle =  {
    val measure = Measure(
      navHeight = 0 vh,
      pageContentHeight = (maxVh) vh,
      contentHeight = 96 vh,
      visualizationHeight = 86 vh,
      editorHeight = 93 vh,
      demoSelectionHeight = 3 vh,
      utilsVisualizationHeight = 5 vh
    )
    RootStyle(measure)
  }
}