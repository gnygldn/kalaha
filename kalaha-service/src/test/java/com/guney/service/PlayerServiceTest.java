package com.guney.service;

import com.guney.model.Player;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PlayerServiceTest {

    private final PlayerService playerService = new PlayerService();

    @Test
    void checkPlayerName() {
        String playerName = "player1";
        Player player = playerService.createPlayer(playerName);
        Assertions.assertEquals(playerName, player.getName());
    }

}