package com.example

import scala.concurrent.Await
import scala.concurrent.duration.Duration

import akka.actor.{ ActorRef, ActorSystem }
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

object Boot extends App with FileUploadRoutes {

  implicit val system: ActorSystem = ActorSystem("helloAkkaHttpServer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val s3AlpakkaService = new S3AlpakkaService()

  lazy val routes: Route = fileUploadRoutes

  Http().bindAndHandle(routes, "0.0.0.0", 8080)

  println(s"Server online at http://0.0.0.0:8080/")

  Await.result(system.whenTerminated, Duration.Inf)
}
