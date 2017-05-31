package shooter.service

import com.intellij.ide.scratch.ScratchFileService
import com.intellij.ide.scratch.ScratchRootType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.util.PathUtil
import com.intellij.util.ui.UIUtil
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import java.util.concurrent.atomic.AtomicLong
import javax.imageio.ImageIO


class ImageParsingService(val project: Project) {

    val logger = Logger.getInstance("#shooter.service.ImageParsingService")
    val nextNumber = AtomicLong()

    companion object ServiceHolder {
        fun getService(project: Project): ImageParsingService {
            return ServiceManager.getService(project, ImageParsingService::class.java)
        }
    }


    fun processImage(image: Image, fileType: FileType?) {
        ApplicationManager.getApplication().executeOnPooledThread({
            processImageImpl(image, fileType)
        })
    }


    private fun processImageImpl(image: Image, fileType: FileType?) {
        saveImageAsIOFile(image)

        val language = (fileType as? LanguageFileType)?.language

        val option = ScratchFileService.Option.create_new_always
        val fileName = "image"
        val text = "class Foo {}"
        val ext = fileType?.defaultExtension

        ApplicationManager.getApplication().invokeLater({
            val scratchFile = ScratchRootType.getInstance().createScratchFile(project,
                    PathUtil.makeFileName(fileName, ext), language, text, option)

            if (scratchFile != null) {
                FileEditorManager.getInstance(project).openFile(scratchFile, true)
            }
        })
    }

    private fun saveImageAsIOFile(image: Image): File? {
        try {
            val bufferedImage = UIUtil.createImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB)

            val bImageGraphics = bufferedImage.createGraphics()
            bImageGraphics.drawImage(image, null, null)
            val outFile = FileUtil.createTempFile("saved_" + nextNumber.incrementAndGet(), ".png", true)

            ImageIO.write(bufferedImage, "png", outFile)
            logMessages("Created file: " + outFile.absolutePath)

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