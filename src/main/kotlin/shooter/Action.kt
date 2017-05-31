package shooter

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class ScreenShooter: AnAction() {

    init {
        templatePresentation.text = "Just screen shooter action"
        templatePresentation.description = "Blah Blah Blah"
    }


    override fun actionPerformed(e: AnActionEvent) {
        println("HEYYO")
    }
}