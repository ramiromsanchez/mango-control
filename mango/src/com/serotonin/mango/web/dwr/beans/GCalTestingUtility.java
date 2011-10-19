/**
 * 
 */
package com.serotonin.mango.web.dwr.beans;

import java.util.ArrayList;
import java.util.List;
import java.net.URL;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import com.google.gdata.client.calendar.CalendarService;
import com.google.gdata.data.calendar.CalendarEntry;
import com.google.gdata.data.calendar.CalendarFeed;

/**
 * @author v-dmkras
 *
 */
public class GCalTestingUtility extends Thread implements TestingUtility {

    private final ResourceBundle bundle;
    private final String username;
    private final String password;
    private final String feedUrl;
    
    private boolean done = false;
    private String errorMessage = null;
    private final ArrayList<String> calendars = new ArrayList<String>();

    public GCalTestingUtility(ResourceBundle bundle, String feedUrl, String username,
            String password) {
        this.bundle = bundle;
        this.feedUrl = feedUrl;
        this.username = username;
        this.password = password;
        start();
    }
    
    @Override
    public void run() {
    	try
    	{
    	CalendarService service = new CalendarService("");
    	service.setUserCredentials(username, password);
    	// Send the request and print the response
    	CalendarFeed resultFeed = service.getFeed(new URL(feedUrl), CalendarFeed.class);

    	for (int i = 0; i < resultFeed.getEntries().size(); i++) {
    		CalendarEntry entry = resultFeed.getEntries().get(i);
    		calendars.add(entry.getTitle().getPlainText());
    		}
    	}
        catch (Exception e) {
            errorMessage = e.getClass() + ": " + e.getMessage();
        }
        done = true;
    }
    
    public List<String> getCalendars()
    {
    	return calendars;
    }
    
    public boolean isDone() 
    {
    	return done;
    }
    
    public String getErrorMessage()
    {
    	return errorMessage;
    }

	/* (non-Javadoc)
	 * @see com.serotonin.mango.web.dwr.beans.TestingUtility#cancel()
	 */
	@Override
	public void cancel() {
		// TODO Auto-generated method stub

	}

}
