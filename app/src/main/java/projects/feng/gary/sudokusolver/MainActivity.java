package projects.feng.gary.sudokusolver;

import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private static final int SOLVE_SUDOKU_LOADER_ID = 0;
    private static final int CELL_MAX_LEN = 1;
    private static final int CELL_TEXT_SIZE = 14;
    private static final int THICKER_BORDER_MARGIN = 4;
    private static final int DEFAULT_MARGIN = 8;

    private ConstraintLayout mConstraintLayout;
    private ProgressBar mProgressBar;
    private TableLayout mTableLayout;
    private SwipeRefreshLayout mSwipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mConstraintLayout = findViewById(R.id.layout);
        mProgressBar = findViewById(R.id.progressBar);
        mTableLayout = findViewById(R.id.grid);

        mProgressBar.setVisibility(View.GONE);

        setUpSudokuGrid();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.app_name);

        mSwipeRefresh = findViewById(R.id.swipeRefresh);
        mSwipeRefresh.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        resetGrid();
                        mSwipeRefresh.setRefreshing(false);
                    }
                }
        );

        final ConstraintLayout constraintLayout = findViewById(R.id.layout);
        constraintLayout.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    hideKeyboard();
                }
            }
        });
    }


    //--------------------------SUDOKU GRID BORDER SETUP--------------------------------------------


    private void setUpSudokuGrid() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        // Grid should have around 1 cell of empty space to its right and left
        int tableDim = (size.x < size.y ? size.x : size.y) * 9 / 11;
        ConstraintLayout.LayoutParams constraintLayoutParams = new ConstraintLayout.LayoutParams(tableDim, tableDim);
        constraintLayoutParams.setMargins(DEFAULT_MARGIN, DEFAULT_MARGIN, DEFAULT_MARGIN, DEFAULT_MARGIN);
        mTableLayout.setLayoutParams(constraintLayoutParams);

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(mConstraintLayout);
        constraintSet.connect(R.id.grid, ConstraintSet.LEFT, R.id.layout, ConstraintSet.LEFT);
        constraintSet.connect(R.id.grid, ConstraintSet.TOP, R.id.layout, ConstraintSet.TOP);
        constraintSet.connect(R.id.grid, ConstraintSet.RIGHT, R.id.layout, ConstraintSet.RIGHT);
        constraintSet.connect(R.id.grid, ConstraintSet.BOTTOM, R.id.layout, ConstraintSet.BOTTOM);
        constraintSet.applyTo(mConstraintLayout);

        EditText prevCell = null;

        for (int i = 0; i < Sudoku.DIMEN; ++i) {
            TableRow row = new TableRow(this);
            for (int j = 0; j < Sudoku.DIMEN; ++j) {
                EditText cell = createCell(i, j);
                int cellId = View.generateViewId();
                cell.setId(cellId);

                if (prevCell != null) {
                    prevCell.setNextFocusForwardId(cellId);
                }

                if (i > 0) {
                    TableRow upRow = (TableRow) mTableLayout.getChildAt(i - 1);
                    EditText upCell = (EditText) upRow.getChildAt(j);
                    upCell.setNextFocusDownId(cellId);
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
    }

    private void setCellMargins(TableRow.LayoutParams layoutParams, int row, int col) {
        int left = 0;
        int top = 0;
        int right = 0;
        int bottom = 0;

        if (col % 3 == 0) {
            left = THICKER_BORDER_MARGIN;
        } else if (col % 9 == 8) {
            right = THICKER_BORDER_MARGIN;
        }
        if (row % 3 == 0) {
            top = THICKER_BORDER_MARGIN;
        } else if (row % 9 == 8) {
            bottom = THICKER_BORDER_MARGIN;
        }

        layoutParams.setMargins(left, top, right, bottom);
    }

    private void setTableRowMargins(TableLayout.LayoutParams layoutParams, int row) {
        int left = THICKER_BORDER_MARGIN;
        int top = 0;
        int right = THICKER_BORDER_MARGIN;
        int bottom = 0;

        if (row % 9 == 0) {
            top = THICKER_BORDER_MARGIN;
        } else if (row % 9 == 8) {
            bottom = THICKER_BORDER_MARGIN;
        }

        layoutParams.setMargins(left, top, right, bottom);
    }


    //--------------------------CELL METHODS--------------------------------------------------------


    private EditText createCell(int row, int col) {
        final EditText cell = new EditText(this);

        cell.setRawInputType(InputType.TYPE_CLASS_NUMBER);
        cell.setText("");
        cell.setTextSize(TypedValue.COMPLEX_UNIT_DIP, CELL_TEXT_SIZE);
        cell.setFilters(new InputFilter[]{new InputFilter.LengthFilter(CELL_MAX_LEN)});
        cell.setBackgroundResource(R.drawable.border_white);
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(
                0,
                ViewGroup.LayoutParams.MATCH_PARENT,
                1);
        setCellMargins(layoutParams, row, col);
        cell.setLayoutParams(layoutParams);
        cell.setSelectAllOnFocus(true);
        cell.setGravity(Gravity.CENTER);

        // handle cell navigation

        cell.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_NULL || event.getKeyCode() == KeyEvent.FLAG_EDITOR_ACTION) {
                    int downId = v.getNextFocusDownId();
                    if (downId >= 0) {
                        EditText down = findViewById(downId);
                        down.requestFocus();
                        return true;
                    }

                    v.clearFocus();
                    hideKeyboard();
                }
                return false;
            }
        });

        cell.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (charSequence.length() == 0 || cell.getTag() != null) {
                    return;
                }

                char enteredChar = charSequence.charAt(0);

                if (enteredChar == ' ' || enteredChar == '0') {
                    setCellText(cell, "");
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

    private void setCellText(EditText editText, String text) {
        editText.setTag("changed programmatically");
        editText.setText(text);
        editText.setTag(null);
    }


    //--------------------------SUDOKU GRID MANIPULATION--------------------------------------------


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
        getSupportLoaderManager().restartLoader(SOLVE_SUDOKU_LOADER_ID, args, new SudokuLoaderCallbacks<Sudoku>() {
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
        if (!sudoku.hasSolution()) {
            Snackbar.make(findViewById(android.R.id.content), R.string.no_solution, Snackbar.LENGTH_LONG)
                    .setAction(R.string.dismiss, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Simply dismiss the message
                        }
                    })
                    .show();

            for (int i = 0; i < Sudoku.DIMEN; ++i) {
                TableRow row = (TableRow) mTableLayout.getChildAt(i);

                for (int j = 0; j < Sudoku.DIMEN; ++j) {
                    EditText cell = (EditText) row.getChildAt(j);
                    cell.setFocusableInTouchMode(true);
                }
            }
        } else {
            for (int i = 0; i < Sudoku.DIMEN; ++i) {
                TableRow row = (TableRow) mTableLayout.getChildAt(i);

                for (int j = 0; j < Sudoku.DIMEN; ++j) {
                    EditText cell = (EditText) row.getChildAt(j);
                    int index = i * Sudoku.DIMEN + j;
                    if (sudoku.getPuzzleAt(index) == 0) {
                        setCellText(cell, String.valueOf(sudoku.getSolutionAt(index)));
                        cell.setTextColor(Color.BLUE);
                        cell.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                    }
                }
            }
        }

        findViewById(R.id.layout).requestFocus();
    }

    private void resetGrid() {
        for (int i = 0; i < Sudoku.DIMEN; ++i) {
            TableRow row = (TableRow) mTableLayout.getChildAt(i);

            for (int j = 0; j < Sudoku.DIMEN; ++j) {
                EditText cell = (EditText) row.getChildAt(j);
                setCellText(cell, "");
                cell.setTextColor(Color.BLACK);
                cell.setTypeface(null);
                cell.clearFocus();
                cell.setFocusableInTouchMode(true);
            }
        }
    }


    //--------------------------OPTIONS MENU--------------------------------------------------------


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.resetButton:
                hideKeyboard();

                mSwipeRefresh.setRefreshing(true);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        resetGrid();
                        mSwipeRefresh.setRefreshing(false);
                    }
                }, 500);
                return true;

            case R.id.solveButton:
                hideKeyboard();
                mProgressBar.setVisibility(View.VISIBLE);
                solveSudoku();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    //--------------------------OTHER---------------------------------------------------------------


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
