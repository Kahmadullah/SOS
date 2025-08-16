package com.sosproject;

import javax.swing.SwingUtilities;

public class App {
  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      SosFrame frame = new SosFrame();
      frame.setVisible(true);
    });
  }
}
