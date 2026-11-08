package com.minotaur.cardtracker.gui;

import java.util.List;
import java.util.Locale;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ItemPickerScreen extends Screen {
	private static final int CELL = 18;
	private static final int TOP = 32;

	private record Entry(Item item, ItemStack stack, String search) {
	}

	private final Screen parent;
	private List<Entry> all;
	private List<Entry> filtered = List.of();
	private EditBox search;
	private int scrollRow;

	public ItemPickerScreen(Screen parent) {
		super(Component.translatable("cardtracker.picker.title"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		if (all == null) {
			// Name and registry id are both searchable, so modded items are reachable even with odd names.
			all = BuiltInRegistries.ITEM.stream()
				.filter(i -> i != Items.AIR)
				.map(i -> {
					ItemStack s = new ItemStack(i);
					String key = s.getHoverName().getString() + " " + BuiltInRegistries.ITEM.getKey(i);
					return new Entry(i, s, key.toLowerCase(Locale.ROOT));
				})
				.toList();
		}
		String query = search == null ? "" : search.getValue();
		search = new EditBox(font, width / 2 - 100, 8, 200, 18, Component.translatable("cardtracker.picker.search"));
		search.setHint(Component.translatable("cardtracker.picker.search"));
		search.setValue(query);
		search.setResponder(this::refilter);
		addRenderableWidget(search);
		setInitialFocus(search);
		refilter(query);
	}

	private void refilter(String query) {
		String q = query.trim().toLowerCase(Locale.ROOT);
		filtered = q.isEmpty() ? all : all.stream().filter(e -> e.search.contains(q)).toList();
		scrollRow = 0;
	}

	private int cols() {
		return Math.max(1, (width - 20) / CELL);
	}

	private int left() {
		return (width - cols() * CELL) / 2;
	}

	private int visibleRows() {
		return Math.max(1, (height - TOP - 8) / CELL);
	}

	private int maxScroll() {
		int rows = (filtered.size() + cols() - 1) / cols();
		return Math.max(0, rows - visibleRows());
	}

	private int indexAt(double mx, double my) {
		if (mx < left() || my < TOP) return -1;
		int col = (int) ((mx - left()) / CELL), row = (int) ((my - TOP) / CELL);
		if (col >= cols() || row >= visibleRows()) return -1;
		int i = (scrollRow + row) * cols() + col;
		return i < filtered.size() ? i : -1;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(g, mouseX, mouseY, partialTick);
		if (filtered.isEmpty()) {
			g.centeredText(font, Component.translatable("cardtracker.picker.empty"), width / 2, TOP + 8, 0xFFAAAAAA);
			return;
		}
		int cols = cols(), first = scrollRow * cols;
		int last = Math.min(filtered.size(), first + visibleRows() * cols);
		int hovered = indexAt(mouseX, mouseY);
		for (int i = first; i < last; i++) {
			int x = left() + (i - first) % cols * CELL, y = TOP + (i - first) / cols * CELL;
			if (i == hovered) g.fill(x, y, x + CELL, y + CELL, 0x80FFFFFF);
			g.item(filtered.get(i).stack, x + 1, y + 1);
		}
		if (hovered >= 0) g.setTooltipForNextFrame(font, filtered.get(hovered).stack, mouseX, mouseY);
	}

	@Override
	public boolean mouseScrolled(double mx, double my, double scrollX, double scrollY) {
		scrollRow = Math.clamp(scrollRow - (long) Math.signum(scrollY), 0, maxScroll());
		return true;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent e, boolean doubleClick) {
		if (super.mouseClicked(e, doubleClick)) return true;
		int i = indexAt(e.x(), e.y());
		if (e.button() != 0 || i < 0) return false;
		minecraft.gui.setScreen(new AmountScreen(parent, filtered.get(i).item));
		return true;
	}

	@Override
	public void onClose() {
		minecraft.gui.setScreen(parent);
	}
}
