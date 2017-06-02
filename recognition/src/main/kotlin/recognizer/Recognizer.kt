package recognizer

import org.apache.commons.io.FileUtils
import java.io.File


fun recognize(filePath: String, tessPath: String, debugDir: File?): RecognitionResult {
    val file = File(filePath)
    val (isDark, lines) = ImagePreprocessor.imageLineFiles(filePath)


    val result = lines.mapIndexed { index, fileName ->
        if (debugDir != null) {
            FileUtils.copyFile(File(fileName), File(debugDir, "${index}_after_preprocess.png"))
        }

        val newFile = File(fileName)
        val path = if (newFile.exists()) newFile.absolutePath else file.absolutePath

        try {
            val text = TessIntegration.instance.recognize(isDark, path, tessPath, debugDir)
            RecognitionResult(Status.OK, text)
        } catch (e: Exception) {
            RecognitionResult(Status.FAILED, e.toString())
        }
    }


    if (result.all { it.status == Status.OK }) {
        val text = result.joinToString("\n", transform = { it.text })
        return RecognitionResult(Status.OK, text)
    }
    else {
        val text = result.find { it.status != Status.OK }?.text ?: ""
        return RecognitionResult(Status.FAILED, text)
    }
}

enum class Status {
    OK,
    FAILED,
    NOT_FINISHED
}

class RecognitionResult(val status: Status, val text: String)