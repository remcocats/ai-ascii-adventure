package dev.rabauer.ai_ascii_adventure.dto;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Npc extends AbstractCharacter {

    public Npc(String firstName, String lastName, String race, String klass) {
        super(firstName, lastName, race, klass, "npc");

    }

}
