package com.minotaur.cardtracker.config;

public enum SizePreset {
	S(1.0f), M(1.5f), L(2.0f), XL(2.5f);

	public final float scale;

	SizePreset(float scale) {
		this.scale = scale;
	}

	public SizePreset next() {
		return values()[(ordinal() + 1) % values().length];
	}

	public SizePreset prev() {
		return values()[(ordinal() + values().length - 1) % values().length];
	}
}
