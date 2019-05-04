package lambdas.JasonWebTokens

import java.time.Instant
import scala.language.higherKinds
import spray.json.DefaultJsonProtocol._
import cats.{Applicative, Monad}
import pdi.jwt.{JwtSprayJson, JwtAlgorithm, JwtClaim}
import spray.json._
import lambdas.config.UserSessionConfig

case class LoginRequest(val username: String)

class JasonWebTokenGenerator {
    def encode(loginRequest: LoginRequest)(implicit userSessionConfig: UserSessionConfig): Option[String] = {
        implicit val loginRequestFormatter = jsonFormat1(LoginRequest)
        val algo = JwtAlgorithm.HS256
        val jsObject = loginRequest.toJson.asJsObject
        Some(JwtSprayJson.encode(jsObject, userSessionConfig.SECRET_KEY, algo))
    }
}
object flyWeight {
    implicit val jasonWebTokenGenerator = new JasonWebTokenGenerator
}
