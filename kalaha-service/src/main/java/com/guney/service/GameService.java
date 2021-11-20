package com.guney.service;

import com.google.gson.Gson;
import com.guney.enums.BoardStatus;
import com.guney.enums.GameStatus;
import com.guney.exception.InvalidGameException;
import com.guney.exception.InvalidMoveException;
import com.guney.model.Board;
import com.guney.model.Bucket;
import com.guney.model.Game;
import com.guney.model.Player;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Getter
@RequiredArgsConstructor
public class GameService {

    public static ConcurrentHashMap<String, Game> openGames = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, Game> gamesInProgress = new ConcurrentHashMap<>();
    private final Gson gson;
    private final PlayerService playerService;

    /**
     * @param name        a string that is used to create player1
     * @param privateGame a boolean to decide if the create game is private
     * @param stoneCount  an integer to set stones to buckets
     * @param boxCount    an integer to determine default bucket count
     * @return last created game object
     */
    public Game createGame(String name, boolean privateGame, int stoneCount, int boxCount) {
        Player player1 = playerService.createPlayer(name);
        Game game = Game.builder().board(new Board(stoneCount, boxCount)).player1(player1).gameId(UUID.randomUUID().toString())
                .gameStatus(GameStatus.WAITING_OPPONENT).privateGame(privateGame).player1Turn(true).build();
        openGames.put(game.getGameId(), game);
        return game;
    }

    /**
     * @param gameId string version of an uuid of the game to find an exact game to connect
     * @param name   a string of the player2
     * @return game with the given id
     * @throws InvalidGameException if someone else joined the game with given Id
     */
    public Game connectToGame(String gameId, String name) throws InvalidGameException {
        Game game = openGames.get(gameId);
        if (game == null) {
            throw new InvalidGameException("The game you are trying to join is no longer exists");
        }
        return startGame(game, playerService.createPlayer(name));
    }

    /**
     * @param name a string of the player2
     * @return a non-private game waiting for a player to join
     * @throws InvalidGameException if no open non-private game exists
     */
    public Game connectToRandomGame(String name) throws InvalidGameException {
        Game game = openGames.values().stream().filter(e -> !e.isPrivateGame()).findFirst()
                .orElseThrow(() -> new InvalidGameException("There is no games available right now. Please create one or try again in a few minutes"));
        return startGame(game, playerService.createPlayer(name));
    }

    /**
     * sets second player and starts the game
     *
     * @param game    game to be started
     * @param player2 player to be joined to the game
     * @return the given game after second player is joined
     */
    public Game startGame(Game game, Player player2) {
        game.setGameStatus(GameStatus.IN_PROGRESS);
        game.setPlayer2(player2);
        openGames.remove(game.getGameId());
        gamesInProgress.put(game.getGameId(), game);
        return game;
    }

    /**
     * @param gameId      a gameId which a player made a move
     * @param playerId    a playerId of a player who made a move
     * @param bucketIndex a bucket index which player wants to take and distribute the stones within
     * @return game after a move is made
     */
    public Game play(String gameId, String playerId, int bucketIndex) throws InvalidGameException, InvalidMoveException {

        Game game = gamesInProgress.get(gameId);

        if(game == null) {
            throw new InvalidGameException("There is no games available right now. Please create one or try again in a few minutes");
        }

        checkTurn(game, playerId);

        placeStones(game, playerId, bucketIndex);

        checkWinner(game);

        return game;
    }

    /**
     * checks the game and the player who made the move it it is that player's turn.
     *
     * @param game     game to be checked if correct player tried to make a move
     * @param playerId playerId of a player who tried to make a move
     * @return if right player made a move
     */
    private void checkTurn(Game game, String playerId) throws InvalidMoveException {
        if((game.getPlayer1().getPlayerId().equals(playerId) && !game.isPlayer1Turn()) || (game.getPlayer2().getPlayerId().equals(playerId) && game.isPlayer1Turn())) {
            throw new InvalidMoveException("Opponent's turn");
        }
    }

    /**
     * @param game        a game which a player made a move
     * @param playerId    playerId of a player who made a move
     * @param bucketIndex a bucket index which player wants to take and distribute the stones within
     */
    private void placeStones(Game game, String playerId, int bucketIndex) throws InvalidMoveException {
        boolean changeTurn = true;
        List<Bucket> ownerBuckets = game.getOwnerBuckets(playerId);
        List<Bucket> opponentBuckets = game.getOpponentBuckets(playerId);
        int stoneCount = ownerBuckets.get(bucketIndex).removeAllStones();
        if(stoneCount == 0) {
            throw new InvalidMoveException("Bucket has no stones");
        }

        while (stoneCount > 0) {

            for (int i = bucketIndex + 1; i < ownerBuckets.size() && stoneCount > 0; i++) {
                int bucketStoneCount = ownerBuckets.get(i).addStone(1);
                if (--stoneCount == 0) {
                    if (bucketStoneCount == 1 && opponentBuckets.get(game.getBoard().getDefaultBoxCount() - 1 - i).getStoneCount() > 0) {
                        int stones = ownerBuckets.get(i).removeAllStones();
                        stones += opponentBuckets.get(game.getBoard().getDefaultBoxCount() - 1 - i).removeAllStones();
                        game.getOwnerPool(playerId).addStone(stones);
                    }
                }
            }
            bucketIndex = -1;

            if (stoneCount > 0) {
                game.getOwnerPool(playerId).addStone(1);
                if (--stoneCount == 0) {
                    changeTurn = false;
                }
            }
            for (int i = 0; i < opponentBuckets.size() && stoneCount > 0; i++, stoneCount--) {
                opponentBuckets.get(i).addStone(1);
            }
        }

        if (changeTurn) {
            game.setPlayer1Turn(!game.isPlayer1Turn());
        }
    }

    /**
     * Checks if the game is over, if it is; sets new board status and if not a draw sets winner
     *
     * @param game game to check if it is over
     */
    public void checkWinner(Game game) {
        Board board = game.getBoard();
        if (board.getPlayer1Pool().getStoneCount() > board.getDefaultStoneCount() * board.getDefaultBoxCount()) {
            game.setWinner(game.getPlayer1());
            finishGame(game, BoardStatus.WIN);
        } else if (board.getPlayer2Pool().getStoneCount() > board.getDefaultStoneCount() * board.getDefaultBoxCount()) {
            game.setWinner(game.getPlayer2());
            finishGame(game, BoardStatus.WIN);
        } else if (!playersHaveMove(board)) {
            countStonesAndDecideWinner(game);
        }
    }

    /**
     * Sets board and game status and removes game from the list.
     *
     * @param game        game that ends
     * @param boardStatus last status of the bpard
     */
    private void finishGame(Game game, BoardStatus boardStatus) {
        game.getBoard().setBoardStatus(boardStatus);
        game.setGameStatus(GameStatus.OVER);
        gamesInProgress.remove(game.getGameId());
    }

    /**
     * Takes a game if there is no move left on the board and counts player1 stones to decide winner
     *
     * @param game game to count it's player1's stones
     */
    private void countStonesAndDecideWinner(Game game) {
        Board board = game.getBoard();
        int stoneCount = board.getPlayer1Pool().getStoneCount();
        stoneCount += board.getPlayer1Buckets().stream().mapToInt(Bucket::getStoneCount).sum();
        board.setEndStateStonesByPlayer1StoneCount(stoneCount);

        if (stoneCount == board.getDefaultStoneCount() * board.getDefaultBoxCount()) {
            finishGame(game, BoardStatus.DRAW);
        } else if (stoneCount > board.getDefaultStoneCount() * board.getDefaultBoxCount()) {
            game.setWinner(game.getPlayer1());
            finishGame(game, BoardStatus.WIN);
        } else {
            game.setWinner(game.getPlayer2());
            finishGame(game, BoardStatus.WIN);
        }
    }

    /**
     * Takes the board and checks if any player has any move
     *
     * @param board board to check if any move left for any player
     * @return if any player has any move
     */
    private boolean playersHaveMove(Board board) {
        return board.getPlayer1Buckets().stream().anyMatch(bucket -> bucket.getStoneCount() > 0)
                && board.getPlayer2Buckets().stream().anyMatch(bucket -> bucket.getStoneCount() > 0);
    }
}
