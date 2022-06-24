import { Injectable } from '@angular/core';
import { PlayGameDTO } from 'src/dto/playGameDto';
import { GameModel } from 'src/model/game';
import { GameService } from './game.service';

@Injectable({
  providedIn: 'root'
})
export class GamePlaySocketService {

  webSocket: WebSocket;
  gm: GameModel;

  constructor(public gameService: GameService) { }


  public openWebSocket(game: GameModel) {
    this.gm = game;
    this.webSocket = new WebSocket('ws://localhost:8080/gameHandler/' + game.gameId);
    this.webSocket.onopen = event => { }

    this.webSocket.onmessage = event => {

      this.gm = <GameModel>JSON.parse(event.data);
    }

    this.webSocket.onclose = event => { }
  }

  public play(gameId, playerId, bucketIndex) {
    this.webSocket.send(JSON.stringify(new PlayGameDTO(gameId, playerId, bucketIndex, false)));
  }

  public closeSocket() {
    this.webSocket.close();
  }

  public updateGame(gameId) {
    this.webSocket.send(JSON.stringify(new PlayGameDTO(gameId, "", 0, true)));
  }
}
