package opencv;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class OpenCVTest {

    static class PixelColor {
        final double red;
        final double blue;
        final double green;
        PixelColor(double[] input) {
            this(input[0], input[1], input[2]);
        }
        PixelColor(double red, double blue, double green) {
            this.red = red;
            this.blue = blue;
            this.green = green;
        }
        @Override
        public boolean equals(Object other) {
            if (other instanceof PixelColor) {
                PixelColor otherColor = (PixelColor) other;
                return otherColor.red == red && otherColor.blue == blue && otherColor.green == green;
            } else {
                return false;
            }
        }
        @Override
        public int hashCode() {
            return (int) Math.round(red + blue + green);
        }
        double[] toPoint() {
            return new double[]{red, blue, green};
        }
    }

    static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    public static boolean isDiffOk(double[] first, double[] second, double epsilon) {
        for (int i = 0; i < first.length; ++i) {
            if (Math.abs(first[i] - second[i]) > epsilon) return false;
        }
        return true;
    }

    public static boolean isSizeOneTemplate(Mat source, int x, int y, double epsilon) {
        double[] center = source.get(y, x);
        return getCenterCount(source, x, y, 1, epsilon) <= 3 &&
                (isDiffOk(center, source.get(y - 1, x - 1), epsilon) &&
                isDiffOk(center, source.get(y + 1, x + 1), epsilon) ||
                isDiffOk(center, source.get(y + 1, x -1), epsilon) &&
                isDiffOk(center, source.get(y - 1, x + 1), epsilon) ||
                isDiffOk(center, source.get(y - 1, x - 1), epsilon) &&
                isDiffOk(center, source.get(y - 1, x + 1), epsilon) ||
                isDiffOk(center, source.get(y + 1, x - 1), epsilon) &&
                isDiffOk(center, source.get(y + 1, x + 1), epsilon));
    }

    public static void simplifyColors(Mat source, Mat destination, int bitCount) {
        for (int x = 0; x < source.width(); ++x) {
            for (int y = 0; y < source.height(); ++y) {
                double[] src = source.get(y, x);
                double[] dst = new double[src.length];
                for (int i = 0; i < src.length; ++i) {
                    dst[i] = (Math.round(src[i]) >> bitCount) << bitCount;
                }
                destination.put(y, x, dst);
            }
        }
    }

    public static int getCenterCount(Mat source, int x, int y, int size, double epsilon) {
        double[] center = source.get(y, x);
        int centerCount = 0;
        for (int x1 = x - size; x1 <= x + size; ++x1) {
            for (int y1 = y - size; y1 <= y + size; ++y1) {
                double[] current = source.get(y1, x1);
                if (isDiffOk(center, current, epsilon)) centerCount++;
            }
        }
        return centerCount;
    }

    public static PixelColor getMaxColor(Mat source) {
        Map<PixelColor, Integer> colorMap = new HashMap<>();

        for (int x = 0; x < source.width(); ++x) {
            for (int y = 0; y < source.height(); ++y) {
                PixelColor color = new PixelColor(source.get(y, x));
                if (colorMap.containsKey(color)) {
                    colorMap.put(color, colorMap.get(color) + 1);
                } else {
                    colorMap.put(color, 1);
                }
            }
        }

        PixelColor maxColor = null;
        int max = 0;
        for (Map.Entry<PixelColor, Integer> entry : colorMap.entrySet()) {
            if (max < entry.getValue()) {
                maxColor = entry.getKey();
                max = entry.getValue();
            }
        }
        return maxColor;
    }

    public static void main(String[] args) {
        String image = args[0];

        Mat source = Imgcodecs.imread(image,  Imgcodecs.CV_LOAD_IMAGE_COLOR);
        simplifyColors(source, source, 3);
        Mat destination = Imgcodecs.imread(image,  Imgcodecs.CV_LOAD_IMAGE_COLOR);

        PixelColor maxColor = getMaxColor(source);

        int filterOffset = 1;
        double epsilon = 10.0;
        long replaceCount = 0;
        for (int x = filterOffset; x < source.width() - filterOffset; ++x) {
            for (int y = filterOffset; y < source.height() - filterOffset; ++y) {
                double[] center = source.get(y, x);
                if (isSizeOneTemplate(source, x, y, epsilon)) {//centerCount <= 5) {
                    //replace current pixel
                    replaceCount++;
                    double[] placeholder = maxColor.toPoint();
                    destination.put(y, x, placeholder);
                    //System.arraycopy(average, 0, dst, 0, dst.length);
                } else {
                    destination.put(y, x, center);
                    //System.arraycopy(center, 0, dst, 0, dst.length);
                }
            }
        }
        System.out.println("Replaced " + replaceCount + " out of " + source.height() * 1.0* source.width());
        Imgcodecs.imwrite("blur"+image, destination);
        Imgproc.cvtColor(destination, destination, Imgproc.COLOR_RGB2GRAY);
        Imgcodecs.imwrite("grayblur"+image, destination);
    }
}
