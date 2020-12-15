package com.quickschools.reportdesigner.modules.reportdesigner.features.sqlgenerator.service;

import com.quickschools.reportdesigner.entities.Field;
import com.quickschools.reportdesigner.entities.Join;
import com.quickschools.reportdesigner.modules.reportdesigner.features.sqlgenerator.service.exceptions.SqlGenerationException;
import com.quickschools.reportdesigner.shares.services.EntityLookupService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SqlGeneratorServiceTest {

    @Autowired
    SqlGeneratorService sqlGeneratorService;

    @Test
    void generate() throws Exception {

        // Test One Entity (Student)
        testOneEntity();

        // Test One Join (Student, Grade)
        testOneJoinStudentAndGrade();

        // Test One Join (Grade, Lesson)
        testOneJoinGradeAndLesson();

        // Test Two Joins (Student, Grade, Lesson)
        testTwoJoinStudentAndGradeAndLesson();

        // Test Joins don't cover all the entities of selected fields
        testJoinsDoNotCoverSelectedFields();

        // Test Fields list is not allowed to be empty
        testFieldsListIsNotAllowedToBeEmpty();
    }

    // Test One Entity for Student
    private void testOneEntity() throws Exception {
        List<Field> fields = EntityLookupService.getInstance()
                .getFieldsByEntityNameAndFieldsName("Student", Arrays.asList("Name", "Gender"));

        String generate = sqlGeneratorService.generate(fields, Collections.emptyList());
        System.out.println("TEST ONE ENTITY:");
        System.out.println(generate);
        System.out.println("--------------------------------------");
        assertEquals("select student.name, student.gender from student;", generate);
    }

    // Test One Join between Student and Grade
    private void testOneJoinStudentAndGrade() throws Exception {
        List<Field> joinFields = new ArrayList<>();
        // Add fields of student entity
        joinFields.addAll(
                EntityLookupService.getInstance()
                        .getFieldsByEntityNameAndFieldsName("Student", Arrays.asList("Name", "Gender"))
        );
        // Add fields of grade entity
        joinFields.addAll(
                EntityLookupService.getInstance()
                        .getFieldsByEntityNameAndFieldsName("Grade", Collections.singletonList("Name"))
        );
        // Add the join
        Join join = EntityLookupService.getInstance()
                .getJoin("Student", "ID", "Grade", "StudentId");
        // Generate Sql statement
        String joinGenerate = sqlGeneratorService.generate(joinFields, Collections.singletonList(join));
        // Print out the result
        System.out.println("TEST ONE JOIN (Student, Grade):");
        System.out.println(joinGenerate);
        System.out.println("--------------------------------------");
        // Test
        assertEquals("select student.name, student.gender, grade.name from student LEFT JOIN grade ON student.id = grade.student_id;", joinGenerate);
    }

    // Test One Join between Grade and Lesson
    private void testOneJoinGradeAndLesson() throws Exception {
        List<Field> joinFields = new ArrayList<>();
        // Add fields of grade entity
        joinFields.addAll(
                EntityLookupService.getInstance()
                        .getFieldsByEntityNameAndFieldsName("Grade", Collections.singletonList("Name"))
        );
        // Add fields of lesson entity
        joinFields.addAll(
                EntityLookupService.getInstance()
                        .getFieldsByEntityNameAndFieldsName("Lesson", Collections.singletonList("Title"))
        );
        // Add the join
        Join join = EntityLookupService.getInstance()
                .getJoin("Grade", "LessonId", "Lesson", "ID");
        // Generate Sql statement
        String joinGenerate = sqlGeneratorService.generate(joinFields, Collections.singletonList(join));
        // Print out the result
        System.out.println("TEST ONE JOIN (Grade, LESSON):");
        System.out.println(joinGenerate);
        System.out.println("--------------------------------------");
        assertEquals("select grade.name, lesson.title from grade JOIN lesson ON grade.lesson_id = lesson.id;", joinGenerate);
    }

    // Test Two joins among Student and Grade and Lesson
    private void testTwoJoinStudentAndGradeAndLesson() throws Exception {
        List<Field> joinFields = new ArrayList<>();
        // Add fields of student entity
        joinFields.addAll(
                EntityLookupService.getInstance()
                        .getFieldsByEntityNameAndFieldsName("Student", Arrays.asList("Name", "Gender"))
        );
        // Add fields of grade entity
        joinFields.addAll(
                EntityLookupService.getInstance()
                        .getFieldsByEntityNameAndFieldsName("Grade", Collections.singletonList("Name"))
        );
        // Add fields of lesson entity
        joinFields.addAll(
                EntityLookupService.getInstance()
                        .getFieldsByEntityNameAndFieldsName("Lesson", Collections.singletonList("Title"))
        );
        // Add the joins
        Join joinStudentGrade = EntityLookupService.getInstance()
                .getJoin("Student", "ID", "Grade", "StudentId");
        Join joinGradeLesson = EntityLookupService.getInstance()
                .getJoin("Grade", "LessonId", "Lesson", "ID");

        String joinGenerate = sqlGeneratorService.generate(joinFields, Arrays.asList(joinStudentGrade, joinGradeLesson));
        System.out.println("TEST TWO JOINS (STUDENT, GRADE, LESSON):");
        System.out.println(joinGenerate);
        System.out.println("--------------------------------------");
        assertEquals("select student.name, student.gender, grade.name, lesson.title from student LEFT JOIN grade ON student.id = grade.student_id JOIN lesson ON grade.lesson_id = lesson.id;", joinGenerate);
    }

    // Test Joins don't cover all the entities of selected fields
    private void testJoinsDoNotCoverSelectedFields() {

        try {
            List<Field> joinFields = new ArrayList<>();
            joinFields.addAll(
                    EntityLookupService.getInstance()
                            .getFieldsByEntityNameAndFieldsName("Student", Arrays.asList("Name", "Gender"))
            );
            joinFields.addAll(
                    EntityLookupService.getInstance()
                            .getFieldsByEntityNameAndFieldsName("Grade", Collections.singletonList("Name"))
            );
            joinFields.addAll(
                    EntityLookupService.getInstance()
                            .getFieldsByEntityNameAndFieldsName("Lesson", Collections.singletonList("Title"))
            );
            Join joinStudentGrade = EntityLookupService.getInstance()
                    .getJoin("Student", "ID", "Grade", "StudentId");

            sqlGeneratorService.generate(joinFields, Collections.singletonList(joinStudentGrade));
        } catch (Exception e) {
            System.out.println("TEST JOINS DON'T COVER ALL THE ENTITIES OF SELECTED FIELD");
            System.out.println(e.getMessage());
            System.out.println("--------------------------------------");
            assertEquals("Entities are not fully covered by joins", e.getMessage());
        }
    }

    // Test Fields list is not allowed to be empty
    private void testFieldsListIsNotAllowedToBeEmpty() {
        try {
            sqlGeneratorService.generate(Collections.emptyList(), Collections.emptyList());
        } catch (SqlGenerationException e) {
            System.out.println("TEST FIELDS lIST IS NOT ALLOWED TO BE EMPTY");
            System.out.println(e.getMessage());
            System.out.println("--------------------------------------");
            assertEquals("Fields list is empty!", e.getMessage());
        }
    }
}
