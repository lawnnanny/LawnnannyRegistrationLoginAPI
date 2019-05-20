package lambdas.handlers

import cats.effect.IO
import com.amazonaws.services.lambda.runtime.Context
import handlers.GetMessageAndStatus.eitherToGetMessageAndStatus
import lambdas.ResponseAndMessageTypes.{ApiGatewayResponse, UserNameAndPasswordEvent}
import lambdas.database.flyweight.ioUserTable
import handlers.UserLogic.{UserLogicOperations, flyWeight}
import scala.collection.JavaConverters
import scala.language.higherKinds
import handlers.UserLogic.flyWeight.userLogicOperations

class UserSessionHandler extends ApiGatewayHandler {

    def handleRequest(event: UserNameAndPasswordEvent, context: Context): ApiGatewayResponse = {
        val headers = Map("x-custom-response-header" -> "my custom response header value")
        val userLogic = implicitly[UserLogicOperations]
        val responseFromDatabase: Either[String, String] = userLogic.handleUserNameSessionRequest[IO](event).value.unsafeRunSync()
        val messageAndStatus = eitherToGetMessageAndStatus(responseFromDatabase)
        val statusCode = if (messageAndStatus.success) 200 else 600
        ApiGatewayResponse(100, messageAndStatus.message, JavaConverters.mapAsJavaMap[String, Object](headers), true)
    }
}