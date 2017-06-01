package shooter.editor

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.fileEditor.impl.EditorWindow
import com.intellij.openapi.fileEditor.impl.text.FileDropHandler
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
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
        val image = getImage(t)
        if (image is Image) {
            if (project != null) {
                val virtualFile = (editor as EditorEx?)?.virtualFile
                val fileType: FileType? = virtualFile?.fileType

                val text = editor.document.text

                val fileToUse = if (StringUtil.isEmpty(text)) null else virtualFile

                ImageParsingService.getService(project).processImage(image, fileType, fileToUse)
            }
            return
        }

        super.handleDrop(t, project, editorWindow)
    }

    private fun getImage(t: Transferable): Any? {
        if (t.isDataFlavorSupported(DataFlavor.imageFlavor)) {
            return t.getTransferData(DataFlavor.imageFlavor)
        }

        if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            val fileList = t.getTransferData(DataFlavor.javaFileListFlavor)
            if (fileList != null) {

            }
        }

        return null
    }
}