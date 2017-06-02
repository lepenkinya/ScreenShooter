package recognizer

import opencv.OpenCVUtils
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Range
import org.opencv.core.Scalar
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.io.File


object ImagePreprocessor {

    fun imageLineFiles(fileName: String): Pair<Boolean, List<String>> {
        val (mat, isDark) = OpenCVUtils.preprocess(fileName)
        val lines = listOf(mat)

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


        var currentStripeEmpty = true

        var emptyLinesStartRow = 0
        var segmentStart = 0

        val segments = mutableListOf<Segment>()


        for (currentRow in 1..rows.size - 1) {
            val current = rows[currentRow]
            val delta = Math.abs(rows[currentRow - 1].foregroundPixels - current.foregroundPixels)

            if (currentStripeEmpty && delta > 3) {
                currentStripeEmpty = false

                val emptyRegionLength = currentRow - emptyLinesStartRow
                val emptyRegionCenterRow = emptyLinesStartRow + emptyRegionLength / 2

                if (emptyRegionCenterRow > segmentStart) {
                    segments.add(Segment(segmentStart, emptyRegionCenterRow))
                }

                segmentStart = emptyRegionCenterRow
            }
            else if (!currentStripeEmpty && current.foregroundPixels < 4) {
                currentStripeEmpty = true
                emptyLinesStartRow = currentRow
            }
        }

        if (segmentStart < rows.size - 1) {
            segments.add(Segment(segmentStart, rows.size - 1))
        }

        val imageWidth = image.width() - 1
        val blue = Scalar(255.0, 0.0, 0.0)
        segments.forEach {
            val left_top = Point(0.0, it.y_top.toDouble())
            val right_bottom = Point(imageWidth.toDouble(), it.y_bottom.toDouble())
            Imgproc.rectangle(image, left_top, right_bottom, blue)
        }


        Imgcodecs.imwrite("idi_davai.png", image)

        return segments.map {
            Mat(image, Range(it.y_top, it.y_bottom))
        }
    }

}


class Segment(val y_top: Int, val y_bottom: Int)

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
//    OpenCVUtils()
//    val file = File("54image.png")
//    val path = file.absolutePath
//    lines(path)
}