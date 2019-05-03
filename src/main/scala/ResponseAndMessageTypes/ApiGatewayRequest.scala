package lambdas.ResponseAndMessageTypes

import scala.beans.BeanProperty

class UserNameAndPasswordEvent(@BeanProperty var username: String, @BeanProperty var password: String)
{
  def this() = this("", "")
}
