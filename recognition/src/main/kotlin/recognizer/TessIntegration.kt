package recognizer

import org.apache.commons.io.FileUtils
import org.zeroturnaround.exec.ProcessExecutor
import java.io.File


class TessIntegration {

    companion object Instance {
        val instance = TessIntegration()
    }


    fun recognize(isDark: Boolean, fileName: String, testPass: String, debugDir: File?): String {
        val newPath = convertIfRequired(isDark, fileName, debugDir)
        return runCommandLine(newPath, testPass)
    }

    private fun runCommandLine(path: String, tessPath: String): String {
        val file = File(path)
        val directory = file.parent
        val resultFileName = directory + File.separator + file.nameWithoutExtension

//        val configFile = File("./tessconfig")

        val user_words = File("./dict")
        println("File dict exists: ${user_words.exists()} size: ${user_words.length()}")


//        with(configFile) {
//            appendText("load_system_dawg 0\n")
//            appendText("load_freq_dawg 0")
//        }

        val result = ProcessExecutor()
                .command(
                        tessPath,
                        "-l", "eng+Menlo+Monaco",
                        "--user-words", user_words.absolutePath,
                        file.absolutePath, resultFileName//, configFile.absolutePath
                )
                .redirectOutput(System.out)
                .redirectError(System.out)
                .readOutput(true)
                .execute()

        println("Tess exit code: ${result.exitValue}")

        return File(resultFileName + ".txt").readText()
    }

    fun convertIfRequired(isDark: Boolean, fileNameI: String, debugDir: File?): String {
        var fileName = fileNameI
        val file = File(fileName)
        val directory = file.parent
        val fileNameWithoutExt = directory + File.separator + file.nameWithoutExtension
        val resultFileName = fileNameWithoutExt + "_tf.tiff"


        val newName = fileNameWithoutExt + "_n" + file.extension
        if (isDark) {

            ProcessExecutor().command("convert", fileName, "-negate", "-normalize", newName).redirectError(System.out)
                    .redirectOutput(System.out)
                    .execute()


        } else {
            ProcessExecutor().command("convert", fileName, "-normalize", newName).redirectError(System.out)
                    .redirectOutput(System.out)
                    .execute()
        }

        FileUtils.copyFile(File(newName), File(debugDir, "${file.nameWithoutExtension}_after_normalize." + file.extension))
        fileName = newName

        val whiteParams = arrayOf(
                "convert",
                fileName,
                "-density",
                "300",
                "-resize",
                "300%",
                "-threshold",
                "75%",
                resultFileName)


        //-density 300
        ProcessExecutor().command(whiteParams.toList()).redirectError(System.out)
                .redirectOutput(System.out)
                .execute()

        if (debugDir != null) {
            FileUtils.copyFile(File(resultFileName), File(debugDir, "${file.nameWithoutExtension}_after_convert.tiff"))
        }

        return if (File(resultFileName).exists()) resultFileName else fileName
    }


}