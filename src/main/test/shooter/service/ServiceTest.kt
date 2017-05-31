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


    @Test
    fun testRemovePrefixNumbers() {
        val project = myFixture.project
        val service = ImageParsingService.getService(project)
        val template1 = ":48\n" +
                "\n" +
                ":49 private fun getErrors(fileType: LanguageFileType, it: String): JBIterab1e<PsiErrorElement> {\n" +
                ":50 val psiFile = PsiFileFactory.getInstance(project).createFileFromText(\"foo\", fileType, it)\n" +
                ":51 val errors = SyntaxTraverser.psiTraverser(psiFile).traverse().filter(PsiErrorElement::class.java)\n" +
                ":52 return errors\n" +
                "\n" +
                ":53 }\n" +
                "\n" +
                ":54\n" +
                "\n" +
                ":55 private fun removePrefixNumbers(lines: List<String>): List<String> {\n" +
                "\n" +
                ":56\n" +
                "\n" +
                ":57 jog (line in lines) {\n" +
                "\n" +
                ":58 if (line.isEMpty()) continue\n" +
                "\n" +
                "159\n" +
                "\n" +
                "160 try {\n" +
                "\n" +
                ":61 val nquer = Integer.parseInt(Iine)\n" +
                "\n" +
                ":62 } catch (e1 {\n" +
                "\n" +
                "163\n" +
                "\n" +
                ":64 }\n" +
                "\n" +
                ":65\n" +
                "\n" +
                ":66 }\n" +
                "\n" +
                ":67\n" +
                "\n" +
                ":68 return lines\n" +
                "\n" +
                ":69 }\n" +
                "\n" +
                ":70 }\n"


        val detectTypeAndText = service.detectTypeAndProcessText(arrayOf(template1), JavaFileType.INSTANCE)
        TestCase.assertNotNull(detectTypeAndText)
        val text = detectTypeAndText!!.text

        val result = "\n" +
                "\n" +
                " private fun getErrors(fileType: LanguageFileType, it: String): JBIterab1e<PsiErrorElement> {\n" +
                " val psiFile = PsiFileFactory.getInstance(project).createFileFromText(\"foo\", fileType, it)\n" +
                " val errors = SyntaxTraverser.psiTraverser(psiFile).traverse().filter(PsiErrorElement::class.java)\n" +
                " return errors\n" +
                "\n" +
                " }\n" +
                "\n" +
                "\n" +
                "\n" +
                " private fun removePrefixNumbers(lines: List<String>): List<String> {\n" +
                "\n" +
                "\n" +
                "\n" +
                " jog (line in lines) {\n" +
                "\n" +
                " if (line.isEMpty()) continue\n" +
                "\n" +
                "\n" +
                "\n" +
                " try {\n" +
                "\n" +
                " val nquer = Integer.parseInt(Iine)\n" +
                "\n" +
                " } catch (e1 {\n" +
                "\n" +
                "\n" +
                "\n" +
                " }\n" +
                "\n" +
                "\n" +
                "\n" +
                " }\n" +
                "\n" +
                "\n" +
                "\n" +
                " return lines\n" +
                "\n" +
                " }\n" +
                "\n" +
                " }\n"

        TestCase.assertEquals(result, text)
    }
}