package recognizer

import com.google.common.net.HttpHeaders
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.io.FileUtil
import com.intellij.util.ui.UIUtil
import org.apache.http.client.fluent.Request
import org.apache.http.util.EntityUtils
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

    fun runForImage(image: Image, tessPath: String, convertPath: String): String {
        val ioFile = saveImageAsIOFile(image) ?: return ""
        if (ApplicationManager.getApplication().isUnitTestMode) {
            return recognize(ioFile.absolutePath, tessPath, convertPath).text
        }

        val request = Request.Post("http://localhost:4567/ocr")
                .addHeader(HttpHeaders.CONTENT_TYPE, "image/png")
                .bodyByteArray(ioFile.readBytes())

        try {
            val response = request.execute().returnResponse()
            val text = EntityUtils.toString(response.entity)
            if (response.statusLine.statusCode in 200..299) {
                return text
            }
            logger.error(text)
            return ""
        } catch (e: Exception) {
            logger.error(e)
            return ""
        }
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