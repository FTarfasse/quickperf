/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 * Copyright 2019-2020 the original author or authors.
 */

import org.junit.Test;
import org.junit.experimental.results.PrintableResult;
import org.junit.runner.RunWith;
import org.quickperf.junit4.QuickPerfJUnitRunner;
import org.quickperf.sql.Book;
import org.quickperf.sql.annotation.AnalyzeSql;
import org.quickperf.writer.WriterFactory;

import javax.persistence.Query;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class AnalyzeSqlTest {

    private static final String SELECT_FILE_PATH = findTargetPath() + File.separator + "select-result.txt";
    private static final String INSERT_FILE_PATH = findTargetPath() + File.separator + "insert-result.txt";
    private static final String UPDATE_FILE_PATH = findTargetPath() + File.separator + "update-result.txt";
    private static final String DELETE_FILE_PATH = findTargetPath() + File.separator + "delete-result.txt";
    private static final String NOTHING_HAPPENED = findTargetPath() + File.separator + "no-result.txt";
    private static final String MULTIPLE_EXECUTIONS = findTargetPath() + File.separator + "sql-executions.txt";
    private static final String SELECT_SPECIFIC_MESSAGES = findTargetPath() + File.separator + "select-specific.txt";

    private static String findTargetPath() {
        Path targetDirectory = Paths.get("target");
        return targetDirectory.toFile().getAbsolutePath();
    }

   private static String getFileContent(String filePath, int upToLine){

       String fileContent = null;
       try {
           fileContent = Files.lines(Paths.get(filePath))
                   .limit(upToLine)
                   .collect(Collectors.joining(System.lineSeparator()));
       } catch (IOException e) {
           e.printStackTrace();
       }
       return fileContent;
   }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class NoExecution extends SqlTestBase {
        public static class FileWriterBuilder implements WriterFactory {

            @Override
            public Writer buildWriter() throws IOException {
                return new FileWriter(NOTHING_HAPPENED);
            }
        }

        @Test
        @AnalyzeSql(writerFactory = FileWriterBuilder.class)
        public void noExecution() {
        }

    }

    @Test
    public void should_report_nothing() {

        // GIVEN
        Class<?> classUnderTest = NoExecution.class;

        // WHEN
        PrintableResult testResult = PrintableResult.testResult(classUnderTest);

        // THEN
        assertThat(testResult.failureCount()).isZero();

        assertThat(new File(NOTHING_HAPPENED))
                .hasContent("[QUICK PERF] SQL Analyzis:\n"
                        + "SQL EXECUTIONS: 0\n"
                        + "");
    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class SelectExecution extends SqlTestBase {
        public static class FileWriterBuilder implements WriterFactory {

            @Override
            public Writer buildWriter() throws IOException {
                return new FileWriter(SELECT_FILE_PATH);
            }
        }

        @Test
        @AnalyzeSql(writerFactory = FileWriterBuilder.class)
        public void select() {
            executeInATransaction(entityManager -> {
                Query query = entityManager.createQuery("FROM " + Book.class.getCanonicalName());
                query.getResultList();
            });
        }

    }

    @Test
    public void should_report_select() {

        // GIVEN
        Class<?> classUnderTest = SelectExecution.class;

        // WHEN
        PrintableResult testResult = PrintableResult.testResult(classUnderTest);

        // THEN
        assertThat(testResult.failureCount()).isZero();

        assertThat(getFileContent(SELECT_FILE_PATH, 3)).contains(
                        "[QUICK PERF] SQL Analyzis:\n"
                        + "SQL EXECUTIONS: 1\n"
                        + "SELECT: 1");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class InsertExecution extends SqlTestBase {

        public static class FileWriterBuilder implements WriterFactory {

            @Override
            public Writer buildWriter() throws IOException {
                return new FileWriter(INSERT_FILE_PATH);
            }
        }

        @AnalyzeSql(writerFactory = FileWriterBuilder.class)
        @Test
        public void insert() {
            executeInATransaction(entityManager -> {
                Book effectiveJava = new Book();
                effectiveJava.setIsbn("effectiveJavaIsbn");
                effectiveJava.setTitle("Effective Java");
                entityManager.persist(effectiveJava);
            });
        }

    }

    @Test
    public void should_report_insert() {
        // GIVEN
        Class<?> classUnderTest = InsertExecution.class;

        // WHEN
        PrintableResult result = PrintableResult.testResult(classUnderTest);

        // THEN
        assertThat(result.failureCount()).isZero();

        assertThat(getFileContent(INSERT_FILE_PATH,3)).contains(
                        "[QUICK PERF] SQL Analyzis:\n"
                        + "SQL EXECUTIONS: 2\n" // Hibernate sequence call is also called
                        + "INSERT: 1");
    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class UpdateExecution extends SqlTestBase {

        public static class FileWriterBuilder implements WriterFactory {

            @Override
            public Writer buildWriter() throws IOException {
                return new FileWriter(UPDATE_FILE_PATH);
            }
        }

        @AnalyzeSql(writerFactory = FileWriterBuilder.class)
        @Test
        public void update() {
            executeInATransaction(entityManager -> {
                String sql = " UPDATE book"
                        + " SET isbn ='978-0134685991'"
                        + " WHERE id = 1";
                Query query = entityManager.createNativeQuery(sql);
                query.executeUpdate();
            });
        }

    }

    @Test
    public void should_report_update() {
        // GIVEN
        Class<?> classUnderTest = UpdateExecution.class;

        // WHEN
        PrintableResult result = PrintableResult.testResult(classUnderTest);

        // THEN
        assertThat(result.failureCount()).isZero();

        assertThat(getFileContent(UPDATE_FILE_PATH, 3)).contains(
                        "[QUICK PERF] SQL Analyzis:\n"
                        + "SQL EXECUTIONS: 1\n"
                        + "UPDATE: 1");
    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class DeleteExecution extends SqlTestBase {

        public static class FileWriterBuilder implements WriterFactory {

            @Override
            public Writer buildWriter() throws IOException {
                return new FileWriter(DELETE_FILE_PATH);
            }
        }

        @AnalyzeSql(writerFactory = FileWriterBuilder.class)
        @Test
        public void delete() {
            executeInATransaction(entityManager -> {
                Query query = entityManager.createQuery("DELETE FROM " + Book.class.getCanonicalName());
                query.executeUpdate();
            });
        }

    }

    @Test
    public void should_report_delete() {
        // GIVEN
        Class<?> classUnderTest = DeleteExecution.class;

        // WHEN
        PrintableResult result = PrintableResult.testResult(classUnderTest);

        // THEN
        assertThat(result.failureCount()).isZero();

        assertThat(getFileContent(DELETE_FILE_PATH, 3)).contains(
                        "[QUICK PERF] SQL Analyzis:\n"
                        + "SQL EXECUTIONS: 1\n"
                        + "DELETE: 1");
    }

    @Test
    public void should_display_method_body() {
        // GIVEN
        Class<?> classUnderTest = DeleteExecution.class;

        // WHEN
        PrintableResult result = PrintableResult.testResult(classUnderTest);

        // THEN
        assertThat(result.failureCount()).isZero();

        assertThat(getFileContent(DELETE_FILE_PATH, 10)).contains(
                        "[QUICK PERF] SQL Analyzis:\n"
                        + "SQL EXECUTIONS: 1\n"
                        + "DELETE: 1"
                        + "");
    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class SqlExecutions_are_properly_analyzed extends SqlTestBase {

        public static class FileWriterBuilder implements WriterFactory {

            @Override
            public Writer buildWriter() throws IOException {
                return new FileWriter(MULTIPLE_EXECUTIONS);
            }
        }

        @AnalyzeSql(writerFactory = FileWriterBuilder.class)
        @Test
        public void queries() {
            executeInATransaction(entityManager -> {
                Query query = entityManager.createQuery("FROM " + Book.class.getCanonicalName());
                query.getResultList();

                String insertTwo = "INSERT INTO Book (id,title) VALUES (1300, 'Book title')";
                Query secondInsertQuery = entityManager.createNativeQuery(insertTwo);
                secondInsertQuery.executeUpdate();
            });
        }

    }

    @Test
    public void should_report_sql_executions() {
        // GIVEN
        Class<?> classUnderTest = SqlExecutions_are_properly_analyzed.class;

        // WHEN
        PrintableResult result = PrintableResult.testResult(classUnderTest);

        // THEN
        assertThat(result.failureCount()).isZero();

        assertThat(getFileContent(MULTIPLE_EXECUTIONS, 4)).contains(
                        "[QUICK PERF] SQL Analyzis:\n"
                        + "SQL EXECUTIONS: 2\n"
                        + "SELECT: 1\n"
                        + "INSERT: 1");
    }

    @Test
    public void should_display_max_query_execution_time() {
        // GIVEN
        Class<?> classUnderTest = SqlExecutions_are_properly_analyzed.class;

        // WHEN
        PrintableResult result = PrintableResult.testResult(classUnderTest);

        // THEN
        assertThat(result.failureCount()).isZero();

        assertThat(getFileContent(MULTIPLE_EXECUTIONS, 5)).contains(
                        "[QUICK PERF] SQL Analyzis:\n"
                        + "SQL EXECUTIONS: 2\n"
                        + "SELECT: 1\n"
                        + "INSERT: 1\n"
                        + "MAX TIME: 0 ms" );
    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class SelectIssues extends SqlTestBase {

        public static class FileWriterBuilder implements WriterFactory {

            @Override
            public Writer buildWriter() throws IOException {
                return new FileWriter(SELECT_SPECIFIC_MESSAGES);
            }
        }

        @AnalyzeSql(writerFactory = FileWriterBuilder.class)
        @Test
        public void queries() {
            executeInATransaction(entityManager -> {
                Query query = entityManager.createQuery("FROM " + Book.class.getCanonicalName());
                query.getResultList();

                Query sameQueryAgain = entityManager.createQuery("FROM " + Book.class.getCanonicalName());
                sameQueryAgain.getResultList();

            });
        }

    }

    @Test
    public void should_report_same_selects() {
        // GIVEN
        Class<?> classUnderTest = SelectIssues.class;

        // WHEN
        PrintableResult result = PrintableResult.testResult(classUnderTest);

        // THEN
        assertThat(result.failureCount()).isZero();

        assertThat(getFileContent(SELECT_SPECIFIC_MESSAGES, 4)).contains(
                        "[QUICK PERF] SQL Analyzis:\n"
                        + "SQL EXECUTIONS: 2\n"
                        + "SELECT: 2\n"
                        + "    Same SELECT statements");
    }
}
