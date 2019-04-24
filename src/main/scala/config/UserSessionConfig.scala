package lambdas.config

import spray.json.DefaultJsonProtocol

object UserSessionConfigProtocol extends DefaultJsonProtocol {
  implicit val userSessionConfigFormat = jsonFormat2(UserSessionConfig)
}

case class UserSessionConfig(val EXPIRATION_FOR_SESSION: String, val SECRET_KEY: String) extends Config
