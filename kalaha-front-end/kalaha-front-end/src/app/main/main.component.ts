import { Component } from '@angular/core';
import { NavigationExtras, Router } from '@angular/router';
import { GameModel } from 'src/model/game';
import { GameService } from '../services/game.service';


@Component({
  selector: 'app-main',
  templateUrl: './main.component.html',
  styleUrls: ['./main.component.scss']
})
export class MainComponent {

  public gm: GameModel;

  constructor(
    private router: Router,
    private gameService: GameService

  ) {

  }

  createGame(name: string, privateGame: boolean, stoneCount, boxCount) {

    if (name.trim().length === 0) {
      window.alert("Name cannot be empty");
      return;
    }

    this.gameService.createGame(name, privateGame, stoneCount, boxCount).subscribe((data) => {
      let gm = <GameModel>JSON.parse(JSON.stringify(data));
      let navigationExtras: NavigationExtras = {
        state: {
          game: gm,
          player: gm.player1.playerId
        }
      };
      this.router.navigateByUrl('game/' + gm.gameId, navigationExtras);
    });

  }

  joinRandomGame(name: string) {

    if (name.trim().length === 0) {
      window.alert("Name cannot be empty");
      return;
    }

    this.gameService.joinRandomGame(name).subscribe((data) => {
      let gm = <GameModel>JSON.parse(JSON.stringify(data));
      let navigationExtras: NavigationExtras = {
        state: {
          game: gm,
          player: gm.player2.playerId
        }
      };
      this.router.navigateByUrl('game/' + gm.gameId, navigationExtras);
    });
  }

  joinPrivateGame(gameId: string, name: string) {

    if (name.trim().length === 0) {
      window.alert("Name cannot be empty");
      return;
    }
    if (gameId.trim().length === 0) {
      window.alert("Game id cannot be empty");
    }

    this.gameService.joinPrivateGame(gameId, name).subscribe((data) => {
      console.log(data);
      let gm = <GameModel>JSON.parse(JSON.stringify(data));
      let navigationExtras: NavigationExtras = {
        state: {
          game: gm,
          player: gm.player2.playerId
        }
      };
      this.router.navigateByUrl('game/' + gm.gameId, navigationExtras);
    });
  }

}
