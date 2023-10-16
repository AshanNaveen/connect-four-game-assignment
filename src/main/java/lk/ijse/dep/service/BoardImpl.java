package lk.ijse.dep.service;


public class BoardImpl implements Board {
    private final Piece[][] pieces;
    private final BoardUI boardUI;

    public BoardImpl(BoardUI boardUI) {
        this.boardUI = boardUI;
        pieces = new Piece[NUM_OF_COLS][NUM_OF_ROWS];
        for (int i = 0; i < pieces.length; i++) {
            for (int j = 0; j < pieces[i].length; j++) {
                pieces[i][j] = Piece.EMPTY;
            }
        }
    }

    @Override
    public BoardUI getBoardUI() {
        return boardUI;
    }

    @Override
    public int findNextAvailableSpot(int col) {
        //this method use to get first Empty row of given column
        int available = -1;
        for (int i = 0; i < pieces[col].length; i++) {
            if (pieces[col][i] == Piece.EMPTY) {
                available = i;
                break;
            }
        }
        return available;
    }

    @Override
    public boolean isLegalMove(int col) {
        //this method use to find column that given is full or not
        return findNextAvailableSpot(col) != -1;
    }

    @Override
    public boolean existLegalMoves() {
        //this method use to find match is draw or not
        for (int i = 0; i < pieces.length; i++) {
            for (int j = 0; j < pieces[i].length; j++) {
                if (pieces[i][j] == Piece.EMPTY) return true;
            }
        }
        return false;
    }

    @Override
    public void updateMove(int col, Piece move) {
        //this method use to save Piece in leagal method
        pieces[col][findNextAvailableSpot(col)] = move;
    }

    @Override
    public void updateMove(int col, int row, Piece move) {
        //this method use to save Piece in illegal method
        //this is only use in the MCTS class
        pieces[col][row] = move;
    }



    @Override
    public Winner findWinner() {

        //Check if there is any winner.
        for (int i = 0; i < pieces.length; i++) {
            for (int j = 0; j < pieces[0].length; j++) {
                Piece currentPiece = pieces[i][j]; //Take the first piece to check.

                //firstly check current piece is not empty
                if (currentPiece != Piece.EMPTY) {
                    //Vertical check.
                    if (j + 3 < pieces[0].length &&
                            currentPiece == pieces[i][j + 1] &&
                            currentPiece == pieces[i][j + 2] &&
                            currentPiece == pieces[i][j + 3]) {
                        return new Winner(currentPiece, i, j, i, j + 3);
                    }

                    //Horizontal check.
                    if (i + 3 < pieces.length &&
                            currentPiece == pieces[i + 1][j] &&
                            currentPiece == pieces[i + 2][j] &&
                            currentPiece == pieces[i + 3][j]) {
                        return new Winner(currentPiece, i, j, i + 3, j);
                    }
                }
            }
        }
        //If there is no winner.
        return new Winner(Piece.EMPTY);
    }

    @Override
    public Piece[][] getPieces() {
        return pieces;
    }

}
