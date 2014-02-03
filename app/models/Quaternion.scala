package models

case class Quaternion(
  x: Double = 0,
  y: Double = 0,
  z: Double = 0,
  w: Double = 1) {

  def setFromEuler(euler: Euler):Quaternion = {
    var c1 = math.cos(euler.x / 2.0)
    var c2 = math.cos(euler.y / 2.0)
    var c3 = math.cos(euler.z / 2.0)
    var s1 = math.sin(euler.x / 2.0)
    var s2 = math.sin(euler.y / 2.0)
    var s3 = math.sin(euler.z / 2.0)

    // TODO: Needs own euler??

    if (euler.order == "XYZ") {
      new Quaternion(s1 * c2 * c3 + c1 * s2 * s3,
          c1 * s2 * c3 - s1 * c2 * s3,
          c1 * c2 * s3 + s1 * s2 * c3,
          c1 * c2 * c3 - s1 * s2 * s3)
    } else if (euler.order == "YXZ") {
      new Quaternion(s1 * c2 * c3 + c1 * s2 * s3,
          c1 * s2 * c3 - s1 * c2 * s3,
          c1 * c2 * s3 - s1 * s2 * c3,
          c1 * c2 * c3 + s1 * s2 * s3)
    } else if (euler.order == "ZXY") {
      new Quaternion(s1 * c2 * c3 - c1 * s2 * s3,
          c1 * s2 * c3 + s1 * c2 * s3,
          c1 * c2 * s3 + s1 * s2 * c3,
          c1 * c2 * c3 - s1 * s2 * s3)
    } else if (euler.order == "ZYX") {
      new Quaternion(s1 * c2 * c3 - c1 * s2 * s3,
          c1 * s2 * c3 + s1 * c2 * s3,
          c1 * c2 * s3 - s1 * s2 * c3,
          c1 * c2 * c3 + s1 * s2 * s3)
    } else if (euler.order == "YZX") {
      new Quaternion(s1 * c2 * c3 + c1 * s2 * s3,
          c1 * s2 * c3 + s1 * c2 * s3,
          c1 * c2 * s3 - s1 * s2 * c3,
          c1 * c2 * c3 - s1 * s2 * s3)
    } else if (euler.order == "XZY") {
      new Quaternion(s1 * c2 * c3 - c1 * s2 * s3,
          c1 * s2 * c3 - s1 * c2 * s3,
          c1 * c2 * s3 + s1 * s2 * c3,
          c1 * c2 * c3 + s1 * s2 * s3)
    } else {
      return this
    }
  }

}
