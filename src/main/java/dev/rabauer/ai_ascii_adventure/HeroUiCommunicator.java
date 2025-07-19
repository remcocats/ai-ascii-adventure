package dev.rabauer.ai_ascii_adventure;

import com.vaadin.flow.component.progressbar.ProgressBar;
import dev.rabauer.ai_ascii_adventure.dto.Hero;
import org.springframework.ai.tool.annotation.Tool;

public class HeroUiCommunicator {

    private final Hero hero;
    private final ProgressBar healthBar;
    private final ProgressBar manaBar;

    public HeroUiCommunicator(Hero hero, ProgressBar healthBar, ProgressBar manaBar) {
        this.hero = hero;
        this.healthBar = healthBar;
        this.manaBar = manaBar;
    }

    @Tool(description = "Get the health points of the hero, ranging from 0 (dead) to max health as integer.")
    public Integer getHealth() {
        return hero.getHealth();
    }

    @Tool(description = "Set the health points of the hero, ranging from 0 (dead) to max health as integer.")
    public void setHealth(Integer health) {
        this.hero.setHealth(health);
        this.healthBar.getUI().ifPresent(
                ui -> ui.access(() -> {
                    this.healthBar.setMax(this.hero.getMaxHealth());
                    this.healthBar.setMin(0);
                    this.healthBar.setValue(this.hero.getHealth());
                })
        );
    }

    @Tool(description = "Get the mana points of the hero, ranging from 0 to max mana as integer.")
    public Integer getMana() {
        return hero.getMana();
    }

    @Tool(description = "Set the mana points of the hero, ranging from 0 to max mana as integer.")
    public void setMana(Integer mana) {
        this.hero.setMana(mana);
        this.healthBar.getUI().ifPresent(
                ui -> ui.access(() -> {
                    this.manaBar.setMax(this.hero.getMaxMana());
                    this.manaBar.setMin(0);
                    this.manaBar.setValue(this.hero.getMana());
                })
        );
    }
}
