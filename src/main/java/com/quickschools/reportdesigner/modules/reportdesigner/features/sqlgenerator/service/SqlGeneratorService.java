package com.quickschools.reportdesigner.modules.reportdesigner.features.sqlgenerator.service;

import com.quickschools.reportdesigner.entities.Entity;
import com.quickschools.reportdesigner.entities.Field;
import com.quickschools.reportdesigner.entities.Join;
import com.quickschools.reportdesigner.modules.reportdesigner.features.sqlgenerator.service.exceptions.SqlGenerationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SqlGeneratorService {

    public String generate(List<Field> fields, List<Join> joins) throws SqlGenerationException {
        // VALIDATION
        // ---------------------------------------
        validate(fields, joins);

        // GENERATE SQL STATEMENT
        // ---------------------------------------
        //      Portion: select fields
        String selectFields = generateSelectFieldsPortion(fields);
        //      Portion: from tables and join
        String from = generateFromPortion(fields, joins);

        // RETURN
        return selectFields + " " + from + ";";
    }

    // Validate input (just check nulls and list size)
    private void validate(List<Field> fields, List<Join> joins) throws SqlGenerationException {
        if (fields == null) {
            throw new SqlGenerationException("Fields list is null.");
        }
        if (joins == null) {
            throw new SqlGenerationException("Joins list is null.");
        }
        // the Files list size has to be more than zero
        if (fields.size() == 0) {
            throw new SqlGenerationException("Fields list is empty!");
        }
    }

    // Validate joins list
    private void validateJoin(List<Entity> entities, List<Join> joins) throws SqlGenerationException {
        // Check all entities of the fields are covered by joins
        for (Entity entity : entities) {
            boolean isCoveredByAnyJoin = false;
            for (Join join : joins) {
                if (join.getSourcePrimaryKey().getEntity().equals(entity) || join.getDestinationPrimaryKey().getEntity().equals(entity)) {
                    isCoveredByAnyJoin = true;
                    break;
                }
            }
            if (!isCoveredByAnyJoin) {
                throw new SqlGenerationException("Entities are not fully covered by joins");
            }
        }
    }

    // Generate "select fields" portion of query
    private String generateSelectFieldsPortion(List<Field> fields) {
        StringBuilder result = new StringBuilder("select").append(" ");
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            result
                    .append(field.getEntity().getTableName())
                    .append(".")
                    .append(field.getColumnName());
            if (i < fields.size() - 1) {
                result.append(", ");
            }
        }
        return result.toString();
    }

    // Generate "from" portion of query
    private String generateFromPortion(List<Field> fields, List<Join> joins) throws SqlGenerationException {
        // get set of entities from fields
        List<Entity> entities = this.getEntities(fields);
        // initiate result
        StringBuilder result = new StringBuilder("from")
                .append(" ");
        // 1) Single Entity Query
        // for example: select id from table
        if (entities.size() == 1) {
            Entity entity = entities.get(0);
            result.append(entity.getTableName());
        }

        // 2) Join Query
        // for example: select t1.name, t2.title from t2 left join t1 on t1.id = t2.id
        if (entities.size() >= 2) {
            // validate joins
            validateJoin(entities, joins);

            for (int i = 0; i < joins.size(); i++) {
                Join join = joins.get(i);
                String sourceTableName = join.getSourcePrimaryKey().getEntity().getTableName();
                String sourceColumnId = join.getSourcePrimaryKey().getColumnName();
                String destinationTableName = join.getDestinationPrimaryKey().getEntity().getTableName();
                String destinationColumnId = join.getDestinationPrimaryKey().getColumnName();
                if (i == 0) {
                    result
                            .append(sourceTableName).append(" ");
                }
                result
                        .append(join.getJoinType().sql()).append(" ")
                        .append(destinationTableName).append(" ")
                        .append("ON ")
                        .append(sourceTableName).append(".").append(sourceColumnId)
                        .append(" = ")
                        .append(destinationTableName).append(".").append(destinationColumnId);
                if (i < joins.size() - 1) {
                    result.append(" ");
                }
            }
        }
        return result.toString();
    }

    // Get set the entities of fields (unique)
    private List<Entity> getEntities(List<Field> fields) {
        return fields
                .stream()
                .map(Field::getEntity)
                .distinct()
                .collect(Collectors.toList());

    }
}
