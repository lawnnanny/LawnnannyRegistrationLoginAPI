package handlers.UserLogic

import awscala.dynamodbv2._
import cats.{Applicative, Monad}
import cats.data.EitherT
import cats.effect.IO
import cats.effect.concurrent.Ref
import cats.implicits._
import com.github.t3hnar.bcrypt._
import handlers.UserLogic.flyWeight.userLogicOperations
import handlers.UserLogic._
import lambdas.JasonWebTokens.{JasonWebTokenGenerator, _}
import lambdas.ResponseAndMessageTypes.UserNameAndPasswordEvent
import lambdas.config.UserSessionConfig
import lambdas.database.{DatabaseProxy, UserTable}
import lambdas.handlers.RegistrationApiGatewayHandler
import org.scalamock.scalatest.MockFactory
import org.scalatest._
import scala.language.higherKinds
import scala.collection.mutable
import scala.collection.mutable._
import handlers.MessageAndStatus

class UserLogicTest extends FunSpec with Matchers with MockFactory {

  implicit val mockDataBaseProxy: DatabaseProxy[IO, UserTable] = mock[DatabaseProxy[IO, UserTable]]
  implicit val mockJasonWebTokenGenerator: JasonWebTokenGenerator = mock[JasonWebTokenGenerator]
  val testUserNameAndPasswordEvent = new UserNameAndPasswordEvent("username", "password")
  implicit val defaultUserLogic = implicitly[UserLogicOperations]
  val testAttributeValue: AttributeValue = AttributeValue()

  val mockAttributeValue: AttributeValue = testAttributeValue
  val testString = "testString"
  private val correctPassword = "$2a$10$IS19spTbAhtnLr0bR1SpV.lun3HT6I.pw3Q/9e0QfjVovS6pIiClO"

  val attributeValue: AttributeValue = createAttributeValue(correctPassword)
  val listOfAttributes: Seq[Attribute] = createListOfAttributes(attributeValue)
  val testItem = createTestItemIncorrectPassword(listOfAttributes)
  val incorrectBcrypt = "$2a$10$GcuIbGDJqnsquRIcFjDrr.IzBo5alDM/KmpM6sAPO5x.6ErDnM3t6"
  val incorrectPasswordException = createTestItemIncorrectPassword(createListOfAttributes(createAttributeValue("in-correct password")))
  val incorrectPassword = createTestItemIncorrectPassword(createListOfAttributes(createAttributeValue(incorrectBcrypt)))

  def createAttributeValue(password: String) = {
    AttributeValue(s = Some(password))
  }

  def createListOfAttributes(attributeValue: AttributeValue) = {
    Seq(Attribute("test", mockAttributeValue),  Attribute("test", attributeValue))
  }

  def createTestItemIncorrectPassword(listOfAttributes: Seq[Attribute]) = {
    Item(mock[Table],  listOfAttributes)
  }


  describe("UserSessionApiGatewayHandler") {
      describe("handleUserNameSessionRequest") {
        it("Should return an Right with a JWT token") {
          val testUserLogicOperations: UserLogicOperations = new UserLogicOperations {
            override def userExists[F[+_] : Monad](request: UserNameAndPasswordEvent)(implicit awsProxy: DatabaseProxy[F, UserTable]) = {
              EitherT.rightT("User Exists")
            }
            override def passwordIsCorrect[F[+_] : Monad](userNameAndPasswordEvent: UserNameAndPasswordEvent)(implicit awsProxy: DatabaseProxy[F, UserTable]) = {
              EitherT.rightT("Password is correct")
            }

            override def getJwtToken(request: UserNameAndPasswordEvent)(implicit jasonWebTokenGenerator: JasonWebTokenGenerator): String = testString
          }
          val testEither: Either[String, String] = testUserLogicOperations
            .handleUserNameSessionRequest[IO](testUserNameAndPasswordEvent)
            .value
            .unsafeRunSync()

          testEither shouldEqual Right(testString)
        }

        it("Should return a Left with a user doesnt exist error") {
          val testUserLogicOperations: UserLogicOperations = new UserLogicOperations {
            override def userExists[F[+_] : Monad](request: UserNameAndPasswordEvent)(implicit awsProxy: DatabaseProxy[F, UserTable]) = {
              EitherT.leftT("User Does Not Exist")
            }

          }
          val testEither: Either[String, String] = testUserLogicOperations
            .handleUserNameSessionRequest[IO](testUserNameAndPasswordEvent)
            .value
            .unsafeRunSync()

          testEither shouldEqual Left("User Does Not Exist")
        }

        it("Should return a Left with a error about password is not correct") {
          val testUserLogicOperations: UserLogicOperations = new UserLogicOperations {
            override def userExists[F[+_] : Monad](request: UserNameAndPasswordEvent)(implicit awsProxy: DatabaseProxy[F, UserTable]) = {
              EitherT.rightT("User Exists")
            }
            override def passwordIsCorrect[F[+_] : Monad](userNameAndPasswordEvent: UserNameAndPasswordEvent)(implicit awsProxy: DatabaseProxy[F, UserTable]) = {
              EitherT.leftT("Password is not correct")
            }

            override def getJwtToken(request: UserNameAndPasswordEvent)(implicit jasonWebTokenGenerator: JasonWebTokenGenerator): String = testString
          }
          val testEither: Either[String, String] = testUserLogicOperations
            .handleUserNameSessionRequest[IO](testUserNameAndPasswordEvent)
            .value
            .unsafeRunSync()

          testEither shouldEqual Left("Password is not correct")
        }

      }

      describe("getPassword") {
        it("Should return a correct password") {

          (mockDataBaseProxy.get _)
            .expects(testUserNameAndPasswordEvent.username)
            .returning(IO(Some(testItem)))

          val returnedPassword: String = defaultUserLogic.getPassword[IO](testUserNameAndPasswordEvent).unsafeRunSync()

          returnedPassword shouldEqual correctPassword
        }
      }

      describe("getJwtToken") {
        it("Should return JWT token depending on what the jasonWebTokenGenerator returns") {
          implicit val mockUserSessionConfig = mock[UserSessionConfig]
          val fakeJwtToken = "fakeJwtToken"
          (mockJasonWebTokenGenerator.encode (_:LoginRequest)(_:UserSessionConfig))
            .expects(LoginRequest(testUserNameAndPasswordEvent.username), *)
            .returning(fakeJwtToken)

          val returnedJwtToken = defaultUserLogic.getJwtToken(testUserNameAndPasswordEvent)

          returnedJwtToken shouldEqual fakeJwtToken
        }
      }

      describe("userExists") {
        it("should return a Right user doesnt exist") {
          (mockDataBaseProxy.get _)
            .expects(testUserNameAndPasswordEvent.username)
            .returning(IO(None))

          val eitherUserExists = defaultUserLogic.userExists[IO](testUserNameAndPasswordEvent).value.unsafeRunSync()

          eitherUserExists shouldEqual Right("User Does Not Exist")
        }

        it("should return a Left user does exist") {

          val mockItem = mock[Item]
          (mockDataBaseProxy.get _)
            .expects(testUserNameAndPasswordEvent.username)
            .returning(IO(Some(mockItem)))

          val eitherUserExists = defaultUserLogic.userExists[IO](testUserNameAndPasswordEvent).value.unsafeRunSync()

          eitherUserExists shouldEqual Left("User Does Exist")
        }
      }

      describe("passwordIsCorrect") {

        val testUserNameAndPasswordEventWithValidate = new UserNameAndPasswordEvent
        testUserNameAndPasswordEventWithValidate.username = "username1"
        testUserNameAndPasswordEventWithValidate.password = testString

        it("should return a Right with a message if the password is correct") {


          (mockDataBaseProxy.get _)
            .expects(testUserNameAndPasswordEventWithValidate.username)
            .returning(IO(Some(testItem)))

          val passwordIsCorrectEither: Either[String, String] =
            defaultUserLogic
            .passwordIsCorrect[IO](testUserNameAndPasswordEventWithValidate)
            .value
            .unsafeRunSync()

          passwordIsCorrectEither shouldEqual Right("Password Is Valid")
        }

        val passwordIsNotValidMessage = "Password Is Not Valid"
        it("should return a Left with a message that password is not valid because of an exception") {
          val testUserNameAndPasswordEventWithValidate = new UserNameAndPasswordEvent
          testUserNameAndPasswordEventWithValidate.username = "username1"
          testUserNameAndPasswordEventWithValidate.password = testString

          (mockDataBaseProxy.get _)
            .expects(testUserNameAndPasswordEventWithValidate.username)
            .returning(IO(Some(incorrectPasswordException)))

          val passwordIsCorrectEither: Either[String, String] =
            defaultUserLogic
              .passwordIsCorrect[IO](testUserNameAndPasswordEventWithValidate)
              .value
              .unsafeRunSync()

          passwordIsCorrectEither shouldEqual Left(passwordIsNotValidMessage)
        }

        it("should return a Left with a message that password is not valid because its not the correct password") {
          val testUserNameAndPasswordEventWithValidate = new UserNameAndPasswordEvent
          testUserNameAndPasswordEventWithValidate.username = "username1"
          testUserNameAndPasswordEventWithValidate.password = testString

          (mockDataBaseProxy.get _)
            .expects(testUserNameAndPasswordEventWithValidate.username)
            .returning(IO(Some(incorrectPassword)))

          val passwordIsCorrectEither: Either[String, String] =
            defaultUserLogic
              .passwordIsCorrect[IO](testUserNameAndPasswordEventWithValidate)
              .value
              .unsafeRunSync()

          passwordIsCorrectEither shouldEqual Left(passwordIsNotValidMessage)
        }
      }

      describe("handleUserNameRegistration") {
        val userLogicOperations = implicitly[UserLogicOperations]
        val testUserName = "username"
        val testPassword = "password"
        val testUserNameRegistrationEvent : UserNameAndPasswordEvent = new UserNameAndPasswordEvent(testUserName, testPassword)

        it("Should create a user if one doesnt already exist") {

          (mockDataBaseProxy.get _)
            .expects(testUserName)
            .returning(None.pure[IO])

          (mockDataBaseProxy.put _)
            .expects(where {
              (userName : String , body: Any) => {
                val seq = body.asInstanceOf[Seq[(String, Any)]]
                val encryptedPassword :String = seq.head._2.asInstanceOf[String]
                userName == testUserName && seq.head._1 == "Password" && testPassword.isBcrypted(encryptedPassword)
              }
            }).returning(IO{})
          
          val result: MessageAndStatus = userLogicOperations.handleUserNameRegistration[IO](testUserNameRegistrationEvent).unsafeRunSync()

          assert(result.success)
          assert(result.message == "Account Was Created")

        }

        it("Should not create a user if one already exists") {

          val mockItem = mock[Item]

          (mockItem.productIterator _)
            .expects()
            .anyNumberOfTimes

          (mockItem.productPrefix _)
            .expects()
            .anyNumberOfTimes

          (mockDataBaseProxy.get _)
            .expects(testUserName)
            .returning(IO(Some(mockItem)))
          
          (mockDataBaseProxy.put _)
              .expects(*, *)
              .never()
          
          val result: MessageAndStatus = userLogicOperations.handleUserNameRegistration[IO](testUserNameRegistrationEvent).unsafeRunSync()

          assert(!result.success)
          assert(result.message == "Account Already Exists")

        }
      }
  }
}
