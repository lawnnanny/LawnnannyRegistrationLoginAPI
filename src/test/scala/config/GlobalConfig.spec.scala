package lambdas.config

import org.scalatest._
import org.scalamock.scalatest.{AsyncMockFactory, MockFactory}
import awscala._
import lambdas.config._
import lambdas.config.GlobalConfigs._
import lambdas.config.AWSConfigProtocol.awsConfigFormat


class GlobalConfigTest extends FunSpec with Matchers with MockFactory {
    describe("GlobalConfig") {
        it("Given there is string of a JSON returned by fromResource It should return a Config") {

            val testjson = "{ \"AWS_ACCESS_KEY\":\"test1\", \"SECRET_AWS_ACCESS_KEY\":\"test2\", \"REGION\":\"test3\" }"
            val testAwsConfig = new Config {
                override def getJsonStringResourcesString(file: String): String = testjson
            }
            val resultAwsConfig  = testAwsConfig.awsConfig[AWSConfig]("this is not used")
            assert(resultAwsConfig.AWS_ACCESS_KEY.equals("test1"))
            assert(resultAwsConfig.SECRET_AWS_ACCESS_KEY.equals("test2"))
            assert(resultAwsConfig.REGION.equals("test3"))
        }
    }
}
