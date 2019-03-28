package shooter.preview

import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JPanel


class RectangleDrawer(val imageBackground: Image, val myPreferredSize: Dimension) : JPanel() {

    private var mouseAnchor: Point? = null
    private var dragPoint: Point? = null

    private val selectionPane: SelectionPane

    fun getRectangle(): Rectangle? {
        val size = selectionPane.size
        if (size.height == 0 && size.width == 0) {
            return null
        }

        return Rectangle(selectionPane.x, selectionPane.y, selectionPane.width, selectionPane.height)
    }

    init {

        selectionPane = SelectionPane()
        layout = null
        add(selectionPane)

        val adapter = object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                mouseAnchor = e.point
                dragPoint = null
                selectionPane.location = mouseAnchor!!
                selectionPane.setSize(0, 0)
            }

            override fun mouseDragged(e: MouseEvent) {
                dragPoint = e.point
                var width = dragPoint!!.x - mouseAnchor!!.x
                var height = dragPoint!!.y - mouseAnchor!!.y

                var x = mouseAnchor!!.x
                var y = mouseAnchor!!.y

                if (width < 0) {
                    x = dragPoint!!.x
                    width *= -1
                }
                if (height < 0) {
                    y = dragPoint!!.y
                    height *= -1
                }
                selectionPane.setBounds(x, y, width, height)
                selectionPane.revalidate()
                repaint()
            }

        }
        addMouseListener(adapter)
        addMouseMotionListener(adapter)

    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val r = bounds
        val g2d = g.create()

        g2d.drawImage(imageBackground, 0, 0, if (r.width > width) width else r.width, if (r.height > height) height else r.height, this)


        g2d.dispose()
    }


    inner class SelectionPane : JPanel() {

        init {
            isOpaque = false

            layout = GridBagLayout()

            val gbc = GridBagConstraints()
            gbc.gridx = 0
            gbc.gridy = 0

            gbc.gridy++
        }

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            val g2d = g.create()
            g2d.color = Color(128, 128, 128, 64)
            g2d.fillRect(0, 0, width, height)

            g2d.color = Color.BLACK
            g2d.drawRect(0, 0, width - 3, height - 3)
            g2d.dispose()
        }

    }

    override fun getPreferredSize(): Dimension {
        return myPreferredSize
    }
}