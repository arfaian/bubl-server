package models

import play.api.Logger

object Vector3 {
  val X = Vector3(1, 0, 0)
  val Y = Vector3(0, 1, 0)
  val Z = Vector3(0, 0, 1)
}

case class Vector3(
  x: Double = 0.0,
  y: Double = 0.0,
  z: Double = 0.0) {

  lazy val log = Logger("application." + this.getClass.getName)

  def +(v: Vector3):Vector3 = Vector3(x + v.x, y + v.y, z + v.z)
  def *(v: Vector3):Vector3 = Vector3(x * v.x, y * v.y, z * v.z)
  def *(scalar: Double):Vector3 = Vector3(x * scalar, y * scalar, z * scalar)

  def applyQuaternion(quaternion: Quaternion):Vector3 = {
    val qx = quaternion.x;
    val qy = quaternion.y;
    val qz = quaternion.z;
    val qw = quaternion.w;

    val ix: Double =  qw * this.x + qy * this.z - qz * this.y;
    val iy: Double =  qw * this.y + qz * this.x - qx * this.z;
    val iz: Double =  qw * this.z + qx * this.y - qy * this.x;
    val iw: Double = -qx * this.x - qy * this.y - qz * this.z;

    val x = ix * qw + iw * -qx + iy * -qz - iz * -qy;
    val y = ix * qw + iw * -qx + iy * -qz - iz * -qy;
    val z = ix * qw + iw * -qx + iy * -qz - iz * -qy;

    log debug s"inputQuaternion"
    log debug s"x: $x"
    log debug s"y: $y"
    log debug s"z: $z"

    Vector3(x, y, z);
  }
}
