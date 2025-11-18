import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class TicTacToe extends JFrame implements ActionListener {
    private JButton[][] buttons = new JButton[3][3];
    private char[][] board = new char[3][3];
    private final char PLAYER = 'X';
    private final char AI = 'O';
    private final char EMPTY = '-';

    private JLabel statusLabel = new JLabel("Your Turn (X)");
    private JLabel scoreLabel = new JLabel("Score - Player: 0 | AI: 0 | Draws: 0");
    private int playerScore = 0, aiScore = 0, draws = 0;

    public TicTacToe() {
        setTitle("Tic Tac Toe with Minimax AI");
        setSize(420, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel boardPanel = new JPanel(new GridLayout(3, 3));
        Font btnFont = new Font(Font.SANS_SERIF, Font.BOLD, 60);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = EMPTY;
                buttons[i][j] = new JButton("");
                buttons[i][j].setFont(btnFont);
                buttons[i][j].addActionListener(this);
                boardPanel.add(buttons[i][j]);
            }
        }

        JPanel bottomPanel = new JPanel(new BorderLayout());
        JButton resetButton = new JButton("Reset Game");
        resetButton.addActionListener(e -> resetBoard());
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        scoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
        bottomPanel.add(statusLabel, BorderLayout.NORTH);
        bottomPanel.add(scoreLabel, BorderLayout.CENTER);
        bottomPanel.add(resetButton, BorderLayout.SOUTH);

        add(boardPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (e.getSource() == buttons[i][j] && board[i][j] == EMPTY) {
                    makeMove(i, j, PLAYER);
                    if (!isGameOver()) {
                        statusLabel.setText("AI's Turn (O)");
                        int[] aiMove = minimax(board, AI, 0, Integer.MIN_VALUE, Integer.MAX_VALUE).move;
                        if (aiMove[0] != -1) {
                            makeMove(aiMove[0], aiMove[1], AI);
                            statusLabel.setText("Your Turn (X)");
                        }
                    }
                    checkGameStatus();
                }
            }
        }
    }

    private void makeMove(int row, int col, char player) {
        board[row][col] = player;
        buttons[row][col].setText(String.valueOf(player));
        buttons[row][col].setEnabled(false);
    }

    private void checkGameStatus() {
        int state = getGameState(board, PLAYER);
        if (state != 0) {
            String msg;
            if (state == 1000) {
                msg = "You Win!";
                playerScore++;
            } else if (state == -1000) {
                msg = "You Lose!";
                aiScore++;
            } else {
                msg = "It's a Draw!";
                draws++;
            }
            scoreLabel.setText("Score - Player: " + playerScore + " | AI: " + aiScore + " | Draws: " + draws);
            JOptionPane.showMessageDialog(this, msg);
            resetBoard();
        }
    }

    private void resetBoard() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = EMPTY;
                buttons[i][j].setText("");
                buttons[i][j].setEnabled(true);
            }
        }
        statusLabel.setText("Your Turn (X)");
    }

    private boolean isGameOver() {
        return getGameState(board, PLAYER) != 0 || getGameState(board, AI) != 0 || getLegalMoves(board).length == 0;
    }

    private static class MoveScore {
        int score;
        int[] move;

        MoveScore(int score, int[] move) {
            this.score = score;
            this.move = move;
        }
    }

    private MoveScore minimax(char[][] board, char player, int depth, int alpha, int beta) {
        char opponent = (player == PLAYER) ? AI : PLAYER;
        int gameState = getGameState(board, AI);
        if (gameState != 0 || getLegalMoves(board).length == 0) {
            return new MoveScore(gameState, new int[] { -1, -1 });
        }

        int bestScore = (player == AI) ? -10000 : 10000;
        int[] bestMove = { -1, -1 };

        for (int[] move : getLegalMoves(board)) {
            board[move[0]][move[1]] = player;
            int score = minimax(board, opponent, depth + 1, alpha, beta).score;
            board[move[0]][move[1]] = EMPTY;

            if (player == AI && score > bestScore) {
                bestScore = score - depth * 10;
                bestMove = move;
                alpha = Math.max(alpha, bestScore);
            } else if (player == PLAYER && score < bestScore) {
                bestScore = score + depth * 10;
                bestMove = move;
                beta = Math.min(beta, bestScore);
            }

            if (beta <= alpha) break;
        }
        return new MoveScore(bestScore, bestMove);
    }

    private int[][] winningPositions = {
        {0, 0, 0, 1, 0, 2}, {1, 0, 1, 1, 1, 2}, {2, 0, 2, 1, 2, 2},
        {0, 0, 1, 0, 2, 0}, {0, 1, 1, 1, 2, 1}, {0, 2, 1, 2, 2, 2},
        {0, 0, 1, 1, 2, 2}, {0, 2, 1, 1, 2, 0}
    };

    private int getGameState(char[][] board, char marker) {
        char opponent = (marker == PLAYER) ? AI : PLAYER;
        if (checkWin(board, marker)) return 1000;
        if (checkWin(board, opponent)) return -1000;
        if (getLegalMoves(board).length == 0) return 1;
        return 0;
    }

    private boolean checkWin(char[][] board, char marker) {
        for (int[] pos : winningPositions) {
            if (board[pos[0]][pos[1]] == marker &&
                board[pos[2]][pos[3]] == marker &&
                board[pos[4]][pos[5]] == marker) return true;
        }
        return false;
    }

    private int[][] getLegalMoves(char[][] board) {
        java.util.List<int[]> moves = new java.util.ArrayList<>();
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                if (board[i][j] == EMPTY)
                    moves.add(new int[] {i, j});
        return moves.toArray(new int[0][0]);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TicTacToe::new);
    }
}
