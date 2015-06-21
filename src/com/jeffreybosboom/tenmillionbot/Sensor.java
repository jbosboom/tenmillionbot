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

import java.awt.image.DataBuffer;

/**
 *
 * @author Jeffrey Bosboom <jbosboom@csail.mit.edu>
 * @since 6/19/2015
 */
public final class Sensor {
	//origin is 0,0 because we only capture board area
	private static final int TILE_X_ORIGIN = 0, TILE_Y_ORIGIN = 0;
	private static final int TILE_X_STRIDE = 88, TILE_Y_STRIDE = 88;
	//offset of the sense-point
	private static final int TILE_X_OFFSET = 0, TILE_Y_OFFSET = 12;
	private static final int CAPTURE_AREA_WIDTH = Effector.GAME_BOARD_RECT.width;
	private static final int ROWS = 7, COLS = 8;
	public Sensor() {}

	//TODO: reuse the array here
	public Tile[][] sense(DataBuffer board) {
		Tile[][] sensation = new Tile[ROWS][COLS];
		for (int r = 0; r < ROWS; ++r)
			for (int c = 0; c < COLS; ++c) {
				Tile t = Tile.fromColor(pixel(board, r, c));
//				if (t == null) {
//					System.out.println("bad sensation: "+r+", "+c);
//					return null;
//				}
				sensation[r][c] = t;
			}
		return sensation;
	}

	public static int[] tileToPixel(int r, int c) {
		int x = c * TILE_X_STRIDE + TILE_X_ORIGIN + TILE_X_OFFSET;
		int y = r * TILE_Y_STRIDE + TILE_Y_ORIGIN + TILE_Y_OFFSET;
		return new int[]{x, y};
	}

	private static int pixel(DataBuffer board, int r, int c) {
		int[] pixel = tileToPixel(r, c);
		return board.getElem(pixel[1] * CAPTURE_AREA_WIDTH + pixel[0]);
	}
}
