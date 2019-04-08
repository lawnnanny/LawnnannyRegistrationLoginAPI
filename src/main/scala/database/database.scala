package lambdas.database

import awscala._
import cats.effect.IO
import dynamodbv2._
import cats.syntax.apply._
import lambdas.config.AWSConfig
import spray.json.DefaultJsonProtocol
import lambdas.config.GlobalConfigs.AWSConfig

abstract class DatabaseProxy

abstract class AccessKeys

sealed case class AwsAccessKeys(private val config: AWSConfig ) extends AccessKeys {
    def getAccessKey = config.AWS_ACCESS_KEY
    def getSecreateAccessKey = config.SECRET_AWS_ACCESS_KEY
    def getRegion : Region = config.REGION.toLowerCase match {
        case "us-east-1" => Region.US_EAST_1
        case "us-east-2" => Region.US_EAST_2
        case "us-west-1" => Region.US_WEST_1
        case "us-west-2" => Region.US_WEST_2
        case _ => Region.US_EAST_1
    }
}

object AwsDynamoProxy {
    def apply(accessKeys: AwsAccessKeys, tableName: String) = new AwsDynamoProxy(accessKeys, tableName)
}

case class AwsDynamoProxy(accessKeys: AwsAccessKeys, tableName: String ) extends DatabaseProxy {

  def getTable(dynamo: DynamoDB, table: String) : Table = {
      dynamo.table(tableName).get
  }

  def put(primaryKey: String, attributes : Seq[(String, Any)]): IO[Unit] = {
      implicit val region = accessKeys.getRegion
      implicit val awsDynamoDB: DynamoDB = DynamoDB(accessKeys.getAccessKey, accessKeys.getSecreateAccessKey)
      val dynamoTable: Table = getTable(awsDynamoDB, tableName)
      IO(dynamoTable.putAttributes(primaryKey, attributes))
  }
}
