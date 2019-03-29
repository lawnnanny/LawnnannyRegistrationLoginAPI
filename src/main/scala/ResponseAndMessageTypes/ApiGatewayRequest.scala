package lambdas.ResponseAndMessageTypes

import scala.beans.BeanProperty

class ApiGatewayRequest(@BeanProperty var body: String,
                        @BeanProperty var queryStringParameters: java.util.Map[String, Object],
                        @BeanProperty var headers: java.util.Map[String, Object])
{
  def this() = this("", new java.util.HashMap[String, Object], new java.util.HashMap[String, Object])
}