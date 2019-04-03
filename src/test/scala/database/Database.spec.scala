package lambdas.database

import org.scalatest._
import org.scalamock.scalatest.MockFactory
import awscala._
import dynamodbv2._
import org.scalacheck._
import lambdas.database._

class DatabaseTest extends FunSpec with Matchers with MockFactory {

  describe("Database Tests") {
      describe("AccessKeys") {
          it("Access Keys Equality Works") {
            val testAccessString = Gen.alphaNumChar.toString
            val testSecretKey = Gen.alphaNumChar.toString
            val accessKey1 = new AwsAccessKeys(testAccessString, testSecretKey, Region.US_EAST_1)
            val accessKey2 = new AwsAccessKeys(testAccessString, testSecretKey, Region.US_EAST_1)
            assert(accessKey1.equals(accessKey2))
          }
      }

      describe("DynamoDBProxy") {
          it("AwsDynamoProxy apply companion object should return an instance of AwsDynamoProxy") {
            val testAccessString = Gen.alphaNumChar.toString
            val testSecretKey = Gen.alphaNumChar.toString
            val accessKeys: AwsAccessKeys = new AwsAccessKeys(testAccessString, testSecretKey, Region.US_EAST_1)
            val correctAwsDynamoProxy = new AwsDynamoProxy(accessKeys)
            assert(correctAwsDynamoProxy.equals(AwsDynamoProxy(accessKeys)))
          }
      }
  }
}
