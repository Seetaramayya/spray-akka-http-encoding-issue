package demo

import spray.json._
import spray.json.DefaultJsonProtocol._

object Model {
  case class Payload(data: String) {
    override def toString: String = s"Data(payload=$data)"
  }
  object Payload {
    implicit val payloadJsonFormat = jsonFormat1(Payload.apply)
  }
}
