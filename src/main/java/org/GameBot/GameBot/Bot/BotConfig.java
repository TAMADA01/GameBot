package org.GameBot.GameBot.Bot;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@Configuration
@Data
@PropertySource("application.properties")
public class BotConfig {
    @Value("${bot.name}")
    String name;
    @Value("${bot.token}")
    String token;

    @Value("${db.host}")
    private String _host;
    @Value("${db.password}")
    private String _password;
    @Value("${db.name}")
    private String _name;
    @Value("${db.user}")
    private String _user;
}
