package com.guney.model;

import com.guney.enums.GameStatus;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Game {

    private String gameId;
    private Player player1;
    private Player player2;
    private Board board;
    private GameStatus gameStatus;
    private boolean privateGame;
    private boolean player1Turn;
    private Player winner;

    /**
     * takes playerId to return regarding buckets.
     *
     * @param playerId playerId of the player who made a move
     * @return buckets of the player who made the move
     */
    public List<Bucket> getOwnerBuckets(String playerId) {
        if (playerId.equals(player1.getPlayerId())) {
            return board.getPlayer1Buckets();
        }
        return board.getPlayer2Buckets();
    }

    /**
     * takes playerId to return opponent's buckets.
     *
     * @param playerId playerId of the player who made a move
     * @return buckets of the opponent of the player who made the move
     */
    public List<Bucket> getOpponentBuckets(String playerId) {
        if (playerId.equals(player1.getPlayerId())) {
            return board.getPlayer2Buckets();
        }
        return board.getPlayer1Buckets();

    }

    /**
     * takes playerId to return regarding pool.
     *
     * @param playerId playerId of the player who made a move
     * @return pool of the player who made the move
     */
    public Bucket getOwnerPool(String playerId) {
        if (playerId.equals(player1.getPlayerId())) {
            return board.getPlayer1Pool();
        }
        return board.getPlayer2Pool();
    }
}