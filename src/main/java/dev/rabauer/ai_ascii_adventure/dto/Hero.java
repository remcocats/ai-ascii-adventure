package dev.rabauer.ai_ascii_adventure.dto;

import lombok.*;


@EqualsAndHashCode(callSuper = true)
public class Hero extends AbstractCharacter {

    public Hero(String firstName, String lastName, String race, String klass) {
        super(firstName, lastName, race, klass, "hero");
    }

}
