package com.sosproject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class SosFrame extends JFrame {
  private SosGame game;

  // Controls
  private final JComboBox<Integer> boardSizeBox = new JComboBox<>();
  private final JComboBox<SosGame.GameMode> modeBox = new JComboBox<>(SosGame.GameMode.values());
  private final JRadioButton sButton = new JRadioButton("S", true);
  private final JRadioButton oButton = new JRadioButton("O");
  private final JLabel currentPlayerLabel = new JLabel();
  private final JButton newGameButton = new JButton("New Game");

  // Board UI
  private JPanel boardPanel;
  private JButton[][] cellButtons;

  public SosFrame() {
    super("SOS");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setMinimumSize(new Dimension(600, 500));

    // Defaults
    for (int i = 3; i <= 10; i++) boardSizeBox.addItem(i);
    boardSizeBox.setSelectedItem(5);
    modeBox.setSelectedItem(SosGame.GameMode.SIMPLE);

    ButtonGroup letterGroup = new ButtonGroup();
    letterGroup.add(sButton);
    letterGroup.add(oButton);

    JPanel topBar = buildTopBar();
    add(topBar, BorderLayout.NORTH);

    // init game & board
    startNewGame();
    rebuildBoardUI();

    setLocationRelativeTo(null);
  }

  private JPanel buildTopBar() {
    JPanel p = new JPanel(new GridBagLayout());
    GridBagConstraints gc = new GridBagConstraints();
    gc.insets = new Insets(5,5,5,5);
    gc.gridy = 0;
    gc.anchor = GridBagConstraints.WEST;

    // Board size
    p.add(new JLabel("Board:"), gc);
    gc.gridx = 1; p.add(boardSizeBox, gc);

    // Mode
    gc.gridx = 2; p.add(new JLabel("Mode:"), gc);
    gc.gridx = 3; p.add(modeBox, gc);

    // Letter selection
    gc.gridx = 4; p.add(new JLabel("Letter:"), gc);
    gc.gridx = 5; p.add(sButton, gc);
    gc.gridx = 6; p.add(oButton, gc);

    // Current player
    gc.gridx = 7;
    currentPlayerLabel.setText("Current: Player A");
    p.add(currentPlayerLabel, gc);

    // New game
    gc.gridx = 8;
    newGameButton.addActionListener(this::onNewGame);
    p.add(newGameButton, gc);

    return p;
  }

  private void onNewGame(ActionEvent e) {
    startNewGame();
    rebuildBoardUI();
  }

  private void startNewGame() {
    int size = (Integer) boardSizeBox.getSelectedItem();
    SosGame.GameMode mode = (SosGame.GameMode) modeBox.getSelectedItem();
    if (game == null) {
      game = new SosGame(size, mode);
    } else {
      game.setOptions(size, mode);
    }
    updateCurrentPlayerLabel();
  }

  private void rebuildBoardUI() {
    if (boardPanel != null) {
      remove(boardPanel);
    }
    int n = game.getSize();
    boardPanel = new JPanel(new GridLayout(n, n, 2, 2));
    cellButtons = new JButton[n][n];

    for (int r = 0; r < n; r++) {
      for (int c = 0; c < n; c++) {
        JButton b = new JButton("");
        b.setFont(b.getFont().deriveFont(Font.BOLD, 18f));
        final int rr = r, cc = c;
        b.addActionListener(evt -> onCellClicked(rr, cc));
        cellButtons[r][c] = b;
        boardPanel.add(b);
      }
    }

    add(boardPanel, BorderLayout.CENTER);
    revalidate();
    repaint();
  }

  private void onCellClicked(int row, int col) {
    // Determine selected letter
    SosGame.Cell letter = sButton.isSelected() ? SosGame.Cell.S : SosGame.Cell.O;

    // Try to place
    try {
      if (!game.isCellEmpty(row, col)) {
        JOptionPane.showMessageDialog(this, "That cell is already taken.", "Invalid Move",
            JOptionPane.WARNING_MESSAGE);
        return;
      }
      game.placeLetter(row, col, letter);
      cellButtons[row][col].setText(letter == SosGame.Cell.S ? "S" : "O");
      cellButtons[row][col].setEnabled(false);

      // Update current player label
      updateCurrentPlayerLabel();

      // NOTE: No SOS detection yet; that will be added later.
    } catch (RuntimeException ex) {
      JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  private void updateCurrentPlayerLabel() {
    currentPlayerLabel.setText("Current: " + (game.isPlayerATurn() ? "Player A" : "Player B"));
  }
}
