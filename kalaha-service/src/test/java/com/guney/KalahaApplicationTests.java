package com.guney;


import com.guney.enums.BoardStatus;
import com.guney.enums.GameStatus;
import com.guney.exception.InvalidGameException;
import com.guney.exception.InvalidMoveException;
import com.guney.model.Board;
import com.guney.model.Bucket;
import com.guney.model.Game;
import com.guney.model.Player;
import com.guney.service.GameService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class KalahaApplicationTests {

    private final GameService gameService = new GameService(null, null);

    @Test
    void contextLoads() {

    }

    @Test
    void winnerCheckWhenPlayer2HasMoreStones() {
        Game game = createGame("id1", "id2", 6, 6);
        game.getBoard().getPlayer2Pool().setStoneCount(37);
        gameService.checkWinner(game);
        assertEquals(game.getPlayer2(), game.getWinner());
    }

    @Test
    void drawCheck() {
        Game game = createGame("id1", "id2", 6, 6);
        List<Bucket> bucketList = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            bucketList.add(new Bucket(0));
        }
        game.getBoard().setPlayer1Buckets(bucketList);
        game.getBoard().getPlayer1Pool().setStoneCount(36);
        gameService.checkWinner(game);
        assertEquals(game.getBoard().getBoardStatus(), BoardStatus.DRAW);
    }

    @Test
    void winnerCheckWhenPlayer1HasMoreStones() {
        Game game = createGame("id1", "id2", 6, 6);
        game.getBoard().getPlayer1Pool().setStoneCount(37);
        gameService.checkWinner(game);
        assertEquals(game.getPlayer1(), game.getWinner());
        assertEquals(game.getBoard().getBoardStatus(), BoardStatus.WIN);
    }

    @Test
    void winnerCheckNoMovesLeft() {
        Game game = createGame("id1", "id2", 6, 6);
        game.getBoard().getPlayer1Buckets().forEach(bucket -> bucket.setStoneCount(7));
        game.getBoard().getPlayer2Buckets().forEach(bucket -> bucket.setStoneCount(0));
        game.getBoard().getPlayer1Pool().setStoneCount(1);
        game.getBoard().getPlayer2Pool().setStoneCount(29);
        gameService.checkWinner(game);
        assertEquals(game.getPlayer1(), game.getWinner());
        assertEquals(game.getBoard().getBoardStatus(), BoardStatus.WIN);
    }


    @Test
    void getPlayer1Buckets() {
        Game game = createGame("id1", "id2", 6, 6);

        List<Bucket> bucketList = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            bucketList.add(new Bucket(3));
        }

        game.getBoard().setPlayer1Buckets(bucketList);

        assertEquals(bucketList, game.getOwnerBuckets(game.getPlayer1().getPlayerId()));
    }

    @Test
    void getPlayer2Buckets() {
        Game game = createGame("id1", "id2", 6, 6);

        List<Bucket> bucketList = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            bucketList.add(new Bucket(3));
        }

        game.getBoard().setPlayer2Buckets(bucketList);

        assertEquals(bucketList, game.getOwnerBuckets(game.getPlayer2().getPlayerId()));
    }


    @Test
    void player2IllegalMove() {
        Game game = createGame("id1", "id2", 6, 6);
        Assertions.assertThrows(InvalidMoveException.class, () -> gameService.play(game.getGameId(), game.getPlayer2().getPlayerId(), 0));

    }

    @Test
    void player1IllegalMove() {
        Game game = createGame("id1", "id2", 6, 6);
        game.setPlayer1Turn(false);
        Assertions.assertThrows(InvalidMoveException.class, () -> gameService.play(game.getGameId(), game.getPlayer1().getPlayerId(), 0));

    }


    @Test
    void player1MakeFirstMove() throws InvalidGameException, InvalidMoveException {
        Game game = createGame("id1", "id2", 6, 6);
        gameService.play(game.getGameId(), game.getPlayer1().getPlayerId(), 0);

        Game game2 = createGame("id1", "id2", 6, 6);
        game2.getBoard().getPlayer1Buckets().forEach(bucket -> bucket.setStoneCount(7));
        game2.getBoard().getPlayer1Buckets().get(0).setStoneCount(0);
        game2.getBoard().getPlayer1Pool().setStoneCount(1);
        assertEquals(game2, game);
    }

    @Test
    void player1MoveEndsInEmptyPitWhenOpponentBucketIsEmpty() throws InvalidGameException, InvalidMoveException {
        Game game = createGame("id1", "id2", 6, 6);
        List<Integer> bucketStoneCounts = Arrays.asList(1,11,3,11,10,0);
        List<Integer> bucketStoneCounts2 = Arrays.asList(0,9,9,8,1,2);
        for(int i=0; i<6; i++) {
            game.getBoard().getPlayer1Buckets().get(i).setStoneCount(bucketStoneCounts.get(i));
            game.getBoard().getPlayer2Buckets().get(i).setStoneCount(bucketStoneCounts2.get(i));
        }
        game.getBoard().getPlayer1Pool().setStoneCount(3);
        game.getBoard().getPlayer2Pool().setStoneCount(4);

        gameService.play(game.getGameId(), game.getPlayer1().getPlayerId(), 2);

        Game game2 = createGame("id1", "id2", 6, 6);
        game2.setPlayer1Turn(false);
        bucketStoneCounts = Arrays.asList(1,11,0,12,11,1);
        for(int i=0; i<6; i++) {
            game2.getBoard().getPlayer1Buckets().get(i).setStoneCount(bucketStoneCounts.get(i));
            game2.getBoard().getPlayer2Buckets().get(i).setStoneCount(bucketStoneCounts2.get(i));
        }
        game2.getBoard().getPlayer1Pool().setStoneCount(3);
        game2.getBoard().getPlayer2Pool().setStoneCount(4);

        assertEquals(game2, game);

    }

    @Test
    void player2StealsPlayer1Stones() throws InvalidGameException, InvalidMoveException {
        Game game = createGame("id1", "id2", 6, 6);
        game.setPlayer1Turn(false);
        List<Integer> bucketStoneCounts = Arrays.asList(4,14,2,4,0,1);
        List<Integer> bucketStoneCounts2 = Arrays.asList(1,1,11,10,1,0);
        for(int i=0; i<6; i++) {
            game.getBoard().getPlayer1Buckets().get(i).setStoneCount(bucketStoneCounts.get(i));
            game.getBoard().getPlayer2Buckets().get(i).setStoneCount(bucketStoneCounts2.get(i));
        }
        game.getBoard().getPlayer1Pool().setStoneCount(5);
        game.getBoard().getPlayer2Pool().setStoneCount(8);

        gameService.play(game.getGameId(), game.getPlayer2().getPlayerId(), 4);

        Game game2 = createGame("id1", "id2", 6, 6);
        game2.setPlayer1Turn(true);
        bucketStoneCounts = Arrays.asList(0,14,2,4,0,1);
        bucketStoneCounts2 = Arrays.asList(1,1,11,10,0,0);
        for(int i=0; i<6; i++) {
            game2.getBoard().getPlayer1Buckets().get(i).setStoneCount(bucketStoneCounts.get(i));
            game2.getBoard().getPlayer2Buckets().get(i).setStoneCount(bucketStoneCounts2.get(i));
        }
        game2.getBoard().getPlayer1Pool().setStoneCount(5);
        game2.getBoard().getPlayer2Pool().setStoneCount(13);

        assertEquals(game2, game);

    }

    @SuppressWarnings("SameParameterValue")
    private Game createGame(String player1Id, String player2Id, int defaultStoneCount, int defaultBoxCount) {
        Game game = Game.builder().gameId("asd").board(new Board(defaultStoneCount, defaultBoxCount)).gameStatus(GameStatus.IN_PROGRESS).
                player1(Player.builder().playerId(player1Id).name("guney").build()).player2(Player.builder().playerId(player2Id).name("guney2").build()).player1Turn(true).build();
        GameService.gamesInProgress.put(game.getGameId(), game);
        return game;
    }

}
