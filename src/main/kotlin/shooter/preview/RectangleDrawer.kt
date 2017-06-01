package shooter.preview

import java.awt.*
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingUtilities
import javax.swing.border.EmptyBorder


class RectangleDrawer(val background: Image, val myPreferredSize: Dimension) : JPanel() {

    private var mouseAnchor: Point? = null
    private var dragPoint: Point? = null

    private var selectionPane: SelectionPane? = null


    init {

        selectionPane = SelectionPane()
        layout = null
        add(selectionPane)

        val adapter = object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                mouseAnchor = e.point
                dragPoint = null
                selectionPane!!.location = mouseAnchor!!
                selectionPane!!.setSize(0, 0)
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
                selectionPane!!.setBounds(x, y, width, height)
                selectionPane!!.revalidate()
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
        g2d.drawImage(background, 0, 0, this)
        g2d.drawImage(background, 0, 0, if (r.width > width) width else r.width, if (r.height > height) height else r.height, this)

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

            val dash1 = floatArrayOf(10.0f)
//            val dashed = BasicStroke(2.0f,
//                    BasicStroke.CAP_BUTT,
//                    BasicStroke.JOIN_MITER,
//                    10.0f, dash1, 0.0f)
            g2d.color = Color.BLACK
//            (g2d as? Graphics2D)?.stroke = dash1
            g2d.drawRect(0, 0, width - 3, height - 3)
            g2d.dispose()
        }

    }

    override fun getPreferredSize(): Dimension {
        return myPreferredSize
    }
}