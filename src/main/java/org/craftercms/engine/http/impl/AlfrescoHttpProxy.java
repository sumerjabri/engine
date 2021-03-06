/*
 * Copyright (C) 2007-2013 Crafter Software Corporation.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.craftercms.engine.http.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.core.util.HttpServletUtils;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Extension of {@link HttpProxyImpl} that proxies to Alfresco.
 *
 * @author Alfonso Vásquez
 */
public class AlfrescoHttpProxy extends HttpProxyImpl {

    private static final Log logger = LogFactory.getLog(AlfrescoHttpProxy.class);

    private String alfrescoTicketCookieName;

    @Required
    public void setAlfrescoTicketCookieName(String alfrescoTicketCookieName) {
        this.alfrescoTicketCookieName = alfrescoTicketCookieName;
    }

    @Override
    protected String createTargetQueryString(HttpServletRequest request) {
        String queryString = request.getQueryString();
        String alfTicket = getCurrentTicket(request);

        if (queryString == null) {
            queryString = "";
        }

        if (alfTicket != null) {
            Map<String, Object> queryParams = HttpServletUtils.getParamsFromQueryString(queryString);
            queryParams.put("alf_ticket", alfTicket);

            try {
                queryString = HttpServletUtils.getQueryStringFromParams(queryParams, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                logger.error("Unable to encode params " + queryParams + " into a query string", e);
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("'" + alfrescoTicketCookieName + "' cookie not specified. Proxy request will be generated without it");
            }

            if (StringUtils.isNotEmpty(queryString)) {
                queryString = "?" + queryString;
            }
        }

        return queryString;
    }

    protected String getCurrentTicket(HttpServletRequest request) {
        Cookie ticketCookie = HttpServletUtils.getCookie(alfrescoTicketCookieName, request);
        if (ticketCookie != null) {
            return ticketCookie.getValue();
        } else {
            return null;
        }
    }

}
