package recognizer

import com.google.common.net.HttpHeaders
import com.intellij.execution.configurations.PathEnvironmentVariableUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.openapi.util.io.FileUtil
import com.intellij.util.JBHiDPIScaledImage
import org.apache.http.client.fluent.Request
import org.apache.http.util.EntityUtils
import shooter.service.ImageParsingService
import sun.plugin.util.UIUtil
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

    fun runForImage(image: ImageParsingService.ImageWithCrop): String {
        ProgressIndicatorProvider.checkCanceled()
        val progressIndicator: ProgressIndicator? = ProgressIndicatorProvider.getInstance().progressIndicator
        logMessage(progressIndicator, "Saving file")
        val ioFile = saveImageAsIOFile(image) ?: return ""
        if (ApplicationManager.getApplication().isUnitTestMode) {
            val tessPath = PathEnvironmentVariableUtil.findInPath("tesseract")!!.absolutePath
            val convertPath = PathEnvironmentVariableUtil.findInPath("convert")!!.absolutePath
            return recognize(ioFile.absolutePath, tessPath, convertPath).text
        }

        logMessage(progressIndicator, "Send request...")

        //TODO SERVER URL insert 0.0.0.0 on local debug

        val request = Request.Post("http://95.213.236.215:4567/ocr")
                .addHeader(HttpHeaders.CONTENT_TYPE, "image/png")
                .bodyByteArray(ioFile.readBytes())

        try {
            val response = request.execute().returnResponse()
            val text = EntityUtils.toString(response.entity)
            if (response.statusLine.statusCode in 200..299) {
                logMessage(progressIndicator, "Process text")
                return text
            }
            logger.error(text)
            return ""
        } catch (e: Exception) {
            logger.error(e)
            return ""
        }
    }

    private fun logMessage(progressIndicator: ProgressIndicator?, message: String) {
        if (progressIndicator != null) {
            progressIndicator.text2 = message
        }
    }


    fun saveImageAsIOFile(image: ImageParsingService.ImageWithCrop): File? {
        try {
            val bufferedImage = asBufferedImage(image)


            val outFile = FileUtil.createTempFile("saved_" + nextNumber.incrementAndGet(), ".png", true)

            ImageIO.write(bufferedImage, "png", outFile)

            return outFile
        } catch (e: Exception) {
            logMessages("Error while writing file " + e.localizedMessage)
        }

        return null
    }

    private fun asBufferedImage(imageWith: ImageParsingService.ImageWithCrop): BufferedImage {
        val rect = imageWith.rect
        val image = imageWith.image
        val width = rect?.width ?: image.getWidth(null)
        val height = rect?.height ?: image.getHeight(null)

        val bufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

        val bImageGraphics = bufferedImage.createGraphics()
        if (rect != null) {
//            bImageGraphics.drawImage(image, rect.x, rect.y, rect.width, rect.height, null, null)
            bImageGraphics.drawImage(image, 0, 0, rect.width,
                    rect.height, rect.x, rect.y, rect.x + rect.width, rect.y + rect.height, null)
        } else {
            bImageGraphics.drawImage(image, null, null)
        }
        return bufferedImage
    }

    private fun logMessages(text: String) {
        println(text)
        logger.info(text)
    }


}