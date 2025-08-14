package kz.nsanzhar.DailyBrowse.service;

import kz.nsanzhar.DailyBrowse.Db.DBService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DailyReadingService {
    private final DBService dbService;
    private final ServiceMenu serviceMenu;

    public void setDailyGoal(long chatId, String message) {
        String[] parse = message.split(" ");
        if (parse.length == 2){
            dbService.setDailyGoal(Integer.parseInt(parse[1]), chatId);
            serviceMenu.sendMessage(chatId, "Вы успешно обновили цель");
        } else {
            serviceMenu.sendMessage(chatId, "Введеные данные некоректны");
        }
    }

    public void checkDailyProgressForAllUsers() {
        List<Long> allUserId = dbService.getAllIdUsers();

        for (Long chatId : allUserId) {
            int read = dbService.getDailyPagesRead(chatId);
            int goal = dbService.getDailyGoal(chatId);

            if (goal == 0) continue;

            if (read >= goal) {
                serviceMenu.sendMessage(chatId, "Поздравляю вы выполнили ежедневную цель! Вы прочитали: " + read);
            } else {
                serviceMenu.sendMessage(chatId, "Вы прочитали " + read + "стр. из " + goal + "стр. сегодня");
            }
        }
    }
}
