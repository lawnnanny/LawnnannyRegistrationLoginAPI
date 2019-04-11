package lambdas.handlers

import org.scalatest._
import org.scalatest._
import com.amazonaws.services.lambda.runtime._
import org.scalamock.scalatest.{AsyncMockFactory, MockFactory}
import awscala._
import cats.Monad
import lambdas.ResponseAndMessageTypes.{ApiGatewayResponse, UserNameRegistrationRequest}
import cats.effect.IO
import dynamodbv2._
import org.scalacheck._
import lambdas.database._
import org.scalamock.scalatest.{AsyncMockFactory, MockFactory}
import lambdas.config._

import scala.collection.JavaConverters
import com.amazonaws.auth.AWSCredentials
import lambdas.handlers._

class RegistrationHandlerTest extends FunSpec with Matchers with MockFactory {

  describe("Registration Handler") {
      describe("handleRequest") {
          it("Should return a ApiGatewayResponse given a UserNameRegistrationRequest and a Context") {
              assert(true)
          }
      }

      describe("handleUserNameRegistration") {
          it("Should return an IO[MessageAndStatus] given a UserNameRegistrationRequest") {
              assert(true)
          }
      }
  }
}
