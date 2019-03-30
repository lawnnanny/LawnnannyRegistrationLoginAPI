package lambdas.database

import awscala._, dynamodbv2._
import scala.util.{Try,Success,Failure}

abstract class DataBaseProxy

object DynamoDb extends DataBaseProxy

case class DynamoDB(implicit awsKeys: AccessKeys, awsRegion: String ) extends DataBaseProxy {

  val MatchStringAWSREgionToRegionObject = (regionString: String) => {
    try {
      val regionLowerCaps = regionString.toLowerCase()
      regionString match {
        case "us-east-1" => Region.US_EAST_1
        case _ => throw new RuntimeException("No Valid Region")
      }
    }
  } : Region

  implicit val region = MatchStringAWSREgionToRegionObject(awsRegion)
  implicit val dynamoDB = DynamoDB(awsKeys accessKey, awsKeys secretAccessKey)
}

class AccessKeys(val accessKey: String, val secretAccessKey: String)


