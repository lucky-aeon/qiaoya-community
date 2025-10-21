package org.xhy.community.infrastructure.converter;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.xhy.community.domain.oauth2.valueobject.OAuth2ClientStatus;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * OAuth2ClientStatus 枚举类型转换器
 */
@MappedJdbcTypes(JdbcType.VARCHAR)
@MappedTypes(OAuth2ClientStatus.class)
public class OAuth2ClientStatusConverter extends BaseTypeHandler<OAuth2ClientStatus> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, OAuth2ClientStatus parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setString(i, parameter.name());
    }

    @Override
    public OAuth2ClientStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return value == null ? null : OAuth2ClientStatus.valueOf(value);
    }

    @Override
    public OAuth2ClientStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return value == null ? null : OAuth2ClientStatus.valueOf(value);
    }

    @Override
    public OAuth2ClientStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return value == null ? null : OAuth2ClientStatus.valueOf(value);
    }
}
