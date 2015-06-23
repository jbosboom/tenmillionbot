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
import com.jeffreybosboom.tenmillionbot.Effector.Sensation;
import java.awt.AWTException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Jeffrey Bosboom <jbosboom@csail.mit.edu>
 * @since 6/20/2015
 */
public final class Main {
	public static void main(String[] args) throws AWTException {
		Effector e = new Effector();

		int retries = 0;
		while (retries < 200) {
			Sensation s = e.sense();
			int[] move = findMove(s.board());
			System.out.println(Arrays.toString(move) + " " + s.locksDemanded() + ", "+s.attackDemanded());
			//no move, or there's already a match waiting to be resolved
			if (move == null || (move[0] == move[2] && move[1] == move[3])) {
				++retries;
				Uninterruptibles.sleepUninterruptibly(17, TimeUnit.MILLISECONDS);
				continue;
			}
			retries = 0;
			e.move(move[0], move[1], move[2], move[3]);
		}
	}

	private static int[] findMove(Tile[][] board) {
		int[] move = horizMatch(board);
		if (move != null) return move;
		move = horizMatch(transpose(board));
		if (move != null) {
			swap(move, 0, 1);
			swap(move, 2, 3);
			return move;
		}
		return null;
	}

	private static int[] horizMatch(Tile[][] board) {
		for (int i = 0; i < board.length; ++i) {
			for (int j = 0; j < board[i].length - 1; ++j) {
				if (board[i][j] == null) continue;
				if ((j + 2) < board[i].length && board[i][j] == board[i][j+2]) {
					for (int k = 0; k < board.length; ++k)
						if (board[k][j+1] == board[i][j] || board[k][j+1] == Tile.RAINBOW)
							return new int[]{k, j+1, i, j+1};
				}
				if (board[i][j] == board[i][j+1]) {
					if ((j - 1) >= 0)
						for (int k = 0; k < board.length; ++k)
							if (board[k][j-1] == board[i][j] || board[k][j-1] == Tile.RAINBOW)
								return new int[]{k, j-1, i, j-1};
					if ((j + 2) < board[i].length)
						for (int k = 0; k < board.length; ++k)
							if (board[k][j+2] == board[i][j] || board[k][j+2] == Tile.RAINBOW)
								return new int[]{k, j+2, i, j+2};
				}
			}
		}
		return null;
	}

	private static Tile[][] transpose(Tile[][] input) {
		Tile[][] output = new Tile[input[0].length][input.length];
		for (int i = 0; i < input.length; ++i)
			for (int j = 0; j < input[i].length; ++j)
				output[j][i] = input[i][j];
		return output;
	}

	private static void swap(int[] array, int i, int j) {
		int t = array[i];
		array[i] = array[j];
		array[j] = t;
	}
}
