package lambdas.handlers

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import lambdas.ResponseAndMessageTypes.{ApiGatewayResponse, UserNameAndPasswordEvent}

import scala.language.higherKinds

abstract class ApiGatewayHandler extends RequestHandler[UserNameAndPasswordEvent, ApiGatewayResponse] {

    def handleRequest(event: UserNameAndPasswordEvent, context: Context): ApiGatewayResponse
}

case class MessageAndStatus(val success: Boolean, val message: String)
