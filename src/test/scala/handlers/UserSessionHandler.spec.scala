package lambdas.handlers

import cats.Monad
import cats.data.EitherT
import com.amazonaws.services.lambda.runtime.Context
import handlers.UserLogic.UserLogicOperations
import lambdas.ResponseAndMessageTypes.{ApiGatewayResponse, UserNameAndPasswordEvent}
import lambdas.database.{DatabaseProxy, UserTable}
import org.scalamock.scalatest.MockFactory
import org.scalatest._
import scala.language.higherKinds

class UserSessionHandlerTest extends FunSpec with Matchers with MockFactory {

  val testOutputMessage = "outputMessage"
  val testUsername = "userName"
  val testPassword = "testPassword"
  val testErrorMessage = "errorMessage"

  val testUserSessionHandler = new UserSessionHandler

  val testEvent = new UserNameAndPasswordEvent

  testEvent.setUsername(testUsername)
  testEvent.setPassword(testPassword)

  val mockContext = mock[Context]

  val testUserSessionHandlerWithInjectedUserLogicOperationsHappyPath = new UserSessionHandler {
    override def getUserLogic : UserLogicOperations = new UserLogicOperations {
      override def handleUserNameSessionRequest[F[+_] : Monad](request: UserNameAndPasswordEvent)(implicit awsProxy: DatabaseProxy[F, UserTable]): EitherT[F, String, String] = {
        EitherT.rightT(testOutputMessage)
      }
    }
  }

  val testUserSessionHandlerWithInjectedUserLogicOperationsError = new UserSessionHandler {
    override def getUserLogic : UserLogicOperations = new UserLogicOperations {
      override def handleUserNameSessionRequest[F[+_] : Monad](request: UserNameAndPasswordEvent)(implicit awsProxy: DatabaseProxy[F, UserTable]): EitherT[F, String, String] = {
        EitherT.leftT(testErrorMessage)
      }
    }
  }

  describe("UserSessionHandler") {
    describe("handleRequest") {
      it("should return a ApiGatewayResponse") {
        val apiGatewayResponse = testUserSessionHandlerWithInjectedUserLogicOperationsHappyPath.handleRequest(testEvent, mockContext)
        apiGatewayResponse.statusCode shouldEqual 200
        apiGatewayResponse.body shouldEqual testOutputMessage
      }
      it("should return a ApiGatewayResponse with error") {
        val apiGatewayResponse = testUserSessionHandlerWithInjectedUserLogicOperationsError.handleRequest(testEvent, mockContext)
        apiGatewayResponse.statusCode shouldEqual 600
        apiGatewayResponse.body shouldEqual testErrorMessage
      }
    }

    describe("getUserLogic") {
      it("should return a UserLogicOperations") {
        val returnedUserLogic = testUserSessionHandler.getUserLogic
        returnedUserLogic should be theSameInstanceAs handlers.UserLogic.flyWeight.userLogicOperations
      }
    }
  }

}
