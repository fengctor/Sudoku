package osellus.feng.gary.sudokusolver;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

public class SudokuLoader extends AsyncTaskLoader<Sudoku> {
    Sudoku sudoku;

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
        return loadData();
    }

    private Sudoku loadData() {
        sudoku.solve();
        return sudoku;
    }
}
