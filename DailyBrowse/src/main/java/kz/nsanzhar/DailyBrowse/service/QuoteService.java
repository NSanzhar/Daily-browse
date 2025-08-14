package kz.nsanzhar.DailyBrowse.service;

import kz.nsanzhar.DailyBrowse.Db.DBService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuoteService {
    private final DBService dbService;
    private final ServiceMenu serviceMenu;

    public void setQuote(String message, long chatId) {
        String[] parse = message.split(" ", 2);

        if (dbService.setQuote(parse[1], chatId)) {
            serviceMenu.sendMessage(chatId, "Вы успешно поставили цитату");
        } else
            serviceMenu.sendMessage(chatId, "Неверный формат");
    }

    public void checkDailyQuoteForAllUsers() {
        List<Long> allUserId = dbService.getAllIdUsers();

        for (Long chatId : allUserId) {
            String[] quotes = dbService.getDailyQuote(chatId);

            if (quotes == null) continue;

            serviceMenu.sendMessage(chatId, "Ваше вчерашний циататы: ");

            for (String quote : quotes) {
                serviceMenu.sendMessage(chatId, quote);
            }
        }
    }
}
