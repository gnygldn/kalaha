import { PlayerModel } from "./player";
import { BoardModel } from "./board";


export class GameModel {

    constructor(
        public gameId: string,
        public player1: PlayerModel,
        public player2: PlayerModel,
        public board: BoardModel,
        public gameStatus: any,
        public privateGame: boolean,
        public player1Turn: boolean,
        public winner: PlayerModel

    ) {

    }
}