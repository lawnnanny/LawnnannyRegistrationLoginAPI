package lambdas.Models.Users

object Users {
    def apply(userName: String, hashedPassword: String) = new User(userName, hashedPassword)
    implicit class User(val userTuple: (String, String))
}
