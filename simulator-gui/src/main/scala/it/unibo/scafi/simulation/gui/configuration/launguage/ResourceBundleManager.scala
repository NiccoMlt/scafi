package it.unibo.scafi.simulation.gui.configuration.launguage

import java.util.{Locale, ResourceBundle}

/**
  * a manger used to manage different language
  */
object ResourceBundleManager {
  private val Separator = "-"
  private val baseFolder = "bundles//"
  var locale = Locale.ENGLISH

  /**
    * find the word linked to key passed
    * @param key the key name
    * @param file the file where manager find word
    * @return string founded
    */
  def international(key : String)(implicit file : String): String = {
    val bundle = ResourceBundle.getBundle(baseFolder + file,locale)
    bundle.getString(key)
  }

  /**
    * create a key bind a list of word
    * @param key the list of keys
    * @param file the file where manager find word
    * @return the string founded
    */
  def international(key : String *)(implicit file : String): String = {
    val bundle = ResourceBundle.getBundle(baseFolder + file,locale)
    bundle.getString(key.mkString(Separator))
  }

  /**
    * main resource file
    */
  object KeyFile {
    val Configuration = "configuration"
    val CommandDescription = "command-description"
    val Error = "error"
    val CommandName = "command-name"
    val View = "view"
  }

  /**
    * fast way to use resource bundle string
    * @param sc the string context
    */
  implicit class Internationalization(val sc : StringContext)(implicit val file : String) {
    def i() : String = international(sc.parts.head)
  }
}
