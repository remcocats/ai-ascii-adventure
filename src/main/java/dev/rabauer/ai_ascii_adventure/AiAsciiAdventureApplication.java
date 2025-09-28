package dev.rabauer.ai_ascii_adventure;


import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Push
@SpringBootApplication
public class AiAsciiAdventureApplication implements AppShellConfigurator {

    public static void configureNettyNativeTransportFlag() {
        // Workaround: disable Netty native transports (kqueue on macOS) to avoid
        // "Unexpected exception in the selector loop. kevent(..) failed: Invalid argument"
        // See: https://github.com/netty/netty/issues (kqueue related) / affects macOS native transport
        // You can override at runtime with -Dio.netty.transport.noNative=false if needed.
        System.setProperty("io.netty.transport.noNative", "true");
    }

    public static void main(String[] args) {
        configureNettyNativeTransportFlag();
        SpringApplication.run(AiAsciiAdventureApplication.class, args);
    }

}
