package com.sosproject;

import java.util.ArrayList;
import java.util.Random;

public class RandomStrategy implements Strategy {
  private final Random rng;
  public RandomStrategy() { this(1234L); }
  public RandomStrategy(long seed) { this.rng = new Random(seed); }

  @Override
  public Move choose(SosGameBase g) {
    int n = g.getSize();
    var empty = new ArrayList<int[]>();
    for (int r=0;r<n;r++) for (int c=0;c<n;c++) {
      if (g.isCellEmpty(r,c)) empty.add(new int[]{r,c});
    }
    if (empty.isEmpty()) return null;
    int[] cell = empty.get(rng.nextInt(empty.size()));
    SosGameBase.Cell letter = rng.nextBoolean() ? SosGameBase.Cell.S : SosGameBase.Cell.O;
    return new Move(cell[0], cell[1], letter);
  }
}
