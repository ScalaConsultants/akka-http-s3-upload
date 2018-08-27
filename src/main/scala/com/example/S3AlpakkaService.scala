package com.example

import java.io.File
import java.util.UUID
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
import com.amazonaws.services.s3.model.PutObjectRequest

import scala.concurrent.{ Future, Promise }

class S3AlpakkaService()(implicit as: ActorSystem, m: Materializer) extends S3Conf {

  private val s3Client: S3Client = S3Client(credentialsProvider, region)

  def sink(fileInfo: FileInfo): Sink[ByteString, Future[MultipartUploadResult]] = {
    val fileName = UUID.randomUUID().toString + ".tmp"
    val key = s"alpakka/${fileName}"

    s3Client.multipartUpload(bucketName, key)
  }
}
