package com.guney.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class Bucket {

    private int stoneCount;

    public Bucket(int stoneCount) {

        this.stoneCount = stoneCount;
    }

    /**
     * @param addedStoneCount amount of the stones to be added to bucket
     * @return returns the stone count after addition
     */
    public int addStone(int addedStoneCount) {
        stoneCount += addedStoneCount;
        return stoneCount;
    }

    /**
     * sets stoneCount of the bucket to 0
     *
     * @return returns the stoneCount before removing them
     */
    public int removeAllStones() {
        int returnVal = stoneCount;
        stoneCount = 0;
        return returnVal;
    }

}
