package dev.rabauer.ai_ascii_adventure.dto;

import java.util.ArrayList;
import java.util.List;

public class Hero {
    private final String name;
    private Integer health;
    private Integer maxHealth;
    private Integer mana;
    private Integer maxMana;
    private final List<String> inventory = new ArrayList<>();


    public Hero(String name) {
        this.name = name;
        this.maxHealth = 100;
        this.health = maxHealth;
        this.maxMana = 50;
        this.mana = maxMana;
    }

    public String getName() {
        return name;
    }

    public Integer getHealth() {
        return health;
    }

    public void setHealth(Integer health) {
        this.health = health;
    }

    public Integer getMana() {
        return mana;
    }

    public void setMana(Integer mana) {
        this.mana = mana;
    }

    public Integer getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(Integer maxHealth) {
        this.maxHealth = maxHealth;
    }

    public Integer getMaxMana() {
        return maxMana;
    }

    public void setMaxMana(Integer maxMana) {
        this.maxMana = maxMana;
    }


    public List<String> getInventory() {
        return inventory;
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
}
