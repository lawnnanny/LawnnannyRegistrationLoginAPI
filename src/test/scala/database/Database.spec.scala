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

class DatabaseTest extends FunSpec with Matchers with MockFactory {

  describe("Database Tests") {
      describe("AwsAccessKeys") {
          it("Access Key Returns Correct Region for US-EAST-1") {
            val testAccessString = Gen.alphaNumChar.toString
            val testSecretKey = Gen.alphaNumChar.toString
            val testRegion = "US-EAST-1"
            val accessKey = new AwsAccessKeys(new AWSConfig(testAccessString, testSecretKey, testRegion))
            assert(accessKey.getRegion.equals(Region.US_EAST_1))
          }
      }

      describe("AwsDynamoProxy") {
          describe("put") {
              it("AwsDynamoProxy put given a primaryKey and a sequence of attributes should return an IO") {
                val dynamoStub = stub[DynamoDB]
                val accessKey = Gen.alphaNumChar.toString
                val secreteAccessKey = Gen.alphaNumChar.toString
                val testRegion = Gen.alphaNumChar.toString
                val testTableName = Gen.alphaNumChar.toString
                val testAwsConfig = new AWSConfig(accessKey, secreteAccessKey, testRegion)
                val testAccessKeys = new AwsAccessKeys(testAwsConfig)
                val testSequence : Seq[(String, Any)] = (1 to 10).map((n: Int) => (Gen.alphaNumChar.toString, Gen.alphaNumChar.toString))
                val testPrimaryKey: String = Gen.alphaNumChar.toString

                class tableAdapter extends Table("adf", "asdf")  {
                    def put(primaryKey: String,seq: Seq[(String, Any)])(implicit dynamo: DynamoDB) = Unit
                }
                val testTableAdapter = stub[tableAdapter]
                (testTableAdapter.put ( _:String, _:Seq[(String, Any)] )(_: DynamoDB))
                    .when(testPrimaryKey,testSequence,dynamoStub)
                    .returning(Unit)

                (dynamoStub.table (_: String))
                    .when(testTableName).returning(Some(testTableAdapter))
                class testAwsDynamoProxy extends AwsDynamoProxy[IO](testAccessKeys, testTableName) {
                    override def getTable(dynamo: DynamoDB, table: String): Table = testTableAdapter
                }

                val subClassTestAwsDynamoProxy = new testAwsDynamoProxy
                assert(subClassTestAwsDynamoProxy.put(testPrimaryKey, testSequence).isInstanceOf[IO[Unit]])
              }
          }
          describe("get") {
              it("should return a value given a primaryKey") {
                  val dynamoStub = stub[DynamoDB]

                  val testData = Gen.alphaNumChar.toString
                  val accessKey = Gen.alphaNumChar.toString
                  val secreteAccessKey = Gen.alphaNumChar.toString
                  val testRegion = Gen.alphaNumChar.toString
                  val testTableName = Gen.alphaNumChar.toString
                  val testPrimaryKey: String = Gen.alphaNumChar.toString
                  val testAwsConfig = new AWSConfig(accessKey, secreteAccessKey, testRegion)
                  val testAccessKeys = new AwsAccessKeys(testAwsConfig)
                  class tableAdapter extends Table("adf", "asdf")  {
                      def get(primaryKey: String)(implicit dynamo: DynamoDB) = Some(testData)
                  }
                  val testTableAdapter = stub[tableAdapter]
                  class testAwsDynamoProxy extends AwsDynamoProxy[IO](testAccessKeys, testTableName) {
                      override def getTable(dynamo: DynamoDB, table: String): Table = testTableAdapter
                  }

                  (testTableAdapter.get ( _:String)(_: DynamoDB))
                      .when(testPrimaryKey, dynamoStub)
                      .returning(Some(testData))

                  (dynamoStub.table (_: String))
                      .when(testTableName).returning(Some(testTableAdapter))

                  val subClassTestAwsDynamoProxy = new testAwsDynamoProxy
                  assert(subClassTestAwsDynamoProxy.get(testPrimaryKey).equals(Some(testData)))
              }
          }
      }
  }
}
