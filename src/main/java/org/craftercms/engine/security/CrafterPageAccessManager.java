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
package org.craftercms.engine.security;

import org.apache.commons.collections.CollectionUtils;
import org.craftercms.engine.model.SiteItem;
import org.craftercms.security.annotations.RunIfSecurityEnabled;
import org.craftercms.security.api.RequestContext;
import org.craftercms.security.api.SecurityConstants;
import org.craftercms.security.api.UserProfile;
import org.craftercms.security.exception.AccessDeniedException;
import org.craftercms.security.exception.AuthenticationRequiredException;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;

/**
 * Manages access to Crafter pages, depending on the roles specified in the page and the current user's roles.
 *
 * @author Russ Danner
 * @author Alfonso Vásquez
 */
public class CrafterPageAccessManager {

    protected String authorizedRolesXPathQuery;

    @Required
    public void setAuthorizedRolesXPathQuery(String authorizedRolesXPathQuery) {
        this.authorizedRolesXPathQuery = authorizedRolesXPathQuery;
    }

    /**
     * Checks if the user has sufficient rights to access the specified page:
     *
     * <ol>
     *     <li>If the page doesn't contain any required role, no authentication is needed.</li>
     *     <li>If the page has the role "Anonymous", no authentication is needed.</li>
     *     <li>If the page has the role "Authenticated", just authentication is needed.</li>
     *     <li>If the page has any other the roles, the user needs to have any of those roles.</li>
     * </ol>
     */
    @RunIfSecurityEnabled
    public void checkAccess(SiteItem page) throws AuthenticationRequiredException, AccessDeniedException {
        RequestContext context = RequestContext.getCurrent();

        if (context != null && context.getAuthenticationToken() != null && context.getAuthenticationToken().getProfile() != null) {
            UserProfile profile = context.getAuthenticationToken().getProfile();
            String username = profile.getUserName();
            String pageUrl = page.getStoreUrl();
            List<String> authorizedRoles = getAuthorizedRolesForPage(page);

            if (CollectionUtils.isNotEmpty(authorizedRoles) && !containsRole(SecurityConstants.ANONYMOUS_USERNAME, authorizedRoles)) {
                if (profile.isAnonymous()) {
                    throw new AuthenticationRequiredException("User is anonymous but page '" + pageUrl + "' requires authentication");
                }
                if (!containsRole("Authenticated", authorizedRoles) && !profile.hasAnyRole(authorizedRoles)) {
                    throw new AccessDeniedException("User '" + username + "' is not authorized to view page '" + pageUrl + "'");
                }
            }
        }
    }

    protected List<String> getAuthorizedRolesForPage(SiteItem page) {
        return page.getItem().queryDescriptorValues(authorizedRolesXPathQuery);
    }

    protected boolean containsRole(String role, List<String> roles) {
        for (String r : roles) {
            if (r.equals(role)) {
                return true;
            }
        }

        return false;
    }

}
