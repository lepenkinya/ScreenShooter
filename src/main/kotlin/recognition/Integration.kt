package recognition

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.io.FileUtil
import com.intellij.util.ui.UIUtil
import opencv.OpenCVTest
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import java.util.concurrent.atomic.AtomicLong
import javax.imageio.ImageIO

class Integration {

    val logger = Logger.getInstance("#shooter.service.ImageParsingService")

    companion object {
        val instance = Integration()
    }


    val nextNumber = AtomicLong()

    fun runForImage(image: Image): String {
        val ioFile = saveImageAsIOFile(image) ?: return ""
        logMessages("Created file for image ${ioFile.absolutePath}")
        var afterPreprocessed = OpenCVTest.preprocess(ioFile.absolutePath)
        if (!File(afterPreprocessed).exists()) {
            afterPreprocessed = ioFile.absolutePath
        }
        val readyForParsing = convertIfRequired(afterPreprocessed)
        return TessIntegration.instance.recognize(readyForParsing)
    }


    fun convertIfRequired(path: String): String {


        return path
    }

    fun saveImageAsIOFile(image: Image): File? {
        try {
            val bufferedImage = UIUtil.createImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB)

            val bImageGraphics = bufferedImage.createGraphics()
            bImageGraphics.drawImage(image, null, null)
            val outFile = FileUtil.createTempFile("saved_" + nextNumber.incrementAndGet(), ".png", true)

            ImageIO.write(bufferedImage, "png", outFile)

            return outFile
        } catch (e: Exception) {
            logMessages("Error while writing file " + e.localizedMessage)
        }

        return null
    }

    private fun logMessages(text: String) {
        println(text)
        logger.info(text)
    }


}