package shooter.service

import org.junit.Assume
import org.junit.Test
import recognition.TessIntegration
import java.io.File

class TessTest {

    @Test
    fun testSimple() {
        val file = File("reference.png")
        println(file.absolutePath)
        val tessIntegration = TessIntegration()
        val result = tessIntegration.recognize(file.absolutePath)

        Assume.assumeNotNull(result)
        println(result)
    }
}