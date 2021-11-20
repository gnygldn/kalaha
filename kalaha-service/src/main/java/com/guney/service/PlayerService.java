package com.guney.service;

import com.guney.model.Player;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Getter
public class PlayerService {

    public static ConcurrentHashMap<String, Player> playerList = new ConcurrentHashMap<>();

    public Player createPlayer(String name) {
        Player player = Player.builder().playerId(UUID.randomUUID().toString()).name(name).build();
        playerList.put(player.getPlayerId(), player);
        return player;
    }
}
