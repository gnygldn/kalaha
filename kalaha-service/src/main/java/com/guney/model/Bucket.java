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

    public int addStone(int i) {
        stoneCount += i;
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
