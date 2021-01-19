package org.quickperf.sql.annotation;

import org.quickperf.writer.DefaultWriterFactory;
import org.quickperf.writer.WriterFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface AnalyzeSql {

    String QUICKPREF_SQL_REPORT = "[QUICK PERF] SQL Analyzis:\n%s";

    String format() default QUICKPREF_SQL_REPORT;

    Class<? extends WriterFactory> writerFactory() default DefaultWriterFactory.class;
}
