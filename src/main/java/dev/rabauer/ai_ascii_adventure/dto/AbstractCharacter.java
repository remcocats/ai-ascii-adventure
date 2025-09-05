package dev.rabauer.ai_ascii_adventure.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
public abstract class AbstractCharacter {
    private final String firstName;
    private final String lastName;
    private final String race;
    private final String klass;
    private final String role;
    private Integer health;
    private Integer maxHealth;
    private Integer mana;
    private Integer maxMana;
    private Integer spellSlots;
    private Integer maxSpellSlots;
    private final List<String> inventory = new ArrayList<>();
    private final List<String> weapons = new ArrayList<>();


    public AbstractCharacter(String firstName, String lastName, String race, String klass, String role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.race = race;
        this.klass = klass;
        this.role = role;
        this.maxHealth = 100;
        this.health = maxHealth;
        this.maxMana = 50;
        this.mana = maxMana;
        this.maxSpellSlots = 4;
        this.spellSlots = maxSpellSlots;
    }

    public String getName() {
        return this.firstName + " " + this.lastName;
    }

    public void addInventory(String newInventoryItem) {
        this.inventory.add(newInventoryItem);
    }

    public void clearInventory() {
        this.inventory.clear();
    }

    public void removeInventory(String inventoryItemToRemove) {
        this.inventory.remove(inventoryItemToRemove);
    }

    public void clearWeapons() {
        this.weapons.clear();
    }

    public void addWeapon(String newWeapon) {
        this.weapons.add(newWeapon);
    }

    public void removeWeapon(String weaponToRemove) {
        this.weapons.remove(weaponToRemove);
    }

}
