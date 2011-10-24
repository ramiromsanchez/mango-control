/*
    Mango - Open Source M2M - http://mango.serotoninsoftware.com
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc.
    @author Matthew Lohbihler
    
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.serotonin.mango.rt.dataSource.gcal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.List;

import com.google.gdata.client.*;
import com.google.gdata.client.calendar.*;
import com.google.gdata.data.*;

import com.google.gdata.data.calendar.*;
import com.google.gdata.data.extensions.*;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.io.StreamUtils;
import com.serotonin.mango.DataTypes;
import com.serotonin.mango.rt.dataImage.DataPointRT;
import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.rt.dataImage.SetPointSource;
import com.serotonin.mango.rt.dataImage.types.AlphanumericValue;
import com.serotonin.mango.rt.dataImage.types.BinaryValue;
import com.serotonin.mango.rt.dataImage.types.ImageValue;
import com.serotonin.mango.rt.dataImage.types.MangoValue;
import com.serotonin.mango.rt.dataImage.types.MultistateValue;
import com.serotonin.mango.rt.dataImage.types.NumericValue;
import com.serotonin.mango.rt.dataSource.DataSourceRT;
import com.serotonin.mango.rt.dataSource.PollingDataSource;
import com.serotonin.mango.vo.dataSource.gcal.GCalDataSourceVO;
import com.serotonin.mango.vo.dataSource.gcal.GCalPointLocatorVO;
import com.serotonin.util.StringUtils;
import com.serotonin.web.i18n.LocalizableMessage;

/**
 * @author Dmitry xxx
 */
public class GCalDataSourceRT extends PollingDataSource {
    public static final int DATA_SOURCE_EXCEPTION_EVENT = 1;
    public static final int STATEMENT_EXCEPTION_EVENT = 2;
    public static final String GOOGLE_CAL_URL_PREFIX = "http://www.google.com/calendar/feeds/";
	private static final String prefix = "http://www.google.com/calendar/feeds/default/calendars/";

    private final Log log = LogFactory.getLog(GCalDataSourceRT.class);

    private final GCalDataSourceVO vo;
    private Connection conn;
    private CalendarService service;
    private CalendarQuery query;
    private URL feedURL;

    public GCalDataSourceRT(GCalDataSourceVO vo) {
        super(vo);
        setPollingPeriod(vo.getUpdatePeriodType(), vo.getUpdatePeriods(), false);
        this.vo = vo;
    }

    @Override
    public void setPointValue(DataPointRT dataPoint, PointValueTime valueTime, SetPointSource source) {
    	
 	
        dataPoint.setPointValue(valueTime, source);

    }

    @Override
    protected void doPoll(long time) {

    	
        for (DataPointRT dp : dataPoints) {
            GCalPointLocatorRT locatorRT = dp.getPointLocator();
            GCalPointLocatorVO locatorVO = locatorRT.getVO();
            
            locatorVO.setEvents(locatorRT.getEvents(service, vo.getCalendarFeed(), locatorVO.getDaysPeriod()));
            MangoValue value;
            try {
                value = getValue(locatorVO, time);
                dp.updatePointValue(new PointValueTime(value, time));
            }
            catch (IOException e) {
                continue;
            }
            
//            dp.updatePointValue(newValue);
            
        }
//    	CalendarQuery myQuery = new CalendarQuery(new URL("https://www.google.com/calendar/feeds/default/public/full"));
//    	myQuery.setMinimumStartTime(DateTime.parseDateTime("2011-10-16T00:00:00"));
//    	myQuery.setMaximumStartTime(DateTime.parseDateTime("2011-10-24T23:59:59"));
//    	CalendarEventFeed eventFeed = service.query(myQuery, CalendarEventFeed.class);
//    	for (int i = 0; i < eventFeed.getEntries().size(); i++) {
//    		CalendarEventEntry eEntry = eventFeed.getEntries().get(i);
//    		System.out.println("\t" + eEntry.getTitle().getPlainText());
//    		List<When> whens= eEntry.getTimes();
//    		for (When when1 : whens) {
//        		System.out.println("\t" + when1.getStartTime());
//        		System.out.println("\t" + when1.getEndTime());      			
//    		}
//    		}
    	
        // If there is no select statement, don't bother. It's true that we wouldn't need to bother polling at all,
        // but for now this will do.
//        if (StringUtils.isEmpty(vo.getSelectStatement()))
//            return;

//        PreparedStatement stmt = null;

//        try {
//            stmt = conn.prepareStatement(vo.getSelectStatement());
//            if (vo.isRowBasedQuery())
//                doRowPollImpl(time, stmt);
//            else
//                doColumnPollImpl(time, stmt);
//        }
//        catch (Exception e) {
//            raiseEvent(STATEMENT_EXCEPTION_EVENT, time, true, getExceptionMessage(e));
//        }
//        finally {
//            try {
//                if (stmt != null)
//                    stmt.close();
//            }
//            catch (SQLException e) {
//                // no op
//            }
//        }
    }


    private MangoValue getValue(GCalPointLocatorVO locatorVO, long time)
            throws IOException {

    	int dataType = locatorVO.getDataTypeId();
            if (dataType == DataTypes.ALPHANUMERIC)
                return new AlphanumericValue(locatorVO.getEvents());
            else if (dataType == DataTypes.BINARY)
                return new BinaryValue(locatorVO.getNumEvents() > 0);
            else if (dataType == DataTypes.MULTISTATE)
                return new MultistateValue(locatorVO.getNumEvents());
            else if (dataType == DataTypes.NUMERIC)
                return new NumericValue(locatorVO.getNumEvents());
            else if (dataType == DataTypes.IMAGE) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
//                StreamUtils.transfer(rs.getBlob(fieldName).getBinaryStream(), out);
                return new ImageValue(out.toByteArray(), ImageValue.TYPE_JPG);
            }
            else
                throw new ShouldNeverHappenException("What's this?: " + locatorVO.getDataTypeId());
    }


    //
    // /
    // / Lifecycle
    // /
    //
    @Override
    public void initialize() {
    	
    	
        // Get a connection to the database. No need to pool, because we don't intend to close it until we shut down.
        try {
        	service = new CalendarService("");
        	service.setUserCredentials(vo.getUsername(), vo.getPassword());

        	
        	// Send the request and print the response
        	feedURL = new URL(GOOGLE_CAL_URL_PREFIX + vo.getCalendarFeed() + "/private/full");
        	query = new CalendarQuery(new URL("https://www.google.com/calendar/feeds/default/public/full"));
//    		CalendarEventFeed calFeed = service.getFeed(feedURL, CalendarEventFeed.class);
//        		System.out.println("\tFeedcontent of " + feedUrl.toString());
//        		CalendarEventFeed calFeed = service.getFeed(feedUrl, CalendarEventFeed.class);
//        		System.out.println("\t" + calFeed.getEntries());
//        		for (CalendarEventEntry  myEntry : calFeed.getEntries()) {
//            		System.out.println("\t" + myEntry.getTitle().getPlainText());
//            		List<When> whens= myEntry.getTimes();
//            		for (When when1 : whens) {
//                		System.out.println("\t" + when1.getStartTime());
//                		System.out.println("\t" + when1.getEndTime());      			
//            		}
//        		}
////        	}
        	
//        	CalendarQuery myQuery = new CalendarQuery(new URL("https://www.google.com/calendar/feeds/default/public/full"));
//        	myQuery.setMinimumStartTime(DateTime.parseDateTime("2011-10-16T00:00:00"));
//        	myQuery.setMaximumStartTime(DateTime.parseDateTime("2011-10-24T23:59:59"));
//        	CalendarEventFeed eventFeed = service.query(myQuery, CalendarEventFeed.class);
//        	for (int i = 0; i < eventFeed.getEntries().size(); i++) {
//        		CalendarEventEntry eEntry = eventFeed.getEntries().get(i);
//        		System.out.println("\t" + eEntry.getTitle().getPlainText());
//        		List<When> whens= eEntry.getTimes();
//        		for (When when1 : whens) {
//            		System.out.println("\t" + when1.getStartTime());
//            		System.out.println("\t" + when1.getEndTime());      			
//        		}
//        		}
        	
//        	DriverManager.registerDriver((Driver) Class.forName(vo.getDriverClassname()).newInstance());
//            conn = DriverManager.getConnection(vo.getConnectionUrl(), vo.getUsername(), vo.getPassword());
//
//            // Test the connection.
//            conn.getMetaData();

            // Deactivate any existing event.
            returnToNormal(DATA_SOURCE_EXCEPTION_EVENT, System.currentTimeMillis());
        }
        catch (Exception e) {
            raiseEvent(DATA_SOURCE_EXCEPTION_EVENT, System.currentTimeMillis(), true,
                    DataSourceRT.getExceptionMessage(e));
            log.info("Error while initializing data source", e);
            return;
        }

        super.initialize();
    }

    @Override
    public void terminate() {
        super.terminate();

//        try {
//            if (conn != null)
//                conn.close();
//        }
//        catch (SQLException e) {
//            throw new ShouldNeverHappenException(e);
//        }
    }
}
