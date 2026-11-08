package com.minotaur.cardtracker.gui;

import com.minotaur.cardtracker.CardTrackerClient;
import com.minotaur.cardtracker.config.Card;
import com.minotaur.cardtracker.config.CardStorage;
import com.minotaur.cardtracker.gui.widget.CardWidget;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class CardEditorScreen extends Screen {
	private static final int CLICK_SLOP = 2;

	private Card dragging;
	private double grabDX, grabDY, pressX, pressY;
	private int dragX, dragY;
	private boolean moved;

	public CardEditorScreen() {
		super(Component.translatable("cardtracker.editor.title"));
	}

	@Override
	protected void init() {
		int y = height - 28, x = width / 2 - 144;
		addRenderableWidget(Button.builder(Component.translatable("cardtracker.editor.style"),
				b -> minecraft.gui.setScreen(new StyleScreen(this)))
			.bounds(x, y, 80, 20).build());
		addRenderableWidget(Button.builder(Component.translatable("cardtracker.editor.add"),
				b -> minecraft.gui.setScreen(new ItemPickerScreen(this)))
			.bounds(x + 84, y, 120, 20).build());
		addRenderableWidget(Button.builder(Component.translatable("cardtracker.editor.close"), b -> onClose())
			.bounds(x + 208, y, 80, 20).build());
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
		g.fill(0, 0, width, height, 0x40000000);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
		g.centeredText(font, Component.translatable("cardtracker.editor.help"), width / 2, 6, 0xFFFFFFFF);
		for (Card c : CardStorage.byOrder()) {
			int x = c == dragging ? dragX : CardWidget.x(c, width);
			int y = c == dragging ? dragY : CardWidget.y(c, height);
			CardWidget.render(g, font, c, x, y, true);
		}
		super.extractRenderState(g, mouseX, mouseY, partialTick);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent e, boolean doubleClick) {
		if (super.mouseClicked(e, doubleClick)) return true;
		if (e.button() != 0) return false;
		Card c = cardAt(e.x(), e.y());
		if (c == null) return false;

		int x = CardWidget.x(c, width), y = CardWidget.y(c, height);
		if (CardWidget.overClose(c, x, y, e.x(), e.y())) {
			CardStorage.cards().remove(c);
			CardStorage.save();
			return true;
		}
		dragging = c;
		c.order = CardStorage.nextOrder();
		grabDX = e.x() - x;
		grabDY = e.y() - y;
		pressX = e.x();
		pressY = e.y();
		dragX = x;
		dragY = y;
		moved = false;
		return true;
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent e, double dx, double dy) {
		if (dragging == null) return super.mouseDragged(e, dx, dy);
		dragX = (int) Math.round(e.x() - grabDX);
		dragY = (int) Math.round(e.y() - grabDY);
		if (Math.abs(e.x() - pressX) > CLICK_SLOP || Math.abs(e.y() - pressY) > CLICK_SLOP) moved = true;
		return true;
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent e) {
		if (dragging == null || e.button() != 0) return super.mouseReleased(e);
		Card c = dragging;
		dragging = null;
		if (moved) {
			int w = CardWidget.width(c), h = CardWidget.height(c);
			c.anchorX = CardWidget.toAnchor(CardWidget.snap(dragX, w, width), w, width);
			c.anchorY = CardWidget.toAnchor(CardWidget.snap(dragY, h, height), h, height);
			CardStorage.save();
		} else {
			CardStorage.save();
			minecraft.gui.setScreen(new AmountScreen(this, c));
		}
		return true;
	}

	@Override
	public boolean mouseScrolled(double mx, double my, double scrollX, double scrollY) {
		Card c = cardAt(mx, my);
		if (c == null || scrollY == 0) return super.mouseScrolled(mx, my, scrollX, scrollY);
		c.size = scrollY > 0 ? c.size.next() : c.size.prev();
		CardStorage.save();
		return true;
	}

	@Override
	public boolean keyPressed(KeyEvent e) {
		if (CardTrackerClient.editorKey.matches(e)) {
			onClose();
			return true;
		}
		return super.keyPressed(e);
	}

	private Card cardAt(double mx, double my) {
		List<Card> cards = CardStorage.byOrder();
		for (int i = cards.size() - 1; i >= 0; i--) {
			Card c = cards.get(i);
			if (CardWidget.contains(c, CardWidget.x(c, width), CardWidget.y(c, height), mx, my)) return c;
		}
		return null;
	}
}
