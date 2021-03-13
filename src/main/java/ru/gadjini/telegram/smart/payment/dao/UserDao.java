package ru.gadjini.telegram.smart.payment.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.telegram.smart.payment.domain.TgUser;

@Repository
public class UserDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void blockUser(int userId) {
        jdbcTemplate.update(
                "UPDATE tg_user SET blocked = true WHERE user_id = " + userId
        );
    }

    public String getLocale(int userId) {
        return jdbcTemplate.query(
                "SELECT locale FROM tg_user WHERE user_id = ?",
                ps -> ps.setInt(1, userId),
                rs -> {
                    if (rs.next()) {
                        return rs.getString(TgUser.LOCALE);
                    }

                    return null;
                }
        );
    }
}
