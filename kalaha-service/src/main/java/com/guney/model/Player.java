package com.guney.model;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
@Builder
public class Player {

    private String playerId;
    private String name;

}
