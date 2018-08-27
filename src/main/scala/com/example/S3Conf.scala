package com.example

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider

object S3Conf extends S3Conf

trait S3Conf {

  val bucketName = "tmp-file-upload-test"

  val region = "eu-central-1"

  val credentialsProvider = new EnvironmentVariableCredentialsProvider()

}
