package shooter.preview

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.OnePixelDivider
import com.intellij.util.RetinaImage
import com.intellij.util.ui.UIUtil
import java.awt.Dimension
import java.awt.Image
import java.awt.Rectangle
import javax.swing.BorderFactory
import javax.swing.JComponent

class CropSelectionDialog : DialogWrapper {

    val image: Image

    constructor(project: Project, startImage: Image) : super(project) {
//        image = if (UIUtil.isRetina()) RetinaImage.createFrom(startImage) else startImage
        image = startImage
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

    private val WIDTH = 600
    private val HEIGHT = 600

    val myPreferredSize: Dimension
    val rectangleDrawer: RectangleDrawer
    val factor: Float

    fun getRectangle(): Rectangle? {
        val rectangle = rectangleDrawer.getRectangle() ?: return null

        return Rectangle(
                (rectangle.x / factor).toInt(),
                (rectangle.y / factor).toInt(),
                (rectangle.width / factor).toInt(),
                (rectangle.height / factor).toInt()
        )
    }

    override fun createCenterPanel(): JComponent? {
        return rectangleDrawer
    }

    override fun getPreferredSize(): Dimension {
        return myPreferredSize
    }
}