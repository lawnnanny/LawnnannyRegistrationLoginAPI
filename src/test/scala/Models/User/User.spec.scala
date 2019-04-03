package lambdas.Models.Users

import lambdas.Models.Users.Users.User
import org.scalacheck._
import org.scalatest._
import org.scalamock.scalatest.MockFactory

class DatabaseTest extends FunSpec with Matchers with MockFactory {
    describe("User") {
        it("User Should Be Implicit") {
            val correctTestUserName = Gen.alphaNumChar.toString
            val correctTestPassword = Gen.alphaNumChar.toString
            val correctUser = new User((correctTestUserName, correctTestPassword))
            def compareUsers(implicit user: User) {
                val testUser = user.userTuple._1
                val testHashPassword = user.userTuple._2
                val testString : String = testUser + testHashPassword
                val correctString : String = correctTestUserName + correctTestPassword
                assert(testString.equals(correctString))
            }
        }
    }
}
