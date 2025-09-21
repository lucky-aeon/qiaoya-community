package org.xhy.community.infrastructure.converter;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.xhy.community.domain.user.valueobject.UserRole;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedJdbcTypes(JdbcType.VARCHAR)
@MappedTypes(UserRole.class)
public class UserRoleConverter extends BaseTypeHandler<UserRole> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, UserRole parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setString(i, parameter.name());
    }

    @Override
    public UserRole getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return value == null ? null : UserRole.fromCode(value);
    }

    @Override
    public UserRole getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return value == null ? null : UserRole.fromCode(value);
    }

    @Override
    public UserRole getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return value == null ? null : UserRole.fromCode(value);
    }
}