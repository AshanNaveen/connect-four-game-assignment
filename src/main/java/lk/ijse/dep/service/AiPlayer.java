package lk.ijse.dep.service;

public class AiPlayer extends Player {
    public AiPlayer(Board board) {
        super(board);
    }

    @Override
    public void movePiece(int col) {
        do {
            col = (int) Math.floor(Math.random() * 6);
        } while (!board.isLegalMove(col));
        this.board.updateMove(col, Piece.GREEN);
        this.board.getBoardUi().update(col, false);
        Winner winner = this.board.findWinner();
        if (winner.getWinningPiece() != Piece.EMPTY) {
            this.board.getBoardUi().notifyWinner(winner);
        } else {
            this.board.existLegalMoves();
        }

    }
}
