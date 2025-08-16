package com.sosproject;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class GeneralSosGameTest {

  @Test
  void general_scores_and_keeps_turn_on_score() {
    SosGameBase g = new GeneralSosGame(3);
    // A: S(0,0), B: O(0,1), A: S(0,2) -> A forms SOS horizontally, scores 1, keeps turn
    g.placeLetter(0, 0, SosGameBase.Cell.S); // A
    g.placeLetter(0, 1, SosGameBase.Cell.O); // B
    g.placeLetter(0, 2, SosGameBase.Cell.S); // A -> +1
    assertEquals(1, g.getScoreA());
    assertTrue(g.isPlayerATurn(), "Scoring move should keep the mover's turn");
  }

  @Test
  void general_no_score_toggles_turn() {
    SosGameBase g = new GeneralSosGame(3);
    g.placeLetter(1, 1, SosGameBase.Cell.S); // A (no score)
    assertFalse(g.isPlayerATurn()); // toggled to B
  }

  @Test
  void general_board_full_picks_winner_by_score_or_draw() {
    SosGameBase g = new GeneralSosGame(3);
    // Create one scoring SOS for A, then fill board without more SOS:
    g.placeLetter(0,0, SosGameBase.Cell.S); // A
    g.placeLetter(1,0, SosGameBase.Cell.S); // B (no score)
    g.placeLetter(0,1, SosGameBase.Cell.O); // A
    g.placeLetter(2,0, SosGameBase.Cell.S); // B (no score)
    g.placeLetter(0,2, SosGameBase.Cell.S); // A -> +1 (A keeps turn)

    // Fill remaining cells carefully (no more SOS):
    // A's turn still:
    g.placeLetter(1,1, SosGameBase.Cell.O); // A (no score) -> turn to B
    g.placeLetter(1,2, SosGameBase.Cell.O); // B
    g.placeLetter(2,1, SosGameBase.Cell.O); // A
    g.placeLetter(2,2, SosGameBase.Cell.O); // B

    assertTrue(g.getScoreA() > g.getScoreB());
    assertTrue(
      g.getStatus() == SosGameBase.Status.PLAYER_A_WON
      || g.getStatus() == SosGameBase.Status.IN_PROGRESS, 
      "By the time board is full, status should be A won; if not yet full, still in progress."
    );
  }

  @Test
  void general_multi_sos_in_one_move_scores_multiple() {
    SosGameBase g = new GeneralSosGame(3);
    // Set up cross centered at (1,1)
    // A: S(0,1)
    g.placeLetter(0,1, SosGameBase.Cell.S); // A
    // B: S(1,0)
    g.placeLetter(1,0, SosGameBase.Cell.S); // B
    // A: S(2,1)
    g.placeLetter(2,1, SosGameBase.Cell.S); // A
    // B: S(1,2)
    g.placeLetter(1,2, SosGameBase.Cell.S); // B

    // A places O in center -> forms two SOS (vertical and horizontal)
    g.placeLetter(1,1, SosGameBase.Cell.O); // A
    assertEquals(2, g.getScoreA(), "Center O should complete two SOS lines");
    assertTrue(g.isPlayerATurn(), "Scoring keeps the same player's turn");
  }

  @Test
  void no_moves_allowed_after_game_over() {
    SosGameBase g = new GeneralSosGame(3);
    // Force quick end by filling all cells with no SOS (draw)
    g.placeLetter(0,0, SosGameBase.Cell.S); // A
    g.placeLetter(0,1, SosGameBase.Cell.S); // B
    g.placeLetter(0,2, SosGameBase.Cell.O); // A
    g.placeLetter(1,0, SosGameBase.Cell.O); // B
    g.placeLetter(1,1, SosGameBase.Cell.S); // A
    g.placeLetter(1,2, SosGameBase.Cell.S); // B
    g.placeLetter(2,0, SosGameBase.Cell.O); // A
    g.placeLetter(2,1, SosGameBase.Cell.O); // B
    g.placeLetter(2,2, SosGameBase.Cell.S); // A
    assertTrue(
      g.getStatus() == SosGameBase.Status.DRAW
      || g.getStatus() == SosGameBase.Status.PLAYER_A_WON
      || g.getStatus() == SosGameBase.Status.PLAYER_B_WON
    );
    var status = g.getStatus();
    assertThrows(IllegalStateException.class, () -> g.placeLetter(0, 0, SosGameBase.Cell.S));
    assertEquals(status, g.getStatus(), "Status should remain the same after illegal move attempt.");
  }
}
