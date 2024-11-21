import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;

public class HexapawnGame extends JFrame {
    // Komponen UI utama
    private JPanel boardPanel;
    private JLabel statusLabel, scoreLabel;
    private JButton[][] cells;

    // Status permainan
    private boolean isWhiteTurn = true;
    private int whiteScore = 0;
    private int blackScore = 0;
    private String whitePlayerName = "";
    private String blackPlayerName = "";

    // Warna untuk berbagai elemen UI
    private Color selectedCellColor = new Color(255, 255, 0, 100);     // Warna kuning transparan untuk sel yang dipilih
    private Color lightSquareColor = new Color(222, 184, 135);         // Warna coklat muda untuk kotak papan terang
    private Color darkSquareColor = new Color(139, 69, 19);           // Warna coklat tua untuk kotak papan gelap
    private Color highlightMoveColor = new Color(0, 255, 0, 100);     // Warna hijau transparan untuk langkah yang mungkin
    private Color highlightCaptureColor = new Color(255, 0, 0, 100);  // Warna merah transparan untuk langkah capture

    // Variabel untuk mengelola seleksi dan animasi
    private JButton selectedButton = null;
    private Timer animationTimer;
    private float alpha = 0.0f;
    private boolean isAnimating = false;

    // Constructor utama
    public HexapawnGame() {
        setTitle("Hexapawn Frans");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 800);
        setLocationRelativeTo(null);
        setupAnimationTimer();
    }

    // Dialog untuk mendapatkan nama pemain
    private void getPlayerNames() {
        JDialog dialog = new JDialog(this, "Enter Player Names", true);
        dialog.setSize(400, 250); // Sedikit diperbesar tingginya
        dialog.setLocationRelativeTo(this);

        // Setup panel utama dengan BorderLayout untuk kontrol yang lebih baik
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(40, 40, 40));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Panel untuk input fields
        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        inputPanel.setBackground(new Color(40, 40, 40));

        // Label untuk input nama
        JLabel whiteLabel = new JLabel("X Player Name:");
        JLabel blackLabel = new JLabel("O Player Name:");
        whiteLabel.setForeground(Color.WHITE);
        blackLabel.setForeground(Color.WHITE);

        // Field untuk input nama
        JTextField whiteField = new JTextField(15);
        JTextField blackField = new JTextField(15);

        // Menambahkan komponen ke input panel
        inputPanel.add(whiteLabel);
        inputPanel.add(whiteField);
        inputPanel.add(blackLabel);
        inputPanel.add(blackField);

        // Panel untuk tombol-tombol dengan FlowLayout
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(new Color(40, 40, 40));

        // Tombol back dengan gradient
        JButton backButton = createGradientButton("Back", new Color(139, 0, 0), new Color(89, 0, 0));
        backButton.setPreferredSize(new Dimension(120, 35));
        backButton.setFont(new Font("Arial", Font.BOLD, 14));

        // Tombol start dengan gradient
        JButton startButton = createGradientButton("Start Game", new Color(0, 100, 0), new Color(0, 50, 0));
        startButton.setPreferredSize(new Dimension(120, 35));
        startButton.setFont(new Font("Arial", Font.BOLD, 14));

        // Menambahkan tombol ke panel tombol
        buttonPanel.add(backButton);
        buttonPanel.add(startButton);

        // Menambahkan panel-panel ke panel utama
        mainPanel.add(inputPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Action listener untuk tombol start
        startButton.addActionListener(e -> {
            String whiteName = whiteField.getText().trim();
            String blackName = blackField.getText().trim();

            // Validasi input nama
            if (whiteName.isEmpty() || blackName.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        "Masukkan Nama kedua pemain!!!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Menyimpan nama pemain dan memulai game
            whitePlayerName = whiteName;
            blackPlayerName = blackName;
            dialog.dispose();
            setupUI();
            setVisible(true);
        });

        // Action listener untuk tombol back
        backButton.addActionListener(e -> {
            dialog.dispose(); // Tutup dialog
            showMainMenu(); // Kembali ke main menu
            setVisible(false);
        });

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    // Setup antarmuka pengguna utama
    private void setupUI() {
        // Membuat menu bar
        JMenuBar menuBar = new JMenuBar();
        JMenu gameMenu = new JMenu("Game Menu");
        gameMenu.setForeground(Color.black);
        menuBar.setBackground(new Color(50, 50, 50));

        // Menu items
        JMenuItem newGameItem = new JMenuItem("New Game");
        JMenuItem exitItem = new JMenuItem("Exit");

        newGameItem.addActionListener(e -> resetGame());
        exitItem.addActionListener(e -> System.exit(0));

        // Menambahkan items ke menu
        gameMenu.add(newGameItem);
        gameMenu.addSeparator();
        gameMenu.add(exitItem);

        menuBar.add(gameMenu);
        setJMenuBar(menuBar);

        // Panel utama
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(40, 40, 40));

        // Setup papan permainan
        boardPanel = new JPanel(new GridLayout(3, 3, 5, 5));
        boardPanel.setBorder(BorderFactory.createLineBorder(Color.yellow, 2));
        boardPanel.setBackground(new Color(40, 40, 40));
        cells = new JButton[3][3];

        // Membuat sel-sel papan permainan
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                cells[row][col] = createGameButton(row, col);
                boardPanel.add(cells[row][col]);
            }
        }

        // Panel status
        JPanel statusPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        statusPanel.setBackground(new Color(40, 40, 40));
        statusLabel = new JLabel("Turn: " + whitePlayerName + " (X)", SwingConstants.CENTER);
        scoreLabel = new JLabel(whitePlayerName + ": 0 | " + blackPlayerName + ": 0", SwingConstants.CENTER);

        // Styling label status
        statusLabel.setFont(new Font("Arial", Font.BOLD, 20));
        statusLabel.setForeground(Color.WHITE);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 18));
        scoreLabel.setForeground(Color.WHITE);

        statusPanel.add(statusLabel);
        statusPanel.add(scoreLabel);

        mainPanel.add(boardPanel, BorderLayout.CENTER);
        mainPanel.add(statusPanel, BorderLayout.SOUTH);

        add(mainPanel);
        resetBoard();
    }

    // Membuat tombol untuk sel papan permainan
    private JButton createGameButton(int row, int col) {
        JButton button = new JButton();
        button.setFont(new Font("Arial", Font.BOLD, 48));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        button.setForeground(Color.black);

        updateButtonColor(button, row, col);
        button.addActionListener(new CellClickListener(row, col));

        // Mouse listener untuk highlight langkah yang mungkin
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.getText().equals("X") && isWhiteTurn ||
                        button.getText().equals("O") && !isWhiteTurn) {
                    showPossibleMoves(row, col);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (selectedButton == null) {
                    clearHighlights();
                }
            }
        });

        return button;
    }

    // Menampilkan langkah yang mungkin untuk bidak yang dipilih
    private void showPossibleMoves(int row, int col) {
        String piece = cells[row][col].getText();
        if ((piece.equals("X") && isWhiteTurn) || (piece.equals("O") && !isWhiteTurn)) {
            highlightPossibleMoves(row, col, piece);
        }
    }

    // Update warna tombol berdasarkan posisi
    private void updateButtonColor(JButton button, int row, int col) {
        Color baseColor = (row + col) % 2 == 0 ? lightSquareColor : darkSquareColor;
        button.setBackground(baseColor);
    }

    // Listener untuk klik pada sel papan permainan
    private class CellClickListener implements ActionListener {
        private final int row, col;

        public CellClickListener(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JButton button = (JButton) e.getSource();

            if (selectedButton != null) {
                if (button.getBackground() == highlightMoveColor ||
                        button.getBackground() == highlightCaptureColor) {
                    movePiece(selectedButton, button);
                    selectedButton = null;
                    clearHighlights();
                } else {
                    selectedButton = null;
                    clearHighlights();
                    checkAndSelectPiece(button, row, col);
                }
            } else {
                checkAndSelectPiece(button, row, col);
            }
        }
    }

    // Memeriksa dan memilih bidak
    private void checkAndSelectPiece(JButton button, int row, int col) {
        if (isWhiteTurn && button.getText().equals("X")) {
            selectedButton = button;
            highlightPossibleMoves(row, col, "X");
            button.setBackground(selectedCellColor);
        } else if (!isWhiteTurn && button.getText().equals("O")) {
            selectedButton = button;
            highlightPossibleMoves(row, col, "O");
            button.setBackground(selectedCellColor);
        }
    }

    // Highlight langkah yang mungkin untuk bidak
    private void highlightPossibleMoves(int row, int col, String piece) {
        if (piece.equals("X")) {
            // Cek gerakan ke atas untuk X
            if (row > 0) {
                // Gerakan maju
                if (cells[row-1][col].getText().isEmpty()) {
                    cells[row-1][col].setBackground(highlightMoveColor);
                }
                // Capture diagonal kiri
                if (col > 0 && cells[row-1][col-1].getText().equals("O")) {
                    cells[row-1][col-1].setBackground(highlightCaptureColor);
                }
                // Capture diagonal kanan
                if (col < 2 && cells[row-1][col+1].getText().equals("O")) {
                    cells[row-1][col+1].setBackground(highlightCaptureColor);
                }
            }
        } else {
            // Cek gerakan ke bawah untuk O
            if (row < 2) {
                // Gerakan maju
                if (cells[row+1][col].getText().isEmpty()) {
                    cells[row+1][col].setBackground(highlightMoveColor);
                }
                // Capture diagonal kiri
                if (col > 0 && cells[row+1][col-1].getText().equals("X")) {
                    cells[row+1][col-1].setBackground(highlightCaptureColor);
                }
                // Capture diagonal kanan
                if (col < 2 && cells[row+1][col+1].getText().equals("X")) {
                    cells[row+1][col+1].setBackground(highlightCaptureColor);
                }
            }
        }
    }

    // Memindahkan bidak
    private void movePiece(JButton from, JButton to) {
        String piece = from.getText();
        from.setText("");
        to.setText(piece);

        isAnimating = true;
        checkWinCondition();
        isWhiteTurn = !isWhiteTurn;
        updateStatusLabel();
    }

    // Membersihkan semua highlight
    private void clearHighlights() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                updateButtonColor(cells[row][col], row, col);
            }
        }
    }

    // Setup timer untuk animasi
    private void setupAnimationTimer() {
        animationTimer = new Timer(50, e -> { //50ms
            if (isAnimating) {
                alpha += 0.1f;       //meningkat sebesar 0,1
                if (alpha >= 1.0f) {
                    alpha = 0.0f;
                    isAnimating = false;
                }
                repaint();
            }
        });
        animationTimer.start();
    }

    // Menampilkan menu utama
    private void showMainMenu() {
        JFrame mainMenu = new JFrame("Hexapawn - Main Menu");
        mainMenu.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainMenu.setSize(800, 800);
        mainMenu.setLocationRelativeTo(null);

        // Setup panel utama dengan gradient
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(30, 30, 30));

        // Panel judul dengan gradient
        JPanel titlePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(60, 0, 60),
                        0, getHeight(), new Color(30, 0, 30)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        titlePanel.setPreferredSize(new Dimension(800, 200));

        // Label judul
        JLabel titleLabel = new JLabel("HEXAPAWN");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 72));
        titleLabel.setForeground(new Color(255, 215, 0));
        titlePanel.add(titleLabel);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));  // Menambahkan 30 piksel ruang di atas

        titlePanel.add(titleLabel);
        // Panel tombol dengan gradient
        JPanel buttonPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(30, 0, 30),
                        0, getHeight(), new Color(15, 0, 15)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };

// Setup layout untuk tombol
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;

        // Membuat array tombol menu
        JButton[] menuButtons = {
                createGradientButton("Start Game", new Color(0, 100, 0), new Color(0, 50, 0)),
                createGradientButton("How to Play", new Color(0, 0, 100), new Color(0, 0, 50)),
                createGradientButton("Exit", new Color(100, 0, 0), new Color(50, 0, 0))
        };

        // Menambahkan tombol ke panel
        for (int i = 0; i < menuButtons.length; i++) {
            gbc.gridy = i;
            buttonPanel.add(menuButtons[i], gbc);
        }

        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        mainMenu.add(mainPanel);

        // Action listeners untuk tombol menu
        menuButtons[0].addActionListener(e -> {
            mainMenu.dispose();
            getPlayerNames();
        });
        menuButtons[1].addActionListener(e -> showHowToPlay());
        menuButtons[2].addActionListener(e -> System.exit(0));

        mainMenu.setVisible(true);
    }

    // Membuat tombol dengan efek gradient
    private JButton createGradientButton(String text, Color startColor, Color endColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(
                        0, 0, startColor,
                        0, getHeight(), endColor
                );
                g2d.setPaint(gradient);
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                // Menggambar border halus
                g2d.setColor(Color.GRAY);
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);

                // Menggambar teks
                FontMetrics fm = g2d.getFontMetrics();
                Rectangle2D rect = fm.getStringBounds(text, g2d);

                int textX = (getWidth() - (int) rect.getWidth()) / 2;
                int textY = (getHeight() - (int) rect.getHeight()) / 2 + fm.getAscent();

                g2d.setColor(Color.WHITE);
                g2d.drawString(text, textX, textY);
            }
        };

        // Setup properti tombol
        button.setFont(new Font("Arial", Font.BOLD, 24));
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(300, 80));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);

        // Menambahkan efek hover
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });

        return button;
    }

    // Menampilkan dialog cara bermain
    private void showHowToPlay() {
        JDialog dialog = new JDialog(this, "How to Play", true);
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(5, 0, 50));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Teks aturan permainan
        String rules = """
            Cara Bermain Hexapawn:
            
            1. Permainan dimainkan di atas papan 3x3
            2. Setiap pemain memiliki 3 pion
            3. Pion putih (X) bergerak ke atas
            4. Piom hitam (O) bergerak ke bawah
            5. Pion hanya dapat bergerak satu langkah ke depan
            6. Pion dapat menangkap secara diagonal
            
            Cara untuk Menang:
            - Mencapai baris terjauh lawan
            - Tangkap semua pion lawan
            - Membuat lawan tidak bisa bergerak
            """;

        // Setup area teks untuk aturan
        JTextArea textArea = new JTextArea(rules);
        textArea.setFont(new Font("Arial", Font.PLAIN, 16));
        textArea.setForeground(Color.WHITE);
        textArea.setBackground(new Color(50, 50, 50));
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        // Tombol tutup
        JButton closeButton = createGradientButton("Close", new Color(0, 100, 100), new Color(0, 50, 50));
        closeButton.addActionListener(e -> dialog.dispose());
        closeButton.setPreferredSize(new Dimension(150, 40));

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(closeButton, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    // Update label status pemain
    private void updateStatusLabel() {
        statusLabel.setText("Turn: " + (isWhiteTurn ? whitePlayerName : blackPlayerName) + " (" + (isWhiteTurn ? "X" : "O") + ")");
    }

    // Memeriksa kondisi kemenangan
    private void checkWinCondition() {
        boolean whiteWins = false;
        boolean blackWins = false;

        // Memeriksa jika ada pion yang mencapai sisi lawan
        for (int col = 0; col < 3; col++) {
            if (cells[0][col].getText().equals("X")) {
                whiteWins = true;
            }
            if (cells[2][col].getText().equals("O")) {
                blackWins = true;
            }
        }

        // Memeriksa jika ada pemain yang kehilangan semua pionnya
        boolean whiteHasPieces = false;
        boolean blackHasPieces = false;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (cells[row][col].getText().equals("X")) {
                    whiteHasPieces = true;
                }
                if (cells[row][col].getText().equals("O")) {
                    blackHasPieces = true;
                }
            }
        }

        if (!blackHasPieces) whiteWins = true;
        if (!whiteHasPieces) blackWins = true;

        // Memeriksa jika pemain saat ini tidak memiliki langkah yang valid
        if (!hasValidMoves()) {
            if (isWhiteTurn) {
                blackWins = true;
            } else {
                whiteWins = true;
            }
        }

        // Menampilkan dialog kemenangan jika ada pemenang
        if (whiteWins || blackWins) {
            String winner = whiteWins ? whitePlayerName : blackPlayerName;

            // Membuat dialog kemenangan kustom
            JDialog winDialog = new JDialog(this, "Game Over", true);
            winDialog.setSize(300, 200);
            winDialog.setLocationRelativeTo(this);

            JPanel winPanel = new JPanel(new BorderLayout(10, 10));
            winPanel.setBackground(new Color(40, 40, 40));
            winPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            JLabel winLabel = new JLabel(winner + " Wins!", SwingConstants.CENTER);
            winLabel.setFont(new Font("Arial", Font.BOLD, 24));
            winLabel.setForeground(Color.WHITE);

            JButton okButton = createGradientButton("OK", new Color(0, 100, 0), new Color(0, 50, 0));
            okButton.addActionListener(e -> winDialog.dispose());

            winPanel.add(winLabel, BorderLayout.CENTER);
            winPanel.add(okButton, BorderLayout.SOUTH);

            winDialog.add(winPanel);
            winDialog.setVisible(true);

            // Update skor dan reset papan
            if (whiteWins) whiteScore++;
            if (blackWins) blackScore++;
            updateScoreLabel();
            resetBoard();
        }
    }

    // Memeriksa apakah ada langkah valid yang tersedia
    private boolean hasValidMoves() {
        String piece = isWhiteTurn ? "X" : "O";
        int direction = isWhiteTurn ? -1 : 1;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (cells[row][col].getText().equals(piece)) {
                    // Memeriksa langkah maju
                    int newRow = row + direction;
                    if (newRow >= 0 && newRow < 3 && cells[newRow][col].getText().isEmpty()) {
                        return true;
                    }
                    // Memeriksa langkah capture diagonal
                    if (col > 0 && newRow >= 0 && newRow < 3 &&
                            cells[newRow][col-1].getText().equals(isWhiteTurn ? "O" : "X")) {
                        return true;
                    }
                    if (col < 2 && newRow >= 0 && newRow < 3 &&
                            cells[newRow][col+1].getText().equals(isWhiteTurn ? "O" : "X")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // Update label skor
    private void updateScoreLabel() {
        scoreLabel.setText(String.format("%s: %d | %s: %d", whitePlayerName, whiteScore, blackPlayerName, blackScore));
    }

    // Reset papan permainan ke posisi awal
    private void resetBoard() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                cells[row][col].setText("");
                updateButtonColor(cells[row][col], row, col);
            }
        }

        // Menempatkan pion di posisi awal
        for (int col = 0; col < 3; col++) {
            cells[0][col].setText("O");
            cells[2][col].setText("X");
        }

        boolean isblackTurn = true;
        updateStatusLabel();
        selectedButton = null;
    }

    // Reset permainan (skor dan papan)
    private void resetGame() {
        whiteScore = 0;
        blackScore = 0;
        updateScoreLabel();
        resetBoard();
    }

    // Method main untuk menjalankan aplikasi
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            HexapawnGame game = new HexapawnGame();
            game.showMainMenu();
        });
    }
}