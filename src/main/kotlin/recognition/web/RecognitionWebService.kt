package recognition.web

import opencv.OpenCVTest
import recognition.CognitiveApi
import recognition.Integration
import recognition.TessIntegration
import recognition.web.Status.*
import spark.Redirect
import spark.Request
import spark.Response
import spark.Service.ignite
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

fun main(args: Array<String>) {
    val http = ignite()

    val recognitionService = RecognitionWebService()

    http.get("/hello") { _, _ -> "Hello World" }
    http.post("/ocr", { request: Request, response: Response ->
        val type = request.headers("Content-Type")
        println("Type: $type")
        val ext = type.substringAfter("/")
        val fileName = "image.$ext"
        println("fileName: " + fileName)

        val result = recognitionService.ocr(fileName, request.bodyAsBytes())

        response.status(if (result.status == OK) 200 else 500)
        result.text
    })
}


class RecognitionWebService {
    private val counter = AtomicInteger()

    private fun uniqueDirName(): File {
        val uniqueDirName = counter.incrementAndGet().toString()
        val dir = File(uniqueDirName)
        if (dir.exists()) {
            dir.deleteRecursively()
        }
        dir.mkdir()
        return dir
    }

    fun ocr(fileName: String, body: ByteArray): RecognitionResult {
        val dirName = uniqueDirName()
        val filename = dirName.absolutePath + "/$fileName"
        File(filename).writeBytes(body)

        val result: RecognitionResult
        try {
            result = recognize(filename)
        } finally {
            dirName.deleteRecursively()
        }
        return result
    }

}


fun recognize(filePath: String): RecognitionResult {
    val file = File(filePath)
    val newImagePath = OpenCVTest.preprocess(file.absolutePath)
    val newFile = File(newImagePath)

    val path = if (newFile.exists()) newFile.absolutePath else file.absolutePath

    return try {
        val text = TessIntegration.instance.recognize(path)
        RecognitionResult(OK, text)
    } catch (e: Exception) {
        RecognitionResult(FAILED, e.toString())
    }
}

enum class Status {
    OK,
    FAILED,
    NOT_FINISHED
}

class RecognitionResult(val status: Status, val text: String)