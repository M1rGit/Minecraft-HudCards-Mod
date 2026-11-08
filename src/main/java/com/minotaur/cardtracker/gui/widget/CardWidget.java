package com.minotaur.cardtracker.gui.widget;

import com.minotaur.cardtracker.config.Card;
import com.minotaur.cardtracker.config.CardStorage;
import com.minotaur.cardtracker.config.CardStyle;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2fStack;

// Layout is in GUI units at preset scale 1, derived from the global style; the preset scale is applied through the pose matrix.
public final class CardWidget {
	public static final int SNAP_DISTANCE = 15;
	private static final int PAD = 2;
	private static final int GAP = 2;
	private static final int LINE = 9;
	private static final int LINE_GAP = 1;
	private static final int TEXT_W = 74;
	private static final int ICON = 16;
	private static final int CLOSE = 8;

	private static final int TEXT = 0xFFFFFFFF;
	private static final int DONE = 0xFF55FF55;
	private static final String ELLIPSIS = "...";

	private record Layout(int w, int h, float icon, float iconY, float textX, float textW, float labelY, float counterY) {
		static Layout of(CardStyle s) {
			float icon = ICON * s.iconScale;
			float labelH = LINE * s.labelScale;
			float textH = labelH + LINE_GAP + LINE * s.counterScale;
			int h = (int) Math.ceil(PAD * 2 + Math.max(icon, textH));
			float textX = PAD + icon + GAP;
			float textW = TEXT_W * Math.max(s.labelScale, s.counterScale);
			int w = (int) Math.ceil(textX + textW + PAD);
			float labelY = (h - textH) / 2;
			return new Layout(w, h, icon, (h - icon) / 2, textX, textW, labelY, labelY + labelH + LINE_GAP);
		}
	}

	private CardWidget() {
	}

	public static int width(Card c) {
		return Math.round(Layout.of(CardStorage.style()).w * c.size.scale);
	}

	public static int height(Card c) {
		return Math.round(Layout.of(CardStorage.style()).h * c.size.scale);
	}

	// Anchor is the fraction of the free space (screen minus card), so 0 and 1 stay flush with the edges after a resize.
	public static int toPixels(float anchor, int size, int screen) {
		return Math.round(anchor * Math.max(0, screen - size));
	}

	public static float toAnchor(int pos, int size, int screen) {
		int free = screen - size;
		return free <= 0 ? 0f : Math.clamp(pos / (float) free, 0f, 1f);
	}

	public static int snap(int pos, int size, int screen) {
		if (pos < SNAP_DISTANCE) return 0;
		if (screen - (pos + size) < SNAP_DISTANCE) return Math.max(0, screen - size);
		return pos;
	}

	public static int x(Card c, int screenW) {
		return toPixels(c.anchorX, width(c), screenW);
	}

	public static int y(Card c, int screenH) {
		return toPixels(c.anchorY, height(c), screenH);
	}

	public static boolean contains(Card c, int x, int y, double mx, double my) {
		return mx >= x && my >= y && mx < x + width(c) && my < y + height(c);
	}

	public static boolean overClose(Card c, int x, int y, double mx, double my) {
		float s = c.size.scale;
		int w = Layout.of(CardStorage.style()).w;
		double lx = (mx - x) / s, ly = (my - y) / s;
		return lx >= w - CLOSE && lx < w && ly >= 0 && ly < CLOSE;
	}

	public static void render(GuiGraphicsExtractor g, Font font, Card c, int x, int y, boolean editing) {
		CardStyle style = CardStorage.style();
		Layout l = Layout.of(style);
		Matrix3x2fStack pose = g.pose();
		pose.pushMatrix();
		pose.translate(x, y);
		pose.scale(c.size.scale, c.size.scale);

		g.fill(0, 0, l.w, l.h, style.backgroundArgb());

		pose.pushMatrix();
		pose.translate(PAD, l.iconY);
		pose.scale(style.iconScale, style.iconScale);
		g.item(c.stack(), 0, 0);
		pose.popMatrix();

		float labelW = (l.textW - (editing ? CLOSE : 0)) / style.labelScale;
		text(g, font, ellipsize(font, c.label(), (int) labelW), l.textX, l.labelY, style.labelScale, TEXT);
		text(g, font, c.have + "/" + c.needed, l.textX, l.counterY, style.counterScale, c.have >= c.needed ? DONE : TEXT);

		if (editing) {
			g.outline(0, 0, l.w, l.h, 0xFFAAAAAA);
			g.fill(l.w - CLOSE, 0, l.w, CLOSE, 0xFFAA2222);
			g.text(font, "x", l.w - CLOSE + 2, 0, TEXT, false);
		}
		pose.popMatrix();
	}

	private static void text(GuiGraphicsExtractor g, Font font, String s, float x, float y, float scale, int color) {
		Matrix3x2fStack pose = g.pose();
		pose.pushMatrix();
		pose.translate(x, y);
		pose.scale(scale, scale);
		g.text(font, s, 0, 0, color, true);
		pose.popMatrix();
	}

	private static String ellipsize(Font font, String s, int maxW) {
		if (font.width(s) <= maxW) return s;
		return font.plainSubstrByWidth(s, maxW - font.width(ELLIPSIS)) + ELLIPSIS;
	}
}
