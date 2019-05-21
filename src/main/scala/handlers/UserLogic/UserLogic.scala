package handlers.UserLogic

import cats.Monad
import cats.data.EitherT
import cats.implicits._
import lambdas.JasonWebTokens.flyWeight.jasonWebTokenGenerator
import lambdas.JasonWebTokens.{JasonWebTokenGenerator, LoginRequest}
import lambdas.PasswordHashing.PasswordHashingObject._
import lambdas.ResponseAndMessageTypes.UserNameAndPasswordEvent
import lambdas.config.GlobalConfigs._
import lambdas.database.{DatabaseProxy, UserTable}
import scala.language.higherKinds

class UserLogicOperations {

  def handleUserNameSessionRequest[F[+_] : Monad](request: UserNameAndPasswordEvent)(implicit awsProxy: DatabaseProxy[F, UserTable]): EitherT[F, String, String] = {
    for {
      _ <- userExists[F](request)
      _ <- passwordIsCorrect[F](request)
      jwtToken = getJwtToken(request)
    } yield jwtToken
  }

  def getPassword[F[_] : Monad](request: UserNameAndPasswordEvent)(implicit awsProxy: DatabaseProxy[F, UserTable]): F[String] = {
    for {
      querried <- awsProxy.get(request.username)
      password <- querried.get.attributes.tail.head.value.s.get.pure[F]
    } yield password
  }

  def getJwtToken(request: UserNameAndPasswordEvent)(implicit jasonWebTokenGenerator: JasonWebTokenGenerator): String =
    jasonWebTokenGenerator.encode(LoginRequest(request.username))

  def userExists[F[+_] : Monad](request: UserNameAndPasswordEvent)(implicit awsProxy: DatabaseProxy[F, UserTable]) = {
    val eitherInF: F[Either[String, String]] = for {
      querried <- awsProxy.get(request.username)
      result = querried match {
        case None => Right("User Does Not Exist")
        case Some(_) => Left("User Does Exist")
      }
    } yield result
    EitherT(eitherInF)
  }

  def passwordIsCorrect[F[+_] : Monad](userNameAndPasswordEvent: UserNameAndPasswordEvent)(implicit awsProxy: DatabaseProxy[F, UserTable]): EitherT[F, String, String] = {
    val eitherOfF: F[Either[String, String]] = for {
      correctPassword <- getPassword[F](userNameAndPasswordEvent)
      either: Either[Throwable, Boolean] = userNameAndPasswordEvent.validatePassword(correctPassword).toEither
      eitherWithMessages = either match {
        case Left(x) => Left("Password Is Not Valid")
        case Right(true) => Right("Password Is Valid")
        case Right(false) => Left("Password Is Not Valid")
      }
    } yield eitherWithMessages
    EitherT(eitherOfF)
  }
}

object flyWeight {
  implicit val userLogicOperations : UserLogicOperations = new UserLogicOperations
}