package osellus.feng.gary.sudokusolver;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayout;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    private final int EDIT_MAX_LEN = 1;

    private GridLayout mGridLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGridLayout = findViewById(R.id.grid);
        EditText lastEditText = null;
        int editTextId;

        for (int i = 0; i < 9; ++i) {
            for (int j = 0; j < 9; ++j) {
                EditText editText = createBaseEditText();
                editTextId = View.generateViewId();

                editText.setId(editTextId);

                mGridLayout.addView(editText, i);
                //  lastEditText = editText;
            }
        }

        Button solveButton = findViewById(R.id.solve_button);
        solveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                solveSudoku();
            }
        });
    }

    private EditText createBaseEditText() {
        final EditText editText = new EditText(this);

        editText.setRawInputType(InputType.TYPE_CLASS_NUMBER);
        editText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(EDIT_MAX_LEN)});

        return editText;
    }

    private void solveSudoku() {
        int[] puzzle = new int[Sudoku.DIMEN * Sudoku.DIMEN];

        for (int i = 0; i < Sudoku.DIMEN * Sudoku.DIMEN; ++i) {
            EditText cell = (EditText) mGridLayout.getChildAt(i);
            String cellText = cell.getText().toString();
            if (cellText.isEmpty()) {
                puzzle[i] = 0;
            } else {
                int cellValue = Integer.parseInt(cellText);
                puzzle[i] = cellValue;
            }
        }

        Sudoku sudoku = new Sudoku(puzzle);
        sudoku.solve();

        for (int i = 0; i < Sudoku.DIMEN * Sudoku.DIMEN; ++i) {
            EditText cell = (EditText) mGridLayout.getChildAt(i);
            if (sudoku.getPuzzleAt(i) == 0) {
                cell.setText(String.valueOf(sudoku.getSolutionAt(i)));
                cell.setTextColor(Color.RED);
            }
        }
    }
}
