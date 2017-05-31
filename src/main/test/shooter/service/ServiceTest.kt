package shooter.service

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import junit.framework.TestCase
import org.junit.Test


class ServiceTest : LightPlatformCodeInsightFixtureTestCase() {


    @Test
    fun testSimple() {
        val project = myFixture.project
        TestCase.assertNotNull(project)
        val service = ImageParsingService.getService(project)
        TestCase.assertNotNull(service)

        val template = "class Foo {}"
        val detectTypeAndText = service.detectTypeAndProcessText(arrayOf(template), JavaFileType.INSTANCE)
        TestCase.assertNotNull(detectTypeAndText)
        val text = detectTypeAndText!!.text

        TestCase.assertEquals(template, text)
    }


    @Test
    fun testRemoveIncorrectLine() {
        val project = myFixture.project
        val service = ImageParsingService.getService(project)
        val template1 = "1\n2\n3\n4\n\nclass Foo {}"

        val detectTypeAndText = service.detectTypeAndProcessText(arrayOf(template1), JavaFileType.INSTANCE)
        TestCase.assertNotNull(detectTypeAndText)
        val text = detectTypeAndText!!.text

        TestCase.assertEquals("\nclass Foo {}", text)
    }
}