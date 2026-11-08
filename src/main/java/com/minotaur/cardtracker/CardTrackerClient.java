package com.minotaur.cardtracker;

import com.minotaur.cardtracker.config.CardStorage;
import com.minotaur.cardtracker.gui.CardEditorScreen;
import com.minotaur.cardtracker.hud.HudOverlayLayer;
import com.minotaur.cardtracker.scan.InventoryScanTicker;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public class CardTrackerClient implements ClientModInitializer {
	public static final String MOD_ID = "cardtracker";

	public static KeyMapping editorKey;
	public static KeyMapping toggleKey;

	@Override
	public void onInitializeClient() {
		CardStorage.load();

		KeyMapping.Category category = KeyMapping.Category.register(id("main"));
		editorKey = KeyMappingHelper.registerKeyMapping(
			new KeyMapping("key.cardtracker.editor", InputConstants.Type.KEYSYM, InputConstants.KEY_K, category));
		toggleKey = KeyMappingHelper.registerKeyMapping(
			new KeyMapping("key.cardtracker.toggle", InputConstants.Type.KEYSYM, InputConstants.KEY_J, category));

		HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, id("cards"), new HudOverlayLayer());
		InventoryScanTicker.register();

		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			while (editorKey.consumeClick()) {
				if (mc.gui.screen() == null) mc.gui.setScreen(new CardEditorScreen());
			}
			while (toggleKey.consumeClick()) HudOverlayLayer.visible = !HudOverlayLayer.visible;
		});
	}

	private static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
