package dev.rabauer.ai_ascii_adventure.tools;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import dev.rabauer.ai_ascii_adventure.dto.Npc;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class NpcUiCommunicator  {

    private VerticalLayout verticalLayout;

    public NpcUiCommunicator(VerticalLayout verticalLayout)  {
        this.verticalLayout = verticalLayout;
    }

    @Tool(description = "Get the maximum health points of the npc as integer.")
    public Integer getMaxHealthNpc(@ToolParam(description = "the npc")Npc npc) {
        return npc.getMaxHealth();
    }

    @Tool(description = "Get the health points of the npc, ranging from 0 (dead) to max health as integer.")
    public Integer getHealthNpc(@ToolParam(description = "the npc") Npc npc) {
        return npc.getHealth();
    }

    @Tool(description = "Set the health points of the npc, ranging from 0 (dead) to max health as integer.")
    public void setHealthNpc(@ToolParam(description = "the npc") Npc npc, Integer health) {
        npc.setHealth(health);
    }

    @Tool(description = "Get the maximum mana points of the npc as integer.")
    public Integer getMaxManaNpc(@ToolParam(description = "the npc") Npc npc) {
        return npc.getMaxMana();
    }

    @Tool(description = "Get the mana points of the npc, ranging from 0 to max mana as integer.")
    public Integer getManaNpc(@ToolParam(description = "the npc") Npc npc) {
        return npc.getMana();
    }

    @Tool(description = "Set the mana points of the npc, ranging from 0 to max mana as integer.")
    public void setManaNpc(@ToolParam(description = "the npc") Npc npc, Integer mana) {
        npc.setMana(mana);
    }

    @Tool(description = "Get the maximum spell slots of the npc as integer.")
    public Integer getMaxSpellSlotsNpc(@ToolParam(description = "the npc") Npc npc) {
        return npc.getMaxSpellSlots();
    }

    @Tool(description = "Get the spell slots of the npc, ranging from 0 to max spell slots as integer.")
    public Integer getSpellSlotsNpc(@ToolParam(description = "the npc") Npc npc) {
        return npc.getSpellSlots();
    }

    @Tool(description = "Set the spell slots of the npc, ranging from 0 to max spell slots as integer.")
    public void setSpellSlotsNpc(@ToolParam(description = "the npc") Npc npc, Integer spellSlots) {
        npc.setSpellSlots(spellSlots);
    }

    @Tool(description = "Get the full list of inventory items in the npc's inventory as list of strings.")
    public List<String> getInventoryNpc(@ToolParam(description = "the npc") Npc npc) {
        return npc.getInventory();
    }

    @Tool(description = "Add one inventory item to the npc's inventory as string.")
    public void addInventoryNpc(@ToolParam(description = "the npc") Npc npc, String newInventoryItem) {
        npc.addInventory(newInventoryItem);
    }

    @Tool(description = "Completely clear the npc's inventory.")
    public void clearInventoryNpc(@ToolParam(description = "the npc") Npc npc) {
        npc.clearInventory();
    }

    @Tool(description = "Removes one inventory item from the npc's inventory as string.")
    public void removeInventoryNpc(@ToolParam(description = "the npc") Npc npc, String inventoryItemToRemove) {
        npc.removeInventory(inventoryItemToRemove);
    }

    @Tool(description = "Get the full list of weapon items in the npc's weapons as list of strings.")
    public List<String> getWeaponsNpc(@ToolParam(description = "the npc") Npc npc) {
        return npc.getWeapons();
    }

    @Tool(description = "Add one weapon item to the npc's weapons as string.")
    public void addWeaponNpc(@ToolParam(description = "the npc") Npc npc, String newWeapon) {
        npc.addWeapon(newWeapon);
        updateWeapons(npc);
    }

    @Tool(description = "Completely clear the npc's weapons.")
    public void clearWeaponsNpc(@ToolParam(description = "the npc") Npc npc) {
        npc.clearWeapons();
        updateWeapons(npc);
    }

    @Tool(description = "Removes one weapon item from the npc's weapons as string.")
    public void removeWeaponNpc(@ToolParam(description = "the npc") Npc npc, String weaponToRemove) {
        npc.removeWeapon(weaponToRemove);
        updateWeapons(npc);
    }

    private void updateWeapons(Npc npc) {
        getElement(npc.getFirstName()).ifPresent(
                element -> element.getChildren()
                        .filter(Span.class::isInstance)
                        .filter(span -> { try {
                            span.getElement().getAttribute("weapons");
                            return false;
                        } catch (Exception e) {
                            return true;
                        }
                        })
                        .findFirst()
                        .ifPresentOrElse(span ->
                            span.getElement().setText(String.join("", npc.getWeapons()))
                        , () ->
                                {
                                    Span weaponSpan = new Span(String.join("", npc.getWeapons()));
                                    weaponSpan.getElement().setAttribute("weapons", "true");
                                    element.add(weaponSpan);
                                }
                        )
                        );
    }

    private Optional<Span> getElement(String firstName) {
        try {
            return verticalLayout.getChildren()
                    .map(Span.class::cast)
                    .filter(component -> Objects.equals(firstName, component.getElement().getAttribute("firstName")))
                    .findFirst();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

}
