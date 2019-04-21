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
import lambdas.database.flyweight.ioUserTable

class AwsDynamoProxyFactoryTest extends FunSpec with Matchers with MockFactory {
  describe("AwsDynamoProxyFactory") {
      it("should have a implicit AwsDynamoProxy[IO, UserTable]") {
          val proxyFactory = implicitly[AwsDynamoProxy[IO, UserTable]]
          assert(proxyFactory.isInstanceOf[AwsDynamoProxy[IO, UserTable]])
      }
  }
}
