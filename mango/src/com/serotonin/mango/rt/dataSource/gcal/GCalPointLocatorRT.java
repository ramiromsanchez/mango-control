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

import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import com.google.gdata.client.calendar.CalendarQuery;
import com.google.gdata.client.calendar.CalendarService;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.calendar.CalendarEventFeed;
import com.serotonin.mango.rt.dataSource.PointLocatorRT;
import com.serotonin.mango.vo.dataSource.gcal.GCalPointLocatorVO;

/**
 * @author Matthew Lohbihler
 */
public class GCalPointLocatorRT extends PointLocatorRT {
    private final GCalPointLocatorVO vo;   
    private static final String GOOGLE_CAL_URL_PREFIX = "https://www.google.com/calendar/feeds/";

    
    public GCalPointLocatorRT(GCalPointLocatorVO vo) {
        this.vo = vo;
    }

    @Override
    public boolean isSettable() {
        return vo.isSettable();
    }

    public GCalPointLocatorVO getVO() {
        return vo;
    }
    
    public List<String> getEvents(CalendarService service, String feed, int days) {

    	ArrayList<String> list = new ArrayList<String>();

    	try {

	    	URL feedURL = new URL(GOOGLE_CAL_URL_PREFIX + feed + "/private/full");
//    		URL feedURL = new URL("https://www.google.com/calendar/feeds/default/allcalendars/full/" + feed);
	    	CalendarQuery query = new CalendarQuery(feedURL);
	    	GregorianCalendar cal = new GregorianCalendar();
	    	cal.setTimeZone(TimeZone.getTimeZone("Russia/Moscow"));
	    	Date today = new Date();
	    	cal.setTime(today);
	    	cal.set(Calendar.HOUR_OF_DAY, 0);
	    	cal.set(Calendar.MINUTE, 0);
	    	cal.set(Calendar.SECOND, 0);
	    	cal.roll(Calendar.DATE, days);
	    	DateTime minDateTime = new DateTime(cal.getTime());
//	    	cal.setTime(new Date());
	    	cal.roll(Calendar.DATE, 1);
	    	cal.roll(Calendar.MILLISECOND, -1);
	    	DateTime maxDateTime = new DateTime(cal.getTime());
//	    	minDateTime.setDateOnly(true);
//	    	maxDateTime.setDateOnly(true);
	    	query.setMinimumStartTime(minDateTime);
	    	query.setMaximumStartTime(maxDateTime);
	    	CalendarEventFeed eventFeed = service.query(query, CalendarEventFeed.class);
	    	for (CalendarEventEntry entry : eventFeed.getEntries()) {
	    		list.add(entry.getTitle().getPlainText());
			}
    	
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return list;
    }
}
