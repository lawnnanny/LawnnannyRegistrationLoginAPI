package lambdas.database

import cats.effect.{IO, Sync}
import lambdas.config.GlobalConfigs.AWSConfig
import lambdas.config._

import scala.language.higherKinds

trait DynamoTable
trait UserTable extends DynamoTable

class AwsDynamoProxyFactory {
    def apply[A[_]: Sync ,T <: DynamoTable](tableName: String)(implicit awsCredentials: AWSConfig) : AwsDynamoProxy[A, T] = {
        AwsDynamoProxy[A, T](new AwsAccessKeys(awsCredentials), tableName)
    }
}

object flyweight {
    val proxyFactory = new AwsDynamoProxyFactory
    implicit val ioUserTable: AwsDynamoProxy[IO, UserTable] = proxyFactory[IO, UserTable]("UserTable")
}
