package shooter

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import org.apache.http.util.EntityUtils
import org.apache.http.HttpEntity
import org.apache.http.entity.StringEntity
import org.apache.http.client.methods.HttpPost
import org.apache.log4j.xml.DOMConfigurator.setParameter
import org.apache.http.client.utils.URIBuilder
import org.apache.http.impl.client.DefaultHttpClient


class ScreenShooter : AnAction() {

    init {
        templatePresentation.text = "Just screen shooter action"
        templatePresentation.description = "Blah Blah Blah"
    }


    override fun actionPerformed(e: AnActionEvent) {
        println("HEYYO")
    }
}


object Main {
}