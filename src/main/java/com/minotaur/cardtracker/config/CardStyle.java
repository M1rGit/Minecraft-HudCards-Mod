package com.minotaur.cardtracker.config;

import java.util.regex.Pattern;

public class CardStyle {
	public static final float MIN_SCALE = 0.5f;
	public static final float MAX_SCALE = 3.0f;
	private static final Pattern HEX = Pattern.compile("#?[0-9a-fA-F]{6}");

	public String background = "#000000";
	public float opacity = 0.5f;
	public float labelScale = 1f;
	public float counterScale = 1f;
	public float iconScale = 1f;

	public static boolean isValidHex(String s) {
		return s != null && HEX.matcher(s.trim()).matches();
	}

	public static String normalizeHex(String s) {
		String t = s.trim().toUpperCase();
		return t.startsWith("#") ? t : "#" + t;
	}

	public int backgroundArgb() {
		int rgb = Integer.parseInt(background.substring(1), 16);
		return Math.round(opacity * 255) << 24 | rgb;
	}

	void sanitize() {
		background = isValidHex(background) ? normalizeHex(background) : "#000000";
		opacity = clamp(opacity, 0f, 1f, 0.5f);
		labelScale = clamp(labelScale, MIN_SCALE, MAX_SCALE, 1f);
		counterScale = clamp(counterScale, MIN_SCALE, MAX_SCALE, 1f);
		iconScale = clamp(iconScale, MIN_SCALE, MAX_SCALE, 1f);
	}

	private static float clamp(float v, float min, float max, float fallback) {
		return Float.isNaN(v) ? fallback : Math.clamp(v, min, max);
	}
}
