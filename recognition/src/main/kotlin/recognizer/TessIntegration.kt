package recognizer

import org.zeroturnaround.exec.ProcessExecutor
import java.io.File


class TessIntegration {

    companion object Instance {
        val instance = TessIntegration()
    }

    val doConvert: Boolean = true
    val convertResize: Int = 400

    fun recognize(path: String, testPass: String, convertPath: String): String {
        return runCommandLine(convertIfRequired(path, convertPath), testPass)
    }

    private fun runCommandLine(path: String, tessPath: String): String {
        val file = File(path)
        val directory = file.parent
        val resultFileName = directory + System.lineSeparator() + file.nameWithoutExtension

        val tess = File(tessPath)
        println("Path: " + tess.absolutePath)


        val result = ProcessExecutor()
                .command(tess.absolutePath, "-l", "eng", file.absolutePath, resultFileName)
                .redirectOutput(System.out)
                .redirectError(System.out)
                .readOutput(true)
                .execute()

        println("Tess exit code: ${result.exitValue}")

        return File(resultFileName + ".txt").readText()
    }

    fun convertIfRequired(path: String, convertPath: String): String {
        if (!doConvert) return path


        val file = File(path)
        val directory = file.parent
        val resultFileName = directory + System.lineSeparator() + file.nameWithoutExtension + "_tf.tiff"


        val convertUtilPath = File(convertPath)
        println("Path: " + convertUtilPath.absolutePath)

        ProcessExecutor().command(
                convertUtilPath.absolutePath, "-resize",
                convertResize.toString() + "%",
                "-type",
                "Grayscale",
                resultFileName
        ).redirectError(System.out)
                .redirectOutput(System.out)
                .execute()


        return resultFileName
    }

}