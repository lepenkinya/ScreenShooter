package recognition

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.PathEnvironmentVariableUtil
import com.intellij.execution.util.ExecUtil
import com.intellij.openapi.util.io.FileUtil
import net.sourceforge.tess4j.ITesseract
import net.sourceforge.tess4j.Tesseract
import java.io.File


class TessIntegration(val tessInstance: ITesseract = Tesseract()) {

    companion object Instance {
        val instance = TessIntegration()
    }

    val doConvert: Boolean = true
    val convertResize: Int = 400

    fun recognize(path: String): String {
        return runCommandLine(convertIfRequired(path))
    }

    private fun runCommandLine(path: String): String {
        val file = File(path)
        val directory = file.parent
        val resultFileName = FileUtil.toSystemDependentName(directory + "/" + FileUtil.getNameWithoutExtension(file.name))


        val tessPath = PathEnvironmentVariableUtil.findInPath("tesseract")
        if (tessPath == null || !tessPath.exists()) {
            throw RuntimeException("Cannot find tesseract in path")
        }

        println("Path: " + tessPath.absolutePath)
        val generalCommandLine = GeneralCommandLine(tessPath.absolutePath, "-l", "eng", file.absolutePath, resultFileName)
        println("command line " + generalCommandLine.commandLineString)
        val output = ExecUtil.execAndGetOutput(generalCommandLine)

        println("out lines: " + output.stdoutLines)
        println("err lines: " + output.stderrLines)

        val results = FileUtil.loadLines(File(resultFileName + ".txt"))

        return results.joinToString(separator = "\n")
    }

    fun convertIfRequired(path: String): String {
        if (!doConvert) return path


        val file = File(path)
        val directory = file.parent
        val resultFileName = FileUtil.toSystemDependentName(directory + "/" + FileUtil.getNameWithoutExtension(file.name) + "_tf.tiff")


        val tessPath = PathEnvironmentVariableUtil.findInPath("convert")
        if (tessPath == null || !tessPath.exists()) {
            return path
        }

        println("Path: " + tessPath.absolutePath)
        val generalCommandLine = GeneralCommandLine(
                tessPath.absolutePath, "-resize",
                convertResize.toString() + "%",
                "-type",
                "Grayscale",
                resultFileName)

        val output = ExecUtil.execAndGetOutput(generalCommandLine)

        println("out lines: " + output.stdoutLines)
        println("err lines: " + output.stderrLines)

        return resultFileName
    }

}