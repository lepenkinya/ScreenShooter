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
        TestCase.assertNotNull(project)
        val service = ImageParsingService.getService(project)
        TestCase.assertNotNull(service)

        val template1 = "class Foo {}"
        val template2 = "unexpected token ' "
        val detectTypeAndText = service.detectTypeAndProcessText(arrayOf(template1, template2), JavaFileType.INSTANCE)
        TestCase.assertNotNull(detectTypeAndText)
        val text = detectTypeAndText!!.text

        TestCase.assertEquals(template1, text)
    }


}