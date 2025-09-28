package dev.rabauer.ai_ascii_adventure.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Hero extends AbstractCharacter {

    public Hero(String firstName, String lastName, String race, String klass) {
        super(firstName, lastName, race, klass, "hero");
    }

    @Builder
    public static Hero buildHero(String firstName, String lastName, String race, String klass) {
        return new Hero(firstName, lastName, race, klass);
    }
}
