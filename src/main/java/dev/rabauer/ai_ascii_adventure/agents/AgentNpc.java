package dev.rabauer.ai_ascii_adventure.agents;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import dev.rabauer.ai_ascii_adventure.ai.AbstractAgentExecutor;
import org.bsc.langgraph4j.GraphStateException;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class AgentNpc extends AbstractAgentExecutor<AgentNpc.Builder> {

    static class Tools {
        record Npc(
                @JsonPropertyDescription("the npc name") String name,
                @JsonPropertyDescription("the npc rise") String rise,
                @JsonPropertyDescription("the npc health") String health) {}

        @Tool( description="create a specific npc")
        Npc createANpc(@ToolParam( description="the npc name") String name, @ToolParam( description="the npc rise") String rise, @ToolParam( description="the npc health") String health ) {
            return new Npc( name, rise, health );
        }

        @Tool(description = "let the npc talk")
        String talkToNpc(@ToolParam(description = "the npc name") String name) {
            return "Hello " + name + "!";
        }

    }

    public static class Builder extends AbstractAgentExecutor.Builder<AgentNpc.Builder> {

        public AgentNpc build() throws GraphStateException {
            this.name("npc")
                    .description("npc agent, ask for what would the npc do")
                    .parameterDescription("all information request about the products")
                    .defaultSystem( """
                    You are the agent that provide tools to interact with the NPC.
                """)
                    .toolsFromObject( new Tools() )
            ;

            return new AgentNpc( this );
        }

    }

    public static Builder builder() {
        return new Builder();
    }

    protected AgentNpc(Builder builder) throws GraphStateException {
        super(builder);


    }


}
