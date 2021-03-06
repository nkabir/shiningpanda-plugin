/*
 * ShiningPanda plug-in for Jenkins
 * Copyright (C) 2011-2012 ShiningPanda S.A.S.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of its license which incorporates the terms and 
 * conditions of version 3 of the GNU Affero General Public License, 
 * supplemented by the additional permissions under the GNU Affero GPL
 * version 3 section 7: if you modify this program, or any covered work, 
 * by linking or combining it with other code, such other code is not 
 * for that reason alone subject to any of the requirements of the GNU
 * Affero GPL version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * license for more details.
 *
 * You should have received a copy of the license along with this program.
 * If not, see <https://raw.github.com/jenkinsci/shiningpanda-plugin/master/LICENSE.txt>.
 */
package jenkins.plugins.shiningpanda.workspace;

import hudson.FilePath;

import java.io.IOException;

import jenkins.plugins.shiningpanda.utils.FilePathUtil;

public class SlaveWorkspace extends Workspace
{

    /**
     * Constructor using fields.
     * 
     * @param home
     *            The home folder of the workspace.
     */
    public SlaveWorkspace(FilePath home)
    {
        // Call super
        super(home);
    }

    /*
     * (non-Javadoc)
     * 
     * @see jenkins.plugins.shiningpanda.workspace.Workspace#getVirtualenvPy()
     */
    @Override
    public FilePath getVirtualenvPy() throws IOException, InterruptedException
    {
        return FilePathUtil.synchronize(getMasterVirtualenvPy(), getHome().child(VIRTUALENV));
    }

    /*
     * (non-Javadoc)
     * 
     * @see jenkins.plugins.shiningpanda.workspace.Workspace#getPackagesDir()
     */
    @Override
    public FilePath getPackagesDir() throws IOException, InterruptedException
    {
        return FilePathUtil.isDirectoryOrNull(FilePathUtil.synchronize(getMasterPackagesDir(), getHome().child(PACKAGES)));
    }

    /*
     * (non-Javadoc)
     * 
     * @see jenkins.plugins.shiningpanda.workspace.Workspace#getBootstrapPy()
     */
    @Override
    public FilePath getBootstrapPy() throws IOException, InterruptedException
    {
        return FilePathUtil.synchronize(getMasterBootstrapPy(), getHome().child(BOOTSTRAP));
    }
}
