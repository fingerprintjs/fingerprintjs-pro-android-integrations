package com.fingerprintjs.android.fpjs_pro.kotlin_client.http_client


import com.fingerprintjs.android.fpjs_pro.logger.Logger
import com.fingerprintjs.android.fpjs_pro.tools.executeSafe
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.URL
import javax.net.ssl.HttpsURLConnection


interface HttpClient {
    fun performRequest(
        request: Request
    ): RawRequestResult
}

class NativeHttpClient(
    private val logger: Logger
) : HttpClient {
    override fun performRequest(request: Request): RawRequestResult {
        return executeSafe({
            sendPostRequest(request)
        }, RawRequestResult(RequestResultType.ERROR, "Network error".toByteArray()))
    }

    private fun sendPostRequest(request: Request): RawRequestResult {

        val reqParamJson = JSONObject(request.bodyAsMap())
        logger.debug(this, reqParamJson)

        val mURL = URL(request.url)

        try {
            with(mURL.openConnection() as HttpsURLConnection) {
                request.headers.keys.forEach {
                    setRequestProperty(it, request.headers[it])
                }

                doOutput = true
                val wr = OutputStreamWriter(outputStream)
                wr.write(reqParamJson.toString())
                wr.flush()

                logger.debug(this, "URL : $url")
                logger.debug(this, "Response Code : $responseCode")

                BufferedReader(InputStreamReader(inputStream)).use {
                    val response = StringBuffer()

                    var inputLine = it.readLine()
                    while (inputLine != null) {
                        response.append(inputLine)
                        inputLine = it.readLine()
                    }

                    logger.debug(this, "Response : $response")

                    if (responseCode == 200) {
                        return RawRequestResult(
                            RequestResultType.SUCCESS,
                            response.toString().toByteArray()
                        )
                    } else {
                        return RawRequestResult(
                            RequestResultType.ERROR,
                            "Error: response code is $responseCode".toByteArray()
                        )
                    }
                }
            }
        } catch (throwable: Throwable) {
            logger.error(this, throwable.message)
            return RawRequestResult(RequestResultType.ERROR, "${throwable.message}".toByteArray())
        }
    }
}
