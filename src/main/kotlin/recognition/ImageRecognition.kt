package recognition

import com.google.gson.Gson
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.utils.URIBuilder
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils

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

            request.setHeader("Content-Type", "application/json")
            request.setHeader("Ocp-Apim-Subscription-Key", info.key1)

            val url = "https://photos-5.dropbox.com/t/2/AADoP_f1cZuDTvkKKsw5QH1ttmdH7DL7BcQPu3w3mhgItQ/12/62224416/png/32x32/3/1496250000/0/2/img-2017-05-31-00-41-55.png/EJW5mjAYhTsgAigC/Z3kFDJfuNu9MvO8bloB5HKBnCpbPJqow3KvsyF4pVqc?dl=0&size=1600x1200&size_mode=3"
            val requestEntity = StringEntity("{\"url\":\"$url\"}")
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