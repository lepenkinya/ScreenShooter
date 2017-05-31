package recognition

import com.google.gson.Gson
import opencv.OpenCVTest
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.utils.URIBuilder
import org.apache.http.entity.FileEntity
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils
import java.io.File

val gson = Gson()


class CognitiveResponse {
    var language: String = ""
    var regions: List<Region> = emptyList()

    fun allLines(): List<Coordinates> {
        return regions.flatMap { it.lines }.map { it.coordinates }
    }

    fun words(): List<Coordinates> {
        return regions.flatMap { it.lines }.flatMap { it.words }.map { it.coordinates }
    }
}

class Region {
    var boundingBox: String = ""
    var lines: List<Line> = emptyList()
}

data class Coordinates(val x_left: Int, val y_up: Int, val width: Int, val height: Int)

fun String.coordinates(): Coordinates {
    val values = split(",").map { it.trim().toInt() }
    return Coordinates(values[0], values[1], values[2], values[3])
}

class Line {
    var boundingBox: String = ""
    var words: List<Word> = emptyList()

    val coordinates: Coordinates
        get() = boundingBox.coordinates()
}

class Word {
    var boundingBox: String = ""
    var text: String = ""

    val coordinates: Coordinates
        get() = boundingBox.coordinates()
}


object CognitiveApi {
    class Info(val endpoint: String, val key1: String, val key2: String)

    val info by lazy {
        val text = CognitiveApi::class.java.classLoader.getResource("api.json").readText()
        gson.fromJson<Info>(text, Info::class.java)
    }


    fun check(inputName: String) {
        val httpClient = DefaultHttpClient()

        try {
            val uriBuilder = URIBuilder("${info.endpoint}/ocr")

            uriBuilder.setParameter("language", "en")
            uriBuilder.setParameter("detectOrientation ", "true")

            val uri = uriBuilder.build()
            val request = HttpPost(uri)

            request.setHeader("Content-Type", "application/octet-stream")
            request.setHeader("Ocp-Apim-Subscription-Key", info.key1)

            val file = File(inputName)
            val requestEntity = FileEntity(file)

            request.entity = requestEntity

            val response = httpClient.execute(request)
            val entity = response.entity

            if (entity != null) {
                val text = EntityUtils.toString(entity)
                println(text)

                val region = gson.fromJson(text, CognitiveResponse::class.java)

                val allWords = region.words()
                val allLines = region.allLines()

                OpenCVTest.addRectangles(inputName, allWords, allLines)
                OpenCVTest.filterTextRectangles(inputName, allWords, 20, OpenCVTest.epsilon, 0.5);

                region.regions.forEach {
                    println("REGION_START")
                    it.lines.forEach {
                        println(it.words.joinToString(" ", transform = { it.text }))
                    }
                    println("REGION_END")
                }
            }

        } catch (e: Exception) {
            println(e.message)
        }

    }

}


fun main(args: Array<String>) {
    CognitiveApi.check("xxx.png")
    println()
}