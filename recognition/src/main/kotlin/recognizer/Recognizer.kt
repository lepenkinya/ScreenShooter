package recognizer

import opencv.OpenCVTest
import org.apache.commons.io.FileUtils
import java.io.File


fun recognize(filePath: String, tessPath: String, debugDir: File?): RecognitionResult {
    val file = File(filePath)
    val newImagePath = OpenCVTest.preprocess(file.absolutePath)

    if (debugDir != null) {
        FileUtils.copyFile(File(newImagePath), File(debugDir, "after_preprocess.png"))
    }

    val newFile = File(newImagePath)

    val path = if (newFile.exists()) newFile.absolutePath else file.absolutePath

    return try {
        val text = TessIntegration.instance.recognize(path, tessPath, debugDir)
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