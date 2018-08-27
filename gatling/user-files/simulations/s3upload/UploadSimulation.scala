package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class UploadSimulation extends Simulation {

  import UploadSimulation._

  val httpConf = http
    .baseURL("http://0.0.0.0:8080/")

  val scenarioAlpakka = scenario("Upload file to S3 with alpakka")
    .exec(uploadFileRequest(alpakkaUploadPath, m1FileName))

  val scenarioTmpFile = scenario("Upload file to S3 with tmp file")
    .exec(uploadFileRequest(tmpFilePath, m1FileName))

  val scenarioTmpMultipartFile = scenario("Upload file to S3 with temp file multipart")
    .exec(uploadFileRequest(tmpFileMultipartPath, m1FileName))

  val users = 32

  setUp(
    // scenarioTmpFile.inject(
    //   atOnceUsers(users)
    //   //splitUsers(50) into (atOnceUsers(5)) separatedBy (30 seconds)
    // ).protocols(httpConf),

    // scenarioTmpMultipartFile.inject(
    //   atOnceUsers(users)
    //   //splitUsers(50) into (atOnceUsers(5)) separatedBy (30 seconds)
    // ).protocols(httpConf),

    scenarioAlpakka.inject(
      atOnceUsers(users)
      //splitUsers(50) into (atOnceUsers(5)) separatedBy (30 seconds)
    ).protocols(httpConf)
  )


  def uploadFileRequest(path: String, fileName: String) = http(s"UploadFile: $fileName, endpoint: $path")
    .post(path)
    .formUpload("file", fileName)
}

object UploadSimulation {
  val alpakkaUploadPath = "files/alpakka"
  val tmpFilePath = "files/temp"
  val tmpFileMultipartPath = "files/temp-multipart"

  val kb10FileName = "lorem_ipsum.txt"
  val m1FileName = "photo.jpg"
}
