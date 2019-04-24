package lambdas.config

import org.scalatest._
import org.scalamock.scalatest.{AsyncMockFactory, MockFactory}
import awscala._
import lambdas.config._
import lambdas.config.GlobalConfigs._
import lambdas.config.AWSConfigProtocol.awsConfigFormat
import lambdas.config.UserSessionConfigProtocol.userSessionConfigFormat

class GlobalConfigTest extends FunSpec with Matchers with MockFactory {
    describe("GlobalConfig") {
        it("Given there is string of a JSON returned by fromResource it should return a AWSConfig") {
            val testjson = "{ \"AWS_ACCESS_KEY\":\"test1\", \"SECRET_AWS_ACCESS_KEY\":\"test2\", \"REGION\":\"test3\" }"
            val testAwsConfig = new Config {
                override def getJsonStringResourcesString(file: String): String = testjson
            }
            val resultAwsConfig = testAwsConfig.sprayConfig[AWSConfig]("this is not used")
            assert(resultAwsConfig.AWS_ACCESS_KEY.equals("test1"))
            assert(resultAwsConfig.SECRET_AWS_ACCESS_KEY.equals("test2"))
            assert(resultAwsConfig.REGION.equals("test3"))
        }
        it("Given there is a string of a JSON returned by fromResource it should return a userSessionConfig") {
            val testjson = "{ \"EXPIRATION_FOR_SESSION\":\"test1\", \"SECRET_KEY\":\"test2\" }"
            val testUserSessionConfig = new Config {
                override def getJsonStringResourcesString(file: String): String = testjson
            }
            val resultAwsConfig = testUserSessionConfig.sprayConfig[UserSessionConfig]("this is not used")
            assert(resultAwsConfig.EXPIRATION_FOR_SESSION.equals("test1"))
            assert(resultAwsConfig.SECRET_KEY.equals("test2"))
        }
    }
}
