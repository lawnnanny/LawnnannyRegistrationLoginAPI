package lambdas.handlers

import org.scalatest._
import org.scalatest._
import com.amazonaws.services.lambda.runtime._
import org.scalamock.scalatest.{AsyncMockFactory, MockFactory}
import awscala._
import lambdas.ResponseAndMessageTypes.{ApiGatewayResponse, UserNameRegistrationRequest}
import cats.effect.IO
import dynamodbv2._
import org.scalacheck._
import lambdas.database._
import org.scalamock.scalatest.{AsyncMockFactory, MockFactory}

import scala.collection.JavaConverters

class DatabaseTest extends FunSpec with Matchers with MockFactory {

  describe("Registration Handler") {
      it("handleUserNameRegistration was called by UserNameRegistrationRequest and returns ApiGatewayResonse") {
          val testUsername = Gen.alphaNumChar.toString
          val testPassword = Gen.alphaNumChar.toString
          val testMessage = Gen.alphaNumChar.toString
          val testContex = new Context {
              override def getAwsRequestId: String = ???

              override def getLogGroupName: String = ???

              override def getLogStreamName: String = ???

              override def getFunctionName: String = ???

              override def getFunctionVersion: String = ???

              override def getInvokedFunctionArn: String = ???

              override def getIdentity: CognitoIdentity = ???

              override def getClientContext: ClientContext = ???

              override def getRemainingTimeInMillis: Int = ???

              override def getMemoryLimitInMB: Int = ???

              override def getLogger: LambdaLogger = ???
          }
          val testUserNameRegistrationRequest: UserNameRegistrationRequest  = new UserNameRegistrationRequest(testUsername, testPassword)
          val testApiGatewayHandler = new ApiGatewayHandler {
              override def handleUserNameRegistration(request: UserNameRegistrationRequest): IO[MessageAndStatus] = {
                  IO(new MessageAndStatus(true, testMessage))
              }
          }
          val apiGatewayResponse = testApiGatewayHandler.handleRequest(testUserNameRegistrationRequest, testContex)
          val headers = Map("x-custom-response-header" -> "my custom response header value")
          val correctApiGatewayResponse = ApiGatewayResponse(200, testMessage, JavaConverters.mapAsJavaMap[String, Object](headers), true)
          assert(apiGatewayResponse.equals(correctApiGatewayResponse))
      }
  }
}
