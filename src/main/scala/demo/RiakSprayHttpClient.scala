package demo

import akka.actor.ActorSystem
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import demo.Model.Payload
import spray.can.Http
import spray.http.ContentTypes.`application/json`
import spray.http.HttpMethods._
import spray.http._
import spray.json._

import scala.concurrent.{ ExecutionContext, Future }

object RiakSprayHttpClient {
  def put(uri: String, data: Payload)(implicit system: ActorSystem, timeout: Timeout, ec: ExecutionContext): Future[HttpResponse] = {
    val request = HttpRequest(PUT, Uri(uri), entity = HttpEntity(`application/json`, data.toJson.compactPrint))
    execute(request)
  }

  def get(uri: String)(implicit system: ActorSystem, timeout: Timeout, ec: ExecutionContext): Future[Payload] = {
    val request = HttpRequest(GET, Uri(uri))
    execute(request).map { response =>
      val data = response.entity.asString
      system.log.info(s"[Spray] Response data: $data")
      data.parseJson.convertTo[Payload]
    }
  }

  private def execute(request: HttpRequest)(implicit system: ActorSystem, timeout: Timeout, ec: ExecutionContext): Future[HttpResponse] =
    IO(Http).ask(request).mapTo[HttpResponse]
}
