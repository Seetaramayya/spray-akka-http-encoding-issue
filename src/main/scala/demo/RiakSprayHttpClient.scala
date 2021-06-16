package demo

import akka.actor.ActorSystem
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import demo.Model.Payload
import spray.can.Http
import spray.http.HttpMethods._
import spray.http._
import spray.json._

import scala.concurrent.{ExecutionContext, Future}

object RiakSprayHttpClient {
  val uri = "http://localhost:8098/buckets/demo-bucket/keys/foo"

  def put(data: Payload)(implicit system: ActorSystem, timeout: Timeout, ec: ExecutionContext): Future[HttpResponse] = {
    val request = HttpRequest(PUT, Uri(uri), entity = HttpEntity(ContentTypes.`application/json`, data.toJson.compactPrint))
    execute(request)
  }

  def get()(implicit system: ActorSystem, timeout: Timeout, ec: ExecutionContext): Future[Payload] = {
    val request = HttpRequest(GET, Uri(uri))
    execute(request).map { response =>
      val data = response.entity.asString
      println(s"[Spray] Response data: $data")
      data.parseJson.convertTo[Payload]
    }
  }

  def execute(request: HttpRequest)(implicit system: ActorSystem, timeout: Timeout, ec: ExecutionContext): Future[HttpResponse] = {
    IO(Http).ask(request).mapTo[HttpResponse].map { response =>
      println(s"[Spray] Response status: ${response.status}")
      println(s"[Spray] Response headers: ${response.headers.mkString("\n", "\n\t", "\n")}")
      response
    }
  }
}
