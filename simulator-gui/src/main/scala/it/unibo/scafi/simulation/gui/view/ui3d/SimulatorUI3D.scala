/*
 * Copyright (C) 2016-2017, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENCE.txt file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package it.unibo.scafi.simulation.gui.view.ui3d

import it.unibo.scafi.renderer3d.manager.NetworkRenderer
import it.unibo.scafi.simulation.gui.view.MyPopupMenu
import javax.swing.{JFrame, JMenuBar}

trait SimulatorUI3D extends JFrame{

  def customPopupMenu: MyPopupMenu

  def getSimulationPanel: NetworkRenderer

  def reset(): Unit

  def getJMenuBar: JMenuBar

  def getUI: JFrame

  def setShowValue(valueKind: NodeValue): Unit

  def setObservation(observation: Any=>Boolean): Unit

  def getObservation(): Any=>Boolean

  def setSensor(sensorName: String, value: Any): Unit
}
