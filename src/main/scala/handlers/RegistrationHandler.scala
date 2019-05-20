package lambdas.handlers

import cats.Monad
import cats.effect.IO
import cats.implicits._
import com.amazonaws.services.lambda.runtime.Context
import handlers.GetMessageAndStatus.eitherToGetMessageAndStatus
import handlers.MessageAndStatus
import lambdas.PasswordHashing._
import lambdas.ResponseAndMessageTypes.{ApiGatewayResponse, UserNameAndPasswordEvent}
import lambdas.database._
import lambdas.database.flyweight.ioUserTable
import scala.collection.JavaConverters
import scala.language.higherKinds

class RegistrationApiGatewayHandler extends ApiGatewayHandler {

    def handleRequest(event: UserNameAndPasswordEvent, context: Context): ApiGatewayResponse = {
        val headers = Map("x-custom-response-header" -> "my custom response header value")
        val responseFromDatabase = handleUserNameRegistration[IO](event).unsafeRunSync()
        val statusCode = if (responseFromDatabase.success) 200 else 600
        ApiGatewayResponse(statusCode, responseFromDatabase.message, JavaConverters.mapAsJavaMap[String, Object](headers), true)
    }

    def handleUserNameRegistration[F[_] : Monad](request: UserNameAndPasswordEvent)(implicit awsProxy: DatabaseProxy[F, UserTable]): F[MessageAndStatus] = {
      for {
          querried <- awsProxy.get(request.username)
          eitherResponse: Either[String, String] = querried match {
              case Some(x) => Left("Account Already Exists")
              case None => Right("Account Was Created")
          }
          _ <- awsProxy.put(request.username, "Password" -> PasswordHashingObject.hashPassword(request.password))
      } yield (eitherToGetMessageAndStatus(eitherResponse))
    }
}
