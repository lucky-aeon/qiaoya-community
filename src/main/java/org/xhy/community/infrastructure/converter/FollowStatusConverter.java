package org.xhy.community.infrastructure.converter;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.xhy.community.domain.follow.valueobject.FollowStatus;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 关注状态转换器
 * 用于MyBatis Plus中枚举与数据库字段的转换
 */
public class FollowStatusConverter extends BaseTypeHandler<FollowStatus> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, FollowStatus parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.name());
    }

    @Override
    public FollowStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return value == null ? null : FollowStatus.valueOf(value);
    }

    @Override
    public FollowStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return value == null ? null : FollowStatus.valueOf(value);
    }

    @Override
    public FollowStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return value == null ? null : FollowStatus.valueOf(value);
    }
}