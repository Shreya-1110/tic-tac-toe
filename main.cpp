#include <SFML/Graphics.hpp>
#include <iostream>
#include <vector>
using namespace std;

const char PLAYER = 'X';
const char AI = 'O';
const char EMPTY = '-';

char board[3][3];
int playerScore = 0, aiScore = 0, draws = 0;

// Winning line positions
int winningPositions[8][6] = {
    {0,0,0,1,0,2}, {1,0,1,1,1,2}, {2,0,2,1,2,2},
    {0,0,1,0,2,0}, {0,1,1,1,2,1}, {0,2,1,2,2,2},
    {0,0,1,1,2,2}, {0,2,1,1,2,0}
};

struct MoveScore {
    int score;
    int r, c;
};

bool checkWin(char marker) {
    for (auto &p : winningPositions) {
        if (board[p[0]][p[1]] == marker &&
            board[p[2]][p[3]] == marker &&
            board[p[4]][p[5]] == marker)
            return true;
    }
    return false;
}

vector<pair<int,int>> getLegalMoves() {
    vector<pair<int,int>> moves;
    for (int i=0; i<3; i++)
        for (int j=0; j<3; j++)
            if (board[i][j] == EMPTY)
                moves.push_back({i,j});
    return moves;
}

int getGameState() {
    if (checkWin(PLAYER)) return 1000;
    if (checkWin(AI)) return -1000;
    if (getLegalMoves().empty()) return 1;
    return 0;
}

MoveScore minimax(char player, int depth, int alpha, int beta) {
    int result = getGameState();
    if (result != 0) return {result, -1, -1};

    char opponent = (player == AI) ? PLAYER : AI;

    MoveScore best;
    best.score = (player == AI) ? -10000 : 10000;

    for (auto &m : getLegalMoves()) {
        int r = m.first, c = m.second;
        board[r][c] = player;

        MoveScore temp = minimax(opponent, depth + 1, alpha, beta);
        board[r][c] = EMPTY;

        int score = temp.score;

        if (player == AI) {
            if (score > best.score) {
                best = {score - depth * 10, r, c};
                alpha = max(alpha, best.score);
            }
        } else {
            if (score < best.score) {
                best = {score + depth * 10, r, c};
                beta = min(beta, best.score);
            }
        }
        if (beta <= alpha) break;
    }
    return best;
}

void resetBoard() {
    for (int i=0;i<3;i++)
        for (int j=0;j<3;j++)
            board[i][j] = EMPTY;
}

int main() {
    resetBoard();
    sf::RenderWindow window(sf::VideoMode(420, 500), "Tic Tac Toe AI");

    sf::Font font;
    font.loadFromFile("arial.ttf");

    bool playerTurn = true;
    string status = "Your Turn (X)";

    while (window.isOpen()) {
        sf::Event event;
        while (window.pollEvent(event)) {
            if (event.type == sf::Event::Closed)
                window.close();

            if (playerTurn && event.type == sf::Event::MouseButtonPressed) {
                int x = event.mouseButton.x;
                int y = event.mouseButton.y;

                int r = y / 140;
                int c = x / 140;

                if (r < 3 && c < 3 && board[r][c] == EMPTY) {
                    board[r][c] = PLAYER;
                    playerTurn = false;
                    status = "AI's Turn (O)";
                }
            }
        }

        if (!playerTurn) {
            MoveScore mv = minimax(AI, 0, -10000, 10000);
            if (mv.r != -1) board[mv.r][mv.c] = AI;

            playerTurn = true;
            status = "Your Turn (X)";
        }

        int gameState = getGameState();
        if (gameState != 0) {
            if (gameState == 1000) { cout << "You Win!\n"; playerScore++; }
            else if (gameState == -1000) { cout << "AI Wins!\n"; aiScore++; }
            else { cout << "Draw!\n"; draws++; }
            resetBoard();
        }

        // Drawing UI
        window.clear(sf::Color::White);

        sf::RectangleShape line(sf::Vector2f(420, 5));
        line.setFillColor(sf::Color::Black);

        line.setPosition(0, 140); window.draw(line);
        line.setPosition(0, 280); window.draw(line);

        line.setSize(sf::Vector2f(5, 420));
        line.setPosition(140, 0); window.draw(line);
        line.setPosition(280, 0); window.draw(line);

        // Draw X and O
        for (int i=0;i<3;i++)
            for (int j=0;j<3;j++) {
                if (board[i][j] != EMPTY) {
                    sf::Text t;
                    t.setFont(font);
                    t.setString(board[i][j]);
                    t.setCharacterSize(100);
                    t.setFillColor(sf::Color::Black);
                    t.setPosition(j * 140 + 40, i * 140 + 20);
                    window.draw(t);
                }
            }

        // Status text
        sf::Text s;
        s.setFont(font);
        s.setString(status);
        s.setFillColor(sf::Color::Black);
        s.setCharacterSize(24);
        s.setPosition(10, 430);
        window.draw(s);

        window.display();
    }

    return 0;
}
