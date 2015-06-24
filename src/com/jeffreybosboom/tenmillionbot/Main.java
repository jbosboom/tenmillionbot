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

import com.google.common.math.IntMath;
import com.google.common.util.concurrent.Uninterruptibles;
import com.jeffreybosboom.tenmillionbot.Effector.Sensation;
import java.awt.AWTException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

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
			Optional<Move> m = findMove(s.board(), (matchset) -> {
				int sword = (int)((matchset & (0xFF << Tile.SWORD.ordinal())) >> Tile.SWORD.ordinal());
				int staff = (int)((matchset & (0xFF << Tile.STAFF.ordinal())) >> Tile.STAFF.ordinal());
				int key = (int)((matchset & (0xFF << Tile.KEY.ordinal())) >> Tile.KEY.ordinal());
				int shield = (int)((matchset & (0xFF << Tile.SHIELD.ordinal())) >> Tile.SHIELD.ordinal());
				int item = (int)((matchset & (0xFF << Tile.ITEM.ordinal())) >> Tile.ITEM.ordinal());
				int wood = (int)((matchset & (0xFF << Tile.WOOD.ordinal())) >> Tile.WOOD.ordinal());
				int stone = (int)((matchset & (0xFF << Tile.STONE.ordinal())) >> Tile.STONE.ordinal());
				int rainbow = (int)((matchset & (0xFF << Tile.RAINBOW.ordinal())) >> Tile.RAINBOW.ordinal());
				return s.locksDemanded() > 0 ? lockRank(s, sword, staff, key, shield, item, wood, stone, rainbow) :
						s.attackDemanded() ? attackRank(s, sword, staff, key, shield, item, wood, stone, rainbow) :
						resourceRank(s, sword, staff, key, shield, item, wood, stone, rainbow);
			});

			System.out.println(m + " " + s.locksDemanded() + ", "+s.attackDemanded());
			//no move, or there's already a match waiting to be resolved
			if (!m.isPresent() || m.get().distance() == 0) {
				++retries;
				Uninterruptibles.sleepUninterruptibly(17, TimeUnit.MILLISECONDS);
				continue;
			}
			retries = 0;
			Move move = m.get();
			if (move.row() != -1)
				e.move(move.row(), 0, move.row(), move.distance());
			else
				e.move(0, move.col(), move.distance(), move.col());
		}
	}

	private static int attackRank(Sensation s, int sword, int staff, int key, int shield,
			int item, int wood, int stone, int rainbow) {
		return 10 * (sword + staff) + (shield + item + wood + stone + rainbow) - 3*key;
	}

	private static int lockRank(Sensation s, int sword, int staff, int key, int shield,
			int item, int wood, int stone, int rainbow) {
		return 10 * Math.max(key, s.locksDemanded())
				- 3 * Math.min(0, key - s.locksDemanded())
				+ (shield + item + wood + stone + rainbow)
				- 3 * (sword + staff);
	}

	private static int resourceRank(Sensation s, int sword, int staff, int key, int shield,
			int item, int wood, int stone, int rainbow) {
		return 10 * (shield + item + wood + stone + rainbow) - 3 * (sword + staff + key);
	}

	private static Optional<Move> findMove(Tile[][] board, Function<Long, Integer> ranking) {
		Map<Move, Long> moves = horizMatch(board);
		Map<Move, Long> transposedMoves = horizMatch(transpose(board));
		transposedMoves.forEach((k, v) -> {
			moves.merge(Move.row(k.col(), k.distance()), v, Long::sum);
		});
		return moves.keySet().stream().max(Comparator.comparing(ranking.compose(moves::get)));
	}

	private static Map<Move, Long> horizMatch(Tile[][] board) {
		Map<Move, Long> moves = new HashMap<>();
		for (int i = 0; i < board.length; ++i) {
			for (int j = 0; j < board[i].length - 1; ++j) {
				if (board[i][j] == null) continue;
				if ((j + 2) < board[i].length && board[i][j] == board[i][j+2]) {
					for (int k = 0; k < board.length; ++k)
						if (board[k][j+1] == board[i][j] || board[k][j+1] == Tile.RAINBOW)
							moves.merge(Move.col(j+1, IntMath.mod(i-k, board.length)),
									1L << board[i][j].ordinal() * 8,
									Long::sum);
				}
				if (board[i][j] == board[i][j+1]) {
					if ((j - 1) >= 0)
						for (int k = 0; k < board.length; ++k)
							if (board[k][j-1] == board[i][j] || board[k][j-1] == Tile.RAINBOW)
								moves.merge(Move.col(j-1, IntMath.mod(i-k, board.length)),
									1L << board[i][j].ordinal() * 8,
									Long::sum);
					if ((j + 2) < board[i].length)
						for (int k = 0; k < board.length; ++k)
							if (board[k][j+2] == board[i][j] || board[k][j+2] == Tile.RAINBOW)
								moves.merge(Move.col(j+2, IntMath.mod(i-k, board.length)),
									1L << board[i][j].ordinal() * 8,
									Long::sum);
				}
			}
		}
		return moves;
	}

	private static final class Move {
		private final int rowcol, distance;
		private Move(int rowcol, int dist) {
			this.rowcol = rowcol;
			this.distance = dist;
		}
		private static Move row(int row, int dist) {
			return new Move(row, dist);
		}
		private static Move col(int col, int dist) {
			return new Move(-col-1, dist);
		}
		public int row() {
			return rowcol >= 0 ? rowcol : -1;
		}
		public int col() {
			return rowcol < 0 ? -(rowcol + 1) : -1;
		}
		public int distance() {
			return distance;
		}
		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final Move other = (Move)obj;
			if (this.rowcol != other.rowcol)
				return false;
			if (this.distance != other.distance)
				return false;
			return true;
		}
		@Override
		public int hashCode() {
			int hash = 5;
			hash = 89 * hash + this.rowcol;
			hash = 89 * hash + this.distance;
			return hash;
		}
		@Override
		public String toString() {
			if (row() != -1)
				return String.format("row %d by %d", row(), distance());
			return String.format("col %d by %d", col(), distance());
		}
	}

	private static Tile[][] transpose(Tile[][] input) {
		Tile[][] output = new Tile[input[0].length][input.length];
		for (int i = 0; i < input.length; ++i)
			for (int j = 0; j < input[i].length; ++j)
				output[j][i] = input[i][j];
		return output;
	}
}
