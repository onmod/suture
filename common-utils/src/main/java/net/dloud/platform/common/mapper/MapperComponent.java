package net.dloud.platform.common.mapper;

/**
 * @author QuDasheng
 * @create 2018-09-13 15:55
 **/
public interface MapperComponent extends BaseComponent {
    default MapperElement choose(String fields) {
        return new MapperElement().choose(fields);
    }

    default MapperElement select(String table) {
        return new MapperElement().select(table);
    }

    default MapperElement select(String table, String fields) {
        return new MapperElement().select(table, fields);
    }

    default MapperElement select(String table, String... fields) {
        return new MapperElement().select(table, fields);
    }

    default MapperElement insert(String table, String fields, String values) {
        return new MapperElement().insert(table, fields, values);
    }

    default MapperElement insertSelect(String table, String fields, String newTable, String newFields) {
        return new MapperElement().insertSelect(table, fields, newTable, newFields);
    }

    default MapperElement insertSelect(String table, String fields, String newTable, String newFields, String... wheres) {
        return new MapperElement().insertSelect(table, fields, newTable, newFields, wheres);
    }

    default MapperElement upsertSelect(String table, String fields, String newTable, String newFields, String upFields, String... wheres) {
        return new MapperElement().upsertSelect(table, fields, newTable, newFields, upFields, wheres);
    }

    default MapperElement upsert(String table, String fields, String values, String serts) {
        return new MapperElement().upsert(table, fields, values, serts);
    }

    default MapperElement update(String table, String... values) {
        return new MapperElement().update(table, values);
    }

    default MapperElement delete(String table) {
        return new MapperElement().delete(table);
    }

    default MapperElement softDelete(String table) {
        return new MapperElement().softDelete(table);
    }

    default MapperElement union(String... sqls) {
        return new MapperElement().union(sqls);
    }

    default MapperElement unionAll(String... sqls) {
        return new MapperElement().unionAll(sqls);
    }
}


