package com.minotaur.cardtracker.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.minotaur.cardtracker.CardTrackerClient;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CardStorage {
	private static final Logger LOG = LoggerFactory.getLogger(CardTrackerClient.MOD_ID);
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().create();

	private static final List<Card> cards = new ArrayList<>();
	private static Path path;
	private static CardStyle style = new CardStyle();

	private record FileFormat(List<Card> cards, CardStyle style) {
	}

	private CardStorage() {
	}

	public static List<Card> cards() {
		return cards;
	}

	public static CardStyle style() {
		return style;
	}

	public static void resetStyle() {
		style = new CardStyle();
	}

	/** Cards sorted by z-order, bottom first. */
	public static List<Card> byOrder() {
		List<Card> sorted = new ArrayList<>(cards);
		sorted.sort(Comparator.comparingInt(c -> c.order));
		return sorted;
	}

	public static int nextOrder() {
		return cards.stream().mapToInt(c -> c.order).max().orElse(-1) + 1;
	}

	public static void load() {
		load(FabricLoader.getInstance().getConfigDir().resolve(CardTrackerClient.MOD_ID).resolve("cards.json"));
	}

	public static void save() {
		try {
			Files.createDirectories(path.getParent());
			Path tmp = path.resolveSibling(path.getFileName() + ".tmp");
			Files.writeString(tmp, GSON.toJson(new FileFormat(cards, style)));
			Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
		} catch (IOException e) {
			LOG.error("Failed to save {}", path, e);
		}
	}

	static void load(Path file) {
		path = file;
		cards.clear();
		style = new CardStyle();
		if (!Files.exists(file)) return;
		try {
			FileFormat data = GSON.fromJson(Files.readString(file), FileFormat.class);
			if (data == null || data.cards() == null) throw new JsonParseException("no \"cards\" array");
			if (data.style() != null) {
				style = data.style();
				style.sanitize();
			}
			for (Card c : data.cards()) {
				if (c == null || c.itemId == null) continue;
				if (c.id == null) c.id = UUID.randomUUID();
				if (c.size == null) c.size = SizePreset.M;
				c.anchorX = clamp01(c.anchorX);
				c.anchorY = clamp01(c.anchorY);
				cards.add(c);
			}
		} catch (IOException | RuntimeException e) {
			Path bak = file.resolveSibling(file.getFileName() + ".bak");
			LOG.warn("Unreadable {}, moving it to {} and starting with an empty list", file, bak, e);
			cards.clear();
			style = new CardStyle();
			try {
				Files.move(file, bak, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException moveError) {
				LOG.error("Failed to back up {}", file, moveError);
			}
		}
	}

	private static float clamp01(float v) {
		return Float.isNaN(v) ? 0.5f : Math.clamp(v, 0f, 1f);
	}
}
