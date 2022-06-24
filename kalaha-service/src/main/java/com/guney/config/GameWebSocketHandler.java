package com.guney.config;

import com.google.gson.Gson;
import com.guney.model.Game;
import com.guney.playGameDTO.PlayGameDTO;
import com.guney.service.GameService;
import lombok.AllArgsConstructor;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class GameWebSocketHandler extends TextWebSocketHandler {

    private final List<WebSocketSession> webSocketSessionList = new ArrayList<>();
    private final GameService gameService;
    private final Gson gson;


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        webSocketSessionList.add(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        try {
            PlayGameDTO playGameDTO = gson.fromJson(message.getPayload(), PlayGameDTO.class);
            Game game;
            if (playGameDTO.isJustUpdate()) {
                game = GameService.gamesInProgress.get(playGameDTO.getGameId());
            } else {
                game = gameService.play(playGameDTO.getGameId(), playGameDTO.getPlayerId(), playGameDTO.getBucketIndex());
            }
            for (WebSocketSession s : webSocketSessionList) {
                s.sendMessage(new TextMessage(gson.toJson(game)));
            }
        } catch (Exception e) {

        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        webSocketSessionList.remove(session);
    }
}
