package lambdas.PasswordHashing

import lambdas.Models.Users.Users.User
import org.scalacheck._
import org.scalatest._
import org.scalamock.scalatest.MockFactory
import lambdas.ResponseAndMessageTypes._

class PasswordHashingTest extends FunSpec with Matchers with MockFactory {
    describe("PasswordHashing") {
        describe("validatePassword") {
            val storedPassword = "$2a$10$9.2GbWUHsIY59Iy1NT4fw.HC/6vGGVUaMDvSkcLzJXS5zGndbxi4u"
            val testUserNameRegistration = new UserNameRegistrationRequest("username", "$2a$10$9zY1y6taOM6JNZcT5BpDz./fJSpuxvr28Glz4WkJ/5b0ufn.3d5ze")
            it("Should return true given a correct hashed password") {
                import lambdas.PasswordHashing.PasswordHashingObject.PasswordHashing
                assert(testUserNameRegistration.validatePassword(storedPassword))
            }
            it("Should return false given a correct hashed password") {
                import lambdas.PasswordHashing.PasswordHashingObject.PasswordHashing
                val wrongTestUserNameRegistration = new UserNameRegistrationRequest("username", "wrong password")
                assert(!wrongTestUserNameRegistration.validatePassword(storedPassword))
            }
        }
    }
}
