package shooter.service

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.util.io.FileUtil
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import junit.framework.TestCase
import java.io.IOException
import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.io.File


class ServiceIntegrationTest : LightPlatformCodeInsightFixtureTestCase() {


    private fun findTestDataPath(): String {
        val f = File("src", "test")
        if (f.exists()) {
            return f.absolutePath
        }
        return PathManager.getHomePath() + "/ScreenShooter/test/data"
    }

    private fun getInfo(fileType: FileType): ImageParsingService.ImageInfo? {
        val project = myFixture.project

        val filePath = testDataPath + getTestName(true) + ".png"


        val img: BufferedImage?
        try {
            img = ImageIO.read(File(filePath))

        } catch (e: IOException) {
            throw RuntimeException("Read file error " + filePath)
        }
        val service = ImageParsingService.getService(project)

        val resultInfo = service.getParsedImageInfo(img, fileType)
        return resultInfo
    }


    private fun doTestForFile(fileType: FileType) {
        val resultInfo = getInfo(fileType)
        TestCase.assertNotNull(resultInfo)

        val text = resultInfo!!.text
        myFixture.configureByText(fileType, text)


        val filePath = testDataPath + getTestName(true) + ".txt"
        val resultText = FileUtil.loadLines(filePath).joinToString(separator = "\n")
        myFixture.checkResult(resultText)
    }


    override fun getTestDataPath(): String {
        return findTestDataPath() + "/resources/"
    }


    fun testUnused() {
        doTestForFile(JavaFileType.INSTANCE)
    }

}