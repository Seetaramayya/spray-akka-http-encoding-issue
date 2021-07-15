package demo

import akka.actor.ActorSystem
import akka.http.scaladsl.model.ContentTypes.`application/json`
import akka.http.scaladsl.model.StatusCodes
import akka.stream.{ ActorMaterializer, Materializer }
import akka.util.Timeout
import demo.Model.Payload
import demo.RiakAkkaHttpClient.`application/json-UTF-8`
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

class SprayAkkaHttpInCompatibleDefaultEncodingSpec extends AsyncWordSpec with Matchers {
  private implicit val system: ActorSystem = ActorSystem()
  private implicit val materializer: Materializer = ActorMaterializer()
  private implicit val ec: ExecutionContext = system.dispatcher
  private implicit val timeout: Timeout = Timeout(1.second)

  private val uri = "http://localhost:8098/buckets/demo-bucket/keys/foo"
  private val data = Payload("é")

  "Akka HttpClient stores data with default encoding, then spray" should {
    "read data in the correct way (failing due to spray default encoding)" ignore {
      val futureResponse = RiakAkkaHttpClient.put(uri, data, `application/json`)
      futureResponse.map { response => response.status shouldBe StatusCodes.NoContent }

      val futurePayloads: Future[(Payload, Payload)] = readAkkaAndSprayPayload

      futurePayloads.map {
        case (akkaHttpPayload, sprayHttpPayload) => akkaHttpPayload shouldBe sprayHttpPayload
      }
    }

    "read data in the correct way" in {
      val futureResponse = RiakAkkaHttpClient.put(uri, data, `application/json-UTF-8`)
      futureResponse.map { response => response.status shouldBe StatusCodes.NoContent }

      val futurePayloads: Future[(Payload, Payload)] = readAkkaAndSprayPayload

      futurePayloads.map {
        case (akkaHttpPayload, sprayHttpPayload) => akkaHttpPayload shouldBe sprayHttpPayload
      }
    }
  }

  private def readAkkaAndSprayPayload: Future[(Payload, Payload)] =
    RiakAkkaHttpClient.get(uri).flatMap(akkaPayload => RiakSprayHttpClient.get(uri).map(sprayPayload => (akkaPayload, sprayPayload)))
}
