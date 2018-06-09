package osellus.feng.gary.sudokusolver;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;

public class MainActivity extends AppCompatActivity {
    private final int EDIT_MAX_LEN = 1;

    private TableLayout mTableLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTableLayout = findViewById(R.id.grid);
        EditText prevEditText = null;
        int editTextId;

        // set up Sudoku grid
        for (int i = 0; i < Sudoku.DIMEN; ++i) {
            TableRow row = new TableRow(this);
            for (int j = 0; j < Sudoku.DIMEN; ++j) {
                EditText editText = createBaseEditText();
                editTextId = View.generateViewId();
                editText.setId(editTextId);

                if (prevEditText != null) {
                    prevEditText.setNextFocusForwardId(editTextId);
                }

                prevEditText = editText;
                row.addView(editText, j);
            }

            TableLayout.LayoutParams layoutParams = new TableLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    1);
            row.setLayoutParams(layoutParams);

            mTableLayout.addView(row, i);
        }

        Button solveButton = findViewById(R.id.solve_button);
        solveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                solveSudoku();
            }
        });

        final Button resetButton = findViewById(R.id.reset_button);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetGrid();
            }
        });
    }

    // Create a cell to add to a TableRow
    private EditText createBaseEditText() {
        final EditText editText = new EditText(this);

        editText.setRawInputType(InputType.TYPE_CLASS_NUMBER);
        editText.setText("0");
        editText.setTextSize(14);
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(EDIT_MAX_LEN)});
        editText.setBackgroundResource(R.drawable.border_white);
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                1);
        editText.setLayoutParams(layoutParams);
        editText.setSelectAllOnFocus(true);
        editText.setGravity(Gravity.CENTER);

        // Handle cell navigation
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (charSequence.length() == 0 || editText.getTag() != null) {
                    return;
                }
                char enteredChar = charSequence.charAt(0);
                if (enteredChar == '\n') {
                    editText.setTag("changed programmatically");
                    editText.setText("0");
                    editText.setTag(null);

                    editText.clearFocus();
                    findViewById(R.id.solve_button).requestFocus();
                    hideKeyboard();
                    return;
                } else if (enteredChar == ' ') {
                    editText.setText("0");
                    return;
                } else if (!Character.isDigit(enteredChar)) {
                    editText.setText("");
                    return;
                }

                int nextId = editText.getNextFocusForwardId();
                if (nextId >= 0) {
                    EditText next = findViewById(nextId);
                    next.requestFocus();
                } else {
                    editText.clearFocus();
                    hideKeyboard();
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        return editText;
    }

    private void solveSudoku() {
        int[] puzzle = new int[Sudoku.DIMEN * Sudoku.DIMEN];

        for (int i = 0; i < Sudoku.DIMEN; ++i) {
            TableRow row = (TableRow) mTableLayout.getChildAt(i);

            for (int j = 0; j < Sudoku.DIMEN; ++j) {
                EditText cell = (EditText) row.getChildAt(j);
                String cellText = cell.getText().toString();
                int index = i * Sudoku.DIMEN + j;

                if (cellText.isEmpty()) {
                    puzzle[index] = 0;
                } else {
                    int cellValue = Integer.parseInt(cellText);
                    puzzle[index] = cellValue;
                }
            }
        }

        Sudoku sudoku = new Sudoku(puzzle);
        sudoku.solve();

        for (int i = 0; i < Sudoku.DIMEN; ++i) {
            TableRow row = (TableRow) mTableLayout.getChildAt(i);

            for (int j = 0; j < Sudoku.DIMEN; ++j) {
                EditText cell = (EditText) row.getChildAt(j);
                int index = i * Sudoku.DIMEN + j;
                if (sudoku.getPuzzleAt(index) == 0) {
                    cell.setTag("changed programmatically");
                    cell.setText(String.valueOf(sudoku.getSolutionAt(index)));
                    cell.setTag(null);
                    cell.setTextColor(Color.RED);
                }
            }
        }
    }

    private void resetGrid() {
        for (int i = 0; i < Sudoku.DIMEN; ++i) {
            TableRow row = (TableRow) mTableLayout.getChildAt(i);

            for (int j = 0; j < Sudoku.DIMEN; ++j) {
                EditText cell = (EditText) row.getChildAt(j);
                cell.setTag("changed programmatically");
                cell.setText("0");
                cell.setTag(null);
                cell.setTextColor(Color.BLACK);
                cell.clearFocus();
            }
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE);
        View view = this.getCurrentFocus();

        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
