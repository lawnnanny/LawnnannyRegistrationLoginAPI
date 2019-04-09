package lambdas.database

import com.amazonaws.auth.AWSCredentials
import lambdas.config._


object AwsDynamoProxyFactory {
    implicit val proxyFactory = new AwsDynamoProxyFactory
}

class AwsDynamoProxyFactory {
    def apply(tableName: String)(implicit awsCredentials: AWSConfig) : AwsDynamoProxy = {
        AwsDynamoProxy(new AwsAccessKeys(awsCredentials), tableName)
    }
}
