package ru.yandex.practicum.filmorate.storage.mappers;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class UserFriendsExtractor implements ResultSetExtractor<Map<Long, Set<Long>>> {
    @Override
    public Map<Long, Set<Long>> extractData(ResultSet rs)
            throws SQLException, DataAccessException {
        Map<Long, Set<Long>> data = new HashMap<>();
        while (rs.next()) {
            Long userId = rs.getLong("user_id");
            data.putIfAbsent(userId, new HashSet<>());
            Long friendId = rs.getLong("friend_id");
            data.get(userId).add(friendId);
        }
        return data;
    }
}
