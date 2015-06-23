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
	private static final int RUNNER_X_ORIGIN = 342, RUNNER_Y_ORIGIN = 15;
	private static final int RUNNER_WIDTH = 955 - RUNNER_X_ORIGIN;
	private static final int TILE_X_ORIGIN = 300, TILE_Y_ORIGIN = 143;
	private static final int TILE_X_STRIDE = 88, TILE_Y_STRIDE = 88;
	//offset of the sense-point
	private static final int TILE_X_OFFSET = 0, TILE_Y_OFFSET = 12;
	private static final int DEMAND_X = 286, DEMAND_Y = 5;
	//the red color of the frame
	private static final int DEMAND_COLOR = rgb(166, 64, 64);
	private static final int LOCK_CENTER_COLOR = rgb(55, 53, 40);
	private static final int LOCK_CENTER_WIDTH = 4, LOCK_CENTER_HEIGHT = 8;
	private static final int LOCK_BODY_COLOR = rgb(102, 101, 94);
	private static final int LOCK_BODY_WIDTH = 20, LOCK_BODY_HEIGHT = 16;
	private static final int LOCK_THICKNESS_ABOVE_CENTER = (LOCK_BODY_HEIGHT - LOCK_CENTER_HEIGHT)/2;
	//if the lock is open, the bottom of the shackle is this many pixels above
	//the top row of the lock body
	private static final int LOCK_OPEN_GAP = 8;
	//61 for chests, 60 for doors because shrug
	private static final int LOCK_CENTER_Y_SCAN = 61;
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
		private final int locksDemanded;
		private final boolean attackDemanded;
		private Sensation(BufferedImage image) {
			DataBuffer pixels = image.getRaster().getDataBuffer();
			this.tiles = new Tile[ROWS][COLS];
			for (int r = 0; r < ROWS; ++r)
				for (int c = 0; c < COLS; ++c)
					tiles[r][c] = Tile.fromColor(pixels.getElem(yForRow(r) * image.getWidth() + xForCol(c)));
			boolean demand = pixels.getElem(DEMAND_Y * image.getWidth() + DEMAND_X) == DEMAND_COLOR;
			locksDemanded = demand ? findLocks(image) : 0;
			attackDemanded = demand && locksDemanded == 0;
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
		 * The returned array is owned by this class.  Modifying it will affect
		 * the return values of the {@link #tile(int, int)} method.
		 * @return
		 */
		public Tile[][] board() {
			return tiles;
		}

		public int locksDemanded() {
			return locksDemanded;
		}

		public boolean attackDemanded() {
			return attackDemanded;
		}

		private static int findLocks(BufferedImage image) {
			DataBuffer pixels = image.getRaster().getDataBuffer();
			int locks = 0;
			//TODO: the lock floats in from the top of the screen, so we need to
			//scan more than just one line
			next_center: for (int x = RUNNER_X_ORIGIN; x < RUNNER_X_ORIGIN + RUNNER_WIDTH; x += LOCK_CENTER_WIDTH) {
				if (pixels.getElem(LOCK_CENTER_Y_SCAN * image.getWidth() + x) == LOCK_CENTER_COLOR) {
					int lockCenterX = x, lockCenterY = LOCK_CENTER_Y_SCAN;
					//we actually only move up (zero or) one step, but eh
					while (pixels.getElem((lockCenterY - 1) * image.getWidth() + lockCenterX) == LOCK_CENTER_COLOR)
						--lockCenterY;
					while (pixels.getElem(lockCenterY * image.getWidth() + (lockCenterX - 1)) == LOCK_CENTER_COLOR)
						--lockCenterX;
					int shackleSearchX = lockCenterX - (LOCK_BODY_WIDTH - LOCK_CENTER_WIDTH)/2, shackleSearchY = lockCenterY - (LOCK_THICKNESS_ABOVE_CENTER + 1);
					for (int dy = 0; dy < LOCK_OPEN_GAP; ++dy)
						for (int dx = 0; dx < LOCK_BODY_WIDTH; ++dx)
							if (pixels.getElem((shackleSearchY - dy) * image.getWidth() + (shackleSearchX + dx)) == LOCK_BODY_COLOR) {
								++locks;
								break next_center;
							}
				}
			}
			return locks;
		}
	}

	private static int rgb(int r, int g, int b) {
		return (((0xFF << 8) | r) << 8 | g) << 8 | b;
	}
}
