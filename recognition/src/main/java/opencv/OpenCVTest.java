package opencv;

import nu.pattern.OpenCV;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import recognizer.CognitiveApi;
import recognizer.Coordinates;
import recognizer.Line;
import recognizer.Word;

import java.io.*;
import java.util.*;

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
        double[] background = maxColor.toPoint();
        for (int x = filterOffset; x < source.width() - filterOffset; ++x) {
            for (int y = filterOffset; y < source.height() - filterOffset; ++y) {
                double[] center = source.get(y, x);
                if (isSizeOneTemplate(source, x, y, epsilon) || isSizeTwoTemplate(source, x, y, epsilon)) {//centerCount <= 5) {
                    //replace current pixel
                    replaceCount++;
                    destination.put(y, x, background);
//                } else if (isDeprecatedLine(source, x, y, epsilon, background)) {
//                    double[] upper = source.get(y - 1, x);
//                    destination.put(y, x, upper);
                } else {
                    destination.put(y, x, center);
                }
            }
        }
        Imgproc.cvtColor(destination, destination, Imgproc.COLOR_RGB2GRAY);
        return destination;
    }

    public static String preprocess(String imageName) {
        Mat intermediate = filterPreprocessing(imageName);
        File imageFile = new File(imageName);
        String irName = (imageFile.getParent() == null ? "" : imageFile.getParent() + "/") + "IR" + imageFile.getName();
        Imgcodecs.imwrite(irName, intermediate);
        CognitiveApi.INSTANCE.check(irName);
        return getPreprocessedName(irName);
    }

    public static String getPreprocessedName(String basePath) {
        File imageFile = new File(basePath);
        String noExt = imageFile.getName().substring(0, imageFile.getName().lastIndexOf(".")).substring(2);
        return (imageFile.getParent() == null ? "" : imageFile.getParent() + "/") + "DONE_" + noExt + ".tiff";
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

    static {
        OpenCV.loadLocally();
    }

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

    public static boolean isSizeTwoTemplate(Mat source, int x, int y, double epsilon) {
        double[] center = source.get(x, y);
        if (x < 2 || y < 2 || x >= source.width() - 2 || y >= source.height() - 2) return false;
        return getCenterCount(source, x, y, 1, 2, 1, 1, epsilon) <= 4 &&
                (isDiffOk(center, source.get(y + 1, x - 1), epsilon) &&
                isDiffOk(center, source.get(y, x + 1), epsilon) &&
                isDiffOk(center, source.get(y + 1, x + 2), epsilon) ||
                isDiffOk(center, source.get(y - 1, x - 2), epsilon) &&
                isDiffOk(center, source.get(y, x - 1), epsilon) &&
                isDiffOk(center, source.get(y - 1, x + 1), epsilon));
    }

    public static boolean isDeprecatedLine(Mat source, int x, int y, double epsilon, double[] background) {
        double[] center = source.get(y, x);
        double[] lower = source.get(y + 1, x);
        double[] upper = source.get(y - 1, x);
        return getCenterCount(source, x, y, 1, epsilon) == 3 &&
                isDiffOk(lower, upper, epsilon) &&
                !isDiffOk(lower, background, epsilon) &&
                !isDiffOk(upper, background, epsilon) &&
                !isDiffOk(center, background, epsilon);
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
        return getCenterCount(source, x, y, size, size, size, size, epsilon);
    }

    public static int getCenterCount(Mat source, int x, int y, int xLeft, int xRight, int yTop, int yBot, double epsilon) {
        double[] center = source.get(y, x);
        int centerCount = 0;
        for (int x1 = x - xLeft; x1 <= x + xRight; ++x1) {
            for (int y1 = y - yTop; y1 <= y + yBot; ++y1) {
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
//        File dir = new File(args[0]);
//        assert(dir.isDirectory());
//        for (File file: dir.listFiles()) {
//            Mat source = Imgcodecs.imread(file.getAbsolutePath(),  Imgcodecs.CV_LOAD_IMAGE_COLOR);
//            findRectangles(source, 20, 5, 0.1);
//            Imgcodecs.imwrite(dir.getAbsolutePath() + "/detected" + file.getName(), source);
//        }
        String image = args[0];
        preprocess(image);
        Mat res = filterPreprocessing(image);

        Imgcodecs.imwrite("grayblur"+image, res);
    }

    public static void dumpIndents(List<Line> allLines, List<Word> words, String inputName) {
        List<Integer> indents = new LinkedList<>();
        double indentSize = 0;
        for (Word word: words) {
            indentSize += word.getCoordinates().getWidth() / word.getText().length();
        }
        indentSize = indentSize / words.size();
        for (Line line : allLines) {
            indents.add(new Double(line.getCoordinates().getX_left() / indentSize).intValue());
        }
        int minIndent = Collections.min(indents);
        File inputFile = new File(inputName);
        String nameNoExt = inputName.substring(0, inputName.lastIndexOf("."));
        File resFile = new File((inputFile.getParent() == null ? "" : inputFile.getParent() + "/") + "INDENT_" + nameNoExt + ".txt");
        try {
            FileOutputStream fos = new FileOutputStream(resFile);
            PrintStream ps = new PrintStream(fos);
            for (Integer indent : indents) {
                ps.println(indent - minIndent);
            }
            ps.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Random myRandom = new Random(System.currentTimeMillis());

    public static void filterTextRectangles(String input, List<Coordinates> wordCoords, List<Word> words, int samplePointsCount, double epsilon, double threshold) {
        Mat source = Imgcodecs.imread(input,  Imgcodecs.CV_LOAD_IMAGE_COLOR);
        filterTextRectangles(source, wordCoords, samplePointsCount, epsilon, threshold);
        double wordHeight = 0;
        for (Word word : words) {
            wordHeight += word.getCoordinates().getWidth() / word.getText().length();
        }
        wordHeight = wordHeight / words.size();
        if (wordHeight < 30) {
            //resize here
            double scaleFactor = 30 / wordHeight;
            Imgproc.resize(source, source, new Size(source.width() * scaleFactor, source.height() * scaleFactor));
        }
        Imgcodecs.imwrite(getPreprocessedName(input), source);
    }

    public static void addRectangles(String input, List<Coordinates> words, List<Coordinates> lines) {
        Mat source = Imgcodecs.imread(input,  Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
        Mat dst = source;
        for (Coordinates coords : words) {
            Point leftUpper = new Point(coords.getX_left(), coords.getY_up());
            Point rightLower = new Point(coords.getX_left() + coords.getWidth(), coords.getY_up() + coords.getHeight());
            Imgproc.rectangle(dst, leftUpper, rightLower, new Scalar(255, 0, 0));
        }
        Imgcodecs.imwrite("rectangles_"+input, dst);
    }

    public static void onFailedPreprocessing(String input) {
        Mat source = Imgcodecs.imread(input, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
        Imgcodecs.imwrite(getPreprocessedName(input), source);
    }

    public static double segmentLength(Point a, Point b) {
        double x = a.x - b.x;
        double y = a.y - b.y;
        return Math.sqrt(x * x + y * y);
    }

    public static void findRectangles(Mat source, double threshold, double areaEpsilon, double hullAreaEpsilon) {
        Mat grayScale = new Mat(source.height(), source.width(), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
        Imgproc.cvtColor(source, grayScale, Imgproc.COLOR_BGR2GRAY);
//        Imgproc.blur(grayScale, grayScale, new Size(3, 3));
        Imgcodecs.imwrite("graydetect.png", grayScale);
        Mat result = new Mat(source.height(), source.width(), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
        Imgproc.Canny(grayScale, result, threshold, threshold * 2);
        Imgcodecs.imwrite("cannyResult.png", result);
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(result, contours, hierarchy, Imgproc.RETR_LIST , Imgproc.CHAIN_APPROX_SIMPLE, new Point(0,0));
        double imageArea = source.height() * source.width();
        int maxContour = 0;
        double maxArea = 0;
        for (int i = 0; i < contours.size(); ++i) {
            MatOfPoint contour = contours.get(i);
            Rect rect = Imgproc.boundingRect(contour);
            Moments moments = Imgproc.moments(contour);
            double area = moments.get_m00();
//            double perimeter = Imgproc.arcLength(new MatOfPoint2f(contour), true);
//            if (area / imageArea > areaEpsilon) {
//            if (Math.abs((area - rect.area()) / area) < areaEpsilon) {// && Math.abs((perimeter - rect.height * 2 - rect.width * 2) / perimeter) < perimeterEpsilon) {
//                Imgproc.drawContours(source, contours, i, new Scalar(0, 0, 255));
//                Imgproc.rectangle(source, rect.tl(), rect.br(), new Scalar(255, 0, 0));
//            }
            Point[] contourPoints = contour.toArray();
            if (contourPoints.length < 300 && imageArea / rect.area() < areaEpsilon) {
                if (rect.area() > maxArea) {
                    maxContour = i;
                    maxArea = rect.area();
                }
//                Mat other = new Mat(source.height(), source.width(), Imgcodecs.CV_LOAD_IMAGE_COLOR);
//                MatOfInt hull = new MatOfInt();
//                Imgproc.convexHull(contour, hull);
//                int[] hullArray = hull.toArray();
//                Point[] convexPoints = new Point[hullArray.length];
//                for (int j = 0; j < convexPoints.length; ++j) {
//                    convexPoints[j] = contourPoints[hullArray[j]];
//                }
//                MatOfPoint convexMat = new MatOfPoint(convexPoints);
//                if (convexPoints.length == 3) {
//                    //triangular convex hull
//                    Set<Double> sides = Sets.newTreeSet();
//                    sides.add(segmentLength(convexPoints[0], convexPoints[1]));
//                    sides.add(segmentLength(convexPoints[0], convexPoints[2]));
//                    sides.add(segmentLength(convexPoints[1], convexPoints[2]));
//                    double max = Collections.max(sides);
//                    sides.remove(max);
//                    double res = 0;
//                    for (Double d : sides) {
//                        res += d * d;
//                    }
//                    if (Math.abs(res - max * max) < epsilon) {
//                        //TODO this is a good convex hull
//                        System.out.println(i + " is a good rectangle");
//                        if (rect.area() > maxArea) {
//                            maxContour = i;
//                            maxArea = rect.area();
//                        }
//
//                    }
//                } else if (convexPoints.length == 4 && Math.abs(area - rect.area()) / rect.area() < hullAreaEpsilon) {
//                    //TODO this is a good convex shell
//                    System.out.println(i + " is a good rectangle");
//                    if (rect.area() > maxArea) {
//                        maxContour = i;
//                        maxArea = rect.area();
//                    }
//                }
//                Moments hullMoments = Imgproc.moments(convexMat);
//                double hullArea = hullMoments.get_m00();
//                source.copyTo(other);
//                Imgproc.rectangle(other, rect.tl(), rect.br(), new Scalar(255, 0, 0));
//                Imgproc.drawContours(other, contours, i, new Scalar(0, 0, 255));
//                Imgproc.drawContours(other, Lists.newArrayList(convexMat), 0, new Scalar(0, 255, 0));
//
//                Imgcodecs.imwrite(i + "contour.png", other);
//                System.out.println("Area for " + i + " = " + area + " rectangle area = " + rect.area() + " hull area = " + hullArea);
            }
        }
        Imgproc.drawContours(source, contours, maxContour, new Scalar(0, 0, 255));
        Rect rect = Imgproc.boundingRect(contours.get(maxContour));
        Imgproc.rectangle(source, rect.tl(), rect.br(), new Scalar(255, 0, 0));
    }
}