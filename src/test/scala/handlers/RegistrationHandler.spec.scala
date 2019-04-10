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
                  override def handleUserNameRegistration(request: UserNameRegistrationRequest)(implicit proxyFactory: AwsDynamoProxyFactory): IO[MessageAndStatus] = {
                      IO(new MessageAndStatus(true, testMessage))
                  }
              }
              val apiGatewayResponse = testApiGatewayHandler.handleRequest(testUserNameRegistrationRequest, testContex)
              val headers = Map("x-custom-response-header" -> "my custom response header value")
              val correctApiGatewayResponse = ApiGatewayResponse(200, testMessage, JavaConverters.mapAsJavaMap[String, Object](headers), true)
              assert(apiGatewayResponse.equals(correctApiGatewayResponse))
          }
      }

      describe("handleUserNameRegistration") {
          it("Should return an IO[MessageAndStatus] given a UserNameRegistrationRequest") {
              val testAccessKey = Gen.alphaNumChar.toString
              val testSecretAccessKey = Gen.alphaNumChar.toString
              val testRegion = Gen.alphaNumChar.toString
              val testUserName = Gen.alphaNumChar.toString
              val testPassword = Gen.alphaNumChar.toString
              val awsConfig = new AWSConfig("testAccessKey", "testSecretAccessKey", "testRegion")
              val testUserNameRegistrationRequest = new UserNameRegistrationRequest(testUserName, testPassword)

              val proxyStub = mock[AwsDynamoProxy]

              (proxyStub.put (_: String, _: Seq[(String, Any)]))
                .expects(testUserName, List(("Password", testPassword)).toSeq)
                .returning(IO {
                    new MessageAndStatus(true, "message")
                })

                val testAwsDynamoProxyFactory = new AwsDynamoProxyFactory {
                    override def apply(tableName: String)(implicit awsCredentials: AWSConfig) : AwsDynamoProxy = proxyStub
                }

                val testApiGatewayHandler = new ApiGatewayHandler
                testApiGatewayHandler.handleUserNameRegistration(testUserNameRegistrationRequest)(testAwsDynamoProxyFactory).unsafeRunSync()
          }
      }
  }
}
