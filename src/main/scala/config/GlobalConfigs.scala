package lambdas.config

import lambdas.config.AWSConfigProtocol.awsConfigFormat
import spray.json._
import scala.io.Source

object GlobalConfigs {
    implicit val awsConfig: AWSConfig = {
        Source
          .fromResource("AWS.json")
          .mkString
          .parseJson
          .convertTo[AWSConfig]
    }
}
