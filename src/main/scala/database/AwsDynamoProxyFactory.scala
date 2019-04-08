package lambdas.database

import com.amazonaws.auth.AWSCredentials
import lambdas.config._


object AwsDynamoProxyFactory {
    implicit val proxyFactory = new AwsDynamoProxyFactory
}

class AwsDynamoProxyFactory {
    def apply(tableName: String)(implicit awsCredentials: AWSConfig) : AwsDynamoProxy = {
        println(awsCredentials.AWS_ACCESS_KEY + " here I am")
        AwsDynamoProxy(new AwsAccessKeys(awsCredentials), tableName)
    }
}
