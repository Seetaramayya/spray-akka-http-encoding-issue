package demo

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.model.ContentTypes.`application/json`
import akka.http.scaladsl.model.HttpMethods.PUT
import akka.http.scaladsl.model.{ ContentType, _ }
import akka.stream.Materializer
import akka.stream.scaladsl.{ Sink, Source }
import akka.util.{ ByteString, Timeout }
import demo.Model.Payload
import spray.json._

import scala.concurrent.{ ExecutionContext, Future }

object RiakAkkaHttpClient {
  // This is the content type which fixes in compatability
  val `application/json-UTF-8`: ContentType.WithFixedCharset = ContentType(MediaTypes.`application/json`.withParams(Map("charset" -> "UTF-8")))

  def inMemoryData(dataBytes: Source[ByteString, Any])(implicit mat: Materializer, timeout: Timeout, ec: ExecutionContext): Future[String] = {
    dataBytes.reduce(_ ++ _).runWith(Sink.head).map(_.utf8String)
  }

  /**
   *  default akka http application/json is breaking spray encoding (because `ISO-8859-1` is the default)
   * @param data to be saved
   * @param contentType to be used for the entity
   * @param uri complete url for riak
   * @return future response body as string
   */
  def put(uri: String, data: Payload, contentType: ContentType = `application/json`)(
      implicit system: ActorSystem,
      mat: Materializer,
      timeout: Timeout,
      ec: ExecutionContext): Future[HttpResponse] = {
    val entity: RequestEntity = HttpEntity(contentType, data.toJson.compactPrint.getBytes)

    Http().singleRequest(HttpRequest(PUT, uri, entity = entity))
  }

  def get(uri: String)(implicit system: ActorSystem, mat: Materializer, timeout: Timeout, ec: ExecutionContext): Future[Payload] = {
    Http()
      .singleRequest(Get(uri))
      .flatMap { response =>
        inMemoryData(response.entity.dataBytes).map { data =>
          system.log.info("[AkkaHttp] Response data: {}", data)
          data.parseJson.convertTo[Payload]
        }
      }
  }
}
