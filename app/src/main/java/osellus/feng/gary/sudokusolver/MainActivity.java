package osellus.feng.gary.sudokusolver;

import android.app.LoaderManager;
import android.content.Loader;
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
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;

public class MainActivity extends AppCompatActivity {
    private final int SOLVE_SUDOKU_LOADER_ID = 0;
    private final int EDIT_MAX_LEN = 1;
    private final int MARGIN_SIZE = 4;

    private ProgressBar mProgressBar;
    private TableLayout mTableLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProgressBar = findViewById(R.id.progressBar);
        mTableLayout = findViewById(R.id.grid);

        mProgressBar.setVisibility(View.GONE);

        EditText prevCell = null;
        int cellId;

        // set up Sudoku grid
        for (int i = 0; i < Sudoku.DIMEN; ++i) {
            TableRow row = new TableRow(this);
            for (int j = 0; j < Sudoku.DIMEN; ++j) {
                EditText cell = createCell(i, j);
                cellId = View.generateViewId();
                cell.setId(cellId);

                if (prevCell != null) {
                    prevCell.setNextFocusForwardId(cellId);
                }

                prevCell = cell;
                row.addView(cell, j);
            }

            TableLayout.LayoutParams layoutParams = new TableLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    0,
                    1);
            setTableRowMargins(layoutParams, i);
            row.setLayoutParams(layoutParams);

            mTableLayout.addView(row, i);
        }

        Button solveButton = findViewById(R.id.solve_button);
        solveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressBar.setVisibility(View.VISIBLE);
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
    private EditText createCell(int row, int col) {
        final EditText cell = new EditText(this);

        cell.setRawInputType(InputType.TYPE_CLASS_NUMBER);
        cell.setText("");
        cell.setTextSize(14);
        cell.setFilters(new InputFilter[]{new InputFilter.LengthFilter(EDIT_MAX_LEN)});
        cell.setBackgroundResource(R.drawable.border_white);
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(
                0,
                ViewGroup.LayoutParams.MATCH_PARENT,
                1);
        setCellMargins(layoutParams, row, col);
        cell.setLayoutParams(layoutParams);
        cell.setSelectAllOnFocus(true);
        cell.setGravity(Gravity.CENTER);

        // Handle cell navigation
        cell.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (charSequence.length() == 0 || cell.getTag() != null) {
                    return;
                }
                char enteredChar = charSequence.charAt(0);
                if (enteredChar == '\n') {
                    setTextProgrammatically(cell, "");

                    cell.clearFocus();
                    findViewById(R.id.solve_button).requestFocus();
                    hideKeyboard();
                    return;
                } else if (enteredChar == ' ' || enteredChar == '0') {
                    setTextProgrammatically(cell, "");
                } else if (!Character.isDigit(enteredChar)) {
                    cell.setText("");
                    return;
                }

                int nextId = cell.getNextFocusForwardId();
                if (nextId >= 0) {
                    EditText next = findViewById(nextId);
                    next.requestFocus();
                } else {
                    cell.clearFocus();
                    hideKeyboard();
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        return cell;
    }

    private void setTextProgrammatically(EditText editText, String text) {
        editText.setTag("changed programmatically");
        editText.setText(text);
        editText.setTag(null);
    }

    private void setCellMargins(TableRow.LayoutParams layoutParams, int row, int col) {
        int left = 0;
        int top = 0;
        int right = 0;
        int bottom = 0;

        if (col % 3 == 0) {
            left = MARGIN_SIZE;
        } else if (col % 9 == 8) {
            right = MARGIN_SIZE;
        }
        if (row % 3 == 0) {
            top = MARGIN_SIZE;
        } else if (row % 9 == 8) {
            bottom = MARGIN_SIZE;
        }

        layoutParams.setMargins(left, top, right, bottom);
    }

    private void setTableRowMargins(TableLayout.LayoutParams layoutParams, int row) {
        int left = MARGIN_SIZE;
        int top = 0;
        int right = MARGIN_SIZE;
        int bottom = 0;

        if (row % 9 == 0) {
            top = MARGIN_SIZE;
        } else if (row % 9 == 8) {
            bottom = MARGIN_SIZE;
        }

        layoutParams.setMargins(left, top, right, bottom);
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

                cell.setFocusableInTouchMode(false);
            }
        }

        Bundle args = new Bundle();
        args.putIntArray("puzzle", puzzle);
        getLoaderManager().restartLoader(SOLVE_SUDOKU_LOADER_ID, args, new SudokuLoaderCallbacks<Sudoku>() {
            @Override
            public Loader<Sudoku> onCreateLoader(int id, Bundle args) {
                return new SudokuLoader(MainActivity.this, new Sudoku(args.getIntArray("puzzle")));
            }

            @Override
            public void onLoadFinished(Loader<Sudoku> loader, Sudoku data) {
                mProgressBar.setVisibility(View.GONE);
                displaySolution(data);
            }
        });
    }

    private void displaySolution(Sudoku sudoku) {
        for (int i = 0; i < Sudoku.DIMEN; ++i) {
            TableRow row = (TableRow) mTableLayout.getChildAt(i);

            for (int j = 0; j < Sudoku.DIMEN; ++j) {
                EditText cell = (EditText) row.getChildAt(j);
                int index = i * Sudoku.DIMEN + j;
                if (sudoku.getPuzzleAt(index) == 0) {
                    setTextProgrammatically(cell, String.valueOf(sudoku.getSolutionAt(index)));
                    cell.setTextColor(Color.RED);
                }

                cell.setFocusableInTouchMode(true);
            }
        }
    }

    private void resetGrid() {
        for (int i = 0; i < Sudoku.DIMEN; ++i) {
            TableRow row = (TableRow) mTableLayout.getChildAt(i);

            for (int j = 0; j < Sudoku.DIMEN; ++j) {
                EditText cell = (EditText) row.getChildAt(j);
                setTextProgrammatically(cell, "");
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


    public abstract class SudokuLoaderCallbacks<Sudoku> implements LoaderManager.LoaderCallbacks<Sudoku> {
        @Override
        public void onLoaderReset(Loader<Sudoku> loader) {
        }
    }
}
