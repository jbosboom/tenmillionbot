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

import com.google.common.util.concurrent.Uninterruptibles;
import com.jeffreybosboom.windowlib.Window;
import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Jeffrey Bosboom <jbosboom@csail.mit.edu>
 * @since 6/19/2015
 */
public final class Effector {
	private static final int TILE_X_ORIGIN = 300, TILE_Y_ORIGIN = 143;
	private static final int TILE_X_STRIDE = 88, TILE_Y_STRIDE = 88;
	//offset of the sense-point
	private static final int TILE_X_OFFSET = 0, TILE_Y_OFFSET = 12;
	private static final int ROWS = 7, COLS = 8;
//	static final Rectangle GAME_BOARD_RECT = new Rectangle(300, 143, 699, 612);
	private final Robot robot;
	private final Rectangle gameRect;
	public Effector() throws AWTException {
		this.robot = new Robot();
		this.gameRect = Window.findWindowByTitle("10000000").getClientAreaScreenCoordinates();
	}

	public Sensation sense() {
		return new Sensation(robot.createScreenCapture(gameRect));
	}

	public void move(int srcRow, int srcCol, int dstRow, int dstCol) {
		//add gameRect base to translate to screen coordinates
		int srcX = xForCol(srcCol) + gameRect.x, srcY = yForRow(srcRow) + gameRect.y;
		int dstX = xForCol(dstCol) + gameRect.x, dstY = yForRow(dstRow) + gameRect.y;
		robot.mouseMove(srcX + 10, srcY + 10);
		Uninterruptibles.sleepUninterruptibly(50, TimeUnit.MILLISECONDS);
		robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
		Uninterruptibles.sleepUninterruptibly(50, TimeUnit.MILLISECONDS);
		robot.mouseMove(dstX + 10, dstY + 10);
		Uninterruptibles.sleepUninterruptibly(50, TimeUnit.MILLISECONDS);
		robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
		Uninterruptibles.sleepUninterruptibly(50, TimeUnit.MILLISECONDS);
	}

	private static int yForRow(int r) {
		return r * TILE_Y_STRIDE + TILE_Y_ORIGIN + TILE_Y_OFFSET;
	}

	private static int xForCol(int c) {
		return c * TILE_X_STRIDE + TILE_X_ORIGIN + TILE_X_OFFSET;
	}

	public static final class Sensation {
		private final Tile[][] tiles;
		private Sensation(BufferedImage image) {
			DataBuffer pixels = image.getRaster().getDataBuffer();
			this.tiles = new Tile[ROWS][COLS];
			for (int r = 0; r < ROWS; ++r)
				for (int c = 0; c < COLS; ++c)
					tiles[r][c] = Tile.fromColor(pixels.getElem(yForRow(r) * image.getWidth() + xForCol(c)));
		}

		Sensation(Tile[][] tiles) {
			this.tiles = tiles;
		}

		/**
		 * Returns the tile at the given row and column, or null if not known or
		 * the given indices are out-of-bounds.
		 * @param r the row
		 * @param c the column
		 * @return the tile at the given row and column, or null if not known or
		 * the given indices are out-of-bounds
		 */
		public Tile tile(int r, int c) {
			if (r < 0 || r >= tiles.length || c < 0 || c >= tiles.length)
				return null;
			return tiles[r][c];
		}

		/**
		 * The returned array is owned by this class and must not be modified.
		 * @return
		 */
		public Tile[][] board() {
			return tiles;
		}
	}
}
