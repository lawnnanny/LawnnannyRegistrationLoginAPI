package lambdas.config

import lambdas.config.AWSConfigProtocol.awsConfigFormat
import lambdas.config.UserSessionConfigProtocol.userSessionConfigFormat
import spray.json._
import scala.io.Source

abstract class Config {
    def getJsonStringResourcesString(file: String): String = Source.fromResource(file).mkString
    def parseFileToJson(fileName: String): JsValue = getJsonStringResourcesString(fileName).parseJson
    def sprayConfig[A](file: String)(implicit reader: JsonReader[A]): A = {
        parseFileToJson(file)
          .convertTo[A]
    }
}

object GlobalConfigs extends Config {
    implicit val AWSConfig = sprayConfig[AWSConfig]("AWS.json")
    implicit val UserSessionConfig = sprayConfig[UserSessionConfig]("UserSessionConfig.json")
}
