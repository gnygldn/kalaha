import { Injectable } from '@angular/core';
import { GameModel } from 'src/model/game';
import { GameService } from './game.service';
import { GamePlaySocketService } from './gameplay-socket.service';

@Injectable({
  providedIn: 'root'
})
export class MessageSocketService {

  webSocket: WebSocket;
  message: String;

  constructor(public gameService: GameService) { }


  public openWebSocket(game: GameModel, gamePlaySocketService: GamePlaySocketService) {

    this.webSocket = new WebSocket('ws://localhost:8080/message/' + game.gameId);
    this.webSocket.onopen = event => {
      if (game.player2) {
        this.sendMessage("Player 2 joined the game");
        gamePlaySocketService.updateGame(game.gameId);
      }
    }

    this.webSocket.onmessage = event => {
      this.message = event.data;
    }

    this.webSocket.onclose = event => {
      window.alert("Opponent left. Game over");
    }
  }

  public playerJoined() {
    this.webSocket.send("New player joined");
  }

  public sendMessage(message) {
    this.webSocket.send(message);
  }

  public closeSocket() {
    this.webSocket.close();
  }
}