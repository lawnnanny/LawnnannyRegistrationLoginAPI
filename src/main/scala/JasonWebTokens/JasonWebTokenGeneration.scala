package lambdas.JasonWebTokens

import lambdas.config.UserSessionConfig
import pdi.jwt.{JwtAlgorithm, JwtSprayJson}
import spray.json.DefaultJsonProtocol._
import spray.json._

import scala.language.higherKinds

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
