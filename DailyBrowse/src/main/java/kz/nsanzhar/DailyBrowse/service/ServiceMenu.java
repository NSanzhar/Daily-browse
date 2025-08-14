package kz.nsanzhar.DailyBrowse.service;

import kz.nsanzhar.DailyBrowse.Db.DBService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.sql.SQLException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceMenu {
    private final DBService dbService;
    private final TelegramClient telegramClient;

    @SneakyThrows
    public void sendMessage(long chatId, String message) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(message)
                .build();

        telegramClient.execute(sendMessage);
    }

    @SneakyThrows
    public void sendMessage(long chatId, String message, InlineKeyboardMarkup markup) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(message)
                .replyMarkup(markup)
                .build();

        telegramClient.execute(sendMessage);
    }

    public void mainMenu(long chatId) {
        InlineKeyboardButton button1 = InlineKeyboardButton.builder()
                .text("Создать аккаунт")
                .callbackData("account")
                .build();

        InlineKeyboardButton button2 = InlineKeyboardButton.builder()
                .text("Все книги")
                .callbackData("allBooks")
                .build();

        InlineKeyboardButton button3 = InlineKeyboardButton.builder()
                .text("Помощь")
                .callbackData("help")
                .build();

        InlineKeyboardButton button4 = InlineKeyboardButton.builder()
                .text("\uD83D\uDCDA Статистика чтения")
                .callbackData("stats")
                .build();

        List<InlineKeyboardRow> keyboardRows = List.of(
            new InlineKeyboardRow(button1),
            new InlineKeyboardRow(button2),
            new InlineKeyboardRow(button3),
            new InlineKeyboardRow(button4)
        );

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(keyboardRows);

        sendMessage(chatId, "Здраствуйте\n" +
                "————————————————————————————\n" +
                "/addBook *назв книги* *общее колличество стр*\n" +
                "/removeBook *название книги*" +
                "————————————————————————————\n" +
                "/addPage *назв книги* *сколько страниц прочитали*\n" +
                "/getPage *назв книги*" +
                "————————————————————————————\n" +
                "/setDailyGoal *установить дневную цель*\n" +
                "/setQuote *Написать цитату*", markup);
    }

    public void addBook(long chatId, String message) throws SQLException {
        try {
            String[] parse = message.split(" ", 2);
            if (parse.length < 2) {
                sendMessage(chatId, "Неверный формат. Пример: /addBook Название книги 300");
                return;
            }

            String[] nameAndPage = parse[1].trim().split(" ");
            if (nameAndPage.length < 2) {
                sendMessage(chatId, "Укажите название и количество страниц. Пример: /addBook Название книги 300");
                return;
            }

            int max_page = Integer.parseInt(nameAndPage[nameAndPage.length - 1]);
            StringBuilder nameBuilder = new StringBuilder();
            for (int i = 0; i < nameAndPage.length - 1; i++) {
                nameBuilder.append(nameAndPage[i]);
                if (i != nameAndPage.length - 2) {
                    nameBuilder.append(" ");
                }
            }
            String name = nameBuilder.toString();

            boolean success = dbService.addBook(chatId, name, max_page);
            if (success) {
                sendMessage(chatId, "Вы успешно добавили книгу!");
            } else {
                sendMessage(chatId, "Книга уже существует");
            }
        } catch (NumberFormatException e){
            sendMessage(chatId, "Последний аргумент должен быть числом — количеством страниц");
        }
    }

    public void removeBook(long chatId, String message) throws SQLException {
        String[] parse = message.split(" ", 2);
        if (parse.length < 2 || parse[1].trim().isEmpty()) {
            sendMessage(chatId, "Неверный формат. Пример: /removeBook Название книги");
            return;
        }

        if (dbService.removeBook(parse[1].trim())){
            sendMessage(chatId, "Вы успешно удалили книгу!");
        } else {
            sendMessage(chatId, "Книга с таким названием не найдена.");
        }
    }

    public void registerAccount(long chatId) throws SQLException {
        Boolean success = dbService.regisrtyAccount(chatId);
        if (success) {
        sendMessage(chatId, "Успешно зарегестрирован аккаунт");
        } else {
            sendMessage(chatId, "Ваш аккаунт уже существует");
        }
    }

    public void addPage (long chatId, String message) {
        try {
            String[] parse = message.split(" ", 2);
            if (parse.length < 2) {
                sendMessage(chatId, "Неверный формат. Пример: /addPage Название книги 300");
                return;
            }

            String[] nameAndPage = parse[1].trim().split(" ");
            if (nameAndPage.length < 2) {
                sendMessage(chatId,"Укажите название и количество страниц. Пример: /addPage Название книги 300");
                return;
            }

            int maxPage = Integer.parseInt(nameAndPage[nameAndPage.length - 1]);

            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < nameAndPage.length - 1; i++) {
                stringBuilder.append(nameAndPage[i]);

                if (i != nameAndPage.length - 2) {
                    stringBuilder.append(" ");
                }
            }
            String name = stringBuilder.toString();

            boolean result = dbService.addPage(name, maxPage, chatId);
            if (result) {
                sendMessage(chatId, "Успешно обновлена страница!");
            } else {
                sendMessage(chatId, "Не удалось обновить. Убедитесь, что книга существует или страницы не привышают максимум");
            }
        } catch (NumberFormatException e) {
            sendMessage(chatId, "Последний аргумент должен быть числом — количеством страниц");
        }
    }

    public void getPage(long chatId, String message) throws SQLException {
        String[] parse = message.split(" ", 2);
        if (parse.length < 2) {
            sendMessage(chatId, "Неверный формат. Пример: /getPage Название книги");
            return;
        }
        String title = parse[1].trim();
        int page = dbService.getPage(title, chatId);

        if (page >= 0) {
            sendMessage(chatId, "Вы прочитали \"" + title + "\" на: " + page + " стр.");
        } else {
            sendMessage(chatId, "Книга не найдена.");
        }
    }

    public void help(long chatId) {
        sendMessage(chatId, "/addBook - Добавить книгу\n" +
                                    "/removeBook - Удалить книгу\n" +
                                    "————————————————————————————\n" +
                                    "/addPage - Добавить прочитанную страницу\n" +
                                    "/getPage - Узнать текущую страницу книги" +
                                    "————————————————————————————\n" +
                                    "/setDailyGoal - Установить количество страниц для чтения в день (0 - не указывать)\n" +
                                    "/setQuote - Установить цитаты, которые придут на следующий день");
    }

    public void readingStats (long chatId) {
        sendMessage(chatId, dbService.getStatsForMonthAndWeak(chatId));
    }

    public void allBooks (long chatId) {
        String[] books = dbService.allBooks(chatId);

        for (String book : books) {
            sendMessage(chatId, book);
        }
    }
}
