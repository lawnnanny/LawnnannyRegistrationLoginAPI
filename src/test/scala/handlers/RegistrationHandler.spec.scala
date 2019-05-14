package lambdas.handlers

import awscala._
import awscala.dynamodbv2._
import cats.Applicative
import cats.effect.IO
import cats.effect.concurrent.Ref
import cats.implicits._
import com.github.t3hnar.bcrypt._
import lambdas.ResponseAndMessageTypes.UserNameAndPasswordEvent
import lambdas.database._
import org.scalamock.scalatest.MockFactory
import org.scalatest._

import scala.language.higherKinds

class RegistrationHandlerTest extends FunSpec with Matchers with MockFactory {
    class TestApiGatewayHandler extends RegistrationApiGatewayHandler
    val testApiGatewayHandler = new TestApiGatewayHandler

  describe("ApiGatewayHandler") {
      describe("handleRequest") {
          it("Should return a ApiGatewayResponse given a UserNameAndPasswordEvent and a Context") {
              assert(true)
          }
      }

      describe("handleUserNameRegistration") {
          it("Should Make A Get Request With The User Name And Not A Put Request") {
              class TestAwsDynamoProxy[F[+_]: Applicative, T <: UserTable](state: Ref[F, List[String]]) extends DatabaseProxy[F, UserTable]{
                  def put(primaryKey: String, values: (String, Any)*): F[Unit] = state.update(_ :+ primaryKey)
                  def get(primaryKey: String): F[Option[Item]] = {
                      state.update(_ :+ primaryKey)
                      None.pure[F]
                  }
              }
              val testApiGatewayHandler = new RegistrationApiGatewayHandler

              val testUserNameRegistration : UserNameAndPasswordEvent = new UserNameAndPasswordEvent("username", "password")
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
                  def put(primaryKey: String, values: (String, Any)*): F[Unit] = state.update(_ :+ values.toList.head._2.asInstanceOf[String])
                  def get(primaryKey: String): F[Option[Item]] = {
                      state.update(_ :+ primaryKey)
                      Some(mock[Item]).pure[F]
                  }
              }
              val testApiGatewayHandler = new RegistrationApiGatewayHandler

              val testUserNameRegistration : UserNameAndPasswordEvent = new UserNameAndPasswordEvent("username", "password")
              val state = Ref.of[IO, List[String]](List.empty[String])
              implicit val testDynamoProxy :TestAwsDynamoProxy[IO, UserTable] = new TestAwsDynamoProxy[IO, UserTable](state.unsafeRunSync())
              val spec = for {
                  _ <- testApiGatewayHandler.handleUserNameRegistration[IO](testUserNameRegistration)
                  st <- state.unsafeRunSync().get
                  as <- IO {
                      assert(st.get(0).get.equals("username"))
                      assert("password".isBcrypted(st.get(1).get))
                  }
              } yield as
              spec.unsafeToFuture()
          }
      }
      // as <- IO { assert(st == List("username", "username")) }
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
