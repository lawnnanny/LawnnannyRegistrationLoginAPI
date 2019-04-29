package lambdas.PasswordHashing

import lambdas.ResponseAndMessageTypes.UserNameRegistrationRequest
import com.github.t3hnar.bcrypt._
import scala.language.higherKinds
import scala.util.{Try, Success, Failure}

object PasswordHashingObject {
    def hashPassword(password: String) : String = password.bcrypt
    implicit class PasswordHashing(userNameRegistration: UserNameRegistrationRequest){
        def validatePassword(correctPassword: String): Try[Boolean] = userNameRegistration.password.isBcryptedSafe(correctPassword)
    }
}
