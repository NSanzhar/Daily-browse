package kz.nsanzhar.DailyBrowse.Db;

import kz.nsanzhar.DailyBrowse.config.DbConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DBService {
    private final DbConfig dbConfig;

    public boolean addBook(long chatId, String title, int page) {
        try (Connection connection = DriverManager.getConnection(
                dbConfig.getUrl(), dbConfig.getUser(), dbConfig.getPassword())) {

            PreparedStatement ps = connection.prepareStatement("INSERT INTO daily_browse.book(user_chat_id, title, max_page) VALUES(?, ?, ?)");
            ps.setLong(1, chatId);
            ps.setString(2, title);
            ps.setInt(3, page);
            ps.execute();
            return true;
        } catch (SQLException e) {
            if (e.getSQLState().startsWith("23")) {
                return false;
            }
            e.printStackTrace();
            return false;
        }
    }

    public Boolean removeBook(String title) {
        try (Connection connection = DriverManager.getConnection(
                dbConfig.getUrl(), dbConfig.getUser(), dbConfig.getPassword())) {

            PreparedStatement ps = connection.prepareStatement("DELETE FROM daily_browse.book WHERE title = ?");
            ps.setString(1, title);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Boolean regisrtyAccount(long chatId) {
        try (Connection connection = DriverManager.getConnection(
                dbConfig.getUrl(), dbConfig.getUser(), dbConfig.getPassword())) {

            PreparedStatement ps = connection.prepareStatement("INSERT INTO daily_browse.users(chat_id) VALUES (?)");
            ps.setLong(1, chatId);
            ps.execute();
            return true;
        } catch (SQLException e) {
            if (e.getSQLState().startsWith("23")) {
                return false;
            }
            e.printStackTrace();
            return false;
        }
    }

    public boolean addPage(String title, int page, long chatId) {
        try (Connection connection = DriverManager.getConnection(
                dbConfig.getUrl(), dbConfig.getUser(), dbConfig.getPassword())) {

            connection.setAutoCommit(false);

            try (PreparedStatement ps = connection.prepareStatement("SELECT page, max_page FROM daily_browse.book WHERE user_chat_id = ? AND title = ?")) {
                ps.setLong(1, chatId);
                ps.setString(2, title);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    int maxPage = rs.getInt("max_page");
                    int currentPage = rs.getInt("page");
                    int nowPage = currentPage + page;

                    if (nowPage >= maxPage) return false;

                    try (PreparedStatement updateBook = connection.prepareStatement("UPDATE daily_browse.book SET page = ? WHERE title = ? AND user_chat_id = ?")) {
                        updateBook.setInt(1, nowPage);
                        updateBook.setString(2, title);
                        updateBook.setLong(3, chatId);
                        updateBook.execute();
                    }

                    try (PreparedStatement updateProgress = connection.prepareStatement("UPDATE daily_browse.daily_progress SET pages_read = pages_read + ? WHERE user_id = ? AND date = CURRENT_DATE")) {
                        updateProgress.setInt(1, page);
                        updateProgress.setLong(2, chatId);
                        int rows = updateProgress.executeUpdate();

                        if (rows == 0) {
                            try (PreparedStatement insertProgress = connection.prepareStatement("INSERT INTO daily_browse.daily_progress (user_id, date, pages_read) VALUES (?, CURRENT_DATE, ?)")) {
                                insertProgress.setLong(1, chatId);
                                insertProgress.setInt(2, page);
                                insertProgress.executeUpdate();
                            }
                        }
                    }
                }
            }
            connection.commit();
            return true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String[] allBooks(long chatId) {
        List<String> books = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(
                dbConfig.getUrl(), dbConfig.getUser(), dbConfig.getPassword())) {

            PreparedStatement ps = connection.prepareStatement("SELECT title, page, max_page FROM daily_browse.book WHERE user_chat_id = ?");
            ps.setLong(1, chatId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                books.add("Книга: " + rs.getString("title") + ", " + rs.getInt("page") + "/" + rs.getInt("max_page") + "стр\n");
            }

            return books.isEmpty() ? new String[]{"У вас нету книг"} : books.toArray(new String[0]);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int getPage(String title, long chatId) throws SQLException {
        try (Connection connection = DriverManager.getConnection(
                dbConfig.getUrl(), dbConfig.getUser(), dbConfig.getPassword())) {

            PreparedStatement ps = connection.prepareStatement("SELECT page FROM daily_browse.book WHERE title = ? AND user_chat_id = ?");
            ps.setString(1, title);
            ps.setLong(2, chatId);
            ResultSet rs = ps.executeQuery();

            return rs.next() ? rs.getInt("page") : 0;
        }
    }

    public void setDailyGoal(int count, long chatId) {
        try (Connection connection = DriverManager.getConnection(
                dbConfig.getUrl(), dbConfig.getUser(), dbConfig.getPassword())) {

            PreparedStatement ps = connection.prepareStatement("UPDATE daily_browse.users SET daily_goal_pages = ? WHERE chat_id = ?");
            ps.setInt(1, count);
            ps.setLong(2, chatId);
            ps.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Boolean setQuote(String quote, long chatId) {
        try (Connection connection = DriverManager.getConnection(
                dbConfig.getUrl(), dbConfig.getUser(), dbConfig.getPassword())) {

            PreparedStatement ps = connection.prepareStatement("INSERT INTO quotes (user_id, date, quote) VALUES (?, CURRENT_DATE, ?)");
            ps.setLong(1, chatId);
            ps.setString(2, quote);
            ps.execute();
            return true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Long> getAllIdUsers() {
        List<Long> allUserId = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(
                dbConfig.getUrl(), dbConfig.getUser(), dbConfig.getPassword())) {

            PreparedStatement ps = connection.prepareStatement("SELECT chat_id FROM daily_browse.users");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                allUserId.add(rs.getLong("chat_id"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return allUserId;
    }

    public int getDailyGoal(long chatId) {
        try (Connection connection = DriverManager.getConnection(
                dbConfig.getUrl(), dbConfig.getUser(), dbConfig.getPassword())) {

            PreparedStatement ps = connection.prepareStatement("SELECT daily_goal_pages FROM users WHERE chat_id = ?");
            ps.setLong(1, chatId);
            ResultSet rs = ps.executeQuery();

            return rs.next() ? rs.getInt("daily_goal_pages") : 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int getDailyPagesRead(long chatId) {
        try (Connection connection = DriverManager.getConnection(
                dbConfig.getUrl(), dbConfig.getUser(), dbConfig.getPassword())) {

            PreparedStatement ps = connection.prepareStatement("SELECT pages_read FROM daily_progress WHERE user_id = ? AND date = CURRENT_DATE");
            ps.setLong(1, chatId);
            ResultSet rs = ps.executeQuery();

            return rs.next() ? rs.getInt("pages_read") : 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String[] getDailyQuote(long chatId) {
        List<String> quotes = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(
                dbConfig.getUrl(), dbConfig.getUser(), dbConfig.getPassword())) {

            PreparedStatement ps = connection.prepareStatement("SELECT quote FROM quotes WHERE user_id = ? AND date = CURRENT_DATE - INTERVAL 1 DAY");
            ps.setLong(1, chatId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                quotes.add(rs.getString("quote"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return quotes.toArray(new String[0]);
    }

    public String getStatsForMonthAndWeak(long chatId) {
        try (Connection connection = DriverManager.getConnection(
                dbConfig.getUrl(), dbConfig.getUser(), dbConfig.getPassword())) {

            int weekly = 0;
            int monthly = 0;

            try (PreparedStatement psWeek = connection.prepareStatement("SELECT COALESCE(SUM(pages_read), 0) AS weekly_total FROM daily_progress WHERE user_id = ? AND date >= CURRENT_DATE - INTERVAL 6 DAY")) {
                psWeek.setLong(1, chatId);
                ResultSet rs = psWeek.executeQuery();
                if (rs.next()) {
                    weekly = rs.getInt("weekly_total");
                }
            }

            try (PreparedStatement psMonth = connection.prepareStatement("SELECT COALESCE(SUM(pages_read), 0) AS monthly_total FROM daily_progress WHERE user_id = ? AND MONTH(date) = MONTH(CURRENT_DATE) AND YEAR(date) = YEAR(CURRENT_DATE)")) {
                psMonth.setLong(1, chatId);
                ResultSet rs = psMonth.executeQuery();
                if (rs.next()) {
                    monthly = rs.getInt("monthly_total");
                }
            }

            return "📚 Твоя статистика чтения:\n\n" +
                    "📆 За неделю: " + weekly + " стр.\n" +
                    "🗓️ За месяц: " + monthly + " стр.";

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
