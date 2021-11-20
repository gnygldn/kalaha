import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http"

@Injectable({
    providedIn: 'root'
})

export class GameService {

    constructor(public http: HttpClient) { }

    createGame(playerName: string, privateGame: boolean, stoneCount: string, boxCount: string) {

        const fd = new FormData();
        fd.append('name', playerName);
        fd.append('privateGame', privateGame.toString());
        fd.append('stoneCount', stoneCount);
        fd.append('boxCount', boxCount);
        return this.http.post<string>('http://localhost:8080/api/kalaha/create', fd).pipe();
    }

    joinRandomGame(name: string) {
        const fd = new FormData();
        fd.append('name', name);

        return this.http.post<string>('http://localhost:8080/api/kalaha/joinRandom', fd).pipe();
    }

    joinPrivateGame(gameId:string, name: string) {
        const fd = new FormData();
        fd.append('gameId', gameId)
        fd.append('name', name);

        return this.http.post<string>('http://localhost:8080/api/kalaha/joinPrivate', fd).pipe();
    }

    play(gameId: string, player: string, bucketIndex) {
        const fd = new FormData();
        fd.append('gameId', gameId);
        fd.append('playerId', player);
        fd.append('bucketIndex', bucketIndex);

        return this.http.post<string>('http://localhost:8080/api/kalaha/playGame', fd).pipe();
    }

    getGame(gameId: string) {
        return this.http.get<string>('http://localhost:8080/api/kalaha/gameInfo/' + gameId).pipe();
    }
}