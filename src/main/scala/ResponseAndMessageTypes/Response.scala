package lambdas.ResponseAndMessageTypes

import scala.beans.BeanProperty

case class Response(@BeanProperty message: String, @BeanProperty request: ApiGatewayRequest)
