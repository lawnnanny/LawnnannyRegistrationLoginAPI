package lambdas.database

import org.scalatest._
import org.scalamock.scalatest.{AsyncMockFactory, MockFactory}
import awscala._
import cats.effect.IO
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
            assert(ops.AwsDynamoProxy.equals(AwsDynamoProxy(ops.accessKeys, ops.fakeTableName)))
          }

          it("AwsDynamoProxy put given a primaryKey and a sequence of attributes should return an IO") {
            val dynamoTableStub = stub[Table]
            val testPrimaryKey: String = Gen.alphaNumChar.toString
            val testSequence : Seq[(String, Any)] = (1 to 10).map((n: Int) => (Gen.alphaNumChar.toString, Gen.alphaNumChar.toString))
            (dynamoTableStub.putAttributes (_: Any, _:Seq[(String, Any)])(_: DynamoDB)).when(testPrimaryKey, testSequence, ops.AwsDynamoProxy.awsDynamoDB).returning(Unit)
            ops.AwsDynamoProxy.put(testPrimaryKey, testSequence)(dynamoTableStub)
          }
      }
  }

  object ops {

    val testAccessString = Gen.alphaNumChar.toString
    val testSecretKey = Gen.alphaNumChar.toString
    val fakeTableName = Gen.alphaNumChar.toString
    val accessKeys: AwsAccessKeys = new AwsAccessKeys(testAccessString, testSecretKey, Region.US_EAST_1)
    val AwsDynamoProxy: AwsDynamoProxy = new AwsDynamoProxy(accessKeys, fakeTableName)
  }
}
