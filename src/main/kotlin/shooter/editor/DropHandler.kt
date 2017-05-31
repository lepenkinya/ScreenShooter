package shooter.editor

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.impl.EditorWindow
import com.intellij.openapi.fileEditor.impl.text.FileDropHandler
import com.intellij.openapi.project.Project
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable

class DropHandler(editor: Editor) : FileDropHandler(editor) {

    val logger = Logger.getInstance("#shooter.editor.DropHandler")


    override fun canHandleDrop(transferFlavors: Array<out DataFlavor>?): Boolean {
        val result = transferFlavors.takeIf { it == DataFlavor.imageFlavor || it == DataFlavor.javaFileListFlavor }
        val size = result?.size ?: 0

        if (size > 0) {
            return true
        }

        return super.canHandleDrop(transferFlavors)
    }


    override fun handleDrop(t: Transferable, project: Project?, editorWindow: EditorWindow?) {
        val image = t.getTransferData(DataFlavor.imageFlavor)
        if (image != null) {
            val text = "Dnd handler Image " + image.javaClass
            logger.info(text)
            println(text)

            return
        }

        super.handleDrop(t, project, editorWindow)
    }
}