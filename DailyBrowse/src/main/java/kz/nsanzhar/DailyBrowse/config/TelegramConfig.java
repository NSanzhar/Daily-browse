package kz.nsanzhar.DailyBrowse.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Configuration
@RequiredArgsConstructor
public class TelegramConfig {
    private final BotConfig config;

    @Bean
    public TelegramClient telegramClient () {
        return new OkHttpTelegramClient(config.getToken());
    }
}
