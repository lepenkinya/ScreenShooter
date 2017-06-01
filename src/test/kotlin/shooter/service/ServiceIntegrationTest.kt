package shooter.service

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.util.io.FileSystemUtil
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import com.intellij.util.io.readText
import junit.framework.TestCase
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import javax.imageio.ImageIO


class ServiceIntegrationTest : LightPlatformCodeInsightFixtureTestCase() {


    private fun findTestDataPath(): String {
        val f = File("src", "test")
        if (f.exists()) {
            return f.absolutePath
        }
        return PathManager.getHomePath() + "/ScreenShooter/test/data"
    }

    private fun getInfo(dir: File, fileType: FileType): ImageParsingService.ImageInfo? {
        val project = myFixture.project

        val filePath = testDataPath + getTestName(true) + ".png"
        FileUtil.copy(File(filePath), File(dir, "original.png"))

        val img: BufferedImage?
        try {
            img = ImageIO.read(File(filePath))
        } catch (e: IOException) {
            throw RuntimeException("Read file error " + filePath)
        }
        val service = ImageParsingService.getService(project)

        val resultInfo = service.getParsedImageInfo(ImageParsingService.ImageWithCrop(img, null), fileType, dir)
        return resultInfo
    }


    private fun doTestForFile(fileType: FileType) {
        val testName = getTestName(true)

        val dir = File("testRuns", testName)
        if (dir.exists()) dir.deleteRecursively()
        dir.mkdir()

        val resultInfo = getInfo(dir, fileType)
        TestCase.assertNotNull(resultInfo)

        val text = resultInfo!!.text
        myFixture.configureByText(fileType, text)


        val filePath = testDataPath + getTestName(true) + ".txt"


        val resultText = StringUtil.convertLineSeparators(Paths.get(filePath).readText())
        myFixture.checkResult(resultText)
    }


    override fun getTestDataPath(): String {
        return findTestDataPath() + "/resources/"
    }


    fun testUnused() {
        doTestForFile(JavaFileType.INSTANCE)
    }

    fun testCreateMethodTS() {
        doTestForFile(PlainTextFileType.INSTANCE)
    }

}