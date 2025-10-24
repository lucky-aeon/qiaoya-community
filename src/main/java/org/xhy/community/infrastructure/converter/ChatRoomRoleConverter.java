package org.xhy.community.infrastructure.converter;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.xhy.community.domain.chat.valueobject.ChatRoomRole;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedJdbcTypes(JdbcType.VARCHAR)
@MappedTypes(ChatRoomRole.class)
public class ChatRoomRoleConverter extends BaseTypeHandler<ChatRoomRole> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, ChatRoomRole parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.name());
    }

    @Override
    public ChatRoomRole getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return value == null ? null : ChatRoomRole.fromCode(value);
    }

    @Override
    public ChatRoomRole getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return value == null ? null : ChatRoomRole.fromCode(value);
    }

    @Override
    public ChatRoomRole getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return value == null ? null : ChatRoomRole.fromCode(value);
    }
}

