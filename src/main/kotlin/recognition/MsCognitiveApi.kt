package recognition

import com.google.gson.Gson
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.utils.URIBuilder
import org.apache.http.entity.FileEntity
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils
import java.io.File

val gson = Gson()

object CognitiveApi {
    class Info(val endpoint: String, val key1: String, val key2: String)

    val info by lazy {
        val text = CognitiveApi::class.java.classLoader.getResource("api.json").readText()
        gson.fromJson<Info>(text, Info::class.java)
    }


    fun check() {
        val httpClient = DefaultHttpClient()

        try {
            val uriBuilder = URIBuilder("${info.endpoint}/ocr")

            uriBuilder.setParameter("language", "unk")
            uriBuilder.setParameter("detectOrientation ", "true")

            val uri = uriBuilder.build()
            val request = HttpPost(uri)

            request.setHeader("Content-Type", "application/octet-stream")
            request.setHeader("Ocp-Apim-Subscription-Key", info.key1)


            val file = File("xx.png")
            val requestEntity = FileEntity(file)

            request.entity = requestEntity

            val response = httpClient.execute(request)
            val entity = response.entity

            if (entity != null) {
                println(EntityUtils.toString(entity))
            }

        } catch (e: Exception) {
            println(e.message)
        }

    }

}


fun main(args: Array<String>) {
    CognitiveApi.check()
    println()
}