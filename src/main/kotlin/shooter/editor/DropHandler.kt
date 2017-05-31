package shooter.editor

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.impl.text.FileDropHandler
import java.awt.datatransfer.DataFlavor

class DropHandler(editor: Editor) : FileDropHandler(editor) {
    override fun canHandleDrop(transferFlavors: Array<out DataFlavor>?): Boolean {
        return super.canHandleDrop(transferFlavors)
    }
}