package com.sosproject;

public class SosGame {
  public enum GameMode { SIMPLE, GENERAL }
  public enum Cell { EMPTY, S, O }

  private int size;
  private GameMode mode;
  private Cell[][] board;
  private boolean playerATurn = true; // true = Player A, false = Player B

  public SosGame(int size, GameMode mode) {
    setOptions(size, mode);
  }

  public final void setOptions(int size, GameMode mode) {
    if (size < 3 || size > 10) {
      throw new IllegalArgumentException("Board size must be between 3 and 10");
    }
    if (mode == null) {
      throw new IllegalArgumentException("Game mode cannot be null");
    }
    this.size = size;
    this.mode = mode;
    this.board = new Cell[size][size];
    resetBoard();
  }

  public void resetBoard() {
    for (int r = 0; r < size; r++) {
      for (int c = 0; c < size; c++) {
        board[r][c] = Cell.EMPTY;
      }
    }
    playerATurn = true;
  }

  public int getSize() {
    return size;
  }

  public GameMode getMode() {
    return mode;
  }

  public boolean isPlayerATurn() {
    return playerATurn;
  }

  public Cell getCell(int row, int col) {
    checkBounds(row, col);
    return board[row][col];
  }

  public boolean isCellEmpty(int row, int col) {
    checkBounds(row, col);
    return board[row][col] == Cell.EMPTY;
  }

  /**
   * Place a letter (S or O) at (row, col).
   * Throws if the cell is taken or indices are invalid.
   * No SOS detection yet. Alternates the player turn on success.
   */
  public void placeLetter(int row, int col, Cell letter) {
    if (letter == null || letter == Cell.EMPTY) {
      throw new IllegalArgumentException("Letter must be S or O");
    }
    checkBounds(row, col);
    if (board[row][col] != Cell.EMPTY) {
      throw new IllegalStateException("Cell is already occupied");
    }
    board[row][col] = letter;
    // Toggle player after a successful move
    playerATurn = !playerATurn;
  }

  private void checkBounds(int row, int col) {
    if (row < 0 || row >= size || col < 0 || col >= size) {
      throw new IndexOutOfBoundsException("Row/Col out of range");
    }
  }
}
