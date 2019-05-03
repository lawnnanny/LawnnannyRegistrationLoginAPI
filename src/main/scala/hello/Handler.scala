package lambdas.hello

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}

import scala.collection.JavaConverters
import lambdas._
import lambdas.ResponseAndMessageTypes._

class ApiGatewayHandler extends RequestHandler[UserNameAndPasswordEvent, ApiGatewayResponse] {

  def handleRequest(event: UserNameAndPasswordEvent, context: Context): ApiGatewayResponse = {
    val headers = Map("x-custom-response-header" -> "my custom response header value")
    ApiGatewayResponse(200, "Go Serverless v1.0! Your function executed successfully!",
      JavaConverters.mapAsJavaMap[String, Object](headers),
      true)
  }
}
