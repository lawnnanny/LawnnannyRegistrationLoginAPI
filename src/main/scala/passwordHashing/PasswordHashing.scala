package lambdas.PasswordHashing

import lambdas.ResponseAndMessageTypes.UserNameRegistrationRequest
import com.github.t3hnar.bcrypt._
import scala.language.higherKinds

object PasswordHashingObject {
    implicit class PasswordHashing(userNameRegistration: UserNameRegistrationRequest){
        def validatePassword(correctPassword: String): Boolean = userNameRegistration.password.isBcrypted(correctPassword)
    }
}
