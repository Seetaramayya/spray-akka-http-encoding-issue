package demo

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.model.HttpMethods.PUT
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.util.{ByteString, Timeout}
import demo.Model.Payload
import spray.json._

import scala.concurrent.{ExecutionContext, Future}

object RiakAkkaHttpClient {
  val uri = "http://localhost:8098/buckets/demo-bucket/keys/foo"
  val applicationJsonWithUTF8: ContentType = ContentType(MediaTypes.`application/json`, () => HttpCharsets.`UTF-8`)
  val jsonUTF8MediaType: MediaType.WithFixedCharset = MediaType.applicationWithFixedCharset("json", HttpCharsets.`UTF-8`, fileExtensions = "json")

  val `application/json(UTF-8)`: ContentType.WithFixedCharset = ContentType(jsonUTF8MediaType)
  def inMemoryData(dataBytes: Source[ByteString, Any])(implicit mat: ActorMaterializer, timeout: Timeout, ec: ExecutionContext): Future[String] = {
    dataBytes.reduce(_ ++ _).runWith(Sink.head).map(_.utf8String)
  }

  def put(data: Payload)(implicit system: ActorSystem, mat: ActorMaterializer, timeout: Timeout, ec: ExecutionContext): Future[String] = {
    // Main fix is here: Spray expects UTF-8 charset in application/json but akka http does not specify
    val `application/json(UTF-8)` = ContentType(MediaTypes.`application/json`.withParams(Map("charset" -> "UTF-8")))
    val entity: RequestEntity = HttpEntity(`application/json(UTF-8)`, data.toJson.compactPrint.getBytes)
    val request = HttpRequest(PUT, uri, entity = entity)
    Http()
      .singleRequest(request)
      .flatMap { response =>
        println(s"[AkkaHttp] Put status : ${response.status}")
        println(s"[AkkaHttp] Put Headers : ${response.headers.mkString("\n", "\n\t", "\n")}")
        inMemoryData(response.entity.dataBytes)
      }
      .map { data =>
        println(s"[AkkaHttp] Response in put $data")
        data
      }
  }

  def get()(implicit system: ActorSystem, mat: ActorMaterializer, timeout: Timeout, ec: ExecutionContext): Future[Payload] = {
    Http().singleRequest(Get(uri)).flatMap { response =>
      val status = response.status
      println(s"[AkkaHttp] Get Status : $status")
      val headers = response.headers
      println(s"[AkkaHttp] Get Headers : ${headers.mkString("\n", "\n\t", "\n")}")
      val futureData = inMemoryData(response.entity.dataBytes)

      futureData.map { data =>
        println(s"[AkkaHttp] Get payload: $data")
        data.parseJson.convertTo[Payload]
      }
    }
  }
}
