package org.xhy.community.infrastructure.converter;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.xhy.community.domain.notification.valueobject.ChannelType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 渠道类型转换器
 */
public class ChannelTypeConverter extends BaseTypeHandler<ChannelType> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, ChannelType parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.name());
    }

    @Override
    public ChannelType getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return value != null ? ChannelType.valueOf(value) : null;
    }

    @Override
    public ChannelType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return value != null ? ChannelType.valueOf(value) : null;
    }

    @Override
    public ChannelType getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return value != null ? ChannelType.valueOf(value) : null;
    }
}