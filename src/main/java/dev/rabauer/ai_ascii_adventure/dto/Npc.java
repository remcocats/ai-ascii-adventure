package dev.rabauer.ai_ascii_adventure.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
public class Npc extends AbstractCharacter {

    public Npc(String firstName, String lastName, String race, String klass) {
        super(firstName, lastName, race, klass, "npc");

    }

}
