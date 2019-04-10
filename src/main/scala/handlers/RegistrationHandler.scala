package lambdas.handlers

import cats.Monad
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import cats.effect.IO
import lambdas.ResponseAndMessageTypes.{ApiGatewayResponse, UserNameRegistrationRequest}
import spray.json._

import scala.collection.JavaConverters._
import com.amazonaws.services.lambda.runtime.events.{APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent}
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import lambdas.database._

import scala.io.Source
import scala.collection.JavaConverters
import cats.effect._
import cats.effect.syntax.all._
import cats.implicits._

import scala.util.Either
import lambdas.database.AwsDynamoProxyFactory
import lambdas.config.GlobalConfigs.AWSConfig

case class MessageAndStatus(val success: Boolean, val message: String)

class ApiGatewayHandler extends RequestHandler[UserNameRegistrationRequest, ApiGatewayResponse] {

  def handleRequest(event: UserNameRegistrationRequest, context: Context): ApiGatewayResponse = {
      val headers = Map("x-custom-response-header" -> "my custom response header value")
      val responseFromDatabase = handleUserNameRegistration[IO](event).unsafeRunSync()
      val statusCode = if (responseFromDatabase.success) 200 else 600
      ApiGatewayResponse(statusCode, responseFromDatabase.message, JavaConverters.mapAsJavaMap[String, Object](headers), true)
  }

  def handleUserNameRegistration[F[_]: Monad](request: UserNameRegistrationRequest)(implicit proxyFactory: AwsDynamoProxyFactory): F[MessageAndStatus] = {

      val awsProxy = proxyFactory("UserTable")
      for {
        _ <- awsProxy.put(request.username, List(("Password", request.password)).toSeq)
      } yield()


      new MessageAndStatus(true, "response")

  }
}
