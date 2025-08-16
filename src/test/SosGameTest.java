package com.sosproject;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class SosGameTest {

  @Test
  void newGame_startsEmpty_andPlayerAFirst() {
    SosGame g = new SosGame(3, SosGame.GameMode.SIMPLE);
    assertTrue(g.isPlayerATurn());
    for (int r = 0; r < g.getSize(); r++) {
      for (int c = 0; c < g.getSize(); c++) {
        assertEquals(SosGame.Cell.EMPTY, g.getCell(r, c));
      }
    }
  }

  @Test
  void placeLetter_placesAndTogglesPlayer() {
    SosGame g = new SosGame(3, SosGame.GameMode.SIMPLE);
    g.placeLetter(0, 0, SosGame.Cell.S);
    assertEquals(SosGame.Cell.S, g.getCell(0, 0));
    assertFalse(g.isPlayerATurn()); // now Player B
  }

  @Test
  void cannotPlaceOnOccupiedCell() {
    SosGame g = new SosGame(3, SosGame.GameMode.SIMPLE);
    g.placeLetter(0, 0, SosGame.Cell.S);
    assertThrows(IllegalStateException.class, () -> g.placeLetter(0, 0, SosGame.Cell.O));
  }
}
