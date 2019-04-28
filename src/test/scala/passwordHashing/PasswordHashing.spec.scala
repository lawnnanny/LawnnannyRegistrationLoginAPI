package lambdas.PasswordHashing

import lambdas.Models.Users.Users.User
import org.scalacheck._
import org.scalatest._
import org.scalamock.scalatest.MockFactory
import lambdas.ResponseAndMessageTypes._

class PasswordHashingTest extends FunSpec with Matchers with MockFactory {
    describe("PasswordHashing") {
        describe("validatePassword") {
            val storedPassword = "$2a$10$7EQTGZBAHgiyEngb9xUBD.2oQnbRppTISd6gjsUifIR8RSTAYkgeC"
            val testUserNameRegistration = new UserNameRegistrationRequest("username", "password")
            it("Should return true given a correct hashed password") {
                import lambdas.PasswordHashing.PasswordHashingObject.PasswordHashing
                assert(testUserNameRegistration.validatePassword(storedPassword).get)
            }
            it("Should return false given a correct hashed password") {
                import lambdas.PasswordHashing.PasswordHashingObject.PasswordHashing
                val wrongTestUserNameRegistration = new UserNameRegistrationRequest("username", "wrong password")
                assert(!wrongTestUserNameRegistration.validatePassword(storedPassword).get)
            }
        }
    }
}
