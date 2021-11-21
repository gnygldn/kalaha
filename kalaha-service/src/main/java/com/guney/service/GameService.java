package com.guney.service;

import com.google.gson.Gson;
import com.guney.exception.InvalidGameException;
import com.guney.exception.InvalidMoveException;
import com.guney.model.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
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
        log.info("Game created with gameId " + game.getGameId());
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
            log.info("No open game is found with gameId " + gameId);
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
        startGame(game, playerService.createPlayer(name));
        log.info(game.getPlayer2().getPlayerId() + " is connected to the game " + game.getGameId());
        return game;
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
        log.info("Game with gameId " + game.getGameId() + " is started.");
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

        if (game == null) {
            log.error("An attempt is made for a game not exists with gameId " + gameId);
            throw new InvalidGameException("No game available");
        }

        checkTurn(game, playerId);

        placeStones(game, playerId, bucketIndex);
        log.debug(playerId + " played with bucket " + bucketIndex + " on game " + gameId);

        checkWinner(game);

        return game;
    }

    /**
     * checks the game and the player who made the move it it is that player's turn.
     *
     * @param game     game to be checked if correct player tried to make a move
     * @param playerId playerId of a player who tried to make a move
     */
    private void checkTurn(Game game, String playerId) throws InvalidMoveException {
        if ((game.getPlayer1().getPlayerId().equals(playerId) && !game.isPlayer1Turn()) || (game.getPlayer2().getPlayerId().equals(playerId) && game.isPlayer1Turn())) {
            log.debug(playerId + " tried to play in opponent's turn on game " + game.getGameId());
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
        if (stoneCount == 0) {
            log.debug("Player with playerId {} tried to play empty bucket", playerId);
            throw new InvalidMoveException("Bucket has no stones");
        }

        while (stoneCount > 0) {

            // visits player's buckets starting from the next one of the selected bucket.
            // bucket index is reset to -1 in the end of the while loop to start from 0
            // if there are any stones left after visiting own and opponent's buckets
            for (int i = bucketIndex + 1; i < ownerBuckets.size() && stoneCount > 0; i++) {
                int bucketStoneCount = ownerBuckets.get(i).addStone(1);

                // This block helps checking if the last stone is dropped to own empty bucket and
                // if the opposite box is not empty put this stone and the stones in the opposite bucket
                // to the pool of the player who made the move
                if (--stoneCount == 0) {
                    Bucket oppositeBucket = opponentBuckets.get(game.getBoard().getDefaultBoxCount() - 1 - i);
                    if (bucketStoneCount == 1 && oppositeBucket.getStoneCount() > 0) {
                        int stones = ownerBuckets.get(i).removeAllStones();
                        stones += oppositeBucket.removeAllStones();
                        game.getOwnerPool(playerId).addStone(stones);
                    }
                }
            }

            if (stoneCount > 0) {
                game.getOwnerPool(playerId).addStone(1);
                //if the last stone is dropped to the player's own pool, it is still that player's turn.
                if (--stoneCount == 0) {
                    changeTurn = false;
                }
            }
            for (int i = 0; i < opponentBuckets.size() && stoneCount > 0; i++, stoneCount--) {
                opponentBuckets.get(i).addStone(1);
            }

            // bucketIndex is set to -1 to start from the first bucket of the turn owner's buckets
            // if any stone left after visiting opponent's buckets
            bucketIndex = -1;
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
        String message = boardStatus == BoardStatus.DRAW ? "Game is draw" : game.getWinner() + "is won";
        log.info("Game with gameId {} is over. {}", game.getGameId(), message);
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

        BoardStatus boardStatus;
        if (stoneCount == board.getDefaultStoneCount() * board.getDefaultBoxCount()) {
            boardStatus = BoardStatus.DRAW;
        } else if (stoneCount > board.getDefaultStoneCount() * board.getDefaultBoxCount()) {
            game.setWinner(game.getPlayer1());
            boardStatus = BoardStatus.WIN;
        } else {
            game.setWinner(game.getPlayer2());
            boardStatus = BoardStatus.WIN;
        }
        finishGame(game, boardStatus);
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
