package lambdas.JasonWebTokens

import java.time.Instant
import scala.language.higherKinds
import cats.{Applicative, Monad}
import spray.json._
import lambdas.config.UserSessionConfig

case class JsonWebToken(val token: String)

case class LoginRequest(username: String)

class JasonWebTokenGenerator[F[_]: Monad] {
    def encode(loginRequest: LoginRequest)(implicit userSessionConfig: UserSessionConfig): F[String] = ???
}
