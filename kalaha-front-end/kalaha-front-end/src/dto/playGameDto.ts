export class PlayGameDTO {

    constructor(
        public gameId: string,
        public playerId: string,
        public bucketIndex: number,
        public justUpdate: boolean
    ) {

    }
}