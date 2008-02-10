package org.apache.maven.vsslib;

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
 * ===================================================================
 */

import org.apache.maven.changelog.ChangeLogGenerator;
import org.apache.maven.changelog.ChangeLogParser;

/**
 * Provides VSS specific instances of the ChangeLogGenerator and ChangeLogParser
 * interfaces.
 * 
 * @author Freddy Mallet
 */
public class VssChangeLogFactory implements
        org.apache.maven.changelog.ChangeLogFactory {

    /**
     * Default no-arg constructor.
     */
    public VssChangeLogFactory() {
    }

    /**
     * Create a VSS specific ChangeLogGenerator.
     * 
     * @return a VSS specific ChangeLogGenerator.
     */
    public ChangeLogGenerator createGenerator() {
        return new VssChangeLogGenerator();
    }

    /**
     * Create a VSS specific ChangeLogParser.
     * 
     * @return a VSS specific ChangeLogParser.
     */
    public ChangeLogParser createParser() {
        return new VssChangeLogParser();
    }
}