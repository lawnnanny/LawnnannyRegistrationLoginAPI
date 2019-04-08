package lambdas.Models.Users

import lambdas.Models.Users.Users.User
import org.scalacheck._
import org.scalatest._
import org.scalamock.scalatest.MockFactory

class DatabaseTest extends FunSpec with Matchers with MockFactory {
    describe("User") {
        it("Users apply function should return User type") {
            val correctTestUserName = Gen.alphaNumChar.toString
            val correctTestPassword = Gen.alphaNumChar.toString
            val userName :User = Users(correctTestUserName, correctTestPassword)
            assert(userName.isInstanceOf[User])
        }
    }
}
