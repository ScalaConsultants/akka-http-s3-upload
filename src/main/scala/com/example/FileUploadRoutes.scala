package com.example

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.Logging
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{ Failure, Success }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.{ Multipart, StatusCodes }
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.FileInfo
import akka.http.scaladsl.server.directives.MethodDirectives.delete
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.MethodDirectives.post
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.stream.scaladsl.Sink
import akka.util.ByteString

import java.io.File
import java.util.UUID

trait FileUploadRoutes {

  implicit def system: ActorSystem

  private lazy val log = Logging(system, classOf[FileUploadRoutes])

  val s3AlpakkaService: S3AlpakkaService

  lazy val fileUploadRoutes: Route =
    pathPrefix("files") {
      post {
        path("temp") {
          extractExecutionContext { implicit executor =>
            storeUploadedFile("file", tempDestination) {
              case (metadata, file) =>
                val uploadFuture = S3ClientService.upload(file.toPath)

                onComplete(uploadFuture) {
                  case Success(result) =>
                    log.info("Uploaded file to: " + result)
                    file.delete()
                    complete(StatusCodes.OK)
                  case Failure(ex) =>
                    log.error(ex, "Error uploading file")
                    file.delete()
                    complete(StatusCodes.FailedDependency, ex.getMessage)
                }
            }
          }
        } ~
          path("temp-multipart") {
            extractExecutionContext { implicit executor =>
              storeUploadedFile("file", tempDestination) {
                case (metadata, file) =>
                  val uploadFuture = S3ClientService.uploadMultipart(file.toPath)

                  uploadFuture.onComplete {
                    case Success(_) => file.delete()
                    case Failure(_) => file.delete()
                  }

                  onComplete(uploadFuture) {
                    case Success(result) =>
                      log.info("Uploaded file to: " + result)
                      complete(StatusCodes.OK)
                    case Failure(ex) =>
                      log.error(ex, "Error uploading file")
                      complete(StatusCodes.FailedDependency, ex.getMessage)
                  }
              }
            }
          } ~
          path("alpakka") {
            extractMaterializer { implicit materializer =>
              fileUpload("file") {
                case (metadata, byteSource) =>
                  val uploadFuture = byteSource.runWith(s3AlpakkaService.sink(metadata))

                  onComplete(uploadFuture) {
                    case Success(result) =>
                      log.info("Uploaded file to: " + result.location.toString)
                      complete(StatusCodes.OK)
                    case Failure(ex) =>
                      log.error(ex, "Error uploading file")
                      complete(StatusCodes.FailedDependency, ex.getMessage)
                  }
              }
            }
          }
      }
    }

  def tempDestination(fileInfo: FileInfo): File =
    File.createTempFile(UUID.randomUUID().toString, ".tmp")

}
