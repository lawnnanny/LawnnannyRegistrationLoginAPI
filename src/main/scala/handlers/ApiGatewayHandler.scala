package lambdas.handlers

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import handlers.UserLogic.UserLogicOperations
import lambdas.ResponseAndMessageTypes.{ApiGatewayResponse, UserNameAndPasswordEvent}
import handlers.UserLogic.flyWeight.userLogicOperations

import scala.language.higherKinds

abstract class ApiGatewayHandler extends RequestHandler[UserNameAndPasswordEvent, ApiGatewayResponse] {

    def handleRequest(event: UserNameAndPasswordEvent, context: Context): ApiGatewayResponse
    def getUserLogic : UserLogicOperations = implicitly[UserLogicOperations]
}

