package demo

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.util.Timeout
import demo.Model.Payload

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

object SprayAkkaHttpEncoding extends App {
  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val timeout = Timeout(1.second)
  implicit val materializer = ActorMaterializer()
  private val InitData = "é"
  blockingAkkaHttpInsert(InitData)

  // You can observe data explosion in the riak database
  (1 to 20).foreach { i =>
    val sprayGetData: Model.Payload = Await.result(RiakSprayHttpClient.get(), 1.second)
    val akkaHttpGetData: Model.Payload = Await.result(RiakAkkaHttpClient.get(), 1.second)

    println(s"[Spray]    Attempt: $i, data: $sprayGetData")
    println(s"[AkkaHttp] Attempt: $i, data: $akkaHttpGetData")

    if ( i % 2 == 0 ) blockingSprayHttpInsert(sprayGetData.data) else blockingAkkaHttpInsert(akkaHttpGetData.data)
  }

  private def blockingAkkaHttpInsert(data: String): Unit = {
    // Inserting data without charset (UTF-8) is failing to read by spray client
    Await.result(RiakAkkaHttpClient.put(Payload(data)), 1.second)
  }

  private def blockingSprayHttpInsert(data: String): Unit = Await.result(RiakSprayHttpClient.put(Payload(data)), 1.second)

  system.terminate()

}
