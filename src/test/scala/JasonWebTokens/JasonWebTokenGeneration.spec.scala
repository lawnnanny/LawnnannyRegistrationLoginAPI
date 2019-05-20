package lambdas.JasonWebTokens

import lambdas.JasonWebTokens.flyWeight._
import lambdas.config._
import lambdas.handlers._
import org.scalamock.scalatest.MockFactory
import org.scalatest._
import pdi.jwt.{JwtAlgorithm, JwtSprayJson}
import scala.language.higherKinds

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
              val returnedJwtToken = optionWithCorrectJwtToken
              assert(returnedJwtToken.equals(correctJwtToken))
          }
          it("Should Be A Valid JWT Encoding") {
              val jsonWebTokenGenerator = implicitly[JasonWebTokenGenerator]
              val testLoginRequest = new LoginRequest("bob")
              val testUserSessionConfig = new UserSessionConfig(0, "secret")
              val returnedJwtToken = jsonWebTokenGenerator.encode(testLoginRequest)(testUserSessionConfig)
              val result = JwtSprayJson.decodeJson(returnedJwtToken, testUserSessionConfig.SECRET_KEY, Seq(JwtAlgorithm.HS256))
              assert(result.isSuccess)
          }
          it("Should Not Be A Valid JWT Encoding") {
              val jsonWebTokenGenerator = implicitly[JasonWebTokenGenerator]
              val testLoginRequest = new LoginRequest("bob")
              val testUserSessionConfig = new UserSessionConfig(0, "secret")
              val returnedJwtToken = jsonWebTokenGenerator.encode(testLoginRequest)(testUserSessionConfig)
              val result = JwtSprayJson.decodeJson(returnedJwtToken, "not the key", Seq(JwtAlgorithm.HS256))
              assert(result.isFailure)
          }
      }
  }
}
