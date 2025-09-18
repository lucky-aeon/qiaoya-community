package org.xhy.community.infrastructure.converter;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.xhy.community.domain.follow.valueobject.FollowTargetType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 关注目标类型转换器
 * 用于MyBatis Plus中枚举与数据库字段的转换
 */
public class FollowTargetTypeConverter extends BaseTypeHandler<FollowTargetType> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, FollowTargetType parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.name());
    }

    @Override
    public FollowTargetType getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return value == null ? null : FollowTargetType.valueOf(value);
    }

    @Override
    public FollowTargetType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return value == null ? null : FollowTargetType.valueOf(value);
    }

    @Override
    public FollowTargetType getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return value == null ? null : FollowTargetType.valueOf(value);
    }
}