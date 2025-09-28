package dev.rabauer.ai_ascii_adventure.tools;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DiceToolTest {

    private final DiceTool diceTool = new DiceTool();

    @RepeatedTest(100)
    void roll_d6_is_between_1_and_6() {
        int result = diceTool.roll(6);
        assertThat(result).isBetween(1, 6);
    }

    @RepeatedTest(100)
    void roll_d20_is_between_1_and_20() {
        int result = diceTool.roll(20);
        assertThat(result).isBetween(1, 20);
    }

    @Test
    void roll_minimum_is_1() {
        // try many times; should never be 0
        for (int i = 0; i < 1000; i++) {
            int r = diceTool.roll(8);
            assertThat(r).isGreaterThanOrEqualTo(1);
        }
    }

    @Test
    void roll_maximum_is_sides() {
        for (int i = 0; i < 1000; i++) {
            int r = diceTool.roll(12);
            assertThat(r).isLessThanOrEqualTo(12);
        }
    }
}
