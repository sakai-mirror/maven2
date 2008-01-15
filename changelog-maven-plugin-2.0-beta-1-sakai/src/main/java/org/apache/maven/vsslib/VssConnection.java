package org.apache.maven.vsslib;

import org.apache.maven.changelog.ChangeLog;
import org.apache.maven.plugin.logging.Log;


/*
 * ====================================================================
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * ====================================================================
 */

/**
 * Bean representation of the vss connection string
 * 
 * @author Freddy Mallet
 */
public class VssConnection {

    /** Log */
    private static final Log LOG = ChangeLog.getLog();

    /**
     * VSS repository path
     */
    private String vssDir;

    /**
     * VSS user information
     */
    private String vssUserInf;

    /**
     * VSS project
     */
    private String vssProject;

    /**
     * Create a new VssConnection objet according to the provided vss connection
     * string. Here is an example:
     * scm:vss:\\lancelot\Vss_EApplications\:guest,password:/Security/LOSCProduction
     * the password is not madatory
     * 
     * @param connection
     *            the vss connection string
     */
    public VssConnection(String connection) {
        if (!connection.startsWith("scm:vss")) {
            throw new IllegalArgumentException(
                    "repositoy connection string does not specify 'vss' as the scm");
        }
        try {
            String[] splitedConnection = connection.split(":");
            vssDir = splitedConnection[2];
            if(!splitedConnection[3].equals("")){
                vssUserInf = splitedConnection[3];
            }
            vssProject = splitedConnection[4];
        } catch (Exception e) {
            String message = "Unable to parse VSS connection string :"
                    + connection;
            LOG.error(message, e);
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Get the vss directory
     * 
     * @return vss directory
     */
    public String getVssDir() {
        return vssDir;
    }

    /**
     * Get the vss project
     * 
     * @return vss project
     */
    public String getVssProject() {
        return vssProject;
    }

    /**
     * Get the vss user information
     * 
     * @return vss user information
     */
    public String getVssUserInf() {
        return vssUserInf;
    }
}