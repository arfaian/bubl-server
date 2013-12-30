package models

import org.scalaequals.ScalaEquals._

case class Position(val x: Long, val y: Long, val z: Long) {
  override def equals(other: Any): Boolean = equalAllVals
  override def hashCode(): Int = hash
  def canEqual(other: Any): Boolean = canEquals
  override def toString: String = genString
}
