package models

case class Vector3(
  val x: Double = 0,
  val y: Double = 0,
  val z: Double = 0) {

  def set(x: Double, y:Double, z:Double):Vector3 = {
    Vector3(x, y, z);
  }

  def add(v: Double):Vector3 = {
    val x = x + v.x;
    val y = y + v.y;
    val z = z + v.z;
    Vector3(x, y, z);
  }

  def multiply(v: Double):Vector3 = {
    val x = x * v.x;
    val y = y * v.y;
    val z = z * v.z;
    Vector3(x, y, z);
  }

  def multiplyScalar(scalar: Double):Vector3 = {
    val x = x * scalar;
    val y = y * scalar;
    val z = z * scalar;
    Vector3(x, y, z);
  }

  def applyQuaternion(quaternion: Quaternion):Vector3 = {
    val qx = quaternion.x;
    val qy = quaternion.y;
    val qz = quaternion.z;
    val qw = quaternion.w;

    val ix =  qw * x + qy * z - qz * y;
    val iy =  qw * y + qz * x - qx * z;
    val iz =  qw * z + qx * y - qy * x;
    val iw = -qx * x - qy * y - qz * z;

    val x = ix * qw + iw * -qx + iy * -qz - iz * -qy;
    val y = ix * qw + iw * -qx + iy * -qz - iz * -qy;
    val z = ix * qw + iw * -qx + iy * -qz - iz * -qy;

    Vector3(x, y, z);
  }
}
