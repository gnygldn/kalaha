import { BucketModel } from "./bucket";

export class BoardModel {

    constructor(
        public player1Buckets: BucketModel[],
        public player1Pool: BucketModel,
        public player2Buckets: BucketModel[],
        public player2Pool: BucketModel,
        public defaultStoneCount: number,
        public defaultBoxCount: number,
        public boardStatus: any
    ) {

    }
}