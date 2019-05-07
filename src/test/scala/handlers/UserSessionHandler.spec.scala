package lambdas.handlers

import awscala._
import awscala.dynamodbv2._
import cats.Monad
import cats.effect.IO
import cats.implicits._
import lambdas.JasonWebTokens._
import lambdas.ResponseAndMessageTypes.UserNameAndPasswordEvent
import lambdas.config.UserSessionConfig
import lambdas.database.{DatabaseProxy, UserTable}
import org.scalamock.scalatest.MockFactory
import org.scalatest._

import scala.language.higherKinds

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
                  override def getPassword[F[_] : Monad](request: UserNameAndPasswordEvent)(implicit awsProxy: DatabaseProxy[F, UserTable]): F[String] = {
                      "Test String".pure[F]
                  }
              }
              val testUserNameAndPasswordEvent = new UserNameAndPasswordEvent {
                  def validatePassword(correctPassword: String) = {
                      assert(correctPassword.equals("Test String"))
                      Some(true)
                  }
              }
              val testUserSessionApiGatewayHandler = new TestUserSessionApiGatewayHandler1
              implicit val mockDatabaseProxy = mock[DatabaseProxy[IO, UserTable]]
              testUserSessionApiGatewayHandler.passwordIsCorrect[IO](testUserNameAndPasswordEvent)
          }
      }
      describe("userExists") {
          it("Should pass request.username to .get of awsProxy") {
              class TestUserSessionApiGatewayHandler2 extends UserSessionApiGatewayHandler
              val testUserSessionApiGatewayHandler = new TestUserSessionApiGatewayHandler2
              val testUserNameAndPasswordEvent = new UserNameAndPasswordEvent("testUserName", "testPassword")
              implicit val mockDatabaseProxy = new DatabaseProxy[IO, UserTable] {
                  def put(primaryKey: String, values: (String, Any)*): IO[Unit] = ???
                  def get(primaryKey: String): IO[Option[Item]] = {
                      assert(primaryKey.equals("testUserName"))
                      IO(Some(mock[Item]))
                  }
              }
              testUserSessionApiGatewayHandler.userExists[IO](testUserNameAndPasswordEvent)
          }
      }
      describe("getJwtToken") {
          it("Should Pass LoginRequest To Encode") {
              class TestUserSessionApiGatewayHandler3 extends UserSessionApiGatewayHandler
              val testUserSessionApiGatewayHandler = new TestUserSessionApiGatewayHandler3
              val testUserNameAndPasswordEvent = new UserNameAndPasswordEvent("testUserName", "testPassword")
              implicit val testJasonWebTokenGenerator = new JasonWebTokenGenerator {
                  override def encode(loginRequest: LoginRequest)(implicit userSessionConfig: UserSessionConfig): Option[String] = {
                      assert(loginRequest.equals(new LoginRequest("testUserName")))
                      Some("String")
                  }
              }
              testUserSessionApiGatewayHandler.getJwtToken[IO](testUserNameAndPasswordEvent)
          }
      }
      describe("handleUserNameSessionRequest") {
          it("Should Call getJwtToken If userExists Returns True And passwordIsValid") {
              class TestUserSessionApiGatewayHandler4 extends UserSessionApiGatewayHandler {
                  override def userExists[F[_] : Monad](request: UserNameAndPasswordEvent)(implicit awsProxy: DatabaseProxy[F, UserTable]): F[Boolean] = {
                      assert(request.username.equals("testUserName"))
                      true.pure[F]
                  }
                  override def passwordIsCorrect[F[_] : Monad](UserNameAndPasswordEvent: UserNameAndPasswordEvent)(implicit awsProxy: DatabaseProxy[F, UserTable]): F[Boolean] = {
                      assert(UserNameAndPasswordEvent.username.equals("testUserName"))
                      true.pure[F]
                  }
                  override def getJwtToken[F[_] : Monad](request: UserNameAndPasswordEvent)(implicit jasonWebTokenGenerator: JasonWebTokenGenerator): F[Option[String]] = {
                      assert(request.username.equals("testUserName"))
                      Option("testJwtToken").pure[F]
                  }
              }
              val testUserSessionApiGatewayHandler4 = new TestUserSessionApiGatewayHandler4
              val testUserNameAndPasswordEvent = new UserNameAndPasswordEvent("testUserName", "testPassword")
              implicit val mockDynamoProxy: DatabaseProxy[IO, UserTable] = mock[DatabaseProxy[IO, UserTable]]
              testUserSessionApiGatewayHandler4.handleUserNameSessionRequest[IO](testUserNameAndPasswordEvent)
          }
          it("Should Return MessageAndStatus With False Status If User Does Not Exist") {
              class TestUserSessionApiGatewayHandler5 extends UserSessionApiGatewayHandler {
                  override def userExists[F[_] : Monad](request: UserNameAndPasswordEvent)(implicit awsProxy: DatabaseProxy[F, UserTable]): F[Boolean] = {
                      assert(request.username.equals("testUserName"))
                      false.pure[F]
                  }
              }
              val testUserSessionApiGatewayHandler5 = new TestUserSessionApiGatewayHandler5
              val testUserNameAndPasswordEvent = new UserNameAndPasswordEvent("testUserName", "testPassword")
              implicit val mockDynamoProxy: DatabaseProxy[IO, UserTable] = mock[DatabaseProxy[IO, UserTable]]
              val messageAndStatus = testUserSessionApiGatewayHandler5.handleUserNameSessionRequest[IO](testUserNameAndPasswordEvent).unsafeRunSync()
              assert(!messageAndStatus.success)
              assert(messageAndStatus.message.equals("Failed To Generate JWT Token"))
          }
          it("Should Return MessageAndStatus With False Status If Password Is Not Valid") {
              class TestUserSessionApiGatewayHandler5 extends UserSessionApiGatewayHandler {
                  override def userExists[F[_] : Monad](request: UserNameAndPasswordEvent)(implicit awsProxy: DatabaseProxy[F, UserTable]): F[Boolean] = {
                      assert(request.username.equals("testUserName"))
                      true.pure[F]
                  }
                  override def passwordIsCorrect[F[_] : Monad](UserNameAndPasswordEvent: UserNameAndPasswordEvent)(implicit awsProxy: DatabaseProxy[F, UserTable]): F[Boolean] = {
                      assert(UserNameAndPasswordEvent.username.equals("testUserName"))
                      false.pure[F]
                  }
              }
              val testUserSessionApiGatewayHandler5 = new TestUserSessionApiGatewayHandler5
              val testUserNameAndPasswordEvent = new UserNameAndPasswordEvent("testUserName", "testPassword")
              implicit val mockDynamoProxy: DatabaseProxy[IO, UserTable] = mock[DatabaseProxy[IO, UserTable]]
              val messageAndStatus = testUserSessionApiGatewayHandler5.handleUserNameSessionRequest[IO](testUserNameAndPasswordEvent).unsafeRunSync()
              assert(!messageAndStatus.success)
              assert(messageAndStatus.message.equals("Failed To Generate JWT Token"))
          }
      }
  }
}
