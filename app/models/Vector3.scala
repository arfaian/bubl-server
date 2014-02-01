package models

case class Vector3(
  val x: Double = 0,
  val y: Double = 0,
  val z: Double = 0) {

  def add(v: Vector3):Vector3 = {
    val x = this.x + v.x;
    val y = this.y + v.y;
    val z = this.z + v.z;
    Vector3(x, y, z);
  }

  def multiply(v: Vector3):Vector3 = {
    val x = this.x * v.x;
    val y = this.y * v.y;
    val z = this.z * v.z;
    Vector3(x, y, z);
  }

  def multiplyScalar(scalar: Double):Vector3 = {
    val x = this.x * scalar;
    val y = this.y * scalar;
    val z = this.z * scalar;
    Vector3(x, y, z);
  }

  def applyQuaternion(quaternion: Quaternion):Vector3 = {
    val qx = quaternion.x;
    val qy = quaternion.y;
    val qz = quaternion.z;
    val qw = quaternion.w;

    val ix: Double =  qw * x + qy * z - qz * y;
    val iy: Double =  qw * y + qz * x - qx * z;
    val iz: Double =  qw * z + qx * y - qy * x;
    val iw: Double = -qx * x - qy * y - qz * z;

    val x = ix * qw + iw * -qx + iy * -qz - iz * -qy;
    val y = ix * qw + iw * -qx + iy * -qz - iz * -qy;
    val z = ix * qw + iw * -qx + iy * -qz - iz * -qy;

    Vector3(x, y, z);
  }
}
