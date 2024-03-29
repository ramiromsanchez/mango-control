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
package com.serotonin.mango.vo.dataSource.gcal;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.Format;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.serotonin.json.JsonException;
import com.serotonin.json.JsonObject;
import com.serotonin.json.JsonReader;
import com.serotonin.json.JsonRemoteEntity;
import com.serotonin.json.JsonRemoteProperty;
import com.serotonin.mango.Common;
import com.serotonin.mango.rt.dataSource.DataSourceRT;
import com.serotonin.mango.rt.dataSource.gcal.GCalDataSourceRT;
import com.serotonin.mango.rt.event.type.AuditEventType;
import com.serotonin.mango.util.ExportCodes;
import com.serotonin.mango.vo.dataSource.DataSourceVO;
import com.serotonin.mango.vo.event.EventTypeVO;
import com.serotonin.util.SerializationHelper;
import com.serotonin.util.StringUtils;
import com.serotonin.web.dwr.DwrResponseI18n;
import com.serotonin.web.i18n.LocalizableMessage;

/**
 * @author Matthew Lohbihler
 */
@JsonRemoteEntity
public class GCalDataSourceVO extends DataSourceVO<GCalDataSourceVO> {
    public static final Type TYPE = Type.GCAL;
    private static final String feedUrl = "https://www.google.com/calendar/feeds/default/owncalendars/full";
//    private static final Format dateFormat = new SimpleDateFormat("YYYY-MM-DDTHH:MM:SS");
//    
//    public String formatDate(Date date) {
//    	return dateFormat.format(date);
//    }

    @Override
    protected void addEventTypes(List<EventTypeVO> ets) {
        ets.add(createEventType(GCalDataSourceRT.DATA_SOURCE_EXCEPTION_EVENT, new LocalizableMessage(
                "event.ds.dataSource")));
        ets.add(createEventType(GCalDataSourceRT.STATEMENT_EXCEPTION_EVENT, new LocalizableMessage(
                        "event.ds.statement")));
    }

    private static final ExportCodes EVENT_CODES = new ExportCodes();
    static {
        EVENT_CODES.addElement(GCalDataSourceRT.DATA_SOURCE_EXCEPTION_EVENT, "DATA_SOURCE_EXCEPTION");
        EVENT_CODES.addElement(GCalDataSourceRT.STATEMENT_EXCEPTION_EVENT, "STATEMENT_EXCEPTION");
    }

    @Override
    public ExportCodes getEventCodes() {
        return EVENT_CODES;
    }

    @Override
    public LocalizableMessage getConnectionDescription() {
        return new LocalizableMessage("common.default", "");
    }

    @Override
    public Type getType() {
        return TYPE;
    }

    @Override
    public DataSourceRT createDataSourceRT() {
        return new GCalDataSourceRT(this);
    }

    @Override
    public GCalPointLocatorVO createPointLocator() {
        return new GCalPointLocatorVO();
    }

//    @JsonRemoteProperty
//    private String driverClassname;
//    @JsonRemoteProperty
//    private String connectionUrl;
    @JsonRemoteProperty
    private String username;
    @JsonRemoteProperty
    private String password;
    @JsonRemoteProperty
    private String calendarName;
    @JsonRemoteProperty
    private String calendarFeed;
    private int updatePeriodType = Common.TimePeriods.MINUTES;
    @JsonRemoteProperty
    private int updatePeriods = 5;
    @JsonRemoteProperty
    private boolean rowBasedQuery = false;

    public String getFeedUrl() {
        return feedUrl;
    }

//    public void setDriverClassname(String driverClassname) {
//        this.driverClassname = driverClassname;
//    }

//    public String getConnectionUrl() {
//        return connectionUrl;
//    }
//
//    public void setConnectionUrl(String connectionUrl) {
//        this.connectionUrl = connectionUrl;
//    }

    public int getUpdatePeriods() {
        return updatePeriods;
    }

    public void setUpdatePeriods(int updatePeriods) {
        this.updatePeriods = updatePeriods;
    }

    public int getUpdatePeriodType() {
        return updatePeriodType;
    }

    public void setUpdatePeriodType(int updatePeriodType) {
        this.updatePeriodType = updatePeriodType;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCalendarName() {
        return calendarName;
    }

    public void setCalendarName(String calendarName) {
        this.calendarName = calendarName;
    }

    public String getCalendarFeed() {
        return calendarFeed;
    }

    public void setCalendarFeed(String calendarFeed) {
        this.calendarFeed = calendarFeed;
    }

    public boolean isRowBasedQuery() {
        return rowBasedQuery;
    }

    public void setRowBasedQuery(boolean rowBasedQuery) {
        this.rowBasedQuery = rowBasedQuery;
    }

    @Override
    public void validate(DwrResponseI18n response) {
        super.validate(response);
        if (!Common.TIME_PERIOD_CODES.isValidId(updatePeriodType))
            response.addContextualMessage("updatePeriodType", "validate.invalidValue");
        if (updatePeriods <= 0)
            response.addContextualMessage("updatePeriods", "validate.greaterThanZero");
        if (StringUtils.isEmpty(username))
            response.addContextualMessage("username", "validate.required");
        if (StringUtils.isEmpty(password))
            response.addContextualMessage("password", "validate.required");
        if (StringUtils.isEmpty(calendarName))
            response.addContextualMessage("calendarName", "validate.required");
    }

    @Override
    protected void addPropertiesImpl(List<LocalizableMessage> list) {
        AuditEventType.addPeriodMessage(list, "dsEdit.updatePeriod", updatePeriodType, updatePeriods);
//        AuditEventType.addPropertyMessage(list, "dsEdit.sql.driverClassName", driverClassname);
//        AuditEventType.addPropertyMessage(list, "dsEdit.sql.connectionString", connectionUrl);
        AuditEventType.addPropertyMessage(list, "dsEdit.sql.username", username);
        AuditEventType.addPropertyMessage(list, "dsEdit.sql.password", password);
        AuditEventType.addPropertyMessage(list, "dsEdit.gcal.calendarName", calendarName);
        AuditEventType.addPropertyMessage(list, "dsEdit.sql.rowQuery", rowBasedQuery);
    }

    @Override
    protected void addPropertyChangesImpl(List<LocalizableMessage> list, GCalDataSourceVO from) {
        AuditEventType.maybeAddPeriodChangeMessage(list, "dsEdit.updatePeriod", from.updatePeriodType,
                from.updatePeriods, updatePeriodType, updatePeriods);
//        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.sql.driverClassName", from.driverClassname,
//                driverClassname);
//        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.sql.connectionString", from.connectionUrl,
//                connectionUrl);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.gcal.username", from.username, username);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.gcal.password", from.password, password);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.gcal.calendarName", from.calendarName, calendarName);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.gcal.rowQuery", from.rowBasedQuery, rowBasedQuery);
    }

    //
    // /
    // / Serialization
    // /
    //
    private static final long serialVersionUID = -1;
    private static final int version = 2;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);
//        SerializationHelper.writeSafeUTF(out, driverClassname);
//        SerializationHelper.writeSafeUTF(out, connectionUrl);
        SerializationHelper.writeSafeUTF(out, username);
        SerializationHelper.writeSafeUTF(out, password);
        SerializationHelper.writeSafeUTF(out, calendarName);
        SerializationHelper.writeSafeUTF(out, calendarFeed);
        out.writeInt(updatePeriodType);
        out.writeInt(updatePeriods);
        out.writeBoolean(rowBasedQuery);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
//            driverClassname = SerializationHelper.readSafeUTF(in);
//            connectionUrl = SerializationHelper.readSafeUTF(in);
            username = SerializationHelper.readSafeUTF(in);
            password = SerializationHelper.readSafeUTF(in);
            calendarName = SerializationHelper.readSafeUTF(in);
            calendarFeed = SerializationHelper.readSafeUTF(in);
            updatePeriodType = in.readInt();
            updatePeriods = in.readInt();
            rowBasedQuery = false;
        }
        else if (ver == 2) {
//            driverClassname = SerializationHelper.readSafeUTF(in);
//            connectionUrl = SerializationHelper.readSafeUTF(in);
            username = SerializationHelper.readSafeUTF(in);
            password = SerializationHelper.readSafeUTF(in);
            calendarName = SerializationHelper.readSafeUTF(in);
            calendarFeed = SerializationHelper.readSafeUTF(in);
            updatePeriodType = in.readInt();
            updatePeriods = in.readInt();
            rowBasedQuery = in.readBoolean();
        }
    }

    @Override
    public void jsonDeserialize(JsonReader reader, JsonObject json) throws JsonException {
        super.jsonDeserialize(reader, json);
        Integer value = deserializeUpdatePeriodType(json);
        if (value != null)
            updatePeriodType = value;
    }

    @Override
    public void jsonSerialize(Map<String, Object> map) {
        super.jsonSerialize(map);
        serializeUpdatePeriodType(map, updatePeriodType);
    }
}
