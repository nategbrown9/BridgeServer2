package org.sagebionetworks.bridge.hibernate;

import static org.sagebionetworks.bridge.TestConstants.CREATED_ON;
import static org.sagebionetworks.bridge.TestConstants.MODIFIED_ON;
import static org.sagebionetworks.bridge.TestConstants.TEST_STUDY_ID;
import static org.sagebionetworks.bridge.TestConstants.TEST_USER_ID;
import static org.sagebionetworks.bridge.hibernate.HibernateStudyActivityEventDao.DELETE_SQL;
import static org.sagebionetworks.bridge.hibernate.HibernateStudyActivityEventDao.EVENT_ID_FIELD;
import static org.sagebionetworks.bridge.hibernate.HibernateStudyActivityEventDao.GET_RECENT_SQL;
import static org.sagebionetworks.bridge.hibernate.HibernateStudyActivityEventDao.HISTORY_SQL;
import static org.sagebionetworks.bridge.hibernate.HibernateStudyActivityEventDao.STUDY_ID_FIELD;
import static org.sagebionetworks.bridge.hibernate.HibernateStudyActivityEventDao.USER_ID_FIELD;
import static org.sagebionetworks.bridge.models.ResourceList.OFFSET_BY;
import static org.sagebionetworks.bridge.models.ResourceList.PAGE_SIZE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.sagebionetworks.bridge.models.PagedResourceList;
import org.sagebionetworks.bridge.models.activities.StudyActivityEvent;

public class HibernateStudyActivityEventDaoTest extends Mockito {

    @Mock
    HibernateHelper mockHelper;
    
    @InjectMocks
    HibernateStudyActivityEventDao dao;
    
    @Captor
    ArgumentCaptor<Map<String,Object>> paramsCaptor;
    
    @BeforeMethod
    public void beforeMethod() {
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void deleteCustomEvent() {
        StudyActivityEvent event = new StudyActivityEvent();
        event.setUserId(TEST_USER_ID);
        event.setStudyId(TEST_STUDY_ID);
        event.setEventId("custom:event1");
        
        dao.deleteCustomEvent(event);
        
        verify(mockHelper).nativeQueryUpdate(eq(DELETE_SQL), paramsCaptor.capture());
        Map<String,Object> params = paramsCaptor.getValue();
        assertEquals(params.get(USER_ID_FIELD), TEST_USER_ID);
        assertEquals(params.get(STUDY_ID_FIELD), TEST_STUDY_ID);
        assertEquals(params.get(EVENT_ID_FIELD), "custom:event1");
    }
    
    @Test
    public void publishEvent() {
        StudyActivityEvent event = new StudyActivityEvent();
        
        dao.publishEvent(event);
        
        verify(mockHelper).saveOrUpdate(event);
    }
    
    @Test
    public void getRecentStudyActivityEvents() { 
        List<StudyActivityEvent> list = ImmutableList.of(
                new StudyActivityEvent(), new StudyActivityEvent());
        when(mockHelper.nativeQueryGet(any(), any(), any(), any(), eq(StudyActivityEvent.class)))
            .thenReturn(list);
        
        List<StudyActivityEvent> retValue = dao.getRecentStudyActivityEvents(
                TEST_USER_ID, TEST_STUDY_ID);
        assertSame(retValue, list);
        
        verify(mockHelper).nativeQueryGet(eq(GET_RECENT_SQL), 
                paramsCaptor.capture(), eq(null), eq(null), eq(StudyActivityEvent.class));
        Map<String,Object> params = paramsCaptor.getValue();
        assertEquals(params.get(USER_ID_FIELD), TEST_USER_ID);
        assertEquals(params.get(STUDY_ID_FIELD), TEST_STUDY_ID);
    }
    
    @Test
    public void getRecentStudyActivityEvent() {
        StudyActivityEvent event1 = new StudyActivityEvent();
        event1.setEventId("custom:event1");
        event1.setTimestamp(CREATED_ON);
        
        StudyActivityEvent event2 = new StudyActivityEvent();
        event2.setEventId("custom:event2");
        event2.setTimestamp(MODIFIED_ON);
        
        when(mockHelper.nativeQueryGet(any(), any(), any(), any(), eq(StudyActivityEvent.class)))
            .thenReturn(ImmutableList.of(event1, event2));
        
        StudyActivityEvent retValue = dao.getRecentStudyActivityEvent(
                TEST_USER_ID, TEST_STUDY_ID, "custom:event2");
        assertSame(retValue, event2);
        
        verify(mockHelper).nativeQueryGet(eq(GET_RECENT_SQL), paramsCaptor.capture(), 
                eq(null), eq(null), eq(StudyActivityEvent.class));
        Map<String,Object> params = paramsCaptor.getValue();
        assertEquals(params.get(USER_ID_FIELD), TEST_USER_ID);
        assertEquals(params.get(STUDY_ID_FIELD), TEST_STUDY_ID);
    }
    
    @Test
    public void getStudyActivityEventHistory() {
        List<StudyActivityEvent> list = ImmutableList.of(new StudyActivityEvent(), new StudyActivityEvent());
        
        when(mockHelper.nativeQueryGet(eq("SELECT * " + HISTORY_SQL), any(), 
                eq(150), eq(50), eq(StudyActivityEvent.class))).thenReturn(list);
        when(mockHelper.nativeQueryCount(eq("SELECT count(*) " + HISTORY_SQL), any())).thenReturn(200);
        
        PagedResourceList<StudyActivityEvent> retValue = dao.getStudyActivityEventHistory(
                TEST_USER_ID, TEST_STUDY_ID, "custom:event1", 150, 50);
        
        assertEquals(retValue.getTotal(), Integer.valueOf(200));
        assertEquals(retValue.getItems().size(), 2);
        assertEquals(retValue.getRequestParams().get(OFFSET_BY), Integer.valueOf(150));
        assertEquals(retValue.getRequestParams().get(PAGE_SIZE), Integer.valueOf(50));
        
        verify(mockHelper).nativeQueryGet(eq("SELECT * " + HISTORY_SQL), paramsCaptor.capture(), 
                eq(150), eq(50), eq(StudyActivityEvent.class));
        verify(mockHelper).nativeQueryCount(eq("SELECT count(*) " + HISTORY_SQL), paramsCaptor.capture());
        
        Map<String,Object> params = paramsCaptor.getAllValues().get(0);
        assertEquals(params.get(USER_ID_FIELD), TEST_USER_ID);
        assertEquals(params.get(STUDY_ID_FIELD), TEST_STUDY_ID);
        assertEquals(params.get(EVENT_ID_FIELD), "custom:event1");

        params = paramsCaptor.getAllValues().get(1);
        assertEquals(params.get(USER_ID_FIELD), TEST_USER_ID);
        assertEquals(params.get(STUDY_ID_FIELD), TEST_STUDY_ID);
        assertEquals(params.get(EVENT_ID_FIELD), "custom:event1");
    }
}
