package com.minotaur.cardtracker.gui;

import com.minotaur.cardtracker.config.Card;
import com.minotaur.cardtracker.config.CardStorage;
import com.minotaur.cardtracker.config.CardStyle;
import com.minotaur.cardtracker.gui.widget.CardWidget;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

// Global card style; edits apply live to CardStorage.style() and are written to disk when the screen closes.
public class StyleScreen extends Screen {
	private static final int ROW = 24;
	private static final int COL_W = 150;
	private static final int TEXT_OK = 0xFFE0E0E0;
	private static final int TEXT_BAD = 0xFFFF5555;

	private final Screen parent;
	private final Card sample = new Card("minecraft:iron_ingot", 64, 0);

	public StyleScreen(Screen parent) {
		super(Component.translatable("cardtracker.style.title"));
		this.parent = parent;
		sample.have = 32;
	}

	@Override
	protected void init() {
		CardStyle s = CardStorage.style();
		int x = width / 2 - COL_W - 10, y = height / 2 - 3 * ROW;

		EditBox hex = new EditBox(font, x + 50, y, COL_W - 50, 20, Component.translatable("cardtracker.style.background"));
		hex.setMaxLength(7);
		hex.setHint(Component.literal("#RRGGBB"));
		hex.setValue(s.background);
		hex.setResponder(v -> {
			boolean ok = CardStyle.isValidHex(v);
			if (ok) CardStorage.style().background = CardStyle.normalizeHex(v);
			hex.setTextColor(ok ? TEXT_OK : TEXT_BAD);
		});
		addRenderableWidget(hex);

		addRenderableWidget(new Slider(x, y += ROW, "cardtracker.style.transparency", 0f, 1f, 0.05f, 1f - s.opacity,
			v -> CardStorage.style().opacity = 1f - v, v -> Math.round(v * 100) + "%"));
		addRenderableWidget(scaleSlider(x, y += ROW, "cardtracker.style.label_size", s.labelScale, v -> CardStorage.style().labelScale = v));
		addRenderableWidget(scaleSlider(x, y += ROW, "cardtracker.style.counter_size", s.counterScale, v -> CardStorage.style().counterScale = v));
		addRenderableWidget(scaleSlider(x, y += ROW, "cardtracker.style.icon_size", s.iconScale, v -> CardStorage.style().iconScale = v));

		y += ROW;
		int half = (COL_W - 4) / 2;
		addRenderableWidget(Button.builder(Component.translatable("controls.reset"), b -> {
			CardStorage.resetStyle();
			rebuildWidgets();
		}).bounds(x, y, half, 20).build());
		addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose())
			.bounds(x + half + 4, y, half, 20).build());
	}

	private static Slider scaleSlider(int x, int y, String key, float value, Consumer<Float> apply) {
		return new Slider(x, y, key, CardStyle.MIN_SCALE, CardStyle.MAX_SCALE, 0.25f, value, apply,
			v -> String.format(Locale.ROOT, "%.2fx", v));
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
		g.fill(0, 0, width, height, 0x60000000);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(g, mouseX, mouseY, partialTick);
		g.centeredText(font, title, width / 2, 10, 0xFFFFFFFF);
		g.text(font, Component.translatable("cardtracker.style.background"), width / 2 - COL_W - 10, height / 2 - 3 * ROW + 6, 0xFFFFFFFF);

		int previewCenter = width / 2 + (width / 2) / 2;
		int px = previewCenter - CardWidget.width(sample) / 2, py = height / 2 - CardWidget.height(sample) / 2;
		CardWidget.render(g, font, sample, px, py, false);
	}

	@Override
	public void onClose() {
		minecraft.gui.setScreen(parent);
	}

	@Override
	public void removed() {
		CardStorage.save();
	}

	private static class Slider extends AbstractSliderButton {
		private final String key;
		private final float min, max, step;
		private final Consumer<Float> apply;
		private final Function<Float, String> format;

		Slider(int x, int y, String key, float min, float max, float step, float initial,
				Consumer<Float> apply, Function<Float, String> format) {
			super(x, y, COL_W, 20, Component.empty(), (initial - min) / (max - min));
			this.key = key;
			this.min = min;
			this.max = max;
			this.step = step;
			this.apply = apply;
			this.format = format;
			updateMessage();
		}

		private float current() {
			return min + Math.round(value * (max - min) / step) * step;
		}

		@Override
		protected void updateMessage() {
			setMessage(Component.translatable(key, format.apply(current())));
		}

		@Override
		protected void applyValue() {
			apply.accept(current());
		}
	}
}
