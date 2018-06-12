package osellus.feng.gary.sudokusolver;

import java.util.Arrays;

public class Sudoku {
    public static int DIMEN = 9;

    private static String INVALID_PUZZLE = "invalid puzzle";

    private int[] puzzle;
    private int[] solution;
    private boolean solved;

    public Sudoku(int[] puzzle) {
        if (puzzle == null || puzzle.length != DIMEN * DIMEN) {
            throw new IllegalArgumentException(INVALID_PUZZLE);
        }

        this.puzzle = puzzle;
        this.solution = Arrays.copyOf(puzzle, DIMEN * DIMEN);
    }

    public int getPuzzleAt(int index) {
        return puzzle[index];
    }

    // if the initial puzzle is not valid, it cannot have a solution
    public boolean mayHaveSolution() {
        for (int i = 0; i < DIMEN * DIMEN; ++i) {
            if (!validChoice(i)) {
                return false;
            }
        }
        return true;
    }

    public int getSolutionAt(int index) {
        return solution[index];
    }

    public boolean hasSolution() {
        return solved;
    }

    public boolean solve() {
        int cur = leastChoicesEmptyCell();
        if (cur < 0) {
            solved = true;
            return true;
        }

        for (int i = 1; i <= DIMEN; ++i) {
            solution[cur] = i;
            if (validChoice(cur)) {
                boolean solved = solve();
                if (solved) {
                    return true;
                }
            }
        }
        solution[cur] = 0;
        solved = false;
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

    private int getPossibleChoices(int index) {
        if (solution[index] != 0) {
            return -1;
        }

        int count = 0;

        for (int i = 1; i <= DIMEN; ++i) {
            solution[index] = i;
            if (validChoice(index)) {
                ++count;
            }
        }

        solution[index] = 0;
        return count;
    }

    private int leastChoicesEmptyCell() {
        int index = -1;
        int minChoicesSoFar = DIMEN;

        for (int i = 0; i < DIMEN * DIMEN; ++i) {
            if (solution[i] == 0) {
                int numPossibleChoices = getPossibleChoices(i);
                if (numPossibleChoices < minChoicesSoFar) {
                    minChoicesSoFar = numPossibleChoices;
                    index = i;
                }
            }
        }

        return index;
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