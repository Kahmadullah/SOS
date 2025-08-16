package com.sosproject;

import java.io.*;
import java.nio.file.*;

public class TextGameRecorder implements Closeable {
  private final Path path;
  private BufferedWriter out;

  public TextGameRecorder(Path path) { this.path = path; }

  public void start(SosGameBase g, PlayerType a, PlayerType b) throws IOException {
    out = Files.newBufferedWriter(path);
    out.write("SOSv1,size=" + g.getSize() + ",mode=" + g.getMode() + ",playerA=" + a + ",playerB=" + b);
    out.newLine();
  }

  public void recordMove(int index, boolean isPlayerA, int row, int col, SosGameBase.Cell letter, SosGameBase g) throws IOException {
    if (out == null) return;
    out.write(index + "," + (isPlayerA ? "A" : "B") + "," + row + "," + col + "," + letter + "," + g.getScoreA() + "," + g.getScoreB() + "," + g.getStatus());
    out.newLine();
    out.flush();
  }

  public void finish(SosGameBase g) throws IOException {
    if (out == null) return;
    out.write("END,status=" + g.getStatus() + ",scoreA=" + g.getScoreA() + ",scoreB=" + g.getScoreB());
    out.newLine();
    out.flush();
    out.close();
    out = null;
  }

  @Override public void close() throws IOException {
    if (out != null) {
      out.flush();
      out.close();
      out = null;
    }
  }
}
