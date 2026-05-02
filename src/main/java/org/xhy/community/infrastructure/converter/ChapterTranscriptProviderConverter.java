package org.xhy.community.infrastructure.converter;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.xhy.community.domain.course.valueobject.ChapterTranscriptProvider;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedJdbcTypes(JdbcType.VARCHAR)
@MappedTypes(ChapterTranscriptProvider.class)
public class ChapterTranscriptProviderConverter extends BaseTypeHandler<ChapterTranscriptProvider> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, ChapterTranscriptProvider parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setString(i, parameter.name());
    }

    @Override
    public ChapterTranscriptProvider getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return value == null ? null : ChapterTranscriptProvider.fromCode(value);
    }

    @Override
    public ChapterTranscriptProvider getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return value == null ? null : ChapterTranscriptProvider.fromCode(value);
    }

    @Override
    public ChapterTranscriptProvider getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return value == null ? null : ChapterTranscriptProvider.fromCode(value);
    }
}
