package com.guney.config;

import com.google.gson.Gson;
import com.guney.service.GameService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@AllArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final GameService gameService;
    private final Gson gson;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(getGameWebSocketHandler(), "/gameHandler/{gameId}").setAllowedOrigins("*");
        registry.addHandler(getMessageWebSocketHandler(), "/message/{gameId}").setAllowedOrigins("*");
    }

    @Bean
    public WebSocketHandler getGameWebSocketHandler() {
        return new GameWebSocketHandler(this.gameService, this.gson);
    }

    @Bean
    public WebSocketHandler getMessageWebSocketHandler() {
        return new MessageWebSocketHandler();
    }
}
