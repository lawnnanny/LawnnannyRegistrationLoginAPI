package lambdas.PasswordHashing

import com.github.t3hnar.bcrypt._
import lambdas.ResponseAndMessageTypes.UserNameAndPasswordEvent
import scala.language.higherKinds

object PasswordHashingObject {
    def hashPassword(password: String) : String = password.bcrypt
    implicit class PasswordHashing(userNameRegistration: UserNameAndPasswordEvent){
        def validatePassword(correctPassword: String) = userNameRegistration.password.isBcryptedSafe(correctPassword)
    }
}
