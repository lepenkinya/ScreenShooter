package recognizer

import opencv.OpenCVTest
import java.io.File


fun recognize(filePath: String, tessPath: String, convertPath: String): RecognitionResult {
    val file = File(filePath)
    val newImagePath = OpenCVTest.preprocess(file.absolutePath)
    val newFile = File(newImagePath)

    val path = if (newFile.exists()) newFile.absolutePath else file.absolutePath

    return try {
        val text = TessIntegration.instance.recognize(path, tessPath, convertPath)
        RecognitionResult(Status.OK, text)
    } catch (e: Exception) {
        RecognitionResult(Status.FAILED, e.toString())
    }
}

enum class Status {
    OK,
    FAILED,
    NOT_FINISHED
}

class RecognitionResult(val status: Status, val text: String)