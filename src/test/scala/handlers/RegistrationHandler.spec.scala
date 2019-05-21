package lambdas.handlers

import cats.Monad
import cats.implicits._
import com.amazonaws.services.lambda.runtime.Context
import handlers.MessageAndStatus
import handlers.UserLogic.UserLogicOperations
import lambdas.ResponseAndMessageTypes.{ApiGatewayResponse, UserNameAndPasswordEvent}
import lambdas.database._
import org.scalamock.scalatest.MockFactory
import org.scalatest._
import scala.language.higherKinds

class RegistrationHandlerTest extends FunSpec with Matchers with MockFactory {
  val testOutputMessage = "outputMessage"
  val testErrorMessage = "errorMessage"

  val testUsername = "testUsername"
  val testPassword  = "testPassword"

  val testUserSessionHandler = new UserSessionHandler

  val testEvent = new UserNameAndPasswordEvent

  testEvent.setUsername(testUsername)
  testEvent.setPassword(testPassword)

  val testSuccessMessageAndStatus = MessageAndStatus(true, testOutputMessage)
  val testFailedMessageAndStatus = MessageAndStatus(false, testErrorMessage)

  val mockContext = mock[Context]

  val testRegistrationHandlerWithInjectedUserLogicOperationsHappyPath = new RegistrationApiGatewayHandler {
    override def getUserLogic : UserLogicOperations = new UserLogicOperations {
      override def handleUserNameRegistration[F[_] : Monad](request: UserNameAndPasswordEvent)(implicit awsProxy: DatabaseProxy[F, UserTable]): F[MessageAndStatus] = {
        testSuccessMessageAndStatus.pure[F]
      }
    }
  }

  val testRegistrationHandlerWithInjectedUserLogicOperationsError = new RegistrationApiGatewayHandler {
    override def getUserLogic : UserLogicOperations = new UserLogicOperations {
      override def handleUserNameRegistration[F[_] : Monad](request: UserNameAndPasswordEvent)(implicit awsProxy: DatabaseProxy[F, UserTable]): F[MessageAndStatus] = {
        testFailedMessageAndStatus.pure[F]
      }
    }
  }

  describe("RegistrationApiGatewayHandler") {
    describe("handleRequest") {
      it("Should return a successful ApiGatewayResponse") {
        val returnedApiGatewayHandler: ApiGatewayResponse = testRegistrationHandlerWithInjectedUserLogicOperationsHappyPath.handleRequest(testEvent, mockContext)
        returnedApiGatewayHandler.statusCode shouldEqual 200
        returnedApiGatewayHandler.body shouldEqual testOutputMessage
      }
      it("Should return a unsuccessful ApiGatewayResponse") {
        val returnedApiGatewayHandler: ApiGatewayResponse = testRegistrationHandlerWithInjectedUserLogicOperationsError.handleRequest(testEvent, mockContext)
        returnedApiGatewayHandler.statusCode shouldEqual 600
        returnedApiGatewayHandler.body shouldEqual testErrorMessage
      }
    }
  }
}
