package handlers

case class MessageAndStatus(val success: Boolean, val message: String)

package object GetMessageAndStatus {
    def eitherToGetMessageAndStatus(either: Either[String, String]) = {
        either match {
            case Right(x) => MessageAndStatus(true, x)
            case Left(x) => MessageAndStatus(false, x)
        }
    }
}
