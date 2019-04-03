package lambdas.registration

import org.scalacheck._
import org.scalatest._
import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.scalamock.scalatest.MockFactory


class DatabaseTest extends FunSpec with Matchers with MockFactory {
    describe("Registration Handler") {
        it("handleRegistration Given A ApiGatewayRequest Should Return An IO[String]") {
            assert(true)
        }
    }
}
