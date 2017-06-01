package shooter

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.impl.java.stubs.index.JavaShortClassNameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import org.apache.http.util.EntityUtils
import org.apache.http.HttpEntity
import org.apache.http.entity.StringEntity
import org.apache.http.client.methods.HttpPost
import org.apache.log4j.xml.DOMConfigurator.setParameter
import org.apache.http.client.utils.URIBuilder
import org.apache.http.impl.client.DefaultHttpClient
import java.io.File


class ScreenShooter : AnAction() {

    init {
        templatePresentation.text = "Just screen shooter action"
        templatePresentation.description = "Blah Blah Blah"
    }


    override fun actionPerformed(e: AnActionEvent) {
        val index = JavaShortClassNameIndex.getInstance()
        val project = e.project!!
        val keys = index.getAllKeys(project)

        val set = mutableSetOf<String>()

        val allScope = GlobalSearchScope.allScope(project)
        keys.forEach { className ->
            println("Classname: $className")
            set.add(className)
            val classes: Collection<PsiClass> = index.get(className, project, allScope)
            classes.forEach {
                it.methods.map {
                    println("Method: ${it.name}")

                    set.add(it.name)
                    it.parameterList.parameters.mapNotNull { it.name }.forEach {
                        set.add(it)
                    }
                }
            }
        }

        val file = File("/Users/yarik/IdeaProjects/ScreenShooter/symbols/jdk")
        if (file.exists()) {
            file.delete()
        }
        file.createNewFile()

        set.forEach {
            file.appendText("$it\n")
        }
    }


}