package org.sagebionetworks.bridge.hibernate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import org.sagebionetworks.bridge.BridgeConstants;
import org.sagebionetworks.bridge.dao.UploadTableRowDao;
import org.sagebionetworks.bridge.models.PagedResourceList;
import org.sagebionetworks.bridge.upload.UploadTableRow;
import org.sagebionetworks.bridge.upload.UploadTableRowQuery;

/** Hibernate implementation of UploadTableRowDao. */
@Component
public class HibernateUploadTableRowDao implements UploadTableRowDao {
    private HibernateHelper hibernateHelper;

    @Resource(name = "basicHibernateHelper")
    public final void setHibernateHelper(HibernateHelper hibernateHelper) {
        this.hibernateHelper = hibernateHelper;
    }

    @Override
    public void deleteUploadTableRow(String appId, String studyId, String recordId) {
        HibernateUploadTableRowId id = new HibernateUploadTableRowId(appId, studyId, recordId);
        hibernateHelper.deleteById(HibernateUploadTableRow.class, id);
    }

    @Override
    public Optional<UploadTableRow> getUploadTableRow(String appId, String studyId, String recordId) {
        HibernateUploadTableRowId id = new HibernateUploadTableRowId(appId, studyId, recordId);
        UploadTableRow row = hibernateHelper.getById(HibernateUploadTableRow.class, id);
        return Optional.ofNullable(row);
    }

    @Override
    public PagedResourceList<UploadTableRow> queryUploadTableRows(UploadTableRowQuery query) {
        QueryBuilder builder = new QueryBuilder();
        builder.append("from HibernateUploadTableRow");

        // appId and studyId are always required. (This is validated in the service.)
        builder.append("WHERE appId = :appId AND studyId = :studyId");
        builder.getParameters().put("appId", query.getAppId());
        builder.getParameters().put("studyId", query.getStudyId());

        // Filter by assessment. If assessment ID is specified, so must assessment revision. (This is validated in the
        // service.)
        if (query.getAssessmentId() != null) {
            builder.append("AND assessmentId = :assessmentId AND assessmentRevision = :assessmentRevision");
            builder.getParameters().put("assessmentId", query.getAssessmentId());
            builder.getParameters().put("assessmentRevision", query.getAssessmentRevision());
        }

        // Filter by date range.
        if (query.getStartDate() != null) {
            builder.append("AND createdOn >= :startDate");
            builder.getParameters().put("startDate", query.getStartDate().getMillis());
        }
        if (query.getEndDate() != null) {
            builder.append("AND createdOn < :endDate");
            builder.getParameters().put("endDate", query.getEndDate().getMillis());
        }

        // Include test data?
        if (!query.getIncludeTestData()) {
            builder.append("AND testData = 0");
        }

        // Order by createdOn.
        builder.append("ORDER BY createdOn ASC");

        // Get total.
        int total = hibernateHelper.queryCount("SELECT COUNT(DISTINCT recordId) " + builder.getQuery(),
                builder.getParameters());

        // Start and count.
        Integer start = query.getStart();
        if (start == null) {
            start = 0;
        }
        Integer pageSize = query.getPageSize();
        if (pageSize == null) {
            pageSize = BridgeConstants.API_DEFAULT_PAGE_SIZE;
        }

        // Query.
        List<HibernateUploadTableRow> hibernateList = hibernateHelper.queryGet(builder.getQuery(),
                builder.getParameters(), start, pageSize, HibernateUploadTableRow.class);

        // Because of Java generic typing issues, we need to convert this to a non-Hibernate UploadTableRow.
        List<UploadTableRow> list = new ArrayList<>(hibernateList);
        return new PagedResourceList<>(list, total)
                .withRequestParam("appId", query.getAppId())
                .withRequestParam("studyId", query.getStudyId())
                .withRequestParam("assessmentId", query.getAssessmentId())
                .withRequestParam("assessmentRevision", query.getAssessmentRevision())
                .withRequestParam("startDate", query.getStartDate())
                .withRequestParam("endDate", query.getEndDate())
                .withRequestParam("includeTestData", query.getIncludeTestData())
                .withRequestParam("start", start)
                .withRequestParam("pageSize", pageSize);
    }

    @Override
    public void saveUploadTableRow(UploadTableRow row) {
        hibernateHelper.saveOrUpdate(row);
    }
}
