package lambdas.registration

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.sun.xml.internal.bind.CycleRecoverable.Context
import lambdas.ResponseAndMessageTypes.{ApiGatewayRequest, ApiGatewayResponse}
import cats.effect.IO

class ApiGatewayHandler extends RequestHandler[ApiGatewayRequest, ApiGatewayResponse] {

  def handleRequest(event: ApiGatewayRequest, context: Context): ApiGatewayResponse = {
    val headers = Map("x-custom-response-header" -> "my custom response header value")
    ApiGatewayResponse(200, "Go Serverless v1.0! Your function executed successfully!",
      JavaConverters.mapAsJavaMap[String, Object](headers),
      true)
  }


  def handleRegistration(event: ApiGatewayRequest): IO[String] = ???
}
