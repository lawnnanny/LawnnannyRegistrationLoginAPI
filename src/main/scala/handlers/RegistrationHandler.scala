package lambdas.handlers

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import cats.effect.IO
import lambdas.ResponseAndMessageTypes.{UserNameRegistrationRequest, ApiGatewayResponse}
import spray.json._
import scala.collection.JavaConverters._
import com.amazonaws.services.lambda.runtime.events.{APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent}
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
import lambdas.database._
import scala.io.Source
import lambdas.config._
import lambdas.config.GlobalConfigs._
import scala.collection.JavaConverters

class ApiGatewayHandler extends RequestHandler[UserNameRegistrationRequest, ApiGatewayResponse] {

  def handleRequest(event: UserNameRegistrationRequest, context: Context): ApiGatewayResponse = {
    val headers = Map("x-custom-response-header" -> "my custom response header value")
    ApiGatewayResponse(200, "submitted", JavaConverters.mapAsJavaMap[String, Object](headers), true)
  }

  def handleUserNameRegistration(request: UserNameRegistrationRequest) = {
      val awsCredentials: AWSConfig = implicitly[AWSConfig]
      val proxy = AwsDynamoProxy(new AwsAccessKeys(awsCredentials), "UserTable")
  }
}
