package com.guney.controller;

import com.google.gson.Gson;
import com.guney.exception.InvalidGameException;
import com.guney.exception.InvalidMoveException;
import com.guney.model.Game;
import com.guney.service.GameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestController
@RequestMapping(path = "api/kalaha")
@CrossOrigin("*")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;
    private final Gson gson;

    @PostMapping(path = "/create")
    public ResponseEntity<String> createNewGame(@NotNull @NotEmpty String name, boolean privateGame, int stoneCount, int boxCount) {
        Game game = gameService.createGame(name, privateGame, stoneCount, boxCount);
        log.info(game.getPlayer1().getPlayerId() + " has created the game " + game.getGameId());
        return ResponseEntity.ok(gson.toJson(game));
    }

    @GetMapping(path = "/getOpenGames")
    public ConcurrentHashMap<String, Game> getOpenGameList() {
        return GameService.openGames;
    }

    @PostMapping(path = "/joinRandom")
    public ResponseEntity<String> joinRandomGame(String name) throws InvalidGameException {
        Game game = gameService.connectToRandomGame(name);
        log.info(game.getPlayer2().getPlayerId() + " has joined the game " + game.getGameId());
        return ResponseEntity.ok(gson.toJson(game));
    }

    @PostMapping(path = "/joinPrivate")
    public ResponseEntity<String> joinPrivateGame(String gameId, String name) throws InvalidGameException {
        Game game = gameService.connectToGame(gameId, name);
        log.info(game.getPlayer2().getPlayerId() + " has joined the game " + game.getGameId());
        return ResponseEntity.ok(gson.toJson(game));
    }

    @PostMapping(path = "/playGame")
    public ResponseEntity<String> playGameByGameId(String gameId, String playerId, int bucketIndex) throws InvalidGameException, InvalidMoveException {
        Game game = gameService.play(gameId, playerId, bucketIndex);
        log.info(playerId + " played the bucket with index " + bucketIndex + " in game " + gameId);
        return ResponseEntity.ok(gson.toJson(game));
    }

    @GetMapping(path = "/gameInfo/{gameId}")
    public ResponseEntity<String> getGameByGameId(@PathVariable String gameId) throws InvalidGameException {
        Game game = GameService.gamesInProgress.get(gameId);
        if (game == null) {
            throw new InvalidGameException("There is no games available right now. Please create one or try again in a few minutes");
        }
        return ResponseEntity.ok(gson.toJson(game));
    }

}
