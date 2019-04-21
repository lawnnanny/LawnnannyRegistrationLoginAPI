package lambdas.Models.Users

object Users {
    def apply(userName: String, hashedPassword: String) = new User(userName, hashedPassword)
    case class User(val userName: String, val hashedPassword: String)
}
