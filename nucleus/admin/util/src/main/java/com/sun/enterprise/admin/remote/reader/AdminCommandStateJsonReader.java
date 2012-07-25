/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.sun.enterprise.admin.remote.reader;

import com.sun.enterprise.admin.remote.AdminCommandStateImpl;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.glassfish.api.admin.AdminCommandState;

/**
 *
 * @author mmares
 */
@Provider
@Consumes(MediaType.APPLICATION_JSON)
public class AdminCommandStateJsonReader implements MessageBodyReader<AdminCommandState> {
    
    private static final JsonFactory factory = new JsonFactory();
    
//    private static final Logger logger =
//            LogDomains.getLogger(AdminCommandStateJsonReader.class, LogDomains.ADMIN_LOGGER);
    
    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type.isAssignableFrom(AdminCommandState.class);
    }
    
    @Override
    public AdminCommandState readFrom(Class<AdminCommandState> type, Type genericType, Annotation[] annotations, 
                    MediaType mediaType, MultivaluedMap<String, String> httpHeaders, 
                    InputStream entityStream) throws IOException, WebApplicationException {
        JsonParser jp = factory.createJsonParser(entityStream);
        try {
            JsonToken token = jp.nextToken(); //sorounding object
            jp.nextToken(); //Name admin-command-state
            JsonToken token2 = jp.nextToken();
            if (token != JsonToken.START_OBJECT || 
                    token2 != JsonToken.START_OBJECT ||
                    !"admin-command-state".equals(jp.getCurrentName())) {
                throw new IOException("Not expected type (admin-command-state) but (" + jp.getCurrentName() + ")");
            }
            return readAdminCommandState(jp);
        } finally {
            jp.close();
        }
    }
    
    public static AdminCommandStateImpl readAdminCommandState(JsonParser jp) throws IOException {
        AdminCommandState.State state = null;
        boolean emptyPayload = true;
        CliActionReport ar = null;
        String id = null;
        while (jp.nextToken() != JsonToken.END_OBJECT) {
            String fieldname = jp.getCurrentName();
            jp.nextToken(); // move to value
            if ("state".equals(fieldname)) {
                state = AdminCommandState.State.valueOf(jp.getText());
            } else if ("empty-payload".equals(fieldname)) {
                emptyPayload = jp.getValueAsBoolean(true);
            } else if ("id".equals(fieldname)) {
                id = jp.getText();
            } else if ("action-report".equals(fieldname)) {
                ar = new CliActionReport();
                ActionReportJson2Reader.fillActionReport(ar, jp);
            }
        }
        return new AdminCommandStateImpl(state, ar, emptyPayload, id);
    }
}
