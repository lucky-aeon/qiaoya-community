package org.xhy.community.infrastructure.converter;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.xhy.community.domain.course.valueobject.ChapterTranscriptStatus;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedJdbcTypes(JdbcType.VARCHAR)
@MappedTypes(ChapterTranscriptStatus.class)
public class ChapterTranscriptStatusConverter extends BaseTypeHandler<ChapterTranscriptStatus> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, ChapterTranscriptStatus parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setString(i, parameter.name());
    }

    @Override
    public ChapterTranscriptStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return value == null ? null : ChapterTranscriptStatus.fromCode(value);
    }

    @Override
    public ChapterTranscriptStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return value == null ? null : ChapterTranscriptStatus.fromCode(value);
    }

    @Override
    public ChapterTranscriptStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return value == null ? null : ChapterTranscriptStatus.fromCode(value);
    }
}
