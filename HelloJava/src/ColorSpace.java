
/**
 * The {@link ColorSpace}.
 * 
 * http://easyrgb.com/en/math.php
 */
public class ColorSpace {

	// Reference: D65 2
	private static final double XYZ_REFERENCE_X = 95.047;
	private static final double XYZ_REFERENCE_Y = 100.000;
	private static final double XYZ_REFERENCE_Z = 108.883;

	public static java.awt.Color toAwtColor(double[] rgb) {
		return new java.awt.Color((float) rgb[0], (float) rgb[1], (float) rgb[2]);
	}

	public static double[] toRGB(java.awt.Color color) {
		return new double[] {
				color.getRed() / 255.0,
				color.getGreen() / 255.0,
				color.getBlue() / 255.0,
		};
	}

	public static double[] toRGB(double r, double g, double b) {
		return new double[] {
				clamp(r, 0f, 1f),
				clamp(g, 0f, 1f),
				clamp(b, 0f, 1f) };
	}

	public static void convertRGBtoHSV(double[] rgb, double[] hsv) {
		convertRGBtoHSV(rgb[0], rgb[1], rgb[2], hsv);
	}

	public static void convertRGBtoHSV(double r, double g, double b, double[] hsv) {
		double h;
		double s;
		double v;

		double max = Math.max(Math.max(r, g), b);
		double min = Math.min(Math.min(r, g), b);

		double delta = max - min;

		v = max;
		if (max != 0) {
			s = delta / max;
		}
		else {
			s = 0;
		}
		if (s == 0) {
			h = 0;
		}
		else {
			double redc = (max - r) / delta;
			double greenc = (max - g) / delta;
			double bluec = (max - b) / delta;

			if (r == max) {
				h = bluec - greenc;
			}
			else if (g == max) {
				h = 2.0 + redc - bluec;
			}
			else {
				h = 4.0 + greenc - redc;
			}
			h = h / 6.0;

			if (h < 0) {
				h += 1.0;
			}
			else if (h > 1.0) {
				h -= 1.0;
			}
		}

		hsv[0] = h;
		hsv[1] = s;
		hsv[2] = v;
	}

	public static void convertHSVtoRGB(double[] hsv, double[] rgb) {
		convertHSVtoRGB(hsv[0], hsv[1], hsv[2], rgb);
	}

	public static void convertHSVtoRGB(double h, double s, double v, double[] rgb) {
		double r;
		double g;
		double b;

		if (s == 0) {
			r = v;
			g = v;
			b = v;
		}
		else {
			double hh = h * 6;
			if (hh == 6.0) {
				hh = 0;
			}

			int part = (int) hh;

			double var1 = v * (1 - s);
			double var2 = v * (1 - s * (hh - part));
			double var3 = v * (1 - s * (1 - (hh - part)));

			switch (part) {
				case 0:
					r = v;
					g = var3;
					b = var1;
					break;
				case 1:
					r = var2;
					g = v;
					b = var1;
					break;
				case 2:
					r = var1;
					g = v;
					b = var3;
					break;
				case 3:
					r = var1;
					g = var2;
					b = v;
					break;
				case 4:
					r = var3;
					g = var1;
					b = v;
					break;
				case 5:
					r = v;
					g = var1;
					b = var2;
					break;
				default:
					throw new IllegalArgumentException("part=" + part);
			}
		}

		rgb[0] = r;
		rgb[1] = g;
		rgb[2] = b;
	}

	public static void convertRGBtoXYZ(double[] rgb, double[] xyz) {
		convertRGBtoXYZ(rgb[0], rgb[1], rgb[2], xyz);
	}

	public static void convertRGBtoXYZ(double r, double g, double b, double[] xyz) {
		double rr = pivotRGB(r);
		double gg = pivotRGB(g);
		double bb = pivotRGB(b);

		double x = rr * 0.4124 + gg * 0.3576 + bb * 0.1805;
		double y = rr * 0.2126 + gg * 0.7152 + bb * 0.0722;
		double z = rr * 0.0193 + gg * 0.1192 + bb * 0.9505;

		xyz[0] = x * 100;
		xyz[1] = y * 100;
		xyz[2] = z * 100;
	}

	public static double[] toXYZ(double x, double y, double z) {
		return new double[] {
				clamp(x, 0f, 100f),
				clamp(y, 0f, 100f),
				clamp(z, 0f, 100f) };
	}

	public static void convertIdentity(double[] from, double[] to) {
		for (int i = 0; i < to.length; i++) {
			to[i] = from[i];
		}
	}

	public static void convertXYZtoRGB(double[] xyz, double[] rgb) {
		convertXYZtoRGB(xyz[0], xyz[1], xyz[2], rgb);
	}

	public static void convertXYZtoRGB(double x, double y, double z, double[] rgb) {
		x = x / 100;
		y = y / 100;
		z = z / 100;

		double r = x * 3.2406 + y * -1.5372 + z * -0.4986;
		double g = x * -0.9689 + y * 1.8758 + z * 0.0415;
		double b = x * 0.0557 + y * -0.2040 + z * 1.0570;

		r = inversePivotRGB(r);
		g = inversePivotRGB(g);
		b = inversePivotRGB(b);

		rgb[0] = clamp(r, 0, 1);
		rgb[1] = clamp(g, 0, 1);
		rgb[2] = clamp(b, 0, 1);
	}

	public static void convertRGBtoCIELAB(double[] rgb, double[] lab) {
		convertRGBtoXYZ(rgb, lab);
		convertXYZtoCIELAB(lab, lab);
	}

	public static void convertCIELABtoRGB(double[] lab, double[] rgb) {
		convertCIELABtoXYZ(lab, rgb);
		convertXYZtoRGB(rgb, rgb);
	}

	public static void convertXYZtoCIELAB(double[] xyz, double[] lab) {
		convertXYZtoCIELAB(xyz[0], xyz[1], xyz[2], lab);
	}

	public static void convertXYZtoCIELAB(double x, double y, double z, double[] lab) {
		double xx = pivotXYZforCIELAB(x / XYZ_REFERENCE_X);
		double yy = pivotXYZforCIELAB(y / XYZ_REFERENCE_Y);
		double zz = pivotXYZforCIELAB(z / XYZ_REFERENCE_Z);

		double l = 116 * yy - 16;
		double a = 500 * (xx - yy);
		double b = 200 * (yy - zz);

		lab[0] = l;
		lab[1] = a;
		lab[2] = b;
	}

	public static void convertCIELABtoXYZ(double[] lab, double[] xyz) {
		convertCIELABtoXYZ(lab[0], lab[1], lab[2], xyz);
	}

	public static void convertCIELABtoXYZ(double l, double a, double b, double[] xyz) {
		double y = (l + 16) / 116;
		double x = a / 500 + y;
		double z = y - b / 200;

		x = inversePivotXYZforCIELAB(x);
		y = inversePivotXYZforCIELAB(y);
		z = inversePivotXYZforCIELAB(z);

		xyz[0] = x * XYZ_REFERENCE_X;
		xyz[1] = y * XYZ_REFERENCE_Y;
		xyz[2] = z * XYZ_REFERENCE_Z;
	}

	private static double pivotXYZforCIELAB(double value) {
		if (value > 0.008856) {
			return Math.pow(value, 1.0 / 3);
		}
		else {
			return (7.787 * value) + (16.0 / 116);
		}
	}

	private static double inversePivotXYZforCIELAB(double value) {
		double valuePower3 = value * value * value;
		if (valuePower3 > 0.008856) {
			return valuePower3;
		}
		else {
			return (value - 16.0 / 116) / 7.787;
		}
	}

	public static void interpolate(double[] color, double[] startColor, double[] endColor, double factor) {
		for (int i = 0; i < color.length; i++) {
			color[i] = startColor[i] + (endColor[i] - startColor[i]) * factor;
		}
	}

	private static double clamp(double value, double min, double max) {
		if (value < min) {
			return min;
		}
		else if (value > max) {
			return max;
		}
		else {
			return value;
		}
	}

	private static double pivotRGB(double value) {
		if (value > 0.04045) {
			return Math.pow((value + 0.055) / 1.055, 2.4);
		}
		else {
			return value / 12.92;
		}
	}

	private static double inversePivotRGB(double value) {
		if (value > 0.0031308) {
			return 1.055 * Math.pow(value, 1 / 2.4) - 0.055;
		}
		else {
			return value * 12.92;
		}
	}
}
