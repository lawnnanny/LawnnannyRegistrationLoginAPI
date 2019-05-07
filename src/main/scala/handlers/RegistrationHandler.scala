package lambdas.handlers

import cats.Monad
import cats.effect.IO
import cats.implicits._
import com.amazonaws.services.lambda.runtime.Context
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
          _ <- if(querried.isEmpty) awsProxy.put(request.username, "Password" -> PasswordHashingObject.hashPassword(request.password)) else None.pure[F]
      } yield(getMessageAndStatus(querried))
    }

    def getMessageAndStatus(querried: Option[_]): MessageAndStatus = {
        if (querried.isEmpty) {
          new MessageAndStatus(true, "Account Was Created")
        } else {
          new MessageAndStatus(false, "Account Already Exists")
        }
    }
}
