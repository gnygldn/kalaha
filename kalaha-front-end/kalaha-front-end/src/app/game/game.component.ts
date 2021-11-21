import { Component, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { GameModel } from 'src/model/game';
import { GamePlaySocketService } from '../services/gameplay-socket.service';
import { MessageSocketService } from '../services/message-socket.service';

@Component({
  selector: 'app-game',
  templateUrl: './game.component.html',
  styleUrls: ['./game.component.scss']
})
export class GameComponent implements OnDestroy {

  public gm: GameModel;
  public playerId: string;


  constructor(
    private router: Router,
    public gamePlaySocketService: GamePlaySocketService,
    public messageSocketService: MessageSocketService
  ) {
    if (this.router.getCurrentNavigation()?.extras.state) {
      this.gm = this.router.getCurrentNavigation().extras.state.game;
      this.playerId = this.router.getCurrentNavigation().extras.state.player;
      this.gamePlaySocketService.openWebSocket(this.gm);
      this.messageSocketService.openWebSocket(this.gm, this.gamePlaySocketService);
    }
  }
  ngOnDestroy(): void {
    this.gamePlaySocketService.closeSocket();
  }

  checkPlayer1() {
    return this.playerId === this.gamePlaySocketService.gm.player1.playerId;
  }

  getPlayer2Name() {
    return this.gamePlaySocketService.gm.player2 ? this.gamePlaySocketService.gm.player2.name : "Waiting Opponent";
  }

  makeMove(bucketIndex, stoneCount) {

    if (this.checkMoveAllowed(stoneCount)) {
      this.gamePlaySocketService.play(this.gm.gameId, this.playerId, bucketIndex);
      let message = "";
      if (this.checkPlayer1()) {
        message = "Player 1 made a move."
      } else {
        message = "Player 2 made a move"
      }

      this.messageSocketService.sendMessage(message);
    }

  }

  getWinner() {
    if (!this.gamePlaySocketService.gm.winner) {
      return "DRAW";
    }
    if (this.gamePlaySocketService.gm.winner.playerId == this.playerId) {
      return "YOU WON <3"
    } else {
      return "OPPONENT WON :("
    }
  }

  copyGameIdToClipboard() {
    navigator.clipboard.writeText(this.gamePlaySocketService.gm.gameId);
    window.alert("Game id copied");
  }

  checkMoveAllowed(stoneCount): boolean {
    if (this.gamePlaySocketService.gm.winner) {
      if (this.gamePlaySocketService.gm.winner.playerId == this.playerId) {
        window.alert("You win");
      } else {
        window.alert("Opponent win");
      }
      return false;
    }

    if (this.gamePlaySocketService.gm.gameStatus === "WAITING_OPPONENT") {
      window.alert("Waiting opponent");
      return false;
    }

    if ((this.playerId === this.gamePlaySocketService.gm.player1.playerId && !this.gamePlaySocketService.gm.player1Turn) ||
      (this.playerId === this.gamePlaySocketService.gm.player2.playerId && this.gamePlaySocketService.gm.player1Turn)) {
      window.alert("Opponent's turn");
      return false;
    }

    if (stoneCount === 0) {
      window.alert("Select a bucket with stones");
      return false;
    }

    if (this.gamePlaySocketService.gm.gameStatus === "OVER") {
      if (this.gamePlaySocketService.gm.board.boardStatus === "WIN") {
        if (this.gamePlaySocketService.gm.winner.playerId === this.playerId) {
          window.alert("Game is over. You win");
        } else {
          window.alert("Game is over. Opponent win");
        }
      } else {
        window.alert("Game is over.Draw");
      }
      return false;
    }

    return true;
  }
}
