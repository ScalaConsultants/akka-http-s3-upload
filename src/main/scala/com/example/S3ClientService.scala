package com.example

import java.io.File
import java.net.URL
import java.nio.file.Path
import com.amazonaws.services.s3.transfer.{ Upload, TransferManager, TransferManagerBuilder }
import akka.actor.ActorSystem
import akka.http.scaladsl.server.directives.FileInfo
import akka.stream.Materializer
import akka.stream.alpakka.s3.impl.ListBucketVersion2
import akka.stream.alpakka.s3.{ MemoryBufferType, S3Settings }
import akka.stream.alpakka.s3.scaladsl.{ MultipartUploadResult, S3Client }
import akka.stream.scaladsl.Sink
import akka.util.ByteString
import com.amazonaws.auth.{ AWSStaticCredentialsProvider, BasicAWSCredentials, DefaultAWSCredentialsProviderChain }
import com.amazonaws.event.{ ProgressEvent, ProgressEventType, ProgressListener }
import com.amazonaws.regions.{ AwsRegionProvider, Region, Regions }
import com.amazonaws.services.s3.{ AmazonS3, AmazonS3Client, AmazonS3ClientBuilder }
import com.amazonaws.services.s3.model.{ PutObjectRequest, UploadPartRequest }

import scala.concurrent.{ Future, Promise }

case class S3UploaderException(msg: String) extends Exception(msg)

object S3ClientService extends S3ClientService

trait S3ClientService extends S3Conf {

  private val s3Client: AmazonS3 = new AmazonS3Client(credentialsProvider.getCredentials())

  def upload(filePath: Path): Future[String] = {
    val fileName = filePath.getFileName().toString()
    val key = s"temp/${fileName}"

    val file: File = new File(filePath.normalize().toString())

    val promise = Promise[String]()
    val listener = new FileUploadProgressListener(key, promise)

    val request = new PutObjectRequest(bucketName, key, file)
    request.setGeneralProgressListener(listener)

    s3Client.putObject(request)

    promise.future
  }

  def uploadMultipart(filePath: Path): Future[String] = {
    val fileName = filePath.getFileName().toString()
    val key = s"temp-multipart/${fileName}"

    val file: File = new File(filePath.normalize().toString())

    val tm: TransferManager = TransferManagerBuilder.standard()
      .withS3Client(s3Client)
      .build()

    val promise = Promise[String]()
    val listener = new FileUploadProgressListener(key, promise)

    val upload: Upload = tm.upload(bucketName, key, file)
    upload.addProgressListener(listener)

    promise.future
  }

  class FileUploadProgressListener(key: String, promise: Promise[String]) extends ProgressListener {
    override def progressChanged(progressEvent: ProgressEvent): Unit = progressEvent.getEventType match {
      case ProgressEventType.TRANSFER_FAILED_EVENT =>
        promise.failure(S3UploaderException(s"Uploading a file with a key: $key"))
      case ProgressEventType.TRANSFER_COMPLETED_EVENT | ProgressEventType.TRANSFER_CANCELED_EVENT =>
        promise.success(key)
    }
  }

}
