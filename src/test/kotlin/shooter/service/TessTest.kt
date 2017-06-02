package shooter.service

import com.intellij.execution.configurations.PathEnvironmentVariableUtil
import org.junit.Assume
import org.junit.Test
import recognizer.TessIntegration
import java.io.File

class TessTest {

    @Test
    fun testSimple() {
        val file = File("reference.png")
        println(file.absolutePath)
        val tessPath = PathEnvironmentVariableUtil.findInPath("tesseract")!!.absolutePath
        val convertPath = PathEnvironmentVariableUtil.findInPath("convert")!!.absolutePath

        val tessIntegration = TessIntegration()
        val result = tessIntegration.recognize(false, file.absolutePath, tessPath, null)

        Assume.assumeNotNull(result)
        println(result)
    }
}