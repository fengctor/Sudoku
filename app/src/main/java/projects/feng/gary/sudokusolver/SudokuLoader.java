package projects.feng.gary.sudokusolver;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

public class SudokuLoader extends AsyncTaskLoader<Sudoku> {
    private Sudoku sudoku;

    public SudokuLoader(Context context, Sudoku sudoku) {
        super(context);
        this.sudoku = sudoku;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    @Override
    public Sudoku loadInBackground() {
        if (sudoku.mayHaveSolution()) {
            sudoku.solve();
        }
        return sudoku;
    }
}
