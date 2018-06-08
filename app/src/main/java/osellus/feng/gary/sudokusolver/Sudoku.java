package osellus.feng.gary.sudokusolver;

import java.util.Arrays;

public class Sudoku {
    public static int DIMEN = 9;

    private int[] puzzle;
    private int[] solution;

    public Sudoku(int[] puzzle) {
        if (puzzle == null || puzzle.length != DIMEN * DIMEN) {
            throw new IllegalArgumentException("Invalid puzzle");
        }

        this.puzzle = puzzle;
        this.solution = Arrays.copyOf(puzzle, DIMEN * DIMEN);
    }

    public int getPuzzleAt(int index) {
        return puzzle[index];
    }

    public int getSolutionAt(int index) {
        return solution[index];
    }

    public boolean solve() {
        return solveFrom(0);
    }

    private boolean solveFrom(int index) {
        int cur = firstEmptyFrom(index);
        if (cur < 0) {
            return true;
        }

        for (int i = 1; i <= DIMEN; ++i) {
            solution[cur] = i;
            if (validChoice(cur)) {
                boolean solved = solveFrom(getIndex(cur / DIMEN, cur % DIMEN));
                if (solved) {
                    return true;
                }
            }
        }
        solution[cur] = 0;
        return false;
    }

    private int getIndex(int row, int col) {
        return row * DIMEN + col;
    }

    private boolean validChoice(int index) {
        int row = index / DIMEN;
        int col = index % DIMEN;
        int curNum = solution[getIndex(row, col)];
        if (curNum == 0) {
            return true;
        }

        return validRow(row) && validCol(col) && validBox(row, col);
    }

    private boolean validRow(int row) {
        int[] numsInRow = new int[DIMEN];

        for (int i = 0; i < DIMEN; ++i) {
            int curNum = solution[getIndex(row, i)];
            if (curNum == 0) {
                continue;
            }
            if (numsInRow[curNum - 1] > 0) {
                return false;
            }
            numsInRow[curNum - 1] = 1;
        }
        return true;
    }

    private boolean validCol(int col) {
        int[] numsInCol = new int[DIMEN];

        for (int i = 0; i < DIMEN; ++i) {
            int curNum = solution[getIndex(i, col)];
            if (curNum == 0) {
                continue;
            }
            if (numsInCol[curNum - 1] > 0) {
                return false;
            }
            numsInCol[curNum - 1] = 1;
        }
        return true;
    }

    private boolean validBox(int row, int col) {
        int[] numsInBox = new int[DIMEN];
        int topLeftBoxRow = row / 3 * 3;
        int topLeftBoxCol = col / 3 * 3;

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                int curNum = solution[getIndex(topLeftBoxRow + i, topLeftBoxCol + j)];
                if (curNum == 0) {
                    continue;
                }
                if (numsInBox[curNum - 1] > 0) {
                    return false;
                }
                numsInBox[curNum - 1] = 1;
            }
        }
        return true;
    }

    private int firstEmptyFrom(int start) {
        for (int i = start; i < DIMEN * DIMEN; ++i) {
            if (solution[i] == 0) {
                return i;
            }
        }

        return -1;
    }

    public void printSolution() {
        for (int i = 0; i < DIMEN * DIMEN; ++i) {
            if (i % DIMEN == 0) {
                System.out.println("");
            }
            System.out.print(solution[i]);
        }
    }
}