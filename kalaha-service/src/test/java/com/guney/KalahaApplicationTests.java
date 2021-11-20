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
        Game game = createGame("id1", "id2", 6 ,6);
        game.getBoard().getPlayer2Pool().setStoneCount(37);
        gameService.checkWinner(game);
        assertEquals(game.getPlayer2(), game.getWinner());
    }

    @Test
    void drawCheck() {
        Game game = createGame("id1", "id2", 6 ,6);
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
        Game game = createGame("id1", "id2", 6 ,6);
        game.getBoard().getPlayer1Pool().setStoneCount(37);
        gameService.checkWinner(game);
        assertEquals(game.getPlayer1(), game.getWinner());
        assertEquals(game.getBoard().getBoardStatus(), BoardStatus.WIN);
    }

    @Test
    void winnerCheckNoMovesLeft() {
        Game game = createGame("id1", "id2", 6 ,6);
        List<Bucket> bucketList = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            bucketList.add(new Bucket(7));
        }
        game.getBoard().setPlayer1Buckets(bucketList);

        List<Bucket> bucketList2 = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            bucketList2.add(new Bucket(0));
        }
        game.getBoard().setPlayer2Buckets(bucketList2);

        game.getBoard().getPlayer1Pool().setStoneCount(1);
        game.getBoard().getPlayer2Pool().setStoneCount(29);
        gameService.checkWinner(game);
        assertEquals(game.getPlayer1(), game.getWinner());
        assertEquals(game.getBoard().getBoardStatus(), BoardStatus.WIN);
    }


    @Test
    void getPlayer1Buckets() {
        Game game = createGame("id1", "id2", 6 ,6);

        List<Bucket> bucketList = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            bucketList.add(new Bucket(3));
        }

        game.getBoard().setPlayer1Buckets(bucketList);

        assertEquals(bucketList, game.getBoard().getPlayer1Buckets());
    }

    @Test
    void getPlayer2Buckets() {
        Game game = createGame("id1", "id2", 6 ,6);

        List<Bucket> bucketList = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            bucketList.add(new Bucket(3));
        }

        game.getBoard().setPlayer2Buckets(bucketList);

        assertEquals(bucketList, game.getBoard().getPlayer2Buckets());
    }


    @Test
    void player2IllegalMove() {
        Game game = createGame("id1", "id2", 6 ,6);
        try {
            gameService.play(game.getGameId(), game.getPlayer2().getPlayerId(), 0);
            Assertions.fail();
        } catch (InvalidGameException e) {
            Assertions.fail();
        }
        catch (InvalidMoveException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    void player1IllegalMove() {
        Game game = createGame("id1", "id2", 6 ,6);
        game.setPlayer1Turn(false);
        try {
            gameService.play(game.getGameId(), game.getPlayer1().getPlayerId(), 0);
            Assertions.fail();
        } catch (InvalidGameException e) {
            Assertions.fail();
        } catch (InvalidMoveException e) {
            Assertions.assertTrue(true);
        }

    }


    @Test
    void playGame() throws InvalidGameException {
        Game game = createGame("id1", "id2", 6 ,6);

        Game game2 = Game.builder().gameId(game.getGameId()).board(new Board(6,6)).gameStatus(game.getGameStatus()).
                player1(game.getPlayer1()).player2(game.getPlayer2()).player1Turn(game.isPlayer1Turn()).build();

        //checks player 2 cant make the first move
        try {
            game = gameService.play(game.getGameId(), game.getPlayer2().getPlayerId(), 0);
        } catch (InvalidMoveException ignored) {}
        assertEquals(game2, game);

        //checks the board status and player1Turn after player1 plays with his first bucket
        try {
            game = gameService.play(game.getGameId(), game.getPlayer1().getPlayerId(), 0);
        } catch (InvalidMoveException ignored) {}
        game2.getBoard().getPlayer1Buckets().forEach(bucket -> bucket.setStoneCount(7));
        game2.getBoard().getPlayer1Buckets().get(0).setStoneCount(0);
        game2.getBoard().getPlayer1Pool().setStoneCount(1);
        assertEquals(game2, game);

        // checks it is still player1Turn
        try {
            game = gameService.play(game.getGameId(), game.getPlayer2().getPlayerId(), 3);
        } catch (InvalidMoveException ignored) {}
        try {
            game = gameService.play(game.getGameId(), game.getPlayer2().getPlayerId(), 1);
        } catch (InvalidMoveException ignored) {}
        try {
            game = gameService.play(game.getGameId(), game.getPlayer2().getPlayerId(), 2);
        } catch (InvalidMoveException ignored) {}
        assertEquals(game2, game);

        //checks the board status after the move and player1Turn should be false after the move
        try {
            game = gameService.play(game.getGameId(), game.getPlayer1().getPlayerId(), 1);
        } catch (InvalidMoveException ignored) {}
        game2.setPlayer1Turn(false);
        game2.getBoard().getPlayer1Buckets().forEach(bucket -> bucket.setStoneCount(8));
        game2.getBoard().getPlayer1Buckets().get(0).setStoneCount(0);
        game2.getBoard().getPlayer1Buckets().get(1).setStoneCount(0);
        game2.getBoard().getPlayer1Pool().setStoneCount(2);
        game2.getBoard().getPlayer2Buckets().get(0).setStoneCount(7);
        game2.getBoard().getPlayer2Buckets().get(1).setStoneCount(7);
        assertEquals(game2, game);

        //checks it is player2Turn
        try {
            game = gameService.play(game.getGameId(), game.getPlayer1().getPlayerId(), 1);
        } catch (InvalidMoveException ignored) {}
        assertEquals(game2, game);

        //checks play with empty pit
        try {
            game = gameService.play(game.getGameId(), game.getPlayer2().getPlayerId(), 0);
            game = gameService.play(game.getGameId(), game.getPlayer1().getPlayerId(), 0);

            List<Integer> intList = Arrays.asList(new Integer[] {0,0,8,8,8,8});
            List<Integer> intList2 = Arrays.asList(new Integer[] {0,8,7,7,0,7});
            for(int i=0; i<6; i++) {
                game2.getBoard().getPlayer1Buckets().get(i).setStoneCount(intList.get(i));
                game2.getBoard().getPlayer2Buckets().get(i).setStoneCount(intList2.get(i));
            }
            game2.getBoard().getPlayer1Pool().setStoneCount(10);
            game2.getBoard().getPlayer2Pool().setStoneCount(1);
            assertEquals(game2, game);
        } catch (InvalidMoveException ignored) {}

    }

    @SuppressWarnings("SameParameterValue")
    private Game createGame(String player1Id, String player2Id, int defaultStoneCount, int defaultBoxCount) {
        Game game = Game.builder().gameId("asd").board(new Board(defaultStoneCount,defaultBoxCount)).gameStatus(GameStatus.IN_PROGRESS).
                player1(Player.builder().playerId(player1Id).name("guney").build()).player2(Player.builder().playerId(player2Id).name("guney2").build()).player1Turn(true).build();
        GameService.gamesInProgress.put(game.getGameId(), game);
        return game;
    }

}
