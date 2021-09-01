package fr.mrtigreroux.tigerreportssupports.bots;

import java.awt.Color;

/**
 * @author MrTigreroux
 */

public enum Status {

	WAITING(Color.GREEN, "https://i.imgur.com/ufOLtSz.png"),
	IN_PROGRESS(Color.ORANGE, "https://i.imgur.com/jbAWsGs.png"),
	IMPORTANT(Color.RED, "https://i.imgur.com/5CWSg2M.png"),
	DONE(Color.CYAN, "https://i.imgur.com/RnVauMT.png");

	private Color color;
	private String icon;

	Status(Color color, String icon) {
		this.color = color;
		this.icon = icon;
	}

	public Color getColor() {
		return color;
	}

	public String getIcon() {
		return icon;
	}

}
