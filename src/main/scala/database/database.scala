package lambdas.database

import awscala._, dynamodbv2._
import cats.syntax.apply._

abstract class DatabaseProxy

abstract class AccessKeys

sealed case class AwsAccessKeys(val accessKey: String, val secretAccessKey: String, val region: Region) extends AccessKeys

object AwsDynamoProxy {
    def apply(accessKeys: AwsAccessKeys) = new AwsDynamoProxy(accessKeys)
}

sealed case class AwsDynamoProxy(accessKeys: AwsAccessKeys ) extends DatabaseProxy {
    private val awsDynamoDB = DynamoDB(accessKeys.accessKey, accessKeys.secretAccessKey)(accessKeys.region)
}
