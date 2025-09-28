package dev.rabauer.ai_ascii_adventure;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AiAsciiAdventureApplicationMainTest {

    @AfterEach
    void resetProperty() {
        System.clearProperty("io.netty.transport.noNative");
    }

    @Test
    void main_sets_netty_native_flag_to_true() {
        // Ensure it is not set before
        System.clearProperty("io.netty.transport.noNative");
        AiAsciiAdventureApplication.configureNettyNativeTransportFlag();
        String flag = System.getProperty("io.netty.transport.noNative");
        assertThat(flag).isEqualTo("true");
    }
}
