package cat.pseudocodi

/**
 * @author Fede
 */
object Main {

  def main(args: Array[String]): Unit = {
    akka.Main.main(Array(classOf[GameLoop].getName))
  }
}
