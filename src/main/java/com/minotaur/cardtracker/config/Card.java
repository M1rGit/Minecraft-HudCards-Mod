package com.minotaur.cardtracker.config;

import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class Card {
	public UUID id = UUID.randomUUID();
	public String itemId;
	public String customLabel;
	public int needed;
	public float anchorX = 0.5f;
	public float anchorY = 0.5f;
	public SizePreset size = SizePreset.M;
	public int order;

	public transient int have;
	private transient Item item;
	private transient ItemStack stack;

	public Card() {
	}

	public Card(String itemId, int needed, int order) {
		this.itemId = itemId;
		this.needed = needed;
		this.order = order;
	}

	/** Items.AIR when the item is not registered (e.g. its mod was removed); the card itself is kept. */
	public Item item() {
		if (item == null) {
			Identifier key = Identifier.tryParse(itemId);
			item = key == null ? Items.AIR : BuiltInRegistries.ITEM.getOptional(key).orElse(Items.AIR);
		}
		return item;
	}

	public ItemStack stack() {
		if (stack == null) stack = new ItemStack(item());
		return stack;
	}

	public String label() {
		if (customLabel != null) return customLabel;
		return item() == Items.AIR ? itemId : stack().getHoverName().getString();
	}
}
