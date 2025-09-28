package dev.rabauer.ai_ascii_adventure.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Npc extends AbstractCharacter {

    public Npc(String firstName, String lastName, String race, String klass) {
        super(firstName, lastName, race, klass, "npc");
    }

    @Builder
    public static Npc buildNpc(String firstName, String lastName, String race, String klass) {
        return new Npc(firstName, lastName, race, klass);
    }
}
