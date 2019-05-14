package lambdas.PasswordHashing

import com.github.t3hnar.bcrypt._
import lambdas.ResponseAndMessageTypes._
import org.scalamock.scalatest.MockFactory
import org.scalatest._

class PasswordHashingTest extends FunSpec with Matchers with MockFactory {
    describe("PasswordHashing") {
        val storedPassword = "$2a$10$7EQTGZBAHgiyEngb9xUBD.2oQnbRppTISd6gjsUifIR8RSTAYkgeC"
        val password = "password"
        val testUserNameRegistration = new UserNameAndPasswordEvent("username", password)
        describe("validatePassword") {
            it("Should return true given a correct hashed password") {
                import lambdas.PasswordHashing.PasswordHashingObject.PasswordHashing
                assert(testUserNameRegistration.validatePassword(storedPassword).get)
            }
            it("Should return false given a correct hashed password") {
                import lambdas.PasswordHashing.PasswordHashingObject.PasswordHashing
                val wrongTestUserNameRegistration = new UserNameAndPasswordEvent("username", "wrong password")
                assert(!wrongTestUserNameRegistration.validatePassword(storedPassword).get)
            }
        }
        describe("hashPassword") {
            it("Should return hashed password given a string") {
                val hashedPassword = PasswordHashingObject.hashPassword(password)
                assert(password.isBcrypted(hashedPassword))
            }
        }
    }
}
