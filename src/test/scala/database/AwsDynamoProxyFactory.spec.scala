package lambdas.database

import org.scalatest._
import org.scalamock.scalatest.{AsyncMockFactory, MockFactory}
import awscala._
import cats.effect.IO
import dynamodbv2._
import org.scalacheck._
import lambdas.database._
import lambdas.config
import lambdas.config.AWSConfig
import lambdas.config
import lambdas.config.GlobalConfigs.AWSConfig

class AwsDynamoProxyFactoryTest extends FunSpec with Matchers with MockFactory {

  describe("AwsDynamoProxyFactory") {
      it("should have a implicit factory") {
          val proxyFactory = implicitly[AwsDynamoProxyFactory[IO]]
          assert(proxyFactory.isInstanceOf[AwsDynamoProxyFactory[IO]])
      }
      it("should return a AwsDynamoProxy") {
        val accessKey = Gen.alphaNumChar.toString
        val secreteAccessKey = Gen.alphaNumChar.toString
        val testRegion = Gen.alphaNumChar.toString
        val testUserTable = Gen.alphaNumChar.toString

        val awsConfig: AWSConfig = new AWSConfig(accessKey, secreteAccessKey, testRegion)
        val testFactory = implicitly[AwsDynamoProxyFactory[IO]]
        val returnedAwsDynamoProxy = testFactory(testUserTable)(awsConfig)
        val correctAwsDynamoProxy = new AwsDynamoProxy[IO](new AwsAccessKeys(awsConfig), testUserTable)
        assert(returnedAwsDynamoProxy.equals(correctAwsDynamoProxy))
      }
  }
}
