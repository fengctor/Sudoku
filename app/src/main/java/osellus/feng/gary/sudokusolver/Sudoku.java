package osellus.feng.gary.sudokusolver;

public class Sudoku {
    private static int DIMEN = 9;

    private int[] puzzle;
    private int[] solution;

    public Sudoku(int[] puzzle) {
        if (puzzle == null || puzzle.length != DIMEN * DIMEN) {
            throw new IllegalArgumentException("Invalid puzzle");
        }

        this.puzzle = puzzle;
        this.solution = puzzle;
    }

    public boolean solveFrom(int row, int col) {
        int cur = firstEmptyFrom(row, col);
        if (cur < 0) {
            return true;
        }

        for (int i = 1; i <= DIMEN; ++i) {
            solution[cur] = i;
            if (validChoice(cur)) {
                boolean solved = solveFrom(row, col);
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

    private int firstEmptyFrom(int row, int col) {
        int start = getIndex(row, col);

        for (int i = start; i < DIMEN * DIMEN; ++i) {
            if (solution[i] == 0) {
                return i;
            }
        }

        return -1;
    }
}
