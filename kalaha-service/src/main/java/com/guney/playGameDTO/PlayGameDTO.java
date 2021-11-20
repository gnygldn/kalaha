package com.guney.playGameDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
@Getter
public class PlayGameDTO {

    private String gameId;
    private String playerId;
    private int bucketIndex;
    private boolean justUpdate;
}
