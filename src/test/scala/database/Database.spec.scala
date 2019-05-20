package lambdas.database

import java.util
import java.util.Map
import awscala._
import awscala.dynamodbv2._
import cats.effect.IO
import com.amazonaws.services.dynamodbv2.model
import com.amazonaws.services.{dynamodbv2 => aws}
import lambdas.config.AWSConfig
import org.scalacheck._
import org.scalamock.scalatest.MockFactory
import org.scalatest._

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
                val testPrimaryKey: String = Gen.alphaNumChar.toString

                class tableAdapter extends Table("adf", "asdf")  {
                    def put(primaryKey: String,seq: (String, Any) )(implicit dynamo: DynamoDB) = Unit
                }
                val testTableAdapter = stub[tableAdapter]

                (testTableAdapter.put ( _:String, _: (String, Any) )(_: DynamoDB))
                    .when(testPrimaryKey,"testkey" -> "testValue" ,dynamoStub)
                    .returning(Unit)

                (dynamoStub.table (_: String))
                    .when(testTableName).returning(Some(testTableAdapter))

                class testAwsDynamoProxy extends AwsDynamoProxy[IO, UserTable](testAccessKeys, testTableName) {
                    override def getTable(dynamo: DynamoDB, table: String): Table = testTableAdapter
                }

                val subClassTestAwsDynamoProxy = new testAwsDynamoProxy
                assert(subClassTestAwsDynamoProxy.put(testPrimaryKey, "testkey" -> "testValue").isInstanceOf[IO[Unit]])
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
                  val testSequence : Seq[(String, Any)] = (1 to 10).map((n: Int) => (Gen.alphaNumChar.toString, Gen.alphaNumChar.toString))

                  val testAtributes = new java.util.Map[String, aws.model.AttributeValue] {
                    override def size(): Int = 0

                    override def isEmpty: Boolean = true

                    override def containsKey(key: Any): Boolean = true

                    override def containsValue(value: Any): Boolean = true

                    override def get(key: Any): model.AttributeValue = new AttributeValue

                    override def put(key: String, value: model.AttributeValue): model.AttributeValue = new AttributeValue

                    override def remove(key: Any): model.AttributeValue = new AttributeValue

                    override def putAll(m: util.Map[_ <: String, _ <: model.AttributeValue]): Unit = Unit

                    override def clear(): Unit = Unit

                    override def keySet(): util.Set[String] = new util.HashSet[String]

                    override def values(): util.Collection[model.AttributeValue] = new util.HashSet[model.AttributeValue]

                    override def entrySet(): util.Set[Map.Entry[String, model.AttributeValue]] = new util.HashSet[Map.Entry[String, model.AttributeValue]]
                  }

                  class tableAdapter extends Table("adf", "asdf")  {
                     override def get(primaryKey: Any)(implicit dynamo: DynamoDB) = {
                        Some(Item(this, testAtributes))
                     }
                  }

                  val testTableAdapter = new tableAdapter
                  val testItem = Item(testTableAdapter, testAtributes)

                  (dynamoStub.table (_: String))
                    .when(testTableName)
                    .returning(Some(testTableAdapter))

                  class testAwsDynamoProxy extends AwsDynamoProxy[IO, UserTable](testAccessKeys, testTableName) {
                      override def getTable(dynamo: DynamoDB, table: String): Table = {
                          testTableAdapter
                      }
                  }

                  val subClassTestAwsDynamoProxy = new testAwsDynamoProxy
                  val returnedValue = subClassTestAwsDynamoProxy.get(testPrimaryKey).unsafeRunSync().get
                  assert(returnedValue.equals(testItem))
              }
          }
      }
  }
}
