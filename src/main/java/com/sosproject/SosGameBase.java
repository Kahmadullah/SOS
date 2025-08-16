package com.sosproject;

/**
 * Base class for SOS modes.
 * Adds:
 *  - Status tracking (in progress / win / draw)
 *  - Scoring (used by General mode)
 *  - SOS detection around the last move
 *  - Turn handling with a hook to allow extra turns (General)
 */
public abstract class SosGameBase {
  public enum Cell { EMPTY, S, O }
  public enum Status { IN_PROGRESS, PLAYER_A_WON, PLAYER_B_WON, DRAW }

  private final int size;
  private final Cell[][] board;
  private boolean playerATurn = true;
  private final GameMode mode;

  private Status status = Status.IN_PROGRESS;
  private int scoreA = 0;
  private int scoreB = 0;

  protected SosGameBase(int size, GameMode mode) {
    if (size < 3 || size > 10) throw new IllegalArgumentException("Board size must be 3..10");
    if (mode == null) throw new IllegalArgumentException("Mode cannot be null");
    this.size = size;
    this.mode = mode;
    this.board = new Cell[size][size];
    resetBoard();
  }

  public int getSize() { return size; }
  public GameMode getMode() { return mode; }
  public boolean isPlayerATurn() { return playerATurn; }
  public Status getStatus() { return status; }
  public int getScoreA() { return scoreA; }
  public int getScoreB() { return scoreB; }

  public void resetBoard() {
    for (int r = 0; r < size; r++) {
      for (int c = 0; c < size; c++) board[r][c] = Cell.EMPTY;
    }
    playerATurn = true;
    status = Status.IN_PROGRESS;
    scoreA = 0;
    scoreB = 0;
  }

  public Cell getCell(int row, int col) {
    checkBounds(row, col);
    return board[row][col];
  }

  public boolean isCellEmpty(int row, int col) {
    checkBounds(row, col);
    return board[row][col] == Cell.EMPTY;
  }

  public void placeLetter(int row, int col, Cell letter) {
    if (status != Status.IN_PROGRESS) {
      throw new IllegalStateException("Game is over.");
    }
    if (letter == null || letter == Cell.EMPTY) throw new IllegalArgumentException("Letter must be S or O");
    checkBounds(row, col);
    if (board[row][col] != Cell.EMPTY) throw new IllegalStateException("Cell is already occupied");

    boolean wasPlayerA = playerATurn;
    board[row][col] = letter;

    // Count SOS lines formed by this move
    int formed = countSOSAt(row, col);

    // Let subclass handle scoring / status; returns whether the mover keeps the turn
    boolean keepTurn = afterMove(row, col, letter, wasPlayerA, formed);

    // If game still running and no extra turn, toggle
    if (status == Status.IN_PROGRESS && !keepTurn) {
      playerATurn = !playerATurn;
    }
  }

  protected void checkBounds(int row, int col) {
    if (row < 0 || row >= size || col < 0 || col >= size) {
      throw new IndexOutOfBoundsException("Row/Col out of range");
    }
  }

  protected boolean isBoardFull() {
    for (int r = 0; r < size; r++) {
      for (int c = 0; c < size; c++) {
        if (board[r][c] == Cell.EMPTY) return false;
      }
    }
    return true;
  }

  /** SOS detection counting only lines that include the placed cell. */
  protected int countSOSAt(int row, int col) {
    int[][] DIRS = { {1,0}, {0,1}, {1,1}, {1,-1} };
    int count = 0;
    Cell placed = board[row][col];

    for (int[] d : DIRS) {
      int dr = d[0], dc = d[1];
      if (placed == Cell.O) {
        // O as center: S (row-dr,col-dc), O (row,col), S (row+dr,col+dc)
        if (in(row-dr, col-dc) && in(row+dr, col+dc)
            && board[row-dr][col-dc] == Cell.S
            && board[row+dr][col+dc] == Cell.S) {
          count++;
        }
      } else if (placed == Cell.S) {
        // S at start: S (row,col), O (row+dr,col+dc), S (row+2dr,col+2dc)
        if (in(row+dr, col+dc) && in(row+2*dr, col+2*dc)
            && board[row+dr][col+dc] == Cell.O
            && board[row+2*dr][col+2*dc] == Cell.S) {
          count++;
        }
        // S at end: S (row,col), O (row-dr,col-dc), S (row-2dr,col-2dc)
        if (in(row-dr, col-dc) && in(row-2*dr, col-2*dc)
            && board[row-dr][col-dc] == Cell.O
            && board[row-2*dr][col-2*dc] == Cell.S) {
          count++;
        }
      }
    }
    return count;
  }

  private boolean in(int r, int c) {
    return r >= 0 && r < size && c >= 0 && c < size;
  }

  // ------ Hooks and helpers for subclasses ------

  /** Subclasses update status/scores and return whether the mover keeps the turn. */
  protected abstract boolean afterMove(int row, int col, Cell letter, boolean wasPlayerA, int sosFormed);

  protected void setStatus(Status s) { this.status = s; }
  protected void addScoreFor(boolean playerA, int delta) {
    if (delta <= 0) return;
    if (playerA) scoreA += delta; else scoreB += delta;
  }
}
