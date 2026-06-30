package com.pcwk.ehr.cmn;

public enum Grade {

	BASIC(1, "BASIC"),
	SILVER(2, "SILVER"),
	GOLD(3, "GOLD");

	private final int level;
	private final String label;

	Grade(int level, String label) {
		this.level = level;
		this.label = label;
	}

	public int getLevel() {
		return level;
	}

	public String getLabel() {
		return label;
	}

	public Grade getNextLevel() {
		switch (this) {
		case BASIC:
			return SILVER;
		case SILVER:
			return GOLD;
		default:
			return null;
		}
	}

}
