package lambdas.database

import awscala._
import cats.effect.IO
import dynamodbv2._
import cats.syntax.apply._

abstract class DatabaseProxy

abstract class AccessKeys

sealed case class AwsAccessKeys(val accessKey: String, val secretAccessKey: String, val region: Region) extends AccessKeys

object AwsDynamoProxy {
    def apply(accessKeys: AwsAccessKeys, tableName: String) = new AwsDynamoProxy(accessKeys, tableName)
}

sealed case class AwsDynamoProxy(accessKeys: AwsAccessKeys, tableName: String ) extends DatabaseProxy {
    implicit val awsDynamoDB: DynamoDB = DynamoDB(accessKeys.accessKey, accessKeys.secretAccessKey)(accessKeys.region)
    def put(primaryKey: String, attributes : Seq[(String, Any)])(implicit table: Table): IO[Unit] = {
        IO(table.putAttributes(primaryKey, attributes))
    }


    object ops {
        lazy implicit val dynamoTable: Table = awsDynamoDB.table(tableName).get
    }
}
