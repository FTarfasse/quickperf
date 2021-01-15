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

import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.QueryType;
import org.quickperf.issue.PerfIssue;
import org.quickperf.issue.VerifiablePerformanceIssue;
import org.quickperf.sql.SqlExecution;
import org.quickperf.sql.SqlExecutions;
import org.quickperf.sql.annotation.AnalyzeSql;
import org.quickperf.sql.bindparams.AllParametersAreBoundExtractor;
import org.quickperf.sql.like.ContainsLikeWithLeadingWildcardExtractor;
import org.quickperf.sql.select.analysis.SelectAnalysis;
import org.quickperf.time.ExecutionTime;
import org.quickperf.writer.PrintWriterBuilder;
import org.quickperf.writer.WriterFactory;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
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
            sqlReport.append(getMaxTime(sqlExecutions));
            sqlReport.append(buildSelectMessages(sqlAnalysis));
            sqlReport.append(buildInsertMessage(sqlExecutions)); // TODO: annotation analyzing the Hibernate sequence call executed in insert statements
            sqlReport.append(buildUpdateMessage(sqlExecutions));
            sqlReport.append(buildDeleteMessage(sqlExecutions));
            sqlReport.append(displayQueries(sqlExecutions));
            sqlReport.append(buildNPlusOneMessage(sqlAnalysis));
            pw.printf(annotation.format(), sqlReport);
        }

        return PerfIssue.NONE;
    }

    private String buildSelectMessages(SqlAnalysis sqlAnalysis) {
        String mes = "";
        if (sqlAnalysis.getSqlExecutions().retrieveQueryNumberOfType(QueryType.SELECT) == 0) return mes;
        SelectAnalysis selectAnalysis = sqlAnalysis.getSelectAnalysis();
        SqlExecutions selectExecutions = sqlAnalysis.getSqlExecutions().filterByQueryType(QueryType.SELECT);
        long selectCount = sqlAnalysis.getSqlExecutions().retrieveQueryNumberOfType(QueryType.SELECT);

        mes += buildSelectCountReport(selectCount);

        if (selectAnalysis.hasSameSelects()) {
            mes += "- Same SELECT statements" + System.lineSeparator();
        }
        if (checkIfWildcard(selectExecutions)) {
            mes += "- Like with leading wildcard detected (% or _)" + System.lineSeparator();
        }
        if (checkIfBindParameters(selectExecutions)) {
            mes += "- Query without bind parameters" + System.lineSeparator();
        }

        return mes;
    }

    private String buildNPlusOneMessage(SqlAnalysis sqlAnalysis) {
        String mes = "";
        if (sqlAnalysis.getSelectAnalysis().getSameSelectTypesWithDifferentParamValues().evaluate()) {
            mes += this.addSeparationString() + "HINTS:" + sqlAnalysis.getSelectAnalysis().getSameSelectTypesWithDifferentParamValues().getSuggestionToFixIt();
        }
        return mes;
    }

    private String displayQueries(SqlExecutions sqlExecutions) {
        if (sqlExecutions.getNumberOfExecutions() == 0) return "";

        List<String> queriesList = new ArrayList<>();
        for (SqlExecution execution : sqlExecutions) {
            for (QueryInfo query : execution.getQueries()) {
                queriesList.add(query.getQuery());
            }
        }
        String queries = "";
        for(String query: queriesList){
           queries += "- " + query + System.lineSeparator();
        }
        // manage plural query / queries
        return sqlExecutions.getNumberOfExecutions() > 1 ? this.addSeparationString() + "QUERIES: " + System.lineSeparator() + queries
                : this.addSeparationString() + "QUERY: " + System.lineSeparator() + queries;
    }

    private String jdbcExecutions(SqlExecutions sqlExecutions) {
        return this.addSeparationString() + "SQL EXECUTIONS: " + sqlExecutions.getNumberOfExecutions() + System.lineSeparator();
    }

    private String buildUpdateCountReport(long updateCount) {
        if (updateCount > 0) {
            return this.addSeparationString() + "UPDATE: " + updateCount + System.lineSeparator();
        }
        return "";
    }

    private String buildInsertCountReport(long insertCount) {
        if (insertCount > 0) {
            return this.addSeparationString() + "INSERT: " + insertCount + System.lineSeparator();
        }
        return "";
    }

    private String buildInsertMessage(SqlExecutions sqlExecutions) {

        if (sqlExecutions.retrieveQueryNumberOfType(QueryType.INSERT) == 0) return "";

        String mes = "";
        long insertCount = sqlExecutions.retrieveQueryNumberOfType(QueryType.INSERT);
        mes += buildInsertCountReport(insertCount);
        SqlExecutions insertExecutions = sqlExecutions.filterByQueryType(QueryType.INSERT);

        if (checkIfBindParameters(insertExecutions)) {
            mes += "- Query without bind parameters" + System.lineSeparator();
        }

        return mes;
    }

    private String buildUpdateMessage(SqlExecutions sqlExecutions) {

        if (sqlExecutions.retrieveQueryNumberOfType(QueryType.UPDATE) == 0) return "";

        String mes = "";
        SqlExecutions updateExecutions = sqlExecutions.filterByQueryType(QueryType.UPDATE);
        long updateCount = sqlExecutions.retrieveQueryNumberOfType(QueryType.UPDATE);
        mes += buildUpdateCountReport(updateCount);

        if (checkIfBindParameters(updateExecutions)) {
            mes += "- Query without bind parameters" + System.lineSeparator();
        }

        return mes;
    }

    private String buildDeleteMessage(SqlExecutions sqlExecutions) {

        if (sqlExecutions.retrieveQueryNumberOfType(QueryType.DELETE) == 0) return "";

        String mes = "";
        SqlExecutions updateExecutions = sqlExecutions.filterByQueryType(QueryType.DELETE);
        long deleteCount = sqlExecutions.retrieveQueryNumberOfType(QueryType.DELETE);
        mes += buildDeleteCountReport(deleteCount);

        if (checkIfBindParameters(updateExecutions)) {
            mes += "- Query without bind parameters" + System.lineSeparator();
        }

        return mes;
    }

    private String buildSelectCountReport(long selectCount) {
        if (selectCount > 0) {
            return this.addSeparationString() + "SELECT: " + selectCount + System.lineSeparator();
        }
        return "";
    }

    private String buildDeleteCountReport(long deleteCount) {
        if (deleteCount > 0) {
            return this.addSeparationString() + "DELETE: " + deleteCount + System.lineSeparator();
        }
        return "";
    }

    private String getMaxTime(SqlExecutions sqlExecutions) {
        if (sqlExecutions.getNumberOfExecutions() == 0) {
            return "";
        }

        long maxExecutionTime = 0;

        for (SqlExecution execution : sqlExecutions) {

            long executionTime = execution.getElapsedTime();

            if (executionTime > maxExecutionTime) {
                maxExecutionTime = executionTime;
            }

        }

        // return this.addSeparationString() + "MAX TIME: " + new ExecutionTime(maxExecutionTime, TimeUnit.MILLISECONDS).toString() + System.lineSeparator();
        return "MAX TIME: " + new ExecutionTime(maxExecutionTime, TimeUnit.MILLISECONDS).toString() + System.lineSeparator();
    }

    private boolean checkIfWildcard(SqlExecutions sqlExecutions) {
        return ContainsLikeWithLeadingWildcardExtractor.INSTANCE.extractPerfMeasureFrom(sqlExecutions).getValue();
    }

    private boolean checkIfBindParameters(SqlExecutions sqlExecutions) {
        return !AllParametersAreBoundExtractor.INSTANCE.extractPerfMeasureFrom(sqlExecutions).getValue();
    }

    private String addSeparationString() {
        return "------------------------------------------------------------------------------------------------------------------------" + System.lineSeparator();

    }
}
