package dev.rabauer.ai_ascii_adventure.tools;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.progressbar.ProgressBar;
import dev.rabauer.ai_ascii_adventure.GameManager;
import dev.rabauer.ai_ascii_adventure.dto.Hero;
import org.springframework.ai.tool.annotation.Tool;

import java.util.List;

public class HeroUiCommunicator {

    private final Hero hero;
    private final ProgressBar prbHealth;
    private final ProgressBar prbMana;
    private final ProgressBar prbSpellSlots;
    private final Span spnInventory;
    private final Span spnWeapons;
    private final GameManager gameManager;

    public HeroUiCommunicator(Hero hero, ProgressBar prbHealth, ProgressBar prbMana, ProgressBar prbSpellSlots, Span spnInventory, Span spnWeapons, GameManager gameManager) {
        this.hero = hero;
        this.prbHealth = prbHealth;
        this.prbMana = prbMana;
        this.prbSpellSlots = prbSpellSlots;
        this.spnInventory = spnInventory;
        this.spnWeapons = spnWeapons;
        this.gameManager = gameManager;
    }

    @Tool(description = "Get the maximum health points of the hero as integer.")
    public Integer getMaxHealthHero() {
        return this.hero.getMaxHealth();
    }

    @Tool(description = "Get the health points of the hero, ranging from 0 (dead) to max health as integer.")
    public Integer getHealthHero() {
        return this.hero.getHealth();
    }

    @Tool(description = "Set the health points of the hero, ranging from 0 (dead) to max health as integer.")
    public void setHealthHero(Integer health) {
        this.hero.setHealth(health);
        this.prbHealth.getUI().ifPresent(
                ui -> ui.access(() -> {
                    this.prbHealth.setMax(this.hero.getMaxHealth());
                    this.prbHealth.setMin(0);
                    this.prbHealth.setValue(this.hero.getHealth());
                })
        );
        checkForDeath();
    }

    @Tool(description = "Get the maximum mana points of the hero as integer.")
    public Integer getMaxManaHero() {
        return this.hero.getMaxMana();
    }

    @Tool(description = "Get the mana points of the hero, ranging from 0 to max mana as integer.")
    public Integer getManaHero() {
        return this.hero.getMana();
    }

    @Tool(description = "Set the mana points of the hero, ranging from 0 to max mana as integer.")
    public void setManaHero(Integer mana) {
        this.hero.setMana(mana);
        this.prbHealth.getUI().ifPresent(
                ui -> ui.access(() -> {
                    this.prbMana.setMax(this.hero.getMaxMana());
                    this.prbMana.setMin(0);
                    this.prbMana.setValue(this.hero.getMana());
                })
        );
    }

    @Tool(description = "Get the maximum spell slots of the hero as integer.")
    public Integer getMaxSpellSlotsHero() {
        return this.hero.getMaxSpellSlots();
    }

    @Tool(description = "Get the spell slots of the hero, ranging from 0 to max spell slots as integer.")
    public Integer getSpellSlotsHero() {
        return this.hero.getSpellSlots();
    }

    @Tool(description = "Set the spell slots of the hero, ranging from 0 to max spell slots as integer.")
    public void setSpellSlotsHero(Integer spellSlots) {
        this.hero.setSpellSlots(spellSlots);
        this.prbHealth.getUI().ifPresent(
                ui -> ui.access(() -> {
                    this.prbSpellSlots.setMax(this.hero.getMaxSpellSlots());
                    this.prbSpellSlots.setMin(0);
                    this.prbSpellSlots.setValue(this.hero.getSpellSlots());
                })
        );
    }

    @Tool(description = "Get the full list of inventory items in the hero's inventory as list of strings.")
    public List<String> getInventoryHero() {
        return this.hero.getInventory();
    }

    @Tool(description = "Add one inventory item to the hero's inventory as string.")
    public void addInventoryHero(String newInventoryItem) {
        this.hero.addInventory(newInventoryItem);
        updateInventory();
    }

    @Tool(description = "Completely clear the hero's inventory.")
    public void clearInventoryHero() {
        this.hero.clearInventory();
        updateInventory();
    }

    @Tool(description = "Removes one inventory item from the hero's inventory as string.")
    public void removeInventoryHero(String inventoryItemToRemove) {
        this.hero.removeInventory(inventoryItemToRemove);
        updateInventory();
    }

    public void updateInventory() {
        spnInventory.getUI().ifPresent(ui -> ui.access(() ->
        {
            spnInventory.setText(String.join(", ", this.hero.getInventory()));
        }));
    }

    @Tool(description = "Get the full list of weapon items in the hero's weapons as list of strings.")
    public List<String> getWeaponsHero() {
        return this.hero.getWeapons();
    }

    @Tool(description = "Add one weapon item to the hero's weapons as string.")
    public void addWeaponHero(String newWeapon) {
        this.hero.addWeapon(newWeapon);
        updateWeapons();
    }

    @Tool(description = "Completely clear the hero's weapons.")
    public void clearWeaponsHero() {
        this.hero.clearWeapons();
        updateWeapons();
    }

    @Tool(description = "Removes one weapon item from the hero's weapons as string.")
    public void removeWeaponHero(String weaponToRemove) {
        this.hero.removeWeapon(weaponToRemove);
        updateWeapons();
    }

    public void updateWeapons() {
        this.spnWeapons.getUI().ifPresent(ui -> ui.access(() ->
        {
            this.spnWeapons.setText(String.join(", ", this.hero.getWeapons()));
        }));
    }

    @Tool(description = "The hero succeeded in this game.")
    private void winTheGameHero() {
        this.gameManager.showGameOver(false);
    }

    public void checkForDeath() {
        if (this.hero.getHealth() <= 0) {
            this.gameManager.showGameOver(true);
        }
    }
}
