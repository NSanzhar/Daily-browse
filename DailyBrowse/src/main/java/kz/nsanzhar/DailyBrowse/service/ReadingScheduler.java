package kz.nsanzhar.DailyBrowse.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReadingScheduler {
    private final DailyReadingService dailyReadingService;
    private final QuoteService quoteService;

    @Scheduled(cron = "0 0 16,19 * * *")
    public void checkDailyReading() {
        dailyReadingService.checkDailyProgressForAllUsers();
        quoteService.checkDailyQuoteForAllUsers();
    }
}
