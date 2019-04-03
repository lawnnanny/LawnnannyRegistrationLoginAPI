package lambdas.registration

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import lambdas.ResponseAndMessageTypes.{ApiGatewayRequest, ApiGatewayResponse}
import cats.effect.IO
import scala.collection.JavaConverters

class ApiGatewayHandler extends RequestHandler[ApiGatewayRequest, ApiGatewayResponse] {

  def handleRequest(event: ApiGatewayRequest, context: Context): ApiGatewayResponse = {
    val headers = Map("x-custom-response-header" -> "my custom response header value")

    ApiGatewayResponse(200, "submitted", JavaConverters.mapAsJavaMap[String, Object](headers), true)

  }

  def handleRegistration(event: ApiGatewayRequest): IO[String] = ???
}
