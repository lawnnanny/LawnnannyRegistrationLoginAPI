package lambdas.handlers

import cats.effect.IO
import com.amazonaws.services.lambda.runtime.Context
import lambdas.ResponseAndMessageTypes.{ApiGatewayResponse, UserNameAndPasswordEvent}
import lambdas.database.flyweight.ioUserTable
import scala.collection.JavaConverters
import scala.language.higherKinds

class RegistrationApiGatewayHandler extends ApiGatewayHandler {

    def handleRequest(event: UserNameAndPasswordEvent, context: Context): ApiGatewayResponse = {
        val headers = Map("x-custom-response-header" -> "my custom response header value")
        val userLogic = getUserLogic
        val responseFromDatabase = userLogic.handleUserNameRegistration[IO](event).unsafeRunSync()
        val statusCode = if (responseFromDatabase.success) 200 else 600
        ApiGatewayResponse(statusCode, responseFromDatabase.message, JavaConverters.mapAsJavaMap[String, Object](headers), true)
    }
}
