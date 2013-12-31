import play.api._

object BublGlobal extends GlobalSettings {

  override def onStart(app: Application) {
    Logger.info("bubl-server has started...")
  }

  override def onStop(app: Application) {
    Logger.info("bubl-server has shutdown...")
  }
}
