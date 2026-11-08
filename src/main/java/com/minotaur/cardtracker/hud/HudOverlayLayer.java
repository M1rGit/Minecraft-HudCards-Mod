package com.minotaur.cardtracker.hud;

import com.minotaur.cardtracker.config.Card;
import com.minotaur.cardtracker.config.CardStorage;
import com.minotaur.cardtracker.gui.CardEditorScreen;
import com.minotaur.cardtracker.gui.widget.CardWidget;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class HudOverlayLayer implements HudElement {
	public static boolean visible = true;

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, DeltaTracker delta) {
		Minecraft mc = Minecraft.getInstance();
		if (!visible || mc.gui.screen() instanceof CardEditorScreen) return;
		int w = g.guiWidth(), h = g.guiHeight();
		for (Card c : CardStorage.byOrder()) {
			CardWidget.render(g, mc.font, c, CardWidget.x(c, w), CardWidget.y(c, h), false);
		}
	}
}
