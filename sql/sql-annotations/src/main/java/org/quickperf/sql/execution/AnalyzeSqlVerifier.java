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

package org.quickperf.sql.execution;

import net.ttddyy.dsproxy.QueryType;
import org.quickperf.issue.PerfIssue;
import org.quickperf.issue.VerifiablePerformanceIssue;
import org.quickperf.sql.SqlExecution;
import org.quickperf.sql.SqlExecutions;
import org.quickperf.sql.annotation.AnalyzeSql;
import org.quickperf.sql.select.analysis.SelectAnalysis;
import org.quickperf.time.ExecutionTime;
import org.quickperf.writer.PrintWriterBuilder;
import org.quickperf.writer.WriterFactory;

import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

public class AnalyzeSqlVerifier implements VerifiablePerformanceIssue<AnalyzeSql, SqlAnalysis> {

    public static AnalyzeSqlVerifier INSTANCE = new AnalyzeSqlVerifier();

    private AnalyzeSqlVerifier() {
    }

    @Override
    public PerfIssue verifyPerfIssue(AnalyzeSql annotation, SqlAnalysis sqlAnalysis) {

        Class<? extends WriterFactory> writerFactoryClass = annotation.writerFactory();
        StringBuilder sqlReport = new StringBuilder();

        try (PrintWriter pw = PrintWriterBuilder.INSTANCE.buildPrintWriterFrom(writerFactoryClass)) {
            SqlExecutions sqlExecutions = sqlAnalysis.getSqlExecutions();

            sqlReport.append(jdbcExecutions(sqlExecutions));

            long selectCount = sqlExecutions.retrieveQueryNumberOfType(QueryType.SELECT);
            sqlReport.append(buildSelectCountReport(selectCount));
            // "Same SELECT statements"
            sqlReport.append(buildSelectMessages(sqlAnalysis));

            // TODO: annotation analyzing the Hibernate sequence call executed in insert statements
            long insertCount = sqlExecutions.retrieveQueryNumberOfType(QueryType.INSERT);
            sqlReport.append(buildInsertCountReport(insertCount));

            long updateCount = sqlExecutions.retrieveQueryNumberOfType(QueryType.UPDATE);
            sqlReport.append(buildUpdateCountReport(updateCount));

            long deleteCount = sqlExecutions.retrieveQueryNumberOfType(QueryType.DELETE);
            sqlReport.append(buildDeleteCountReport(deleteCount));

            sqlReport.append(getMaxTime(sqlExecutions));
            sqlReport.append(numberOfQueries(sqlExecutions) + sqlExecutions.toString());

            pw.printf(annotation.format(), sqlReport);

        }

        return PerfIssue.NONE;

    }

    private String buildSelectMessages(SqlAnalysis sqlAnalysis) {
        String mes = "";
        SelectAnalysis selectAnalysis = sqlAnalysis.getSelectAnalysis();

        if (selectAnalysis.hasSameSelects()) {
            mes += "    Same SELECT statements" + System.lineSeparator();
        }
        if (selectAnalysis.getSameSelectTypesWithDifferentParamValues().evaluate()) {
            mes += sqlAnalysis.getSelectAnalysis().getSameSelectTypesWithDifferentParamValues().getSuggestionToFixIt();
        }

        return mes;
    }

    private String numberOfQueries(SqlExecutions sqlExecutions) {
        if (sqlExecutions.getNumberOfExecutions() == 0){
            return "";
        }
        return sqlExecutions.getNumberOfExecutions() > 1 ? "QUERIES: " + System.lineSeparator() : "QUERY: " + System.lineSeparator();
    }

    private String jdbcExecutions(SqlExecutions sqlExecutions) {
        return "SQL EXECUTIONS: " + sqlExecutions.getNumberOfExecutions() + System.lineSeparator();
    }

    private String buildUpdateCountReport(long updateCount) {
        if (updateCount > 0) {
            return "UPDATE: " + updateCount + System.lineSeparator();
        }
        return "";
    }

    private String buildInsertCountReport(long insertCount) {
        if (insertCount > 0) {
            return "INSERT: " + insertCount + System.lineSeparator();
        }
        return "";
    }

    private String buildSelectCountReport(long selectCount) {
        if (selectCount > 0) {
            return "SELECT: " + selectCount + System.lineSeparator();
        }
        return "";
    }

    private String buildDeleteCountReport(long deleteCount) {
        if (deleteCount > 0) {
            return "DELETE: " + deleteCount + System.lineSeparator();
        }
        return "";
    }

    private String getMaxTime(SqlExecutions sqlExecutions ){
        if (sqlExecutions.getNumberOfExecutions() == 0){
            return "";
        }

        long maxExecutionTime = 0;

        for (SqlExecution execution : sqlExecutions) {

            long executionTime = execution.getElapsedTime();

            if(executionTime > maxExecutionTime) {
                maxExecutionTime = executionTime;
            }

        }

        return "MAX TIME: " + new ExecutionTime(maxExecutionTime, TimeUnit.MILLISECONDS).toString() + System.lineSeparator();
    }

}