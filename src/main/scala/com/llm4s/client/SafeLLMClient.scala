package com.llm4s.client

import scala.concurrent.{Future, ExecutionContext}
import scala.util.{Failure, Success}
import java.net.{HttpURLConnection, URL}
import java.io.{BufferedReader, InputStreamReader, OutputStreamWriter}

/**
 * SafeLLMClient
 *
 * Provides safe asynchronous LLM API calls with proper error handling.
 * Prevents application crashes caused by unhandled Future failures.
 */
object SafeLLMClient {

  /**
   * Public method to safely call LLM API
   */
  def safeLLMCall(
      prompt: String,
      model: String = "gpt-4o-mini",
      apiKey: String
  )(implicit ec: ExecutionContext): Future[String] = {

    Future {
      callLLMApi(prompt, model, apiKey)
    }.recover {
      case ex: Throwable =>
        println(s"[ERROR] LLM request failed: ${ex.getMessage}")
        "Error: Unable to process LLM request at this time."
    }
  }

  /**
   * Internal method to call LLM API
   * (Basic HTTP implementation — can be replaced with sttp or Akka HTTP)
   */
  private def callLLMApi(
      prompt: String,
      model: String,
      apiKey: String
  ): String = {

    val url = new URL("https://api.openai.com/v1/chat/completions")
    val connection = url.openConnection().asInstanceOf[HttpURLConnection]

    connection.setRequestMethod("POST")
    connection.setRequestProperty("Content-Type", "application/json")
    connection.setRequestProperty("Authorization", s"Bearer $apiKey")
    connection.setDoOutput(true)

    val requestBody =
      s"""
         |{
         |  "model": "$model",
         |  "messages": [
         |    {"role": "user", "content": "$prompt"}
         |  ],
         |  "temperature": 0.7
         |}
         |""".stripMargin

    val writer = new OutputStreamWriter(connection.getOutputStream)
    writer.write(requestBody)
    writer.flush()
    writer.close()

    val responseCode = connection.getResponseCode

    if (responseCode != 200) {
      throw new RuntimeException(s"API request failed with status $responseCode")
    }

    val reader = new BufferedReader(
      new InputStreamReader(connection.getInputStream)
    )

    val response = Iterator.continually(reader.readLine())
      .takeWhile(_ != null)
      .mkString("\n")

    reader.close()
    response
  }
}
git add src/main/scala/com/llm4s/client/SafeLLMClient.scala
git commit -m "Fix: Add SafeLLMClient with proper Future error handling"
git push origin fix-scala-future-error-handling
