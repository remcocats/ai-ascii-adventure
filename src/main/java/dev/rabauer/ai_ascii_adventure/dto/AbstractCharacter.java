package dev.rabauer.ai_ascii_adventure.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        this.firstName = requireText(firstName, "firstName");
        this.lastName = requireText(lastName, "lastName");
        this.race = requireText(race, "race");
        this.klass = requireText(klass, "klass");
        this.role = requireText(role, "role");
        this.maxHealth = 100;
        this.health = maxHealth;
        this.maxMana = 50;
        this.mana = maxMana;
        this.maxSpellSlots = 4;
        this.spellSlots = maxSpellSlots;
    }

    private static String requireText(String v, String name) {
        String s = Objects.requireNonNull(v, name).trim();
        if (s.isEmpty()) throw new IllegalArgumentException(name + " must not be blank");
        return s;
    }

    public String getName() {
        return this.firstName + " " + this.lastName;
    }

    public void addInventory(String newInventoryItem) {
        if (!newInventoryItem.isBlank()) {
            this.inventory.add(newInventoryItem.trim());
        }
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
        if (!newWeapon.isBlank()) {
            this.weapons.add(newWeapon.trim());
        }
    }

    public void removeWeapon(String weaponToRemove) {
        this.weapons.remove(weaponToRemove);
    }

    // Invariants: clamp mutable stats within [0, max]
    public void setHealth(Integer health) {
        int h = health;
        int max = this.maxHealth == null ? 0 : this.maxHealth;
        this.health = Math.max(0, Math.min(h, Math.max(0, max)));
    }

    public void setMaxHealth(Integer maxHealth) {
        int max = Math.max(0, maxHealth);
        this.maxHealth = max;
        // adjust current health if above new max
        if (this.health != null && this.health > max) this.health = max;
    }

    public void setMana(Integer mana) {
        int m = mana;
        int max = this.maxMana == null ? 0 : this.maxMana;
        this.mana = Math.max(0, Math.min(m, Math.max(0, max)));
    }

    public void setMaxMana(Integer maxMana) {
        int max = Math.max(0, maxMana);
        this.maxMana = max;
        if (this.mana != null && this.mana > max) this.mana = max;
    }

    public void setSpellSlots(Integer spellSlots) {
        int s = spellSlots;
        int max = this.maxSpellSlots == null ? 0 : this.maxSpellSlots;
        this.spellSlots = Math.max(0, Math.min(s, Math.max(0, max)));
    }

    public void setMaxSpellSlots(Integer maxSpellSlots) {
        int max = Math.max(0, maxSpellSlots);
        this.maxSpellSlots = max;
        if (this.spellSlots != null && this.spellSlots > max) this.spellSlots = max;
    }
}
