/*
 * digitalpetri OPC-UA SDK
 *
 * Copyright (C) 2015 Kevin Herron
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.digitalpetri.opcua.sdk.core.model.objects;

import com.digitalpetri.opcua.sdk.core.model.variables.PropertyType;
import com.digitalpetri.opcua.sdk.server.model.Property;
import com.digitalpetri.opcua.stack.core.types.builtin.DateTime;
import com.digitalpetri.opcua.stack.core.types.builtin.NodeId;
import com.digitalpetri.opcua.stack.core.types.builtin.QualifiedName;

public interface AuditEventType extends BaseEventType {

    Property<DateTime> ACTION_TIME_STAMP = new Property.BasicProperty<>(
            QualifiedName.parse("0:ActionTimeStamp"),
            NodeId.parse("ns=0;i=294"),
            -1,
            DateTime.class
    );

    Property<Boolean> STATUS = new Property.BasicProperty<>(
            QualifiedName.parse("0:Status"),
            NodeId.parse("ns=0;i=1"),
            -1,
            Boolean.class
    );

    Property<String> SERVER_ID = new Property.BasicProperty<>(
            QualifiedName.parse("0:ServerId"),
            NodeId.parse("ns=0;i=12"),
            -1,
            String.class
    );

    Property<String> CLIENT_AUDIT_ENTRY_ID = new Property.BasicProperty<>(
            QualifiedName.parse("0:ClientAuditEntryId"),
            NodeId.parse("ns=0;i=12"),
            -1,
            String.class
    );

    Property<String> CLIENT_USER_ID = new Property.BasicProperty<>(
            QualifiedName.parse("0:ClientUserId"),
            NodeId.parse("ns=0;i=12"),
            -1,
            String.class
    );

    DateTime getActionTimeStamp();

    PropertyType getActionTimeStampNode();

    void setActionTimeStamp(DateTime value);

    Boolean getStatus();

    PropertyType getStatusNode();

    void setStatus(Boolean value);

    String getServerId();

    PropertyType getServerIdNode();

    void setServerId(String value);

    String getClientAuditEntryId();

    PropertyType getClientAuditEntryIdNode();

    void setClientAuditEntryId(String value);

    String getClientUserId();

    PropertyType getClientUserIdNode();

    void setClientUserId(String value);
}
