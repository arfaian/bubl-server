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

    val ix: Double =  qw * this.x + qy * this.z - qz * this.y;
    val iy: Double =  qw * this.y + qz * this.x - qx * this.z;
    val iz: Double =  qw * this.z + qx * this.y - qy * this.x;
    val iw: Double = -qx * this.x - qy * this.y - qz * this.z;

    val x = ix * qw + iw * -qx + iy * -qz - iz * -qy;
    val y = ix * qw + iw * -qx + iy * -qz - iz * -qy;
    val z = ix * qw + iw * -qx + iy * -qz - iz * -qy;

    Vector3(x, y, z);
  }
}
