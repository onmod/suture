package net.dloud.platform.common.mapper;

import static net.dloud.platform.common.mapper.MapperBuildUtil.AND;
import static net.dloud.platform.common.mapper.MapperBuildUtil.AND_SOFT_DELETE;
import static net.dloud.platform.common.mapper.MapperBuildUtil.ASC;
import static net.dloud.platform.common.mapper.MapperBuildUtil.COMMA;
import static net.dloud.platform.common.mapper.MapperBuildUtil.DELETE;
import static net.dloud.platform.common.mapper.MapperBuildUtil.DESC;
import static net.dloud.platform.common.mapper.MapperBuildUtil.FROM;
import static net.dloud.platform.common.mapper.MapperBuildUtil.GROUP_BY;
import static net.dloud.platform.common.mapper.MapperBuildUtil.HAVING;
import static net.dloud.platform.common.mapper.MapperBuildUtil.INNER_JOIN;
import static net.dloud.platform.common.mapper.MapperBuildUtil.INSERT;
import static net.dloud.platform.common.mapper.MapperBuildUtil.LEFT_BRACKET;
import static net.dloud.platform.common.mapper.MapperBuildUtil.LEFT_JOIN;
import static net.dloud.platform.common.mapper.MapperBuildUtil.LIMIT;
import static net.dloud.platform.common.mapper.MapperBuildUtil.MERGE;
import static net.dloud.platform.common.mapper.MapperBuildUtil.ON_DUPLICATE_UPDATE;
import static net.dloud.platform.common.mapper.MapperBuildUtil.ORDER_BY;
import static net.dloud.platform.common.mapper.MapperBuildUtil.RIGHT_BRACKET;
import static net.dloud.platform.common.mapper.MapperBuildUtil.SELECT;
import static net.dloud.platform.common.mapper.MapperBuildUtil.SET;
import static net.dloud.platform.common.mapper.MapperBuildUtil.SET_SOFT_DELETE;
import static net.dloud.platform.common.mapper.MapperBuildUtil.UNION;
import static net.dloud.platform.common.mapper.MapperBuildUtil.UNION_ALL;
import static net.dloud.platform.common.mapper.MapperBuildUtil.UPDATE;
import static net.dloud.platform.common.mapper.MapperBuildUtil.VALUES;
import static net.dloud.platform.common.mapper.MapperBuildUtil.WHERE;

/**
 * @author QuDasheng
 * @create 2018-09-20 09:56
 **/
public class MapperElement {
    private boolean force;
    private StringBuilder sentence = new StringBuilder();

    public MapperElement() {
        this.force = false;
    }

    public MapperElement(boolean force) {
        this.force = force;
    }

    public MapperElement force(boolean force) {
        this.force = force;
        return this;
    }

    public MapperElement select(String table) {
        sentence.append(SELECT).append("*").append(FROM).append(table);
        return this;
    }

    public MapperElement select(String table, String fields) {
        sentence.append(SELECT).append(fields).append(FROM).append(table);
        return this;
    }

    public MapperElement select(String table, String... fields) {
        sentence.append(SELECT);
        fields(fields);
        sentence.append(FROM).append(table);
        return this;
    }

    public MapperElement insert(String table, String fields, String values) {
        sentence.append(INSERT).append(table).append(LEFT_BRACKET).append(fields).append(RIGHT_BRACKET)
                .append(VALUES).append(LEFT_BRACKET).append(values).append(RIGHT_BRACKET);
        return this;
    }

    public MapperElement insertSelect(String table, String fields, String newTable, String newFields, String... wheres) {
        sentence.append(INSERT).append(table).append(LEFT_BRACKET).append(fields).append(RIGHT_BRACKET)
                .append(LEFT_BRACKET);
        select(newTable, newFields).where(wheres);
        sentence.append(RIGHT_BRACKET);
        return this;
    }

    public MapperElement upsertSelect(String table, String fields, String newTable, String newFields, String upFields, String... wheres) {
        sentence.append(INSERT).append(table).append(LEFT_BRACKET).append(fields).append(RIGHT_BRACKET)
                .append(LEFT_BRACKET);
        select(newTable, newFields).where(wheres);
        sentence.append(RIGHT_BRACKET).append(ON_DUPLICATE_UPDATE).append(upFields);
        return this;
    }

    public MapperElement merge(String table, String fields, String values) {
        sentence.append(MERGE).append(table).append(LEFT_BRACKET).append(fields).append(RIGHT_BRACKET)
                .append(VALUES).append(LEFT_BRACKET).append(values).append(RIGHT_BRACKET);
        return this;
    }

    public MapperElement mergeSelect(String table, String fields, String newTable, String newFields, String... wheres) {
        sentence.append(MERGE).append(table).append(LEFT_BRACKET).append(fields).append(RIGHT_BRACKET)
                .append(LEFT_BRACKET);
        select(newTable, newFields).where(wheres);
        sentence.append(RIGHT_BRACKET);
        return this;
    }

    public MapperElement upsert(String table, String fields, String values, String serts) {
        sentence.append(INSERT).append(table).append(LEFT_BRACKET).append(fields).append(RIGHT_BRACKET)
                .append(VALUES).append(LEFT_BRACKET).append(values).append(RIGHT_BRACKET)
                .append(ON_DUPLICATE_UPDATE).append(serts);
        return this;
    }

    public MapperElement update(String table, String... values) {
        final int length = values.length;
        sentence.append(UPDATE).append(table).append(SET);
        for (int i = 0; i < length; i++) {
            sentence.append(values[i]);
            if (i < length - 1) {
                sentence.append(COMMA);
            }
        }
        return this;
    }

    public MapperElement delete(String table) {
        sentence.append(DELETE).append(table);
        return this;
    }

    public MapperElement softDelete(String table) {
        return update(table, SET_SOFT_DELETE);
    }

    public MapperElement choose(String fields) {
        sentence.append(SELECT).append(fields);
        return this;
    }

    public MapperElement subSelect(String asTable, String subSelect) {
        sentence.append(FROM).append(LEFT_BRACKET).append(subSelect).append(RIGHT_BRACKET).append(asTable);
        return this;
    }

    public MapperElement innerJoin(String joinSelect) {
        sentence.append(INNER_JOIN).append(joinSelect);
        return this;
    }

    public MapperElement innerJoin(String asTable, String joinSelect) {
        sentence.append(INNER_JOIN).append(joinSelect).append(asTable);
        return this;
    }

    public MapperElement leftJoin(String joinSelect) {
        sentence.append(LEFT_JOIN).append(joinSelect);
        return this;
    }

    public MapperElement leftJoin(String asTable, String joinSelect) {
        sentence.append(LEFT_JOIN).append(joinSelect).append(asTable);
        return this;
    }

    public MapperElement where(String... wheres) {
        final int length = wheres.length;
        if (length > 0) {
            if (sentence.indexOf(WHERE) > 0) {
                sentence.append(AND);
            } else {
                sentence.append(WHERE);
            }
            if (!force) {
                sentence.append(AND_SOFT_DELETE).append(AND);
            }

            for (int i = 0; i < length; i++) {
                sentence.append(wheres[i]);
                if (i < length - 1) {
                    sentence.append(AND);
                }
            }
        }
        return this;
    }

    public MapperElement union(String... sqls) {
        final int length = sqls.length;
        for (int i = 0; i < length; i++) {
            sentence.append(sqls[i]);
            if (i < length - 1) {
                sentence.append(UNION);
            }
        }
        return this;
    }

    public MapperElement unionAll(String... sqls) {
        final int length = sqls.length;
        for (int i = 0; i < length; i++) {
            sentence.append(sqls[i]);
            if (i < length - 1) {
                sentence.append(UNION_ALL);
            }
        }
        return this;
    }

    public MapperElement group(String... fields) {
        sentence.append(GROUP_BY);
        return fields(fields);
    }

    public MapperElement having(String having) {
        sentence.append(HAVING).append(having);
        return this;
    }

    public MapperElement order(String... fields) {
        sentence.append(ORDER_BY);
        return fields(fields);
    }

    public MapperElement order(String field) {
        sentence.append(ORDER_BY).append(field);
        return this;
    }

    public MapperElement asc() {
        sentence.append(ASC);
        return this;
    }

    public MapperElement desc() {
        sentence.append(DESC);
        return this;
    }

    public MapperElement limit(String size) {
        sentence.append(LIMIT).append(size);
        return this;
    }

    public MapperElement limit(String skip, String size) {
        sentence.append(LIMIT).append(skip).append(COMMA).append(size);
        return this;
    }


    private MapperElement fields(String... fields) {
        final int length = fields.length;
        for (int i = 0; i < length; i++) {
            sentence.append(fields[i]);
            if (i < length - 1) {
                sentence.append(COMMA);
            }
        }
        return this;
    }

    public String build() {
        return sentence.toString();
    }
}
