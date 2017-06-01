package recognizer

import org.zeroturnaround.exec.ProcessExecutor
import java.io.File


class TessIntegration {

    companion object Instance {
        val instance = TessIntegration()
    }


    fun recognize(path: String, testPass: String): String {
        val newPath = convertIfRequired(path)
        return runCommandLine(newPath, testPass)
    }

    private fun runCommandLine(path: String, tessPath: String): String {
        val file = File(path)
        val directory = file.parent
        val resultFileName = directory + System.lineSeparator() + file.nameWithoutExtension

//        val configFile = File("./tessconfig")
//        if (configFile.exists()) configFile.delete()
//        configFile.createNewFile()
//
//        val user_words = File("./user_words")
//        if (user_words.exists()) user_words.delete()
//        user_words.createNewFile()
//
//        with(user_words) {
//            appendText("clientName\n")
//            appendText("generateUrl\n")
//            appendText("languageCode")
//        }

//        with(configFile) {
//            appendText("load_system_dawg 0\n")
//            appendText("load_freq_dawg 0")
//        }

        val result = ProcessExecutor()
                .command(
                        tessPath,
                        "-l", "eng",
//                        "--user-words", user_words.absolutePath,
                        file.absolutePath, resultFileName //, configFile.absolutePath
                )
                .redirectOutput(System.out)
                .redirectError(System.out)
                .readOutput(true)
                .execute()

        println("Tess exit code: ${result.exitValue}")

        return File(resultFileName + ".txt").readText()
    }

    fun convertIfRequired(path: String): String {
        val file = File(path)
        val directory = file.parent
        val resultFileName = directory + System.lineSeparator() + file.nameWithoutExtension + "_tf.tiff"

        //-density 300
        ProcessExecutor().command(
                "convert",
                path,
                "-resize",
                "300%",
                "-type",
                "Grayscale",
                resultFileName
        ).redirectError(System.out)
                .redirectOutput(System.out)
                .execute()

        return if (File(resultFileName).exists()) resultFileName else path
    }


}