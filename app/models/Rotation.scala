package models

import org.scalaequals.ScalaEquals._

class Rotation(val rotation: Float, val look: Float) {
  override def equals(other: Any): Boolean = equalAllVals
  override def hashCode(): Int = hash
  def canEqual(other: Any): Boolean = canEquals
  override def toString: String = genString
}
