package recognizer

import opencv.OpenCVUtils
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Range
import org.opencv.core.Scalar
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import recognizer.Splitter.lines
import java.io.File


object ImagePreprocessor {

    fun imageLineFiles(fileName: String): Pair<Boolean, List<String>> {
        val (mat, isDark) = OpenCVUtils.preprocess(fileName)
        val lines = lines(mat)

        val parentPath = File(fileName).parent

        val baseName = fileName.substringAfterLast('/')

        return isDark to lines.mapIndexed { index, mat ->
            val file = File(parentPath, "${index}_$baseName")
            val path = file.absolutePath
            Imgcodecs.imwrite(path, mat)
            path
        }
    }

}


object Splitter {


    fun lines(image: Mat): List<Mat> {
        val bgColor = OpenCVUtils.backgroundColor(image)

        val rows = (0..image.rows() - 1)
                .map { image.row(it)!! }
                .map { it.lineInfo(bgColor) }

        if (rows.isEmpty()) return emptyList()


        val rects = mutableListOf<Rectangle>()

        for (currentRow in 3..rows.size - 5) {
            val region = ThreeLineRegion(image.rowRange(Range(currentRow - 3, currentRow)), bgColor)
            val filledRegions = region.filledRegions()
            if (filledRegions.isNotEmpty()) {
                val under = image.row(currentRow + 2)

                val colorMap = mutableMapOf<OpenCVUtils.PixelColor, Int>()
                for (i in 0..under.cols() - 1) {
                    val color = OpenCVUtils.PixelColor(under.get(0, i))
                    val value = colorMap[color]
                    if (value == null) {
                        colorMap[color] = 1
                    } else {
                        colorMap[color] = value + 1
                    }
                }

                val sorted = colorMap.values.sortedDescending()
                if (sorted.size == 1 || sorted[0] > 50 * sorted[1]) {
                    filledRegions.forEach {
                        rects.add(Rectangle(it.x_left, currentRow - 2, it.x_right, currentRow))
                    }
                }
            }
        }


        val blue = Scalar(255.0, 0.0, 0.0)
        val bgColorScalar = Scalar(bgColor.toPoint())

        rects.forEach {
            val p1 = Point(it.x_left.toDouble(), it.y_top.toDouble())
            val p2 = Point(it.x_right.toDouble(), it.y_bottom.toDouble())
            Imgproc.rectangle(image, p1, p2, bgColorScalar, -1)
        }


        Imgcodecs.imwrite("idi_davai.png", image)

        return listOf(image)
    }

}

class ThreeLineRegion(val image: Mat, val bgColor: OpenCVUtils.PixelColor) {

    private fun columnEmpty(index: Int): Boolean {
        val column = image.col(index)
        val maxRows = image.rows()
        val result = (0..maxRows - 1)
                .map { OpenCVUtils.isDiffOk(bgColor.toPoint(), column.get(it, 0), OpenCVUtils.epsilon) }
                .all { it }
        return result
    }

    private fun line(): String {
        val maxCols = image.cols()
        return (0..maxCols - 1).map { columnEmpty(it) }.joinToString("", transform = { if (it) "0" else "1" })
    }

    fun filledRegions(): List<XSegment> {
        val line = line()
        val list = mutableListOf<XSegment>()
        var start = 0
        var end = 0

        while (true) {
            start = line.indexOf("1", end)
            if (start < 0) return list

            end = line.indexOf("0", start)
            if (end < 0) return list

            if (end - start > 10) {
                list.add(XSegment(start, end - 1))
            }
        }
    }

}


class Rectangle(val x_left: Int, val y_top: Int, val x_right: Int, val y_bottom: Int)

class XSegment(val x_left: Int, val x_right: Int)

class YSegment(val y_top: Int, val y_bottom: Int)

private fun Mat.lineInfo(bgColor: OpenCVUtils.PixelColor): LineInfo {
    val row = this
    val totalPixels = cols()

    val foregroundPixels = (0..totalPixels - 1).map {
        val pixel = row.get(0, it)
        OpenCVUtils.isDiffOk(pixel, bgColor.toPoint(), OpenCVUtils.epsilon)
    }.count { !it }

    return LineInfo(totalPixels, foregroundPixels)
}


class LineInfo(val totalPixels: Int, val foregroundPixels: Int)


fun main(args: Array<String>) {
    OpenCVUtils()
    val file = File("0_after_preprocess.png")
    val path = file.absolutePath
    val mat = Imgcodecs.imread(path)
    lines(mat)
}