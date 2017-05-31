package opencv;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import recognition.CognitiveApi;
import recognition.Coordinates;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class OpenCVTest {

    /*THIS IS INTENDED TO BE API*/
    public static final double epsilon = 10.0;

    public static void filterTextRectangles(Mat source, List<Coordinates> words, int samplePointsCount, double epsilon, double threshold) {
        double[] backgroundColor = getMaxColor(source).toPoint();
        int diffColorSize = 0;
        for (Coordinates coords : words) {
            for (int i = 0; i < samplePointsCount; ++i) {
                int side = myRandom.nextInt(4);
                int aY = side & 1;
                int aX = 1 - aY;
                int dX = (side & 2) & aY;
                int dY = (side & 2) & (1 - dX);
                double alpha = myRandom.nextDouble();
                int x = new Double(coords.getX_left() + (dX + alpha * aX) * coords.getWidth()).intValue();
                int y = new Double(coords.getY_up() + (dY + alpha * aY) * coords.getHeight()).intValue();
                if (!isDiffOk(backgroundColor, source.get(y, x), epsilon)) {
                    diffColorSize ++;
                }
            }
            if (diffColorSize >= samplePointsCount * threshold) {
                //fill the whole rectangle
                Mat mask = new Mat(source.height() + 2, source.width() + 2, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
                Imgproc.floodFill(source, mask, new Point(coords.getX_left(), coords.getY_up()),
                        new Scalar(backgroundColor));

            }
        }
    }

    public static Mat filterPreprocessing(String image) {
        Mat source = Imgcodecs.imread(image,  Imgcodecs.CV_LOAD_IMAGE_COLOR);
        simplifyColors(source, source, 3);
        Mat destination = Imgcodecs.imread(image,  Imgcodecs.CV_LOAD_IMAGE_COLOR);

        PixelColor maxColor = getMaxColor(source);

        int filterOffset = 1;
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
        Imgproc.cvtColor(destination, destination, Imgproc.COLOR_RGB2GRAY);
        return destination;
    }

    public static String preprocess(String imageName) {
        Mat intermediate = filterPreprocessing(imageName);
        File imageFile = new File(imageName);
        String irName = (imageFile.getParent() == null ? "" : imageFile.getParent() + "/") + "IR" + imageFile.getName();;
        Imgcodecs.imwrite(irName, intermediate);
        CognitiveApi.INSTANCE.check(irName);
        return getPreprocessedName(irName);
    }

    public static String getPreprocessedName(String basePath) {
        File imageFile = new File(basePath);
        return (imageFile.getParent() == null ? "" : imageFile.getParent() + "/") + "DONE_" + imageFile.getName().substring(2);
    }

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
        preprocess(image);
//        Mat res = filterPreprocessing(image);
//
//        Imgcodecs.imwrite("grayblur"+image, res);
    }

    public static Random myRandom = new Random(System.currentTimeMillis());

    public static void filterTextRectangles(String input, List<Coordinates> words, int samplePointsCount, double epsilon, double threshold) {
        Mat source = Imgcodecs.imread(input,  Imgcodecs.CV_LOAD_IMAGE_COLOR);
        filterTextRectangles(source, words, samplePointsCount, epsilon, threshold);
        Imgcodecs.imwrite(getPreprocessedName(input), source);
    }

    public static void addRectangles(String input, List<Coordinates> words, List<Coordinates> lines) {
        Mat source = Imgcodecs.imread(input,  Imgcodecs.CV_LOAD_IMAGE_COLOR);
        Mat dst = source;
        for (Coordinates coords : words) {
            Point leftUpper = new Point(coords.getX_left(), coords.getY_up());
            Point rightLower = new Point(coords.getX_left() + coords.getWidth(), coords.getY_up() + coords.getHeight());
            Imgproc.rectangle(dst, leftUpper, rightLower, new Scalar(255, 0, 0));
        }
        Imgcodecs.imwrite("rectangles_"+input, dst);
    }
}