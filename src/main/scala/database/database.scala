package lambdas.database

import awscala._
import awscala.dynamodbv2._
import cats.effect.Sync
import lambdas.config.AWSConfig
import scala.language.higherKinds

trait DatabaseProxy[F[_], T <: DynamoTable] {
    def put(primaryKey: String, values: (String, Any)*): F[Unit]
    def get(primaryKey: String): F[Option[Item]]
}

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

case class AwsDynamoProxy[F[_]: Sync, T <: DynamoTable](accessKeys: AwsAccessKeys, tableName: String) extends DatabaseProxy[F, T] {

      def getTable(dynamo: DynamoDB, table: String) : Table = dynamo.table(tableName).get

      override def put(primaryKey: String, values : (String, Any)* ) :F[Unit] = {
          implicit val region = accessKeys.getRegion
          implicit val awsDynamoDB: DynamoDB = DynamoDB(accessKeys.getAccessKey, accessKeys.getSecreateAccessKey)
          val dynamoTable: Table = getTable(awsDynamoDB, tableName)
          Sync[F].delay(dynamoTable.put(primaryKey, values: _*))
      }

      override def get(primaryKey: String): F[Option[Item]] = {
          implicit val region = accessKeys.getRegion
          implicit val awsDynamoDB: DynamoDB = DynamoDB(accessKeys.getAccessKey, accessKeys.getSecreateAccessKey)
          val dynamoTable: Table = getTable(awsDynamoDB, tableName)
          val getValue = dynamoTable.get(primaryKey)
          Sync[F].delay(getValue)
      }
}
