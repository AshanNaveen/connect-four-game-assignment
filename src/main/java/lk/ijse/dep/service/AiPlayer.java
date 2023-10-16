package lk.ijse.dep.service;

import java.util.*;

public class AiPlayer extends Player {
    public AiPlayer(Board board) {
        super(board);
    }

    @Override
    public void movePiece(int col) {

        MCTS mcts = new MCTS(board);
        col = mcts.findTheMove();
       /* do {
         col = (int) (Math.random() * 6);
       } while (!board.isLegalMove(col));*/

        board.updateMove(col, Piece.GREEN);
        board.getBoardUI().update(col, false);
        Winner winner = board.findWinner();
        if (winner.getWinningPiece() != Piece.EMPTY) {
            board.getBoardUI().notifyWinner(winner);
        } else if (!board.existLegalMoves()) {
            board.getBoardUI().notifyWinner(new Winner(Piece.EMPTY));
        }


    }

    private static class MCTS {
        private final Board board;

        public MCTS(Board board) {
            this.board = board;
        }

        private int  findTheMove() {

            Node tree = new Node(board, Piece.BLUE);

            for (int i = 0; i < 2500; i++) {
                System.out.println("Start Mcts Searching...");
                //Selection
                Node selectedNode = selectNode(tree);

                //Expand
                Node nodeToExplore = expandNode(selectedNode);

                //Simulation
                Piece result = randomSimulation(nodeToExplore);

                //Backpropagation
                backPropagation(nodeToExplore, result);

                System.out.println("\n\n");
            }

            Node bestNode = tree.getChildWithMaxScore();

            return bestNode.getMove();

        }


        //this method used to select node from children array
        private Node selectNode(Node parentNode) {

            System.out.println("SelectNode");
            Node currentNode = parentNode;
            while (!currentNode.getChildren().isEmpty()) {
                currentNode = findBestNodeWithUCT(currentNode);
            }

            return currentNode;
        }

        //this method used to expand nodes to leagal children
        private Node expandNode(Node parentNode) {

            System.out.println("ExpandNode");
            boolean gameStatus = gameState(parentNode.getBoard());
            if (!gameStatus) {
                return parentNode;
            } else {
                List<BoardWithIndex> nextLegalMoves = getLegalMoves(parentNode);
                for (BoardWithIndex nextLegalMove : nextLegalMoves) {
                    Board move = nextLegalMove.getBoard();
                    Node childNode = new Node(move, (parentNode.getPiece() == Piece.BLUE) ? Piece.GREEN : Piece.BLUE);
                    childNode.setParent(parentNode);
                    childNode.setMove(nextLegalMove.getIndex());
                    parentNode.addChild(childNode);
                }

                return parentNode.getChildren().get((int) (Math.random() * nextLegalMoves.size()));
            }
        }

        // this method used to simulate node to game ending
        private Piece randomSimulation(Node parentNode) {

            System.out.println("Simulating Node.....");
            Board board = copyBoardState(parentNode.getBoard());
            Node node = new Node(board, parentNode.getPiece());
            node.setParent(parentNode.getParent());

            if (node.getBoard().findWinner().getWinningPiece() == Piece.BLUE) {
                node.getParent().setScore(Integer.MIN_VALUE);
                return Piece.BLUE;
            }

            while (gameState(node.getBoard())) {

                BoardWithIndex nextMove = getRandomNextBoard(node);
                Node child = new Node(nextMove.getBoard(), node.getPiece());
                child.setParent(node);
                child.setMove(nextMove.getIndex());
                node.addChild(child);
                node = child;
            }


            Winner winner = node.getBoard().findWinner();
            if (winner.getWinningPiece() == Piece.GREEN) {
                return Piece.GREEN; // winner -- > Machine
            } else if (winner.getWinningPiece() == Piece.BLUE) {
                return Piece.BLUE;// winner -- > Human
            } else {
                return Piece.EMPTY; //Draw
            }
        }

        //this method can back-propagate to parent node from node that given
        private void backPropagation(Node nodeToExplore, Piece result) {

            System.out.println("BackProgate");
            Node node = nodeToExplore;
            while (node != null) {
                node.incrementVisit();
                if (node.getPiece() == result) {
                    node.incrementScore();//this line use to increment Score of node in 1 point
                }
                node = node.getParent();
            }

        }

        //        this method is return node that has Max UCT Value
        public static Node findBestNodeWithUCT(Node parentNode) {

            double maxUct = parentNode.getChildren().get(0).calUct();
            Node bestNode = parentNode.getChildren().get(0);
            for (Node node : parentNode.getChildren()) {
                if (maxUct < node.calUct()) {
                    maxUct = node.calUct();
                    bestNode = node;
                }
            }
            System.out.println("Best UCT is " + maxUct);
            return bestNode;
        }


        //This Method is to get the all the legal moves of the parent node that gi\\
        public List<BoardWithIndex> getLegalMoves(Node parentNode) {

            List<BoardWithIndex> nextMoves = new ArrayList<>();

            Piece nextPiece = (parentNode.getPiece() == Piece.BLUE) ? Piece.GREEN : Piece.BLUE;

            for (int i = 0; i < 6; i++) {
                if (parentNode.getBoard().isLegalMove(i)) {
                    int raw = parentNode.getBoard().findNextAvailableSpot(i);
                    Board copyBoard = copyBoardState(parentNode.getBoard());
                    copyBoard.updateMove(i, raw, nextPiece);
                    BoardWithIndex boardWithIndex = new BoardWithIndex(copyBoard, i);
                    nextMoves.add(boardWithIndex);
                }
            }

            return nextMoves;
        }

        // this method use to get new board with legal move
        private BoardWithIndex getRandomNextBoard(Node node) {
            List<BoardWithIndex> legalMoves = getLegalMoves(node);

            if (legalMoves.isEmpty()) {
                return null;
            }
            return legalMoves.get((int) (Math.random() * legalMoves.size()));
        }


        public boolean gameState(Board board) {
            // this method use to get Game is ongoing or not
            Winner winner = board.findWinner();
            if (winner.getWinningPiece() != Piece.EMPTY) {
                return false;
            } else if (!board.existLegalMoves()) {
                return false;
            }
            return true;
        }


        private Board copyBoardState(Board originalBoard) {
            // Create a new board and copy the state cell by cell
            Board newBoard = new BoardImpl(originalBoard.getBoardUI());
            for (int col = 0; col < Board.NUM_OF_COLS; col++) {
                for (int row = 0; row < Board.NUM_OF_ROWS; row++) {
                    Piece piece = originalBoard.getPieces()[col][row];
                    newBoard.updateMove(col, row, piece);
                }
            }
            return newBoard;
        }


    }

    //this class use to wrap Board and legal Move
    private static class BoardWithIndex {
        private final Board board;
        private final int index;

        public BoardWithIndex(Board board, int index) {
            this.board = board;
            this.index = index;
        }

        public Board getBoard() {
            return board;
        }

        public int getIndex() {
            return index;
        }


    }

    //this class use to make node of the MCTS tree
    private static class Node {
        private Board board;

        private int visit;

        private int score;

        private final ArrayList<Node> children = new ArrayList<>();

        private Node parent = null;

        private Piece piece;

        private int move;

        public Node(Board board, Piece piece) {
            this.setBoard(board);
            this.setPiece(piece);
        }

        public Node getChildWithMaxScore() {
            Node result = getChildren().get(0);
            for (int i = 1; i < getChildren().size(); i++) {
                if (getChildren().get(i).getScore() > result.getScore()) {
                    result = getChildren().get(i);
                }
            }
            return result;
        }

        //this method return uct value of node
        public double calUct() {

            if (this.visit == 0) {
                return Integer.MAX_VALUE;//this line is used to infinity value (0/number = infinty)
            }
            return (score / (double) visit) + 1.41 * Math.sqrt(Math.log(parent.getVisit()) / (double) visit);
        }

        public void addChild(Node node) {
            getChildren().add(node);
        }

        public Board getBoard() {
            return board;
        }


        public void setBoard(Board board) {
            this.board = board;
        }

        public int getVisit() {
            return visit;
        }

        public void incrementVisit() {
            this.visit++;
        }

        public int getScore() {
            return score;
        }

        public void incrementScore() {
            this.score++;
        }

        public void setScore(int score) {
            this.score = score;
        }

        public List<Node> getChildren() {
            return children;
        }

        public Node getParent() {
            return parent;
        }

        public void setParent(Node parent) {
            this.parent = parent;
        }

        public Piece getPiece() {
            return piece;
        }

        public void setPiece(Piece piece) {
            this.piece = piece;
        }

        public int getMove() {
            return move;
        }

        public void setMove(int move) {
            this.move = move;
        }
    }


}
