package com.sosproject;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class SosComputerPlayTest {

  @Test
  void computer_vs_computer_simple_completes() {
    SosGameBase g = new SimpleSosGame(3);
    Strategy aiA = new RandomStrategy(7L);  // deterministic seeds
    Strategy aiB = new RandomStrategy(9L);

    while (g.getStatus() == SosGameBase.Status.IN_PROGRESS) {
      Strategy ai = g.isPlayerATurn() ? aiA : aiB;
      Move m = ai.choose(g);
      assertNotNull(m, "AI should return a move while game is in progress");
      assertTrue(g.isCellEmpty(m.row(), m.col()), "AI must choose an empty cell");
      g.placeLetter(m.row(), m.col(), m.letter());
    }
    assertNotEquals(SosGameBase.Status.IN_PROGRESS, g.getStatus(), "Game should end");
  }

  @Test
  void computer_vs_computer_general_completes() {
    SosGameBase g = new GeneralSosGame(3);
    Strategy aiA = new RandomStrategy(3L);
    Strategy aiB = new RandomStrategy(5L);

    while (g.getStatus() == SosGameBase.Status.IN_PROGRESS) {
      Strategy ai = g.isPlayerATurn() ? aiA : aiB;
      Move m = ai.choose(g);
      assertNotNull(m);
      assertTrue(g.isCellEmpty(m.row(), m.col()));
      g.placeLetter(m.row(), m.col(), m.letter());
      // If a score happens, mover may keep the turn (handled naturally by loop)
    }
    assertNotEquals(SosGameBase.Status.IN_PROGRESS, g.getStatus());
  }

  @Test
  void ai_moves_are_always_legal() {
    SosGameBase g = new SimpleSosGame(3);
    Strategy ai = new RandomStrategy(1234L);

    while (g.getStatus() == SosGameBase.Status.IN_PROGRESS) {
      Move m = ai.choose(g);
      if (m == null) break; // board full
      assertTrue(g.isCellEmpty(m.row(), m.col()));
      g.placeLetter(m.row(), m.col(), m.letter());
    }
  }
}
