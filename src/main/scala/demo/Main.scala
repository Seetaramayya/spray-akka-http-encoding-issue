package demo

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.util.Timeout
import demo.Model.Payload

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._

object Main extends App {
  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val timeout = Timeout(1.second)
  implicit val materializer = ActorMaterializer()
  private val uri = "http://localhost:8098/buckets/demo-bucket/keys/foo"
  private val InitData = "é"
  blockingAkkaHttpInsert(InitData)

  // You can observe data explosion in the riak database
  (1 to 20).foreach { i =>
    val sprayGetData: Model.Payload = Await.result(RiakSprayHttpClient.get(uri), 1.second)
    val akkaHttpGetData: Model.Payload = Await.result(RiakAkkaHttpClient.get(uri), 1.second)

    if (i % 2 == 0) blockingSprayHttpInsert(sprayGetData.data) else blockingAkkaHttpInsert(akkaHttpGetData.data)
  }

  private def blockingAkkaHttpInsert(data: String): Unit = {
    // Inserting data without charset (UTF-8) is failing to read by spray client
    Await.result(RiakAkkaHttpClient.put(uri, Payload(data)), 1.second)
  }

  private def blockingSprayHttpInsert(data: String): Unit = Await.result(RiakSprayHttpClient.put(uri, Payload(data)), 1.second)

  system.terminate()

}
