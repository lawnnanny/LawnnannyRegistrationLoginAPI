package lambdas.handlers

import cats.Monad
import cats.effect.IO
import cats.implicits._
import com.amazonaws.services.lambda.runtime.Context
import lambdas.JasonWebTokens._
import lambdas.JasonWebTokens.flyWeight._
import lambdas.PasswordHashing.PasswordHashingObject._
import lambdas.ResponseAndMessageTypes.{ApiGatewayResponse, UserNameAndPasswordEvent}
import lambdas.config.GlobalConfigs._
import lambdas.database._
import lambdas.database.flyweight.ioUserTable

import scala.collection.JavaConverters
import scala.language.higherKinds

class UserSessionApiGatewayHandler extends ApiGatewayHandler {

    def handleRequest(event: UserNameAndPasswordEvent, context: Context): ApiGatewayResponse = {
        val headers = Map("x-custom-response-header" -> "my custom response header value")
        val responseFromDatabase = handleUserNameSessionRequest[IO](event).unsafeRunSync()
        val statusCode = if (responseFromDatabase.success) 200 else 600
        ApiGatewayResponse(statusCode, responseFromDatabase.message, JavaConverters.mapAsJavaMap[String, Object](headers), true)
    }

    def handleUserNameSessionRequest[F[_] : Monad](request: UserNameAndPasswordEvent)(implicit awsProxy: DatabaseProxy[F, UserTable]): F[MessageAndStatus] = {
        for {
            userExist <- userExists[F](request)
            passwordIsValid <- if(userExist) passwordIsCorrect[F](request) else false.pure[F]
            jwtToken <- if(passwordIsValid) getJwtToken[F](request) else None.pure[F]
        } yield getMessageAndStatus(jwtToken)
    }

    def getPassword[F[_] : Monad](request: UserNameAndPasswordEvent)(implicit awsProxy: DatabaseProxy[F, UserTable]): F[String] = {
        for {
            querried <- awsProxy.get(request.username)
            password <- querried.get.attributes.tail.head.value.s.get.pure[F]
        } yield password
    }

    def getJwtToken[F[_] : Monad](request: UserNameAndPasswordEvent)(implicit jasonWebTokenGenerator: JasonWebTokenGenerator): F[Option[String]] = {
        for {
            jwtToken <- jasonWebTokenGenerator.encode(new LoginRequest(request.username)).pure[F]
        } yield jwtToken
    }

    def userExists[F[_] : Monad](request: UserNameAndPasswordEvent)(implicit awsProxy: DatabaseProxy[F, UserTable]): F[Boolean] = {
        for {
            querried <- awsProxy.get(request.username)
        } yield !querried.isEmpty
    }

    def passwordIsCorrect[F[_] : Monad](UserNameAndPasswordEvent: UserNameAndPasswordEvent)(implicit awsProxy: DatabaseProxy[F, UserTable]): F[Boolean] = {
        for {
            correctPassword <- getPassword[F](UserNameAndPasswordEvent)
            isValid <- UserNameAndPasswordEvent.validatePassword(correctPassword).get.pure[F]
        } yield isValid
    }

    def getMessageAndStatus(querried: Option[String]): MessageAndStatus = {
        if (!querried.isEmpty) {
            new MessageAndStatus(true, querried.get)
        } else {
            new MessageAndStatus(false, "Failed To Generate JWT Token")
        }
    }
}
