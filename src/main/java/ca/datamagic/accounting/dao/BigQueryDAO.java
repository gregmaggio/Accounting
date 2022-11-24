/**
 * 
 */
package ca.datamagic.accounting.dao;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.FieldValueList;
import com.google.cloud.bigquery.Job;
import com.google.cloud.bigquery.JobId;
import com.google.cloud.bigquery.JobInfo;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.TableResult;

import ca.datamagic.accounting.dto.EventDTO;
import ca.datamagic.accounting.dto.EventNameDTO;
import ca.datamagic.accounting.dto.EventTrendDTO;
import ca.datamagic.accounting.dto.EventTrendSeriesDTO;

/**
 * @author gregm
 *
 */
public class BigQueryDAO extends BaseDAO {
	private static final Logger logger = LogManager.getLogger(BigQueryDAO.class);
	private BigQuery bigQuery = null;
	
	public String getProjectId() throws IOException {
		return this.getProperties().getProperty("projectId");
	}
	
	public String getDatasetName() throws IOException {
		return this.getProperties().getProperty("datasetName");
	}

	public String getTableName() throws IOException {
		return this.getProperties().getProperty("tableName");
	}
	
	public BigQuery getBigQuery() throws IOException {
		if (this.bigQuery == null) {
			this.bigQuery = BigQueryOptions.newBuilder().setCredentials(getCredentials()).build().getService();
		}
		return this.bigQuery;
	}
	
	public TableResult runQuery(String query) throws IOException, InterruptedException {
		logger.debug("query: " + query);
		QueryJobConfiguration queryConfig =
                QueryJobConfiguration.newBuilder(query)
                        // Use standard SQL syntax for queries.
                        // See: https://cloud.google.com/bigquery/sql-reference/
                        .setUseLegacySql(false)
                        .build();
        // Create a job ID so that we can safely retry.
        JobId jobId = JobId.of(UUID.randomUUID().toString().toUpperCase());
        Job queryJob = this.getBigQuery().create(JobInfo.newBuilder(queryConfig).setJobId(jobId).build());
        queryJob = queryJob.waitFor();

        // Check for errors
        if (queryJob == null) {
            throw new RuntimeException("Job no longer exists");
        } else if (queryJob.getStatus().getError() != null) {
            // You can also look at queryJob.getStatus().getExecutionErrors() for all
            // errors, not just the latest one.
            throw new RuntimeException(queryJob.getStatus().getError().toString());
        }

        // Get the results
        return queryJob.getQueryResults();
	}
	
	public List<EventDTO> loadEvents(int startYear, int startMonth, int startDay, int endYear, int endMonth, int endDay) throws IOException, InterruptedException {
		String utcStartTime = String.format("%04d-%02d-%02dT00:00:00Z", startYear, startMonth, startDay);
		String utcEndTime = String.format("%04d-%02d-%02dT23:59:59Z", endYear, endMonth, endDay);
		String query = MessageFormat.format("select eventName, eventMessage, count(eventMessage) as eventCount from {0}.{1}.{2} where utcTimeStamp >= {3} and utcTimeStamp <= {4} group by eventName, eventMessage order by eventName, eventMessage", 
				getProjectId(), 
				getDatasetName(), 
				getTableName(),
				"'" + utcStartTime + "'",
				"'" + utcEndTime + "'");
		TableResult result = this.runQuery(query);
		List<EventDTO> events = new ArrayList<EventDTO>();
		for (FieldValueList row : result.iterateAll()) {
			EventDTO dto = new EventDTO();
			dto.setEventName(row.get("eventName").getStringValue());
			dto.setEventMessage(row.get("eventMessage").getStringValue());
			dto.setCount(row.get("eventCount").getLongValue());
			events.add(dto);			
        }
		return events;		
	}
	
	public List<EventNameDTO> loadEventNames() throws IOException, InterruptedException {
		String query = MessageFormat.format("select distinct eventName, eventMessage from {0}.{1}.{2} order by eventName, eventMessage", getProjectId(), getDatasetName(), getTableName());
		TableResult result = this.runQuery(query);
		List<EventNameDTO> eventNames = new ArrayList<EventNameDTO>();
		for (FieldValueList row : result.iterateAll()) {
			EventNameDTO dto = new EventNameDTO();
			dto.setEventName(row.get("eventName").getStringValue());
			dto.setEventMessage(row.get("eventMessage").getStringValue());
			eventNames.add(dto);			
        }
		return eventNames;		
	}
	
	public EventTrendDTO loadEventTrend(int startYear, int startMonth, int startDay, int endYear, int endMonth, int endDay, String eventName, String eventMessage) throws IOException, InterruptedException {
		String utcStartTime = String.format("%04d-%02d-%02dT00:00:00Z", startYear, startMonth, startDay);
		String utcEndTime = String.format("%04d-%02d-%02dT23:59:59Z", endYear, endMonth, endDay);
		String query = MessageFormat.format("select utcYear, utcMonth, utcDay, count(eventMessage) as eventCount from {0}.{1}.{2} where utcTimeStamp >= {3} and utcTimeStamp <= {4} and eventName = {5} and eventMessage = {6} group by utcYear, utcMonth, utcDay order by utcYear, utcMonth, utcDay' ", 
				getProjectId(), 
				getDatasetName(), 
				getTableName(),
				"'" + utcStartTime + "'",
				"'" + utcEndTime + "'",
				"'" + eventName + "'",
				"'" + eventMessage + "'");
		TableResult result = this.runQuery(query);
		List<EventTrendSeriesDTO> eventTrendSeries = new ArrayList<EventTrendSeriesDTO>();
		for (FieldValueList row : result.iterateAll()) {
			EventTrendSeriesDTO dto = new EventTrendSeriesDTO();
			dto.setYear((int)row.get("utcYear").getLongValue());
			dto.setMonth((int)row.get("utcMonth").getLongValue());
			dto.setDay((int)row.get("utcDay").getLongValue());
			dto.setCount(row.get("eventCount").getLongValue());
			eventTrendSeries.add(dto);			
        }
		EventTrendDTO eventTrend = new EventTrendDTO();
		eventTrend.setEventName(eventName);
		eventTrend.setEventMessage(eventMessage);
		eventTrend.setSeries(eventTrendSeries);
		return eventTrend;
	}
	
	public String minTimeStamp() throws IOException, InterruptedException {
		String query = MessageFormat.format("select min(utcTimeStamp) as minTimeStamp from {0}.{1}.{2}", getProjectId(), getDatasetName(), getTableName());
		TableResult result = this.runQuery(query);
		for (FieldValueList row : result.iterateAll()) {
			return row.get("minTimeStamp").getStringValue();
		}
		return null;		
	}
	
	public String maxTimeStamp() throws IOException, InterruptedException {
		String query = MessageFormat.format("select max(utcTimeStamp) as maxTimeStamp from {0}.{1}.{2}", getProjectId(), getDatasetName(), getTableName());
		TableResult result = this.runQuery(query);
		for (FieldValueList row : result.iterateAll()) {
			return row.get("maxTimeStamp").getStringValue();
		}
		return null;
	}
}
