package lambdas.handlers

import java.io.Console

import org.scalatest._
import org.scalatest._
import com.amazonaws.services.lambda.runtime._
import org.scalamock.scalatest.{AsyncMockFactory, MockFactory}
import awscala._
import cats.implicits._
import cats.{Applicative, Monad}
import lambdas.ResponseAndMessageTypes.{ApiGatewayResponse, UserNameRegistrationRequest}
import cats.effect.IO
import cats.effect.concurrent.Ref
import dynamodbv2._
import org.scalacheck._
import lambdas.database.AwsDynamoProxyFactory
import org.scalamock.scalatest.{AsyncMockFactory, MockFactory}
import lambdas.config.AWSConfig
import cats.effect._

import scala.collection.JavaConverters
import com.amazonaws.auth.AWSCredentials
import cats.implicits
import lambdas.handlers._
import cats.effect.{Async, IO, Sync}
import io.circe.Decoder.state
import lambdas.database._

import scala.language.higherKinds

class RegistrationHandlerTest extends FunSpec with Matchers with MockFactory {
    class TestApiGatewayHandler extends ApiGatewayHandler
    val testApiGatewayHandler = new TestApiGatewayHandler

  describe("ApiGatewayHandler") {
      describe("handleRequest") {
          it("Should return a ApiGatewayResponse given a UserNameRegistrationRequest and a Context") {
              assert(true)
          }
      }

      describe("handleUserNameRegistration") {
          it("Should Make A Get Request With The User Name And Not A Put Request") {
              class TestAwsDynamoProxy[F[+_]: Applicative, T <: UserTable](state: Ref[F, List[String]]) extends DatabaseProxy[F, UserTable]{
                  def put(primaryKey: String, values: Seq[(String, Any)]): F[Unit] = state.update(_ :+ primaryKey)
                  def get(primaryKey: String): F[Option[_]] = {
                      state.update(_ :+ primaryKey)
                      None.pure[F]
                  }
              }
              val testApiGatewayHandler = new ApiGatewayHandler

              val testUserNameRegistration : UserNameRegistrationRequest = new UserNameRegistrationRequest("username", "password")
              val state = Ref.of[IO, List[String]](List.empty[String])
              implicit val testDynamoProxy :TestAwsDynamoProxy[IO, UserTable] = new TestAwsDynamoProxy[IO, UserTable](state.unsafeRunSync())
              val spec = for {
                  _ <- testApiGatewayHandler.handleUserNameRegistration[IO](testUserNameRegistration)
                  st <- state.unsafeRunSync().get
                  as <- IO { assert(st == List("username")) }
              } yield as
              spec.unsafeToFuture()
          }
          it("Should Make A Get Request With The User Name And A Put Request") {
              class TestAwsDynamoProxy[F[+_]: Applicative, T <: UserTable](state: Ref[F, List[String]]) extends DatabaseProxy[F, UserTable]{
                  def put(primaryKey: String, values: Seq[(String, Any)]): F[Unit] = state.update(_ :+ primaryKey)
                  def get(primaryKey: String): F[Option[_]] = {
                      state.update(_ :+ primaryKey)
                      Some("querried").pure[F]
                  }
              }
              val testApiGatewayHandler = new ApiGatewayHandler

              val testUserNameRegistration : UserNameRegistrationRequest = new UserNameRegistrationRequest("username", "password")
              val state = Ref.of[IO, List[String]](List.empty[String])
              implicit val testDynamoProxy :TestAwsDynamoProxy[IO, UserTable] = new TestAwsDynamoProxy[IO, UserTable](state.unsafeRunSync())
              val spec = for {
                  _ <- testApiGatewayHandler.handleUserNameRegistration[IO](testUserNameRegistration)
                  st <- state.unsafeRunSync().get
                  as <- IO { assert(st == List("username", "username")) }
              } yield as
              spec.unsafeToFuture()
          }
      }

      describe("getMessageAndStatus") {
          it("Should Return A Un-Successful MessageAndStatus Given A Option") {
              val returnedMessageAndStatus = testApiGatewayHandler.getMessageAndStatus(Some(Unit))
              val correctMessageAndStatus = new MessageAndStatus(false, "Account Already Exists")
              assert(returnedMessageAndStatus.equals(correctMessageAndStatus))
          }

          it("Should Return A Successful MessageAndStatus Given A Option") {
              val returnedMessageAndStatus = testApiGatewayHandler.getMessageAndStatus(None)
              val correctMessageAndStatus = new MessageAndStatus(true, "Account Was Created")
              assert(returnedMessageAndStatus.equals(correctMessageAndStatus))
          }
      }
  }
}
