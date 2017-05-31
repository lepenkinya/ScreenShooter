package shooter

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import org.apache.http.util.EntityUtils
import org.apache.http.HttpEntity
import org.apache.http.entity.StringEntity
import org.apache.http.client.methods.HttpPost
import org.apache.log4j.xml.DOMConfigurator.setParameter
import org.apache.http.client.utils.URIBuilder
import org.apache.http.impl.client.DefaultHttpClient


class ScreenShooter : AnAction() {

    init {
        templatePresentation.text = "Just screen shooter action"
        templatePresentation.description = "Blah Blah Blah"
    }


    override fun actionPerformed(e: AnActionEvent) {
        println("HEYYO")
    }
}


object Main {
    @JvmStatic fun main(args: Array<String>) {
        val httpClient = DefaultHttpClient()

        try {
            // NOTE: You must use the same location in your REST call as you used to obtain your subscription keys.
            //   For example, if you obtained your subscription keys from westus, replace "westcentralus" in the
            //   URL below with "westus".
            val uriBuilder = URIBuilder("https://westcentralus.api.cognitive.microsoft.com/vision/v1.0/ocr")

            uriBuilder.setParameter("language", "unk")
            uriBuilder.setParameter("detectOrientation ", "true")

            val uri = uriBuilder.build()
            val request = HttpPost(uri)

            // Request headers.
            request.setHeader("Content-Type", "application/json")

            // NOTE: Replace the "Ocp-Apim-Subscription-Key" value with a valid subscription key.
            request.setHeader("Ocp-Apim-Subscription-Key", "13hc77781f7e4b19b5fcdd72a8df7156")

            // Request body. Replace the example URL with the URL of a JPEG image containing text.
            val requestEntity = StringEntity("{\"url\":\"http://example.com/images/test.jpg\"}")
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