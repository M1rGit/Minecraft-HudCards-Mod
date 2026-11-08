package com.minotaur.cardtracker.gui;

import com.minotaur.cardtracker.config.Card;
import com.minotaur.cardtracker.config.CardStorage;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

// "How many needed" form: creates a new card for an item, or edits an existing card's amount.
public class AmountScreen extends Screen {
	private static final int MAX = 9_999_999;

	private final Screen parent;
	private final Item item;
	private final Card card;
	private final ItemStack stack;
	private EditBox input;
	private Button done;

	public AmountScreen(Screen parent, Item item) {
		this(parent, item, null);
	}

	public AmountScreen(Screen parent, Card card) {
		this(parent, card.item(), card);
	}

	private AmountScreen(Screen parent, Item item, Card card) {
		super(Component.translatable("cardtracker.amount.title"));
		this.parent = parent;
		this.item = item;
		this.card = card;
		this.stack = card != null ? card.stack() : new ItemStack(item);
	}

	@Override
	protected void init() {
		String value = input != null ? input.getValue() : card != null ? String.valueOf(card.needed) : "";
		input = new EditBox(font, width / 2 - 50, height / 2 - 10, 100, 20, title);
		input.setMaxLength(String.valueOf(MAX).length());
		input.setValue(value);
		input.setResponder(v -> done.active = parse(v) > 0);
		addRenderableWidget(input);
		done = addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> submit())
			.bounds(width / 2 - 50, height / 2 + 16, 100, 20).build());
		done.active = parse(value) > 0;
		setInitialFocus(input);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(g, mouseX, mouseY, partialTick);
		String label = card != null ? card.label() : stack.getHoverName().getString();
		g.item(stack, width / 2 - 8, height / 2 - 52);
		g.centeredText(font, label, width / 2, height / 2 - 32, 0xFFFFFFFF);
		g.centeredText(font, title, width / 2, height / 2 - 22, 0xFFAAAAAA);
	}

	@Override
	public boolean keyPressed(KeyEvent e) {
		if (e.isConfirmation() && done.active) {
			submit();
			return true;
		}
		return super.keyPressed(e);
	}

	private void submit() {
		int n = parse(input.getValue());
		if (n <= 0) return;
		if (card != null) {
			card.needed = n;
		} else {
			String id = BuiltInRegistries.ITEM.getKey(item).toString();
			CardStorage.cards().add(new Card(id, n, CardStorage.nextOrder()));
		}
		CardStorage.save();
		minecraft.gui.setScreen(parent);
	}

	@Override
	public void onClose() {
		minecraft.gui.setScreen(parent);
	}

	private static int parse(String s) {
		try {
			int n = Integer.parseInt(s.trim());
			return n > 0 && n <= MAX ? n : -1;
		} catch (NumberFormatException e) {
			return -1;
		}
	}
}
