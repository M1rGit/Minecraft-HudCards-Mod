package com.minotaur.cardtracker.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.minotaur.cardtracker.gui.widget.CardWidget;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CardStorageTest {
	@TempDir
	Path dir;

	@Test
	void roundTrip() {
		Path file = dir.resolve("cardtracker/cards.json");
		CardStorage.load(file);
		assertTrue(CardStorage.cards().isEmpty());

		Card c = new Card("minecraft:iron_ingot", 24, 0);
		c.anchorX = 0.82f;
		c.size = SizePreset.XL;
		c.have = 5;
		CardStorage.cards().add(c);
		CardStorage.style().background = "#1A2B3C";
		CardStorage.style().opacity = 0.25f;
		CardStorage.style().iconScale = 2f;
		CardStorage.save();

		CardStorage.load(file);
		assertEquals("#1A2B3C", CardStorage.style().background);
		assertEquals(0x401A2B3C, CardStorage.style().backgroundArgb());
		assertEquals(2f, CardStorage.style().iconScale);
		assertEquals(1f, CardStorage.style().labelScale);
		Card r = CardStorage.cards().getFirst();
		assertEquals(c.id, r.id);
		assertEquals("minecraft:iron_ingot", r.itemId);
		assertEquals(24, r.needed);
		assertEquals(0.82f, r.anchorX);
		assertEquals(SizePreset.XL, r.size);
		assertEquals(0, r.have);
	}

	@Test
	void corruptFileIsBackedUp() throws Exception {
		Path file = dir.resolve("cards.json");
		Files.writeString(file, "{ not json");
		CardStorage.load(file);
		assertTrue(CardStorage.cards().isEmpty());
		assertTrue(Files.exists(dir.resolve("cards.json.bak")));
		assertTrue(Files.notExists(file));
	}

	@Test
	void invalidStyleIsSanitized() throws Exception {
		Path file = dir.resolve("cards.json");
		Files.writeString(file, """
			{"cards": [], "style": {"background": "red", "opacity": 7, "labelScale": 0.1}}""");
		CardStorage.load(file);
		assertEquals("#000000", CardStorage.style().background);
		assertEquals(1f, CardStorage.style().opacity);
		assertEquals(CardStyle.MIN_SCALE, CardStorage.style().labelScale);
		assertTrue(CardStyle.isValidHex("ff00aa"));
		assertEquals("#FF00AA", CardStyle.normalizeHex("ff00aa"));
	}

	@Test
	void anchorsAndSnapping() {
		assertEquals(0, CardWidget.snap(14, 100, 400));
		assertEquals(300, CardWidget.snap(290, 100, 400));
		assertEquals(150, CardWidget.snap(150, 100, 400));
		assertEquals(1f, CardWidget.toAnchor(300, 100, 400));
		assertEquals(500, CardWidget.toPixels(1f, 100, 600));
		assertEquals(0f, CardWidget.toAnchor(-20, 100, 400));
		assertEquals(SizePreset.S, SizePreset.XL.next());
		assertEquals(SizePreset.XL, SizePreset.S.prev());
	}
}
