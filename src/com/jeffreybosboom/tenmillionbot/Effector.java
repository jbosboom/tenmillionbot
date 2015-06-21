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
import java.awt.image.DataBuffer;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Jeffrey Bosboom <jbosboom@csail.mit.edu>
 * @since 6/19/2015
 */
public final class Effector {
	static final Rectangle GAME_BOARD_RECT = new Rectangle(300, 143, 699, 612);
	private final Robot robot;
	private final Rectangle gameRect;
	private final Rectangle boardRect;
	public Effector() throws AWTException {
		this.robot = new Robot();
		this.gameRect = Window.findWindowByTitle("10000000").getClientAreaScreenCoordinates();
		this.boardRect = new Rectangle(GAME_BOARD_RECT);
		//convert to screen coordinates
		boardRect.translate(gameRect.x, gameRect.y);
	}

	public DataBuffer senseBoardRect() {
		//really we should pass in the rect to be grabbed, but that would
		//require converting to screen coordinates on each grab
		return robot.createScreenCapture(boardRect).getRaster().getDataBuffer();
	}

	public void move(int srcX, int srcY, int dstX, int dstY) {
		robot.mouseMove(srcX + gameRect.x + 10, srcY + gameRect.y + 10);
		Uninterruptibles.sleepUninterruptibly(50, TimeUnit.MILLISECONDS);
		robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
		Uninterruptibles.sleepUninterruptibly(50, TimeUnit.MILLISECONDS);
		robot.mouseMove(dstX + gameRect.x + 10, dstY + gameRect.y + 10);
		Uninterruptibles.sleepUninterruptibly(50, TimeUnit.MILLISECONDS);
		robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
		Uninterruptibles.sleepUninterruptibly(50, TimeUnit.MILLISECONDS);
	}
}
