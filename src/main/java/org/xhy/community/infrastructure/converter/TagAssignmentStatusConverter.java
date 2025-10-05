package org.xhy.community.infrastructure.converter;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.xhy.community.domain.tag.valueobject.TagAssignmentStatus;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedJdbcTypes(JdbcType.VARCHAR)
@MappedTypes(TagAssignmentStatus.class)
public class TagAssignmentStatusConverter extends BaseTypeHandler<TagAssignmentStatus> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, TagAssignmentStatus parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.name());
    }

    @Override
    public TagAssignmentStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String v = rs.getString(columnName);
        return v == null ? null : TagAssignmentStatus.fromCode(v);
    }

    @Override
    public TagAssignmentStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String v = rs.getString(columnIndex);
        return v == null ? null : TagAssignmentStatus.fromCode(v);
    }

    @Override
    public TagAssignmentStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String v = cs.getString(columnIndex);
        return v == null ? null : TagAssignmentStatus.fromCode(v);
    }
}

