package com.sosproject;

public class SimpleSosGame extends SosGameBase {
  public SimpleSosGame(int size) {
    super(size, GameMode.SIMPLE);
  }

  @Override
  protected boolean afterMove(int row, int col, Cell letter, boolean wasPlayerA, int sosFormed) {
    // First SOS wins immediately
    if (sosFormed > 0) {
      setStatus(wasPlayerA ? Status.PLAYER_A_WON : Status.PLAYER_B_WON);
      return false; // game ends; toggle ignored since status != IN_PROGRESS
    }
    // If board full with no SOS, it's a draw
    if (isBoardFull()) {
      setStatus(Status.DRAW);
    }
    // No extra turn in Simple mode
    return false;
  }
}
