package com.pilotpirxie.party.controllers;

import com.pilotpirxie.party.dto.events.incoming.JoinEvent;
import com.pilotpirxie.party.services.GameService;
import com.pilotpirxie.party.services.SessionGameMappingService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
public class GameController {
    private final SessionGameMappingService sessionGameMappingService;
    private final GameService gameService;

    public GameController(
        SessionGameMappingService sessionGameMappingService,
        GameService gameService
    ) {
        this.sessionGameMappingService = sessionGameMappingService;
        this.gameService = gameService;
    }

    @MessageMapping("/Join")
    public void joinGame(@Payload JoinEvent event, SimpMessageHeaderAccessor headerAccessor) {
        var gameId = event.code().isEmpty()
            ? gameService.createGame()
            : gameService.getGameId(event.code()).orElseGet(gameService::createGame);
        gameService.joinGame(headerAccessor.getSessionId(), event.nickname(), event.avatar(), gameId);
        sessionGameMappingService.mapSessionToGame(headerAccessor.getSessionId(), gameId);
        gameService.sendUsersState(gameId);
    }

    @MessageMapping("/ToggleReady")
    public void toggleReady(SimpMessageHeaderAccessor headerAccessor) {
        gameService.toggleReady(headerAccessor.getSessionId());
        var sessionGame = sessionGameMappingService.getGameId(headerAccessor.getSessionId());
        gameService.sendUsersState(sessionGame.gameId());
    }

    @MessageMapping("/StartGame")
    public void startGame(SimpMessageHeaderAccessor headerAccessor) {
        var sessionGame = sessionGameMappingService.getGameId(headerAccessor.getSessionId());
        gameService.startGame(sessionGame.gameId());
        gameService.sendGameState(sessionGame.gameId());
    }
}
