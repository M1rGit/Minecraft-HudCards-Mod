package com.minotaur.cardtracker.scan;

import com.minotaur.cardtracker.config.Card;
import com.minotaur.cardtracker.config.CardStorage;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

// Main inventory + hotbar + offhand; armor and nested containers are ignored, items match by base Item only.
public final class InventoryScanTicker {
	private static final int INTERVAL_TICKS = 10;
	private static int ticks;

	private InventoryScanTicker() {
	}

	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(InventoryScanTicker::tick);
	}

	private static void tick(Minecraft mc) {
		LocalPlayer player = mc.player;
		if (player == null || ++ticks % INTERVAL_TICKS != 0) return;

		Map<Item, Integer> counts = new HashMap<>();
		for (ItemStack s : player.getInventory().getNonEquipmentItems()) add(counts, s);
		add(counts, player.getOffhandItem());

		for (Card c : CardStorage.cards()) c.have = counts.getOrDefault(c.item(), 0);
	}

	private static void add(Map<Item, Integer> counts, ItemStack s) {
		if (!s.isEmpty()) counts.merge(s.getItem(), s.getCount(), Integer::sum);
	}
}
