package lambdas.database

import scala.language.higherKinds
import com.amazonaws.auth.AWSCredentials
import cats.effect.{Async, IO, Sync}
import lambdas.config._

object AwsDynamoProxyFactory {
    implicit val IOProxyFactory = new AwsDynamoProxyFactory[IO]
}

class AwsDynamoProxyFactory[F[_]: Sync] {
    def apply(tableName: String)(implicit awsCredentials: AWSConfig) : AwsDynamoProxy[F] = {
        AwsDynamoProxy[F](new AwsAccessKeys(awsCredentials), tableName)
    }
}
