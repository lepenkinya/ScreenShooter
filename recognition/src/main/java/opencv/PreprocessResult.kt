package opencv

import org.opencv.core.Mat

data class PreprocessResult(val mat: Mat, val isDark: Boolean)