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
import lambdas.JasonWebTokens._
import lambdas.config.UserSessionConfig

class UserSessionHandlerTest extends FunSpec with Matchers with MockFactory {
    class TestUserSessionApiGatewayHandler extends UserSessionApiGatewayHandler
    val testUserSessionApiGatewayHandler = new TestUserSessionApiGatewayHandler

  describe("UserSessionApiGatewayHandler") {
      describe("getMessageAndStatus") {
          it("Should Return A MessageAndStatus Given A Option That Is Not Empty") {
              val testMessageAndStatus = testUserSessionApiGatewayHandler.getMessageAndStatus(Some("test string"))
              assert(testMessageAndStatus.equals(new MessageAndStatus(true, "test string")))
          }
          it("Should Return A MessageAndStatus Given A Option That is Empty") {
              val testMessageAndStatus = testUserSessionApiGatewayHandler.getMessageAndStatus(None)
              assert(testMessageAndStatus.equals(new MessageAndStatus(false, "Failed To Generate JWT Token")))
          }
      }
      describe("passwordIsCorrect") {
          it("Should Pass Password To ValidatePassword") {
              class TestUserSessionApiGatewayHandler1 extends UserSessionApiGatewayHandler {
                  override def getPassword[F[_] : Monad](request: UserNameRegistrationRequest)(implicit awsProxy: DatabaseProxy[F, UserTable]): F[String] = {
                      "Test String".pure[F]
                  }
              }
              val testUserNameRegistrationRequest = new UserNameRegistrationRequest {
                  def validatePassword(correctPassword: String) = {
                      assert(correctPassword.equals("Test String"))
                      Some(true)
                  }
              }
              val testUserSessionApiGatewayHandler = new TestUserSessionApiGatewayHandler1
              import lambdas.database.flyweight.ioUserTable
              testUserSessionApiGatewayHandler.passwordIsCorrect[IO](testUserNameRegistrationRequest)
          }
      }
      describe("userExists") {
          it("Should pass request.username to .get of awsProxy") {
              class TestUserSessionApiGatewayHandler2 extends UserSessionApiGatewayHandler
              val testUserSessionApiGatewayHandler = new TestUserSessionApiGatewayHandler2
              val testUserNameRegistrationRequest = new UserNameRegistrationRequest("testUserName", "testPassword")
              implicit val mockDatabaseProxy = new DatabaseProxy[IO, UserTable] {
                  def put(primaryKey: String, values: (String, Any)*): IO[Unit] = ???
                  def get(primaryKey: String): IO[Option[Item]] = {
                      assert(primaryKey.equals("testUserName"))
                      IO(Some(mock[Item]))
                  }
              }
              testUserSessionApiGatewayHandler.userExists[IO](testUserNameRegistrationRequest)
          }
      }
      describe("getJwtToken") {
          it("Should Pass LoginRequest To Encode") {
              class TestUserSessionApiGatewayHandler3 extends UserSessionApiGatewayHandler
              val testUserSessionApiGatewayHandler = new TestUserSessionApiGatewayHandler3
              val testUserNameRegistrationRequest = new UserNameRegistrationRequest("testUserName", "testPassword")
              implicit val testJasonWebTokenGenerator = new JasonWebTokenGenerator {
                  override def encode(loginRequest: LoginRequest)(implicit userSessionConfig: UserSessionConfig): Option[String] = {
                      assert(loginRequest.equals(new LoginRequest("testUserName")))
                      Some("String")
                  }
              }
              testUserSessionApiGatewayHandler.getJwtToken[IO](testUserNameRegistrationRequest)
          }
      }
      describe("handleUserNameSessionRequest") {
          it("Should Call getJwtToken If userExists Returns True And passwordIsValid") {
              class TestUserSessionApiGatewayHandler4 extends UserSessionApiGatewayHandler {
                  override def userExists[F[_] : Monad](request: UserNameRegistrationRequest)(implicit awsProxy: DatabaseProxy[F, UserTable]): F[Boolean] = {
                      assert(request.username.equals("testUserName"))
                      true.pure[F]
                  }
                  override def passwordIsCorrect[F[_] : Monad](userNameRegistrationRequest: UserNameRegistrationRequest)(implicit awsProxy: DatabaseProxy[F, UserTable]): F[Boolean] = {
                      assert(userNameRegistrationRequest.username.equals("testUserName"))
                      true.pure[F]
                  }
                  override def getJwtToken[F[_] : Monad](request: UserNameRegistrationRequest)(implicit jasonWebTokenGenerator: JasonWebTokenGenerator): F[Option[String]] = {
                      assert(request.username.equals("testUserName"))
                      Option("testJwtToken").pure[F]
                  }
              }
              val testUserSessionApiGatewayHandler4 = new TestUserSessionApiGatewayHandler4
              val testUserNameRegistrationRequest = new UserNameRegistrationRequest("testUserName", "testPassword")
              implicit val mockDynamoProxy: DatabaseProxy[IO, UserTable] = mock[DatabaseProxy[IO, UserTable]]
              testUserSessionApiGatewayHandler4.handleUserNameSessionRequest[IO](testUserNameRegistrationRequest)
          }
          it("Should Return MessageAndStatus With False Status If User Does Not Exist") {
              class TestUserSessionApiGatewayHandler5 extends UserSessionApiGatewayHandler {
                  override def userExists[F[_] : Monad](request: UserNameRegistrationRequest)(implicit awsProxy: DatabaseProxy[F, UserTable]): F[Boolean] = {
                      assert(request.username.equals("testUserName"))
                      false.pure[F]
                  }
              }
              val testUserSessionApiGatewayHandler5 = new TestUserSessionApiGatewayHandler5
              val testUserNameRegistrationRequest = new UserNameRegistrationRequest("testUserName", "testPassword")
              implicit val mockDynamoProxy: DatabaseProxy[IO, UserTable] = mock[DatabaseProxy[IO, UserTable]]
              val messageAndStatus = testUserSessionApiGatewayHandler5.handleUserNameSessionRequest[IO](testUserNameRegistrationRequest).unsafeRunSync()
              assert(!messageAndStatus.success)
              assert(messageAndStatus.message.equals("Failed To Generate JWT Token"))
          }
          it("Should Return MessageAndStatus With False Status If Password Is Not Valid") {
              class TestUserSessionApiGatewayHandler5 extends UserSessionApiGatewayHandler {
                  override def userExists[F[_] : Monad](request: UserNameRegistrationRequest)(implicit awsProxy: DatabaseProxy[F, UserTable]): F[Boolean] = {
                      assert(request.username.equals("testUserName"))
                      true.pure[F]
                  }
                  override def passwordIsCorrect[F[_] : Monad](userNameRegistrationRequest: UserNameRegistrationRequest)(implicit awsProxy: DatabaseProxy[F, UserTable]): F[Boolean] = {
                      assert(userNameRegistrationRequest.username.equals("testUserName"))
                      false.pure[F]
                  }
              }
              val testUserSessionApiGatewayHandler5 = new TestUserSessionApiGatewayHandler5
              val testUserNameRegistrationRequest = new UserNameRegistrationRequest("testUserName", "testPassword")
              implicit val mockDynamoProxy: DatabaseProxy[IO, UserTable] = mock[DatabaseProxy[IO, UserTable]]
              val messageAndStatus = testUserSessionApiGatewayHandler5.handleUserNameSessionRequest[IO](testUserNameRegistrationRequest).unsafeRunSync()
              assert(!messageAndStatus.success)
              assert(messageAndStatus.message.equals("Failed To Generate JWT Token"))
          }
      }
  }
}
