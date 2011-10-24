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
import java.util.List;
import java.util.Map;

import com.google.gdata.client.calendar.CalendarQuery;
import com.serotonin.json.JsonException;
import com.serotonin.json.JsonObject;
import com.serotonin.json.JsonReader;
import com.serotonin.json.JsonRemoteEntity;
import com.serotonin.json.JsonRemoteProperty;
import com.serotonin.json.JsonSerializable;
import com.serotonin.mango.DataTypes;
import com.serotonin.mango.rt.dataSource.PointLocatorRT;
import com.serotonin.mango.rt.dataSource.gcal.GCalPointLocatorRT;
import com.serotonin.mango.rt.event.type.AuditEventType;
import com.serotonin.mango.vo.dataSource.AbstractPointLocatorVO;
import com.serotonin.util.SerializationHelper;
import com.serotonin.util.StringUtils;
import com.serotonin.web.dwr.DwrResponseI18n;
import com.serotonin.web.i18n.LocalizableMessage;

/**
 * @author Matthew Lohbihler
 */
@JsonRemoteEntity
public class GCalPointLocatorVO extends AbstractPointLocatorVO implements JsonSerializable {
    public LocalizableMessage getConfigurationDescription() {
        return new LocalizableMessage("common.default", daysPeriod);
    }

    public boolean isSettable() {
        return true;
    }

    public PointLocatorRT createRuntime() {
        return new GCalPointLocatorRT(this);
    }

    @JsonRemoteProperty
    private String timeOverrideName;
    private int dataTypeId;
    @JsonRemoteProperty
    private Integer daysPeriod = 0;
    
    private String strEvents = "";
    
    private int numEvents = 0;
    
    public String getEvents() {
    	return strEvents;
    }
    
    public void setEvents(List<String> events) {
    	strEvents = "";
    	numEvents = 0;
    	for (String evt : events) {
    		strEvents = strEvents + "<event>" + evt + "</event>";
    		numEvents++;
    	}
    }
    
    public int getNumEvents() {
    	return numEvents;
    }

    public String getTimeOverrideName() {
        return timeOverrideName;
    }

    public void setTimeOverrideName(String timeOverrideName) {
        this.timeOverrideName = timeOverrideName;
    }

    public int getDataTypeId() {
        return dataTypeId;
    }

    public void setDataTypeId(int dataTypeId) {
        this.dataTypeId = dataTypeId;
    }

    public int getDaysPeriod() {
        return daysPeriod;
    }

    public void setDaysPeriod(int daysPeriod) {
        this.daysPeriod = daysPeriod;
    }

    public void validate(DwrResponseI18n response) {
        if (!DataTypes.CODES.isValidId(dataTypeId))
            response.addContextualMessage("dataTypeId", "validate.invalidValue");
        if (daysPeriod < 0)
            response.addContextualMessage("daysPeriod", "validate.daysPeriod");
    }

    @Override
    public void addProperties(List<LocalizableMessage> list) {
        AuditEventType.addDataTypeMessage(list, "dsEdit.pointDataType", dataTypeId);
        AuditEventType.addPropertyMessage(list, "dsEdit.gcal.daysPeriod", daysPeriod);
    }

    @Override
    public void addPropertyChanges(List<LocalizableMessage> list, Object o) {
        GCalPointLocatorVO from = (GCalPointLocatorVO) o;
        AuditEventType.maybeAddDataTypeChangeMessage(list, "dsEdit.pointDataType", from.dataTypeId, dataTypeId);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.gcal.daysPeriod", from.daysPeriod, daysPeriod);
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
        out.writeInt(dataTypeId);
        out.writeInt(daysPeriod);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            dataTypeId = in.readInt();
            daysPeriod = in.readInt();
        }
        else if (ver == 2) {
            dataTypeId = in.readInt();
            daysPeriod = in.readInt();
        }
    }

    @Override
    public void jsonDeserialize(JsonReader reader, JsonObject json) throws JsonException {
        Integer value = deserializeDataType(json, DataTypes.IMAGE);
        if (value != null)
            dataTypeId = value;
    }

    @Override
    public void jsonSerialize(Map<String, Object> map) {
        serializeDataType(map);
    }
}
