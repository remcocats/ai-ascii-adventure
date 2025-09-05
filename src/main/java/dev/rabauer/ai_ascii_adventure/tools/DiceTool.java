package dev.rabauer.ai_ascii_adventure.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

@Slf4j
public class DiceTool {

    @Tool(description = "Roll a die with the given number of sides (e.g., 20 for d20). Use for checks, saves, attacks, or damage. Returns an integer between 1 and 'sides'.")
    public int roll(@ToolParam(description = "number of sides on the die, e.g., 20 for d20, 6 for d6") int sides) {
        var number = (int) (Math.random() * sides) + 1;
        log.info("Rolled a {} on a {} sided dice.", number, sides);
        return number;
    }
}
