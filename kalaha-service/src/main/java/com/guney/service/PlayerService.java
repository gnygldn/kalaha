package com.guney.service;

import com.guney.model.Player;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Getter
public class PlayerService {

    public Player createPlayer(String name) {
        Player player = Player.builder().playerId(UUID.randomUUID().toString()).name(name).build();
        log.info("Player {} created.", player.getPlayerId());
        return player;
    }
}
