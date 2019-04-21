package lambdas.config

import spray.json.DefaultJsonProtocol

object AWSConfigProtocol extends DefaultJsonProtocol {
  implicit val awsConfigFormat = jsonFormat3(AWSConfig)
}

case class AWSConfig(val AWS_ACCESS_KEY: String, val SECRET_AWS_ACCESS_KEY: String, val REGION: String) extends Config
