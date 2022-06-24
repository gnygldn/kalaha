package com.guney.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Board {

    private List<Bucket> player1Buckets = new ArrayList<>();
    private Bucket player1Pool = new Bucket(0);
    private List<Bucket> player2Buckets = new ArrayList<>();
    private Bucket player2Pool = new Bucket(0);
    private int defaultStoneCount;
    private int defaultBoxCount;
    private BoardStatus boardStatus;

    public Board(int defaultStoneCount, int defaultBoxCount) {
        this.defaultStoneCount = defaultStoneCount;
        this.defaultBoxCount = defaultBoxCount;
        this.boardStatus = BoardStatus.IN_PROGRESS;
        for (int i = 0; i < defaultBoxCount; i++) {
            player1Buckets.add(new Bucket(defaultStoneCount));
            player2Buckets.add(new Bucket(defaultStoneCount));
        }
    }

    /**
     * Takes player1 stones as parameter and places all stones to regarding pools.
     *
     * @param stoneCount takes the last stone count of player1
     */
    public void setEndStateStonesByPlayer1StoneCount(int stoneCount) {
        player1Pool.setStoneCount(stoneCount);
        player2Pool.setStoneCount((defaultStoneCount * defaultBoxCount * 2) - stoneCount);
        player1Buckets.forEach(bucket -> bucket.setStoneCount(0));
        player2Buckets.forEach(bucket -> bucket.setStoneCount(0));
    }
}
