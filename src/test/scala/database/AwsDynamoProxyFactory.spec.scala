package lambdas.database

import cats.effect.IO
import lambdas.database.flyweight.ioUserTable
import org.scalamock.scalatest.MockFactory
import org.scalatest._

class AwsDynamoProxyFactoryTest extends FunSpec with Matchers with MockFactory {
  describe("AwsDynamoProxyFactory") {
      it("should have a implicit AwsDynamoProxy[IO, UserTable]") {
          val proxyFactory = implicitly[AwsDynamoProxy[IO, UserTable]]
          assert(proxyFactory.isInstanceOf[AwsDynamoProxy[IO, UserTable]])
      }
  }
}
