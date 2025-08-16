package com.sosproject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class SosFrame extends JFrame {
  private SosGameBase game;

  private final JComboBox<Integer> boardSizeBox = new JComboBox<>();
  private final JComboBox<GameMode> modeBox = new JComboBox<>(GameMode.values());
  private final JRadioButton sButton = new JRadioButton("S", true);
  private final JRadioButton oButton = new JRadioButton("O");
  private final JLabel currentPlayerLabel = new JLabel();
  private final JLabel scoreLabel = new JLabel("Score A: 0 | Score B: 0");
  private final JLabel statusLabel = new JLabel("Status: In progress");
  private final JButton newGameButton = new JButton("New Game");

  // player type selectors + simple AI
  private final JComboBox<PlayerType> playerATypeBox = new JComboBox<>(PlayerType.values());
  private final JComboBox<PlayerType> playerBTypeBox = new JComboBox<>(PlayerType.values());
  private Strategy aiStrategy = new RandomStrategy();  // simple AI

  private JPanel boardPanel;
  private JButton[][] cellButtons;

  public SosFrame() {
    super("SOS");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setMinimumSize(new Dimension(650, 520));

    for (int i = 3; i <= 10; i++) boardSizeBox.addItem(i);
    boardSizeBox.setSelectedItem(5);
    modeBox.setSelectedItem(GameMode.SIMPLE);

    ButtonGroup letterGroup = new ButtonGroup();
    letterGroup.add(sButton);
    letterGroup.add(oButton);

    JPanel topBar = buildTopBar();
    add(topBar, BorderLayout.NORTH);

    startNewGame();
    rebuildBoardUI();
    setLocationRelativeTo(null);
  }

  private JPanel buildTopBar() {
    JPanel p = new JPanel(new GridBagLayout());
    GridBagConstraints gc = new GridBagConstraints();
    gc.insets = new Insets(5,5,5,5);
    gc.gridy = 0; gc.anchor = GridBagConstraints.WEST;

    p.add(new JLabel("Board:"), gc);
    gc.gridx = 1; p.add(boardSizeBox, gc);

    gc.gridx = 2; p.add(new JLabel("Mode:"), gc);
    gc.gridx = 3; p.add(modeBox, gc);

    // Player type selectors after Mode
    gc.gridx = 4; p.add(new JLabel("A Type:"), gc);
    gc.gridx = 5;
    playerATypeBox.setSelectedItem(PlayerType.HUMAN);
    p.add(playerATypeBox, gc);

    gc.gridx = 6; p.add(new JLabel("B Type:"), gc);
    gc.gridx = 7;
    playerBTypeBox.setSelectedItem(PlayerType.COMPUTER);
    p.add(playerBTypeBox, gc);

    // Shift existing controls to the right (indices updated)
    gc.gridx = 8; p.add(new JLabel("Letter:"), gc);
    gc.gridx = 9; p.add(sButton, gc);
    gc.gridx = 10; p.add(oButton, gc);

    gc.gridx = 11; p.add(currentPlayerLabel, gc);
    gc.gridx = 12; p.add(scoreLabel, gc);
    gc.gridx = 13; p.add(statusLabel, gc);

    gc.gridx = 14;
    newGameButton.addActionListener(this::onNewGame);
    p.add(newGameButton, gc);

    return p;
  }

  private void onNewGame(ActionEvent e) {
    startNewGame();
    rebuildBoardUI();
    maybeStartAiTurn(); // if AI starts, let it move immediately
  }

  private void startNewGame() {
    int size = (Integer) boardSizeBox.getSelectedItem();
    GameMode mode = (GameMode) modeBox.getSelectedItem();
    game = SosGames.create(size, mode);
    updateLabels();
    setBoardEnabled(true); // enable grid for a fresh game
  }

  private void rebuildBoardUI() {
    if (boardPanel != null) remove(boardPanel);

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
    if (game.getStatus() != SosGameBase.Status.IN_PROGRESS) return;

    // If it's a computer's turn, ignore human clicks
    boolean aTurn = game.isPlayerATurn();
    PlayerType current = aTurn ? (PlayerType) playerATypeBox.getSelectedItem()
                               : (PlayerType) playerBTypeBox.getSelectedItem();
    if (current == PlayerType.COMPUTER) return;

    SosGameBase.Cell letter = sButton.isSelected() ? SosGameBase.Cell.S : SosGameBase.Cell.O;
    try {
      if (!game.isCellEmpty(row, col)) {
        JOptionPane.showMessageDialog(this, "That cell is already taken.", "Invalid Move",
            JOptionPane.WARNING_MESSAGE);
        return;
      }
      boolean wasPlayerA = game.isPlayerATurn();       // whose move is this?
      game.placeLetter(row, col, letter);
      setCellText(row, col, letter, wasPlayerA);       // colorize (A=blue, B=red)

      updateLabels();
      if (game.getStatus() != SosGameBase.Status.IN_PROGRESS) {
        disableRemainingCells();
        announceResult();
      } else {
        maybeStartAiTurn(); // after human move, let AI respond
      }
    } catch (RuntimeException ex) {
      JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  private void updateLabels() {
    currentPlayerLabel.setText("Current: " + (game.isPlayerATurn() ? "Player A" : "Player B"));
    scoreLabel.setText("Score A: " + game.getScoreA() + " | Score B: " + game.getScoreB());
    statusLabel.setText("Status: " + switch (game.getStatus()) {
      case IN_PROGRESS -> "In progress";
      case PLAYER_A_WON -> "Player A won";
      case PLAYER_B_WON -> "Player B won";
      case DRAW -> "Draw";
    });
  }

  private void disableRemainingCells() {
    int n = game.getSize();
    for (int r = 0; r < n; r++) {
      for (int c = 0; c < n; c++) {
        if (cellButtons[r][c].isEnabled()) cellButtons[r][c].setEnabled(false);
      }
    }
  }

  // enable/disable only empty cells (used around AI turns)
  private void setBoardEnabled(boolean enabled) {
    if (cellButtons == null) return; // prevent NPE before board exists
    int n = game.getSize();
    for (int r = 0; r < n; r++) {
      for (int c = 0; c < n; c++) {
        if (cellButtons[r][c].getText().isEmpty()) {
          cellButtons[r][c].setEnabled(enabled);
        }
      }
    }
  }

  // helper: set text + color by player (A=blue, B=red), stays colored even when disabled
  private void setCellText(int row, int col, SosGameBase.Cell letter, boolean isPlayerA) {
    JButton b = cellButtons[row][col];
    String ch = (letter == SosGameBase.Cell.S) ? "S" : "O";
    String colorHex = isPlayerA ? "#1565c0" : "#d32f2f";
    b.setText("<html><b><span style='color:" + colorHex + ";'>" + ch + "</span></b></html>");
    b.setForeground(isPlayerA ? new Color(21,101,192) : new Color(211,47,47));
    b.setEnabled(true);
  }

  // drive AI turns (handles extra turns in GENERAL automatically)
  private void maybeStartAiTurn() {
    if (game.getStatus() != SosGameBase.Status.IN_PROGRESS) return;

    boolean aTurn = game.isPlayerATurn();
    PlayerType current = aTurn ? (PlayerType) playerATypeBox.getSelectedItem()
                               : (PlayerType) playerBTypeBox.getSelectedItem();
    if (current != PlayerType.COMPUTER) {
      setBoardEnabled(true); // human's turn
      return;
    }

    setBoardEnabled(false); // lock UI while AI thinks/plays
    new SwingWorker<Move, Void>() {
      @Override protected Move doInBackground() {
        return aiStrategy.choose(game);
      }
      @Override protected void done() {
        try {
          Move m = get();
          if (m != null && game.getStatus() == SosGameBase.Status.IN_PROGRESS) {
            game.placeLetter(m.row(), m.col(), m.letter());
            setCellText(m.row(), m.col(), m.letter(), aTurn); // color by mover
          }
          updateLabels();

          if (game.getStatus() != SosGameBase.Status.IN_PROGRESS) {
            disableRemainingCells();
            announceResult();
            return;
          }

          PlayerType nextType = game.isPlayerATurn()
              ? (PlayerType) playerATypeBox.getSelectedItem()
              : (PlayerType) playerBTypeBox.getSelectedItem();

          if (nextType == PlayerType.COMPUTER) {
            // Continue AI chain (extra turn in GENERAL or other side is AI)
            maybeStartAiTurn();
          } else {
            setBoardEnabled(true); // back to human
          }
        } catch (Exception ex) {
          JOptionPane.showMessageDialog(SosFrame.this, ex.getMessage(), "AI Error", JOptionPane.ERROR_MESSAGE);
          setBoardEnabled(true);
        }
      }
    }.execute();
  }

  private void announceResult() {
    String msg = switch (game.getStatus()) {
      case PLAYER_A_WON -> "Player A wins!";
      case PLAYER_B_WON -> "Player B wins!";
      case DRAW -> "It's a draw!";
      default -> "Game over.";
    };
    JOptionPane.showMessageDialog(this, msg, "Game Over", JOptionPane.INFORMATION_MESSAGE);
  }
}
