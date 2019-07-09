package handlers.UserLogic

import cats.Monad
import cats.data.EitherT
import cats.implicits._
import cats.data.EitherT
import handlers.GetMessageAndStatus.eitherToGetMessageAndStatus
import handlers.MessageAndStatus
import lambdas.JasonWebTokens.flyWeight.jasonWebTokenGenerator
import lambdas.JasonWebTokens.{JasonWebTokenGenerator, LoginRequest}
import lambdas.PasswordHashing.PasswordHashingObject
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

  def handleUserNameRegistration[F[_] : Monad](request: UserNameAndPasswordEvent)(implicit awsProxy: DatabaseProxy[F, UserTable]) : F[MessageAndStatus]  = {

    println(request.username)
    println(request.password)

    val userExists: EitherT[F, String, String] = EitherT( for {
      querried <- awsProxy.get(request.username)
    } yield({
      querried match {
        case Some(x) => Left("Account Already Exists")
        case None => Right("Account Will Be Created")
      }
    }))

    //println(userExists.value)

    lazy val createUser: EitherT[F, String, String] = EitherT({
      for {
        _ <- awsProxy.put(request.username, "Password" -> PasswordHashingObject.hashPassword(request.password))
      } yield (
        Right("Account Was Created"))
    })
    
    println("Create user 1")
    println("After create user")

    val resultOfRegistration: EitherT[F, String, String] = for {
      stuff <- userExists
      wasItCreated <- createUser
    } yield (wasItCreated)

    resultOfRegistration.value.flatMap((result: Either[String, String]) => {
      (result match {
        case Right(x) => MessageAndStatus(true, x)
        case Left(x) => MessageAndStatus(false ,x)
      }).pure[F]
    })
  }
}

object flyWeight {
  implicit val userLogicOperations : UserLogicOperations = new UserLogicOperations
}

object UserLogicOperations {
  implicit def apply : UserLogicOperations = new UserLogicOperations
}
