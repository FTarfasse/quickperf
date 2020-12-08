import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.experimental.results.PrintableResult;
import org.junit.runner.RunWith;
import org.quickperf.junit4.QuickPerfJUnitRunner;
import org.quickperf.sql.Book;
import org.quickperf.sql.annotation.AnalyzeSql;

import javax.persistence.EntityManager;
import javax.persistence.Query;

public class AnalyzeSqlTest {

    @RunWith(QuickPerfJUnitRunner.class)
    @AnalyzeSql
    public static class AClassAnnotatedWithSqlAnalyze extends SqlTestBase {

        @Test
        public void select() {
            EntityManager em = emf.createEntityManager();
            Query query = em.createQuery("FROM " + Book.class.getCanonicalName());
            query.getResultList();
        }

//        @Test
//        public void insert() {
//            EntityManager em = emf.createEntityManager();
//            em.getTransaction().begin();
//
//            Book effectiveJava = new Book();
//            effectiveJava.setIsbn("effectiveJavaIsbn");
//            effectiveJava.setTitle("Effective Java");
//
//            em.persist(effectiveJava);
//
//            em.getTransaction().commit();
//        }
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

//    @Test
//    public void should_generate_a_global_report() {
//        // GIVEN
//        Class<?> classUnderTest = AClassAnnotatedWithSqlAnalyze.class;
//
//        // WHEN
//        PrintableResult result = PrintableResult.testResult(classUnderTest);
//
//        // THEN
//        Assertions.assertThat(result.toString())
//                .contains("SELECT: 1");
//    }

}