package lambdas.JasonWebTokens

import java.io.Console
import org.scalatest._
import org.scalatest._
import com.amazonaws.services.lambda.runtime._
import org.scalamock.scalatest.{AsyncMockFactory, MockFactory}
import awscala._
import cats.implicits._
import cats.{Applicative, Monad}
import lambdas.ResponseAndMessageTypes.{ApiGatewayResponse, UserNameAndPasswordEvent}
import cats.effect.IO
import cats.effect.concurrent.Ref
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
import spray.json._
import DefaultJsonProtocol._
import lambdas.config._
import lambdas.JasonWebTokens.flyWeight._

class JasonWebTokenGenerationTest extends FunSpec with Matchers with MockFactory {
    class TestApiGatewayHandler extends RegistrationApiGatewayHandler
    val testApiGatewayHandler = new TestApiGatewayHandler

  describe("JasonWebTokens") {
      describe("JasonWebTokenGenerator") {
          it("Should encode a LoginRequest") {
              val correctJwtToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6InNoYW5lIn0.RFZANu5IlO18zxcteXYKhrYlF9VRU8tgiN6w1V5steU"
              val jsonWebTokenGenerator = implicitly[JasonWebTokenGenerator]
              val testLoginRequest = new LoginRequest("shane")
              val testUserSessionConfig = new UserSessionConfig(0, "secret")
              val optionWithCorrectJwtToken = jsonWebTokenGenerator.encode(testLoginRequest)(testUserSessionConfig)
              val returnedJwtToken = optionWithCorrectJwtToken.get
              assert(returnedJwtToken.equals(correctJwtToken))
          }
      }
  }
}
