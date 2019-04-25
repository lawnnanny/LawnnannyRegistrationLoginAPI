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

class UserSessionHandlerTest extends FunSpec with Matchers with MockFactory {
    class TestApiGatewayHandler extends RegistrationApiGatewayHandler
    val testApiGatewayHandler = new TestApiGatewayHandler

  describe("ApiGatewayHandler") {
      describe("handleRequest") {
          it("Should return a ApiGatewayResponse given a UserNameRegistrationRequest and a Context") {
              assert(true)
          }
      }
  }
}
