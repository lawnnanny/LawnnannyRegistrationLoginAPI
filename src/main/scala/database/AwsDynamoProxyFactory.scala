package lambdas.database

import scala.language.higherKinds
import cats.Monad
import com.amazonaws.auth.AWSCredentials
import cats.effect.{Async, IO, Sync}
import lambdas.config._
import lambdas.config.GlobalConfigs.AWSConfig

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
