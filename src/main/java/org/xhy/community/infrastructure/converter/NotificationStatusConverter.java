package org.xhy.community.infrastructure.converter;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.xhy.community.domain.notification.valueobject.NotificationStatus;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 通知状态转换器
 */
public class NotificationStatusConverter extends BaseTypeHandler<NotificationStatus> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, NotificationStatus parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.name());
    }

    @Override
    public NotificationStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return value != null ? NotificationStatus.valueOf(value) : null;
    }

    @Override
    public NotificationStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return value != null ? NotificationStatus.valueOf(value) : null;
    }

    @Override
    public NotificationStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return value != null ? NotificationStatus.valueOf(value) : null;
    }
}