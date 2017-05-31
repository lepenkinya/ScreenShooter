package shooter.editor

import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.vfs.VirtualFile

class OverrideDropHandlerActivity : StartupActivity {
    override fun runActivity(project: Project) {

        updateEditors(FileEditorManager.getInstance(project).allEditors)

        project.messageBus.connect(project).subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, object : FileEditorManagerListener {

            override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
                updateEditors(source.allEditors)
            }
        })
    }

    private fun updateEditors(allEditors: Array<out FileEditor>) {
        allEditors
                .asSequence()
                .filterIsInstance<TextEditor>()
                .map { it.editor }
                .filterIsInstance<EditorImpl>()
                .forEach { it.setDropHandler(DropHandler(it)) }
    }
}