package org.xhy.community.infrastructure.converter;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.xhy.community.domain.common.valueobject.ContentType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 内容类型转换器
 */
public class ContentTypeConverter extends BaseTypeHandler<ContentType> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, ContentType parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.name());
    }

    @Override
    public ContentType getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return value != null ? ContentType.valueOf(value) : null;
    }

    @Override
    public ContentType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return value != null ? ContentType.valueOf(value) : null;
    }

    @Override
    public ContentType getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return value != null ? ContentType.valueOf(value) : null;
    }
}
