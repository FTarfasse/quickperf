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

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.experimental.results.PrintableResult;
import org.junit.runner.RunWith;
import org.quickperf.junit4.QuickPerfJUnitRunner;
import org.quickperf.sql.Book;
import org.quickperf.sql.annotation.AnalyzeSql;
import org.quickperf.sql.annotation.DisplaySqlOfTestMethodBody;
import org.quickperf.writer.WriterFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class AnalyzeSqlTest {

    public static final String SELECT_FILE_PATH = findTargetPath() + File.separator + "select-result.txt";

    private static String findTargetPath() {
        Path targetDirectory = Paths.get("target");
        return targetDirectory.toFile().getAbsolutePath();
    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class SelectExecution extends SqlTestBase {

        @Test
        @AnalyzeSql(writerFactory = FileWriterBuilder.class)
        public void select() {
            EntityManager em = emf.createEntityManager();
            Query query = em.createQuery("FROM " + Book.class.getCanonicalName());
            query.getResultList();
        }

    }

    public static class FileWriterBuilder implements WriterFactory {

        @Override
        public Writer buildWriter() throws IOException {
            return new FileWriter(SELECT_FILE_PATH);
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

        assertThat(new File(SELECT_FILE_PATH))
                .hasContent("[QUICK PERF] SQL Analyzis:\n"
                           + "SELECT: 1");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class InsertExecution extends SqlTestBase {

        @AnalyzeSql
        @Test
        public void insert() {
            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();

            Book effectiveJava = new Book();
            effectiveJava.setIsbn("effectiveJavaIsbn");
            effectiveJava.setTitle("Effective Java");

            em.persist(effectiveJava);

            em.getTransaction().commit();
        }
//
//        @Test
//        public void update() {
//            EntityManager em = emf.createEntityManager();
//            em.getTransaction().begin();
//            Query nativeQuery = em.createNativeQuery("UPDATE book SET isbn = :isbn, title = :title WHERE id = :id")
//                    .setParameter("isbn", 42)
//                    .setParameter("title", "Tristan")
//                    .setParameter("id", 40);
//            nativeQuery.executeUpdate();
//            em.getTransaction().commit();
//        }
//
//        @Test
//        public void delete() {
//            EntityManager em = emf.createEntityManager();
//            em.getTransaction().begin();
//
//            Query query = em.createQuery("DELETE FROM " + Book.class.getCanonicalName());
//            query.executeUpdate();
//
//            em.getTransaction().commit();
//        }
//
//        @Test
//        public void batch() {
//
//        }
//
//        @Test
//        public void sameParameters() {
//            EntityManager em = emf.createEntityManager();
//
//            String hqlQuery = " FROM " + Book.class.getCanonicalName() + " b"
//                    + " WHERE b.id=:idParam";
//
//            Query query = em.createQuery(hqlQuery);
//            query.setParameter("idParam", 1L);
//            query.getResultList();
//
//            Query query2 = em.createQuery(hqlQuery);
//            query2.setParameter("idParam", 1L);
//            query2.getResultList();
//        }

    }

    @Test
    public void should_report_insert() {
        // GIVEN
        Class<?> classUnderTest = InsertExecution.class;

        // WHEN
        PrintableResult result = PrintableResult.testResult(classUnderTest);

        // THEN
        Assertions.assertThat(result.toString())
                 .contains("[QUICK PERF] SQL Analyzis:")
                 .contains("INSERT: 1");
    }

}