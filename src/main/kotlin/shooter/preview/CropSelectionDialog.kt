package shooter.preview

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.OnePixelDivider
import java.awt.Dimension
import java.awt.Image
import java.awt.Rectangle
import javax.swing.BorderFactory
import javax.swing.JComponent

class CropSelectionDialog(project: Project, startImage: Image) : DialogWrapper(project) {

    val image: Image = startImage

    private val WIDTH = 600
    private val HEIGHT = 600

    val myPreferredSize: Dimension
    val rectangleDrawer: RectangleDrawer
    val factor: Float

    fun getRectangle(): Rectangle? {
        val rectangle = rectangleDrawer.getRectangle() ?: return null

        val size = rectangleDrawer.size
        var factorY = 1f
        var factorX = 1f

        if (size.width != preferredSize.width) {
            factorX = (preferredSize.width.toFloat() / size.width)
        }

        if (size.height != preferredSize.height) {
            factorY = (preferredSize.height.toFloat() / size.height)
        }



        return Rectangle(
                ((factorX * rectangle.x) / factor).toInt(),
                ((factorY * rectangle.y) / factor).toInt(),
                ((factorX * rectangle.width) / factor).toInt(),
                ((factorY * rectangle.height) / factor).toInt()
        )
    }

    override fun createCenterPanel(): JComponent? {
        return rectangleDrawer
    }

    override fun getPreferredSize(): Dimension {
        return myPreferredSize
    }

    init {
        title = "Select code area"
        setOKButtonText("Recognize text")

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
        myPreferredFocusedComponent = rectangleDrawer
        init()
    }
}