package com.sosproject;

public class GeneralSosGame extends SosGameBase {
  public GeneralSosGame(int size) {
    super(size, GameMode.GENERAL);
  }

  @Override
  protected boolean afterMove(int row, int col, Cell letter, boolean wasPlayerA, int sosFormed) {
    // Score all SOS lines formed; if scored, player keeps the turn
    if (sosFormed > 0) {
      addScoreFor(wasPlayerA, sosFormed);
    }

    // If board is full, decide winner by score
    if (isBoardFull()) {
      if (getScoreA() > getScoreB()) setStatus(Status.PLAYER_A_WON);
      else if (getScoreB() > getScoreA()) setStatus(Status.PLAYER_B_WON);
      else setStatus(Status.DRAW);
      return false; // game ends
    }

    // Keep turn only if you scored this move
    return sosFormed > 0;
  }
}
