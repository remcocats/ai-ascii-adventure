package dev.rabauer.ai_ascii_adventure.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

@Slf4j
public class DiceTool {

    @Tool(description = "Roll the dice.")
    public int roll(@ToolParam(description = "amount of dice sides") int sides) {
        var number = (int) (Math.random() * sides) + 1;
        log.info("Rolled a {} on a {} sided dice.", number, sides);
        return number;
    }
}
