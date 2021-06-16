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

  // Inserting data without charset (UTF-8) is failing to read by spray client
  Await.result(RiakAkkaHttpClient.put(Payload("é")), 1.second)

  val sprayGetDataFuture: Future[Model.Payload] = RiakSprayHttpClient.get()
  val akkaHttpGetDataFuture: Future[Model.Payload] = RiakAkkaHttpClient.get()

  Future.sequence(List(sprayGetDataFuture, akkaHttpGetDataFuture)).foreach { data =>
    println("-" * 40)
    println(s"Spray data ${data.head}")
    println(s"AkkaHttp data ${data.last}")
    println("-" * 40)
    system.terminate()
  }

}
