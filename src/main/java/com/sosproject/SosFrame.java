package com.sosproject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;

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

  private final JComboBox<PlayerType> playerATypeBox = new JComboBox<>(PlayerType.values());
  private final JComboBox<PlayerType> playerBTypeBox = new JComboBox<>(PlayerType.values());
  private Strategy aiStrategy = new RandomStrategy();

  private final JCheckBox recordCheck = new JCheckBox("Record game");
  private final JButton replayButton = new JButton("Replay");

  private TextGameRecorder recorder;
  private boolean isReplaying = false;
  private int moveIndex = 0;

  private JPanel boardPanel;
  private JButton[][] cellButtons;

  public SosFrame() {
    super("SOS");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setMinimumSize(new Dimension(760, 560));

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

    gc.gridx = 4; p.add(new JLabel("A Type:"), gc);
    gc.gridx = 5;
    playerATypeBox.setSelectedItem(PlayerType.HUMAN);
    p.add(playerATypeBox, gc);

    gc.gridx = 6; p.add(new JLabel("B Type:"), gc);
    gc.gridx = 7;
    playerBTypeBox.setSelectedItem(PlayerType.COMPUTER);
    p.add(playerBTypeBox, gc);

    gc.gridx = 8; p.add(new JLabel("Letter:"), gc);
    gc.gridx = 9; p.add(sButton, gc);
    gc.gridx = 10; p.add(oButton, gc);

    gc.gridx = 11; p.add(currentPlayerLabel, gc);
    gc.gridx = 12; p.add(scoreLabel, gc);
    gc.gridx = 13; p.add(statusLabel, gc);

    gc.gridx = 14; p.add(recordCheck, gc);
    gc.gridx = 15;
    replayButton.addActionListener(this::onReplay);
    p.add(replayButton, gc);

    gc.gridx = 16;
    newGameButton.addActionListener(this::onNewGame);
    p.add(newGameButton, gc);

    return p;
  }

  private void onNewGame(ActionEvent e) {
    startNewGame();
    rebuildBoardUI();
    maybeStartAiTurn();
  }

  private void startNewGame() {
    closeRecorderQuietly();
    isReplaying = false;
    moveIndex = 0;

    int size = (Integer) boardSizeBox.getSelectedItem();
    GameMode mode = (GameMode) modeBox.getSelectedItem();
    game = SosGames.create(size, mode);
    updateLabels();
    setBoardEnabled(true);

    if (recordCheck.isSelected()) {
      JFileChooser fc = new JFileChooser();
      fc.setDialogTitle("Save recording");
      if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
        try {
          Path p = fc.getSelectedFile().toPath();
          recorder = new TextGameRecorder(p);
          recorder.start(game, (PlayerType) playerATypeBox.getSelectedItem(), (PlayerType) playerBTypeBox.getSelectedItem());
        } catch (IOException ex) {
          JOptionPane.showMessageDialog(this, "Failed to start recording: " + ex.getMessage(), "I/O Error", JOptionPane.ERROR_MESSAGE);
          recorder = null;
          recordCheck.setSelected(false);
        }
      } else {
        recordCheck.setSelected(false);
      }
    }
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
    if (isReplaying) return;
    if (game.getStatus() != SosGameBase.Status.IN_PROGRESS) return;

    boolean aTurn = game.isPlayerATurn();
    PlayerType current = aTurn ? (PlayerType) playerATypeBox.getSelectedItem()
                               : (PlayerType) playerBTypeBox.getSelectedItem();
    if (current == PlayerType.COMPUTER) return;

    SosGameBase.Cell letter = sButton.isSelected() ? SosGameBase.Cell.S : SosGameBase.Cell.O;
    try {
      if (!game.isCellEmpty(row, col)) {
        JOptionPane.showMessageDialog(this, "That cell is already taken.", "Invalid Move", JOptionPane.WARNING_MESSAGE);
        return;
      }
      boolean wasPlayerA = game.isPlayerATurn();
      game.placeLetter(row, col, letter);
      setCellText(row, col, letter, wasPlayerA);

      if (recorder != null) {
        try { recorder.recordMove(++moveIndex, wasPlayerA, row, col, letter, game); } catch (IOException ignored) {}
      }

      updateLabels();
      if (game.getStatus() != SosGameBase.Status.IN_PROGRESS) {
        disableRemainingCells();
        announceResult();
      } else {
        maybeStartAiTurn();
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

  private void setBoardEnabled(boolean enabled) {
    if (cellButtons == null) return;
    int n = game.getSize();
    for (int r = 0; r < n; r++) {
      for (int c = 0; c < n; c++) {
        if (cellButtons[r][c].getText().isEmpty()) {
          cellButtons[r][c].setEnabled(enabled);
        }
      }
    }
  }

  private void setCellText(int row, int col, SosGameBase.Cell letter, boolean isPlayerA) {
    JButton b = cellButtons[row][col];
    String ch = (letter == SosGameBase.Cell.S) ? "S" : "O";
    String colorHex = isPlayerA ? "#1565c0" : "#d32f2f";
    b.setText("<html><b><span style='color:" + colorHex + ";'>" + ch + "</span></b></html>");
    b.setForeground(isPlayerA ? new Color(21,101,192) : new Color(211,47,47));
    b.setEnabled(true);
  }

  private void maybeStartAiTurn() {
    if (isReplaying) return;
    if (game.getStatus() != SosGameBase.Status.IN_PROGRESS) return;

    boolean aTurn = game.isPlayerATurn();
    PlayerType current = aTurn ? (PlayerType) playerATypeBox.getSelectedItem()
                               : (PlayerType) playerBTypeBox.getSelectedItem();
    if (current != PlayerType.COMPUTER) {
      setBoardEnabled(true);
      return;
    }

    setBoardEnabled(false);
    new SwingWorker<Move, Void>() {
      @Override protected Move doInBackground() {
        return aiStrategy.choose(game);
      }
      @Override protected void done() {
        try {
          Move m = get();
          if (m != null && game.getStatus() == SosGameBase.Status.IN_PROGRESS) {
            game.placeLetter(m.row(), m.col(), m.letter());
            setCellText(m.row(), m.col(), m.letter(), aTurn);
            if (recorder != null) {
              try { recorder.recordMove(++moveIndex, aTurn, m.row(), m.col(), m.letter(), game); } catch (IOException ignored) {}
            }
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
            maybeStartAiTurn();
          } else {
            setBoardEnabled(true);
          }
        } catch (Exception ex) {
          JOptionPane.showMessageDialog(SosFrame.this, ex.getMessage(), "AI Error", JOptionPane.ERROR_MESSAGE);
          setBoardEnabled(true);
        }
      }
    }.execute();
  }

  private void onReplay(ActionEvent e) {
    JFileChooser fc = new JFileChooser();
    fc.setDialogTitle("Open recording");
    if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

    Path p = fc.getSelectedFile().toPath();
    GameReplayer.Loaded loaded;
    try {
      loaded = GameReplayer.load(p);
    } catch (IOException ex) {
      JOptionPane.showMessageDialog(this, "Failed to load recording: " + ex.getMessage(), "I/O Error", JOptionPane.ERROR_MESSAGE);
      return;
    }

    isReplaying = true;
    recordCheck.setSelected(false);
    closeRecorderQuietly();

    game = SosGames.create(loaded.size, loaded.mode);
    updateLabels();
    rebuildBoardUI();
    setBoardEnabled(false);

    List<GameReplayer.MoveRec> moves = loaded.moves;
    final int[] idx = {0};
    Timer t = new Timer(450, ae -> {
      if (idx[0] >= moves.size() || game.getStatus() != SosGameBase.Status.IN_PROGRESS) {
        ((Timer) ae.getSource()).stop();
        isReplaying = false;
        disableRemainingCells();
        announceResult();
        return;
      }
      GameReplayer.MoveRec mr = moves.get(idx[0]++);
      try {
        game.placeLetter(mr.row, mr.col, mr.letter);
        setCellText(mr.row, mr.col, mr.letter, mr.isPlayerA);
        updateLabels();
      } catch (RuntimeException ex) {
        ((Timer) ae.getSource()).stop();
        isReplaying = false;
        JOptionPane.showMessageDialog(this, "Replay failed: " + ex.getMessage(), "Replay Error", JOptionPane.ERROR_MESSAGE);
      }
    });
    t.setInitialDelay(400);
    t.start();
  }

  private void announceResult() {
    String msg = switch (game.getStatus()) {
      case PLAYER_A_WON -> "Player A wins!";
      case PLAYER_B_WON -> "Player B wins!";
      case DRAW -> "It's a draw!";
      default -> "Game over.";
    };
    JOptionPane.showMessageDialog(this, msg, "Game Over", JOptionPane.INFORMATION_MESSAGE);
    if (recorder != null) {
      try { recorder.finish(game); } catch (IOException ignored) {}
      recorder = null;
    }
  }

  private void closeRecorderQuietly() {
    if (recorder != null) {
      try { recorder.close(); } catch (IOException ignored) {}
      recorder = null;
    }
  }
}
