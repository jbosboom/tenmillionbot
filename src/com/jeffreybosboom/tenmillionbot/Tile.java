/*
 * Copyright 2015 Jeffrey Bosboom.
 * This file is part of tenmillionbot.
 *
 * tenmillionbot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * tenmillionbot is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with tenmillionbot.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jeffreybosboom.tenmillionbot;

import java.awt.Color;

/**
 *
 * @author Jeffrey Bosboom <jbosboom@csail.mit.edu>
 * @since 6/19/2015
 */
public enum Tile {
	SWORD(55, 105, 157),
	STAFF(149, 31, 25),
	KEY(48, 173, 72),
	SHIELD(6, 118, 92),
	ITEM(171, 74, 198),
	WOOD(132, 93, 56),
	STONE(76, 86, 106),
	RAINBOW(203, 204, 107);
	//color of the upper-left pixel of the tile's left edge border (not part of corner)
	private final int rgb;
	private Tile(int r, int g, int b) {
		this.rgb = new Color(r, g, b).getRGB() | (0xFF << 24);
	}

	public static Tile fromColor(int rgb) {
		//we can't use a switch here :(
		for (Tile t : values())
			if (t.color() == rgb)
				return t;
		return null;
	}

	public int color() {
		return rgb;
	}
}
