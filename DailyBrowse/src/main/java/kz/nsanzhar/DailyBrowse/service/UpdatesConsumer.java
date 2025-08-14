package kz.nsanzhar.DailyBrowse.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class UpdatesConsumer implements LongPollingSingleThreadUpdateConsumer {
    private final ServiceMenu serviceMenu;
    private final DailyReadingService dailyReadingService;
    private final QuoteService quoteService;

    @SneakyThrows
    @Override
    public void consume(Update update) {
        if (update.hasMessage()) {
            long chatId = update.getMessage().getChatId();
            String message = update.getMessage().getText();

            switch (message) {
                case "/start" -> serviceMenu.mainMenu(chatId);
            }

            if (message.startsWith("/addBook")) {
                serviceMenu.addBook(chatId, message);
            } else if (message.startsWith("/removeBook")) {
                serviceMenu.removeBook(chatId, message);
            } else if (message.startsWith("/addPage")) {
                serviceMenu.addPage(chatId, message);
            } else if (message.startsWith("/getPage")) {
                serviceMenu.getPage(chatId, message);
            } else if (message.startsWith("/setDailyGoal")) {
                dailyReadingService.setDailyGoal(chatId, message);
            } else if (message.startsWith("/setQuote")) {
                quoteService.setQuote(message, chatId);
            }

        } else if (update.hasCallbackQuery()) {
            handleCallbackQuery(update.getCallbackQuery());
        }
    }

    @SneakyThrows
    private void handleCallbackQuery(CallbackQuery callbackQuery) {
        long chatId = callbackQuery.getFrom().getId();

        switch (callbackQuery.getData()) {
            case "account" -> serviceMenu.registerAccount(chatId);
            case "allBooks" -> serviceMenu.allBooks(chatId);
            case "help" -> serviceMenu.help(chatId);
            case "stats" -> serviceMenu.readingStats(chatId);
        }
    }
}
