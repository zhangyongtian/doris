// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.apache.doris.statistics;

import org.apache.doris.catalog.Database;
import org.apache.doris.catalog.Env;
import org.apache.doris.catalog.MaterializedIndexMeta;
import org.apache.doris.catalog.OlapTable;
import org.apache.doris.catalog.Table;
import org.apache.doris.common.Config;
import org.apache.doris.common.FeConstants;
import org.apache.doris.common.util.MasterDaemon;
import org.apache.doris.datasource.CatalogIf;
import org.apache.doris.datasource.InternalCatalog;
import org.apache.doris.statistics.util.InternalQueryResult.ResultRow;
import org.apache.doris.statistics.util.StatisticsUtil;
import org.apache.doris.system.SystemInfoService;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.text.StringSubstitutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Maintenance the internal statistics table.
 * Delete rows that corresponding DB/Table/Column not exists anymore.
 */
public class StatisticsCleaner extends MasterDaemon {

    private static final Logger LOG = LogManager.getLogger(StatisticsCleaner.class);

    private OlapTable colStatsTbl;
    private OlapTable histStatsTbl;

    private OlapTable jobTbl;

    private Map<Long, CatalogIf> idToCatalog;

    /* Internal DB only */
    private Map<Long, Database> idToDb;

    /* Internal tbl only */
    private Map<Long, Table> idToTbl;

    private Map<Long, MaterializedIndexMeta> idToMVIdx;

    public StatisticsCleaner() {
        super("Statistics Table Cleaner",
                TimeUnit.HOURS.toMillis(StatisticConstants.STATISTIC_CLEAN_INTERVAL_IN_HOURS));
    }

    @Override
    protected void runAfterCatalogReady() {
        if (!Env.getCurrentEnv().isMaster()) {
            return;
        }
        clear();
    }

    public synchronized void clear() {
        if (!init()) {
            return;
        }
        clearStats(colStatsTbl);
        clearStats(histStatsTbl);
        clearJobTbl();
    }

    private void clearStats(OlapTable statsTbl) {
        ExpiredStats expiredStats = null;
        long offset = 0;
        do {
            expiredStats = new ExpiredStats();
            offset = findExpiredStats(statsTbl, expiredStats, offset);
            deleteExpiredStats(expiredStats, statsTbl.getName());
        } while (!expiredStats.isEmpty());
    }

    private void clearJobTbl() {
        List<String> jobIds = null;
        long offset = 0;
        do {
            jobIds = new ArrayList<>();
            offset = findExpiredJobs(jobIds, offset);
            doDelete("job_id", jobIds, FeConstants.INTERNAL_DB_NAME + "."
                    + StatisticConstants.ANALYSIS_JOB_TABLE);
        } while (!jobIds.isEmpty());
    }

    private boolean init() {
        try {
            String dbName = SystemInfoService.DEFAULT_CLUSTER + ":" + FeConstants.INTERNAL_DB_NAME;
            colStatsTbl =
                    (OlapTable) StatisticsUtil
                            .findTable(InternalCatalog.INTERNAL_CATALOG_NAME,
                                    dbName,
                                    StatisticConstants.STATISTIC_TBL_NAME);
            histStatsTbl =
                    (OlapTable) StatisticsUtil
                            .findTable(InternalCatalog.INTERNAL_CATALOG_NAME,
                                    dbName,
                                    StatisticConstants.HISTOGRAM_TBL_NAME);
            jobTbl = (OlapTable) StatisticsUtil.findTable(InternalCatalog.INTERNAL_CATALOG_NAME,
                    dbName,
                    StatisticConstants.ANALYSIS_JOB_TABLE);
        } catch (Throwable t) {
            LOG.warn("Failed to init stats cleaner", t);
            return false;
        }

        idToCatalog = Env.getCurrentEnv().getCatalogMgr().getIdToCatalog();
        idToDb = Env.getCurrentEnv().getInternalCatalog().getIdToDb();
        idToTbl = constructTblMap();
        idToMVIdx = constructIdxMap();
        return true;
    }

    private Map<Long, Table> constructTblMap() {
        Map<Long, Table> idToTbl = new HashMap<>();
        for (Database db : idToDb.values()) {
            idToTbl.putAll(db.getIdToTable());
        }
        return idToTbl;
    }

    private Map<Long, MaterializedIndexMeta> constructIdxMap() {
        Map<Long, MaterializedIndexMeta> idToMVIdx = new HashMap<>();
        for (Table t : idToTbl.values()) {
            if (t instanceof OlapTable) {
                OlapTable olapTable = (OlapTable) t;
                olapTable.getCopyOfIndexIdToMeta()
                        .entrySet()
                        .stream()
                        .filter(idx -> idx.getValue().getDefineStmt() != null)
                        .forEach(e -> idToMVIdx.put(e.getKey(), e.getValue()));
            }
        }
        return idToMVIdx;
    }

    private void deleteExpiredStats(ExpiredStats expiredStats, String tblName) {
        doDelete("catalog_id", expiredStats.expiredCatalog.stream()
                        .map(String::valueOf).collect(Collectors.toList()),
                FeConstants.INTERNAL_DB_NAME + "." + tblName);
        doDelete("db_id", expiredStats.expiredDatabase.stream()
                        .map(String::valueOf).collect(Collectors.toList()),
                FeConstants.INTERNAL_DB_NAME + "." + tblName);
        doDelete("tbl_id", expiredStats.expiredTable.stream()
                        .map(String::valueOf).collect(Collectors.toList()),
                FeConstants.INTERNAL_DB_NAME + "." + tblName);
        doDelete("idx_id", expiredStats.expiredIdxId.stream()
                        .map(String::valueOf).collect(Collectors.toList()),
                FeConstants.INTERNAL_DB_NAME + "." + tblName);
        doDelete("id", expiredStats.ids.stream()
                        .map(String::valueOf).collect(Collectors.toList()),
                FeConstants.INTERNAL_DB_NAME + "." + tblName);
    }

    private void doDelete(String/*col name*/ colName, List<String> pred, String tblName) {
        String deleteTemplate = "DELETE FROM " + tblName + " WHERE ${left} IN (${right})";
        if (CollectionUtils.isEmpty(pred)) {
            return;
        }
        String right = pred.stream().map(s -> "'" + s + "'").collect(Collectors.joining(","));
        Map<String, String> params = new HashMap<>();
        params.put("left", colName);
        params.put("right", right);
        String sql = new StringSubstitutor(params).replace(deleteTemplate);
        try {
            StatisticsUtil.execUpdate(sql);
        } catch (Exception e) {
            LOG.warn("Failed to delete expired stats!", e);
        }
    }

    private long findExpiredStats(OlapTable statsTbl, ExpiredStats expiredStats, long offset) {
        long pos = offset;
        while (pos < statsTbl.getRowCount()
                && !expiredStats.isFull()) {
            List<ResultRow> rows = StatisticsRepository.fetchStatsFullName(StatisticConstants.FETCH_LIMIT, pos);
            pos += StatisticConstants.FETCH_LIMIT;
            for (ResultRow r : rows) {
                try {
                    String id = r.getColumnValue("id");
                    long catalogId = Long.parseLong(r.getColumnValue("catalog_id"));
                    if (!idToCatalog.containsKey(catalogId)) {
                        expiredStats.expiredCatalog.add(catalogId);
                        continue;
                    }
                    long dbId = Long.parseLong(r.getColumnValue("db_id"));
                    if (!idToDb.containsKey(dbId)) {
                        expiredStats.expiredDatabase.add(dbId);
                        continue;
                    }
                    long tblId = Long.parseLong(r.getColumnValue("tbl_id"));
                    if (!idToTbl.containsKey(tblId)) {
                        expiredStats.expiredTable.add(tblId);
                        continue;
                    }

                    long idxId = Long.parseLong(r.getColumnValue("idx_id"));
                    if (idxId != -1 && !idToMVIdx.containsKey(idxId)) {
                        expiredStats.expiredIdxId.add(idxId);
                        continue;
                    }

                    Table t = idToTbl.get(tblId);
                    String colId = r.getColumnValue("col_id");
                    if (t.getColumn(colId) == null) {
                        expiredStats.ids.add(id);
                        continue;
                    }
                    if (!(t instanceof OlapTable)) {
                        continue;
                    }
                    OlapTable olapTable = (OlapTable) t;
                    String partIdStr = r.getColumnValue("part_id");
                    if (partIdStr == null) {
                        continue;
                    }
                    long partId = Long.parseLong(partIdStr);
                    if (!olapTable.getPartitionIds().contains(partId)) {
                        expiredStats.ids.add(id);
                    }
                } catch (Exception e) {
                    LOG.warn("Error occurred when retrieving expired stats", e);
                }
            }
            this.yieldForOtherTask();
        }
        return pos;
    }

    private long findExpiredJobs(List<String> jobIds, long offset) {
        long pos = offset;
        while (pos < jobTbl.getRowCount() && jobIds.size() < Config.max_allowed_in_element_num_of_delete) {
            List<ResultRow> rows = StatisticsRepository.fetchExpiredJobs(StatisticConstants.FETCH_LIMIT, pos);
            for (ResultRow r : rows) {
                try {
                    jobIds.add(r.getColumnValue("job_id"));
                } catch (Exception e) {
                    LOG.warn("Error when get job_id from ResultRow", e);
                }
            }
            pos += StatisticConstants.FETCH_LIMIT;
            this.yieldForOtherTask();
        }
        return pos;
    }

    private static class ExpiredStats {
        Set<Long> expiredCatalog = new HashSet<>();
        Set<Long> expiredDatabase = new HashSet<>();
        Set<Long> expiredTable = new HashSet<>();

        Set<Long> expiredIdxId = new HashSet<>();

        Set<String> ids = new HashSet<>();

        public boolean isFull() {
            return expiredCatalog.size() >= Config.max_allowed_in_element_num_of_delete
                    || expiredDatabase.size() >= Config.max_allowed_in_element_num_of_delete
                    || expiredTable.size() >= Config.max_allowed_in_element_num_of_delete
                    || expiredIdxId.size() >= Config.max_allowed_in_element_num_of_delete
                    || ids.size() >= Config.max_allowed_in_element_num_of_delete;
        }

        public boolean isEmpty() {
            return expiredCatalog.isEmpty()
                    && expiredDatabase.isEmpty()
                    && expiredTable.isEmpty()
                    && expiredIdxId.isEmpty()
                    && ids.size() < Config.max_allowed_in_element_num_of_delete / 10;
        }
    }

    // Avoid this task takes too much IO.
    private void yieldForOtherTask() {
        try {
            Thread.sleep(StatisticConstants.FETCH_INTERVAL_IN_MS);
        } catch (InterruptedException t) {
            // IGNORE
        }
    }

}
