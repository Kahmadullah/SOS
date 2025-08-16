package com.sosproject;

public final class SosGames {
  private SosGames() {}

  public static SosGameBase create(int size, GameMode mode) {
    return switch (mode) {
      case SIMPLE -> new SimpleSosGame(size);
      case GENERAL -> new GeneralSosGame(size);
    };
  }
}
