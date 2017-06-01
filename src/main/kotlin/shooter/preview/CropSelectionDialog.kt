package shooter.preview

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.OnePixelDivider
import java.awt.Dimension
import java.awt.Image
import javax.swing.BorderFactory
import javax.swing.JComponent

class CropSelectionDialog(project: Project, val image: Image) : DialogWrapper(project) {

    private val WIDTH = 600
    private val HEIGHT = 600

    val myPreferredSize: Dimension
    val rectangleDrawer: RectangleDrawer
    val factor: Float

    init {
        title = "Select code area"
        val width = image.getWidth(null)
        val height = image.getHeight(null)

        if (width > WIDTH || height > HEIGHT) {
            factor = 600.0f / Math.max(width, height)
            myPreferredSize = Dimension((width * factor).toInt(), (height * factor).toInt())
        } else {
            myPreferredSize = Dimension(width, height)
            factor = 1.0f
        }

        rectangleDrawer = RectangleDrawer(image, myPreferredSize)
        rectangleDrawer.border = BorderFactory.createLineBorder(OnePixelDivider.BACKGROUND)
        rectangleDrawer.preferredSize = myPreferredSize

        init()
    }

    override fun createCenterPanel(): JComponent? {
        return rectangleDrawer
    }

    override fun getPreferredSize(): Dimension {
        return myPreferredSize
    }
}