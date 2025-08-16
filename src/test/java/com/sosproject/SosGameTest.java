package com.sosproject;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class SosGameTest {

  // --- Your original tests (unchanged) ---

  @Test
  void newGame_startsEmpty_andPlayerAFirst() {
    SosGameBase g = new SimpleSosGame(3);
    assertTrue(g.isPlayerATurn());
    for (int r = 0; r < g.getSize(); r++) {
      for (int c = 0; c < g.getSize(); c++) {
        assertEquals(SosGameBase.Cell.EMPTY, g.getCell(r, c));
      }
    }
  }

  @Test
  void placeLetter_placesAndTogglesPlayer() {
    SosGameBase g = new SimpleSosGame(3);
    g.placeLetter(0, 0, SosGameBase.Cell.S);
    assertEquals(SosGameBase.Cell.S, g.getCell(0, 0));
    assertFalse(g.isPlayerATurn());
  }

  @Test
  void cannotPlaceOnOccupiedCell() {
    SosGameBase g = new SimpleSosGame(3);
    g.placeLetter(0, 0, SosGameBase.Cell.S);
    assertThrows(IllegalStateException.class, () -> g.placeLetter(0, 0, SosGameBase.Cell.O));
  }

  // --- Added tests for Simple mode & base behavior ---

  @Test
  void simple_firstSOS_winsImmediately() {
    SosGameBase g = new SimpleSosGame(3);
    // A: S at (0,0), B: O at (0,1), A: S at (0,2) -> A wins
    g.placeLetter(0, 0, SosGameBase.Cell.S); // A
    g.placeLetter(0, 1, SosGameBase.Cell.O); // B
    g.placeLetter(0, 2, SosGameBase.Cell.S); // A -> SOS
    assertEquals(SosGameBase.Status.PLAYER_A_WON, g.getStatus());
    // No further moves allowed
    assertThrows(IllegalStateException.class, () -> g.placeLetter(1, 1, SosGameBase.Cell.S));
  }

@Test
void simple_draw_when_board_full_no_sos() {
  SosGameBase g = new SimpleSosGame(3);
  // Fill 3x3 with all O's in any order — guarantees no SOS lines.
  for (int r = 0; r < g.getSize(); r++) {
    for (int c = 0; c < g.getSize(); c++) {
      g.placeLetter(r, c, SosGameBase.Cell.O);
    }
  }
  assertEquals(SosGameBase.Status.DRAW, g.getStatus());
}

  @Test
  void bounds_and_invalid_letter_checks() {
    SosGameBase g = new SimpleSosGame(3);
    assertThrows(IndexOutOfBoundsException.class, () -> g.getCell(-1, 0));
    assertThrows(IndexOutOfBoundsException.class, () -> g.getCell(0, 3));
    assertThrows(IndexOutOfBoundsException.class, () -> g.placeLetter(3, 0, SosGameBase.Cell.S));
    assertThrows(IllegalArgumentException.class, () -> g.placeLetter(0, 0, SosGameBase.Cell.EMPTY));
    assertThrows(IllegalArgumentException.class, () -> g.placeLetter(0, 0, null));
  }

  @Test
  void resetBoard_clears_state_and_scores_and_status() {
    SosGameBase g = new SimpleSosGame(3);
    g.placeLetter(0,0, SosGameBase.Cell.S);
    g.resetBoard();
    assertEquals(SosGameBase.Status.IN_PROGRESS, g.getStatus());
    assertTrue(g.isPlayerATurn());
    for (int r = 0; r < g.getSize(); r++) {
      for (int c = 0; c < g.getSize(); c++) {
        assertEquals(SosGameBase.Cell.EMPTY, g.getCell(r, c));
      }
    }
  }
}
