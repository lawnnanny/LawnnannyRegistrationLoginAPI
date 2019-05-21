package handlers

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers}
import handlers.GetMessageAndStatus.eitherToGetMessageAndStatus

class RegistrationHandlerTest extends FunSpec with Matchers with MockFactory {

  val testMessage = "test message"

  describe("eitherToGetMessageAndStatus") {
      it("Should Return MessageAndStatus With True And Message When Given A Right") {
        val givenEither = Right(testMessage)
        val returnedMessageAndStatus = eitherToGetMessageAndStatus(givenEither)
        val correctMessageAndStatus = MessageAndStatus(true, testMessage)
        returnedMessageAndStatus shouldEqual correctMessageAndStatus
      }

      it("Should Return MessageAndStatus With false And Message When Given A Left") {
        val givenEither = Left(testMessage)
        val returnedMessageAndStatus = eitherToGetMessageAndStatus(givenEither)
        val correctMessageAndStatus = MessageAndStatus(false, testMessage)
        returnedMessageAndStatus shouldEqual correctMessageAndStatus
      }
    }
}