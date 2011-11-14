/*
 * ShiningPanda plug-in for Jenkins
 * Copyright (C) 2011 ShiningPanda S.A.S.
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package jenkins.plugins.shiningpanda.builders;

import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;

import java.io.IOException;
import java.io.Serializable;

import jenkins.plugins.shiningpanda.Messages;
import jenkins.plugins.shiningpanda.command.Command;
import jenkins.plugins.shiningpanda.interpreters.Python;
import jenkins.plugins.shiningpanda.matrix.PythonAxis;
import jenkins.plugins.shiningpanda.tools.PythonInstallation;
import jenkins.plugins.shiningpanda.util.BuilderUtil;
import jenkins.plugins.shiningpanda.workspace.Workspace;

import org.kohsuke.stapler.DataBoundConstructor;

public class StandardPythonBuilder extends Builder implements Serializable
{

    /**
     * Name of the PYTHON to invoke
     */
    public final String pythonName;

    /**
     * Do not consider the build as a failure if any of the commands exits with
     * a non-zero exit code.
     */
    public final boolean ignoreExitCode;

    /**
     * The command to execute in the PYTHON environment
     */
    public final String command;

    /**
     * Constructor using fields
     * 
     * @param pythonName
     *            The name of the PYTHON
     * @param ignoreExitCode
     *            Do not consider the build as a failure if any of the commands
     *            exits with a non-zero exit code
     * @param command
     *            The command to execute in PYTHON environment
     */
    @DataBoundConstructor
    public StandardPythonBuilder(String pythonName, String command, boolean ignoreExitCode)
    {
        // Call super
        super();
        // Store the name of the PYTHON to invoke
        this.pythonName = pythonName;
        // Normalize and store the command
        this.command = command;
        // Store the ignore flag
        this.ignoreExitCode = ignoreExitCode;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException,
            IOException
    {
        // Get the workspace
        Workspace workspace = Workspace.fromHome(build.getWorkspace());
        // Get the environment variables for this build
        EnvVars environment = BuilderUtil.getEnvironment(build, listener);
        // Expand the HOME folder with these variables
        PythonInstallation installation = PythonInstallation.fromName(BuilderUtil.isMatrix(build) ? environment
                .get(PythonAxis.KEY) : pythonName);
        //
        if (installation == null)
            return false;
        // Get the PYTHON
        Python interpreter = installation.forBuild(listener, environment).toInterpreter(launcher.getChannel());
        // Check if valid
        if (interpreter == null)
            return false;
        // Set the VIRTUALENV variables
        environment.overrideAll(interpreter.getEnvironment());
        // Set the environment of this specific builder
        return Command.get(workspace.isUnix(), command, ignoreExitCode).launch(launcher, listener, environment,
                workspace.getHome());
    }

    private static final long serialVersionUID = 1L;

    /**
     * Descriptor for this builder
     */
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder>
    {

        /**
         * Flag to determine if PYTHON selection is let to user (useful for
         * build matrix)
         */
        public volatile boolean showInstallations;

        /*
         * (non-Javadoc)
         * 
         * @see hudson.model.Descriptor#getDisplayName()
         */
        @Override
        public String getDisplayName()
        {
            return Messages.StandardPythonBuilder_DisplayName();
        }

        /*
         * (non-Javadoc)
         * 
         * @see hudson.model.Descriptor#getHelpFile()
         */
        @Override
        public String getHelpFile()
        {
            return "/plugin/shiningpanda/help/StandardPythonBuilder/help.html";
        }

        /*
         * (non-Javadoc)
         * 
         * @see hudson.tasks.BuildStepDescriptor#isApplicable(java.lang.Class)
         */
        @Override
        public boolean isApplicable(@SuppressWarnings("rawtypes") Class<? extends AbstractProject> jobType)
        {
            // Set the flag (dirty to do this here, but do not know where to do
            // it)
            showInstallations = !BuilderUtil.isMatrix(jobType);
            // If there's no PYTHON configured, there's no point in PYTHON
            // builders
            return !PythonInstallation.isEmpty();
        }
    }
}