package cat.pseudocodi

/**
 * @author FedericoL
 */
object Main {

  def main(args: Array[String]): Unit = {
    akka.Main.main(Array(classOf[GameLoop].getName))
  }
}
