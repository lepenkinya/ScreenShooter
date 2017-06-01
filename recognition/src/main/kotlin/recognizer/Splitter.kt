package recognizer

import opencv.OpenCVTest
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import recognizer.Splitter.lines
import java.io.File

object Splitter {


    fun lines(imageFileName: String) {
        OpenCVTest()

        val image = Imgcodecs.imread(imageFileName, Imgcodecs.CV_LOAD_IMAGE_COLOR)

        val bgColor = OpenCVTest.backgroundColor(image)

        val rows = (0..image.rows() - 1)
                .map { image.row(it)!! }
                .map { it.lineInfo(bgColor) }

        println()

    }

}

private fun Mat.lineInfo(bgColor: OpenCVTest.PixelColor): LineInfo {
    val row = this
    val totalPixels = cols()

    val foregroundPixels = (0..totalPixels - 1).map {
        val pixel = row.get(0, it)
        OpenCVTest.isDiffOk(pixel, bgColor.toPoint(), OpenCVTest.epsilon)
    }.count { !it }

    return LineInfo(totalPixels, foregroundPixels)
}


class LineInfo(val totalPixels: Int, val foregroundPixels: Int)


fun main(args: Array<String>) {
    val file = File("original.png")
    val path = file.absolutePath

    lines(path)


}