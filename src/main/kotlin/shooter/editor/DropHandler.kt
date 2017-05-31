package shooter.editor

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.fileEditor.impl.EditorWindow
import com.intellij.openapi.fileEditor.impl.text.FileDropHandler
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import shooter.service.ImageParsingService
import java.awt.Image
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable

class DropHandler(val editor: Editor) : FileDropHandler(editor) {

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
        if (image is Image) {
            if (project != null) {
                val fileType: FileType? = (editor as EditorEx?)?.virtualFile?.fileType
                ImageParsingService.getService(project).processImage(image, fileType)
            }
            return
        }

        super.handleDrop(t, project, editorWindow)
    }
}