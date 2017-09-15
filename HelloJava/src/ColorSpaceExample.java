import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;

public class ColorSpaceExample {

	public static void main(String[] args) {
		testInterpolate();
	}

	private static void testInterpolate() {
		//		testConversion("XYZ", Color.RED, ColorSpace::convertRGBtoXYZ, ColorSpace::convertXYZtoRGB);
		//		testConversion("LAB", Color.RED, ColorSpace::convertRGBtoCIELAB, ColorSpace::convertCIELABtoRGB);

		drawScales("red_blue", Color.RED, Color.BLUE);
		drawScales("yellow_magenta", Color.YELLOW, Color.MAGENTA);
		drawScales("orange_green", Color.ORANGE, Color.GREEN);
		drawScales("rnd_rnd", new Color((float) Math.random(), (float) Math.random(), (float) Math.random()), new Color((float) Math.random(), (float) Math.random(), (float) Math.random()));
	}

	private static void drawScales(String name, Color startColor, Color endColor) {
		drawScale(name + "_rgb", startColor, endColor, ColorSpace::convertIdentity, ColorSpace::convertIdentity);
		drawScale(name + "_hsv", startColor, endColor, ColorSpace::convertRGBtoHSV, ColorSpace::convertHSVtoRGB);
		drawScale(name + "_xyz", startColor, endColor, ColorSpace::convertRGBtoXYZ, ColorSpace::convertXYZtoRGB);
		drawScale(name + "_lab", startColor, endColor, ColorSpace::convertRGBtoCIELAB, ColorSpace::convertCIELABtoRGB);
	}

	private static void testConversion(String name, Color color, ColorSpaceConverter fromRgbConverter, ColorSpaceConverter toRgbConverter) {
		System.out.println("AWT:  " + color.toString());

		double[] rgb = ColorSpace.toRGB(color);
		System.out.println("RGB:  " + Arrays.toString(rgb));

		double[] space = new double[3];
		fromRgbConverter.convert(rgb, space);
		System.out.println(name + ":  " + Arrays.toString(space));

		double[] rgb2 = new double[3];
		toRgbConverter.convert(space, rgb2);
		System.out.println("RGB2: " + Arrays.toString(rgb2));

		System.out.println("AWT2: " + ColorSpace.toAwtColor(rgb2));

		System.out.println();
	}

	private static void drawScale(String name, Color startColor, Color endColor, ColorSpaceConverter fromRgbConverter, ColorSpaceConverter toRgbConverter) {
		int width = 800;
		int height = 100;

		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		drawScale(
				image.getGraphics(),
				width,
				height,
				startColor,
				endColor,
				fromRgbConverter,
				toRgbConverter);

		try {
			ImageIO.write(image, "png", new File(name + ".png"));
		}
		catch (IOException theCause) {
			theCause.printStackTrace();
		}
	}

	private static void drawScale(Graphics graphics, int width, int height, Color startColor, Color endColor, ColorSpaceConverter fromRgbConverter, ColorSpaceConverter toRgbConverter) {
		double[] startRgb = ColorSpace.toRGB(startColor);
		double[] endRgb = ColorSpace.toRGB(endColor);
		double[] rgb = ColorSpace.toRGB(startColor);

		double[] startSpace = new double[3];
		double[] endSpace = new double[3];
		double[] space = new double[3];

		fromRgbConverter.convert(startRgb, startSpace);
		fromRgbConverter.convert(endRgb, endSpace);

		for (int x = 0; x < width; x++) {
			double factor = (double) x / width;
			ColorSpace.interpolate(space, startSpace, endSpace, factor);
			toRgbConverter.convert(space, rgb);

			Color color = ColorSpace.toAwtColor(rgb);
			graphics.setColor(color);
			graphics.fillRect(x, 0, x, height);
		}
	}

	public interface ColorSpaceConverter {
		void convert(double[] from, double[] to);
	}
}
