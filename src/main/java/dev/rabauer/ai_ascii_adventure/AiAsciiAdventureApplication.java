package dev.rabauer.ai_ascii_adventure;


import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.shared.ui.Transport;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Push
@SpringBootApplication
public class AiAsciiAdventureApplication implements AppShellConfigurator {

	public static void main(String[] args) {
		SpringApplication.run(AiAsciiAdventureApplication.class, args);
	}

}
