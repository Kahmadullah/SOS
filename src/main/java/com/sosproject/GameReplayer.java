package com.sosproject;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class GameReplayer {

  public static class MoveRec {
    public final boolean isPlayerA;
    public final int row, col;
    public final SosGameBase.Cell letter;
    public MoveRec(boolean isPlayerA, int row, int col, SosGameBase.Cell letter) {
      this.isPlayerA = isPlayerA; this.row = row; this.col = col; this.letter = letter;
    }
  }

  public static class Loaded {
    public final int size;
    public final GameMode mode;
    public final List<MoveRec> moves;
    public Loaded(int size, GameMode mode, List<MoveRec> moves) {
      this.size = size; this.mode = mode; this.moves = moves;
    }
  }

  public static Loaded load(Path path) throws IOException {
    List<String> lines = Files.readAllLines(path);
    if (lines.isEmpty() || !lines.get(0).startsWith("SOSv1")) throw new IOException("Invalid or empty SOS recording");

    String header = lines.get(0);
    int size = parseInt(header, "size");
    GameMode mode = GameMode.valueOf(parseToken(header, "mode"));

    List<MoveRec> moves = new ArrayList<>();
    for (int i = 1; i < lines.size(); i++) {
      String line = lines.get(i).trim();
      if (line.isEmpty() || line.startsWith("END")) continue;
      String[] p = line.split(",");
      if (p.length < 5) continue;
      boolean isA = "A".equals(p[1]);
      int row = Integer.parseInt(p[2]);
      int col = Integer.parseInt(p[3]);
      SosGameBase.Cell letter = SosGameBase.Cell.valueOf(p[4]);
      moves.add(new MoveRec(isA, row, col, letter));
    }
    return new Loaded(size, mode, moves);
  }

  private static int parseInt(String s, String key) {
    return Integer.parseInt(parseToken(s, key));
  }

  private static String parseToken(String s, String key) {
    for (String part : s.split(",")) {
      String[] kv = part.split("=");
      if (kv.length == 2 && kv[0].trim().endsWith(key)) return kv[1].trim();
    }
    throw new IllegalArgumentException("Missing key: " + key);
  }
}
