/**
 * ====================================================================
 *                          Writer Maven Plugin
 *
 * is a new Open Source Maven Plugin to transform pom.xml file to text file
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.common.maven;

import net.vpc.common.maven.shared.MavenProperties;
import net.vpc.common.maven.util.SimpleStringFilter;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Writes a properties file with filtered property names and values
 * reflection the project the pom.xml
 */
@Mojo(name = "copy-properties",
        threadSafe = true,
        defaultPhase = LifecyclePhase.PROCESS_SOURCES,
        requiresDependencyResolution = ResolutionScope.TEST
)
public class CopyPropertiesMojo
    extends AbstractMojo
{
    /**
     * Location of the file.
     * @parameter expression="${project.build.directory}"
     * @required
     */
    @Parameter
    private File outputFile;
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;
    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession mavenSession;
    @Parameter
    private List<String> includes;
    @Parameter
    private List<String> excludes;
    @Parameter
    private boolean regexp;
    @Parameter
    private boolean includeProperties;

    @Component
    private BuildPluginManager pluginManager;

    public Properties createProperties(){
        MavenProperties p=new MavenProperties();
        p.setProject(project);
        p.setFilter(new SimpleStringFilter(includes, excludes, regexp));
        p.setIncludeProperties(includeProperties);
        return p.createProperties();
    }

    public void execute()
        throws MojoExecutionException
    {
        Map pluginContext = getPluginContext();
        File f = outputFile;
        if(f==null){
            f=new File(project.getBuild().getOutputDirectory(),"project.properties");
        }
        if ( f.getParentFile()!=null && !f.getParentFile().exists() )
        {
            f.getParentFile().mkdirs();
        }

        Properties properties = createProperties();
        FileWriter w = null;
        try
        {
            w = new FileWriter( f );
            //noinspection Since15
            properties.store(w,"");
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Error creating file " + f, e );
        }
        finally
        {
            if ( w != null )
            {
                try
                {
                    w.close();
                }
                catch ( IOException e )
                {
                    // ignore
                }
            }
        }
    }

    public File getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    public MavenProject getProject() {
        return project;
    }

    public void setProject(MavenProject project) {
        this.project = project;
    }

    public MavenSession getMavenSession() {
        return mavenSession;
    }

    public void setMavenSession(MavenSession mavenSession) {
        this.mavenSession = mavenSession;
    }

    public List<String> getIncludes() {
        return includes;
    }

    public void setIncludes(List<String> includes) {
        this.includes = includes;
    }

    public List<String> getExcludes() {
        return excludes;
    }

    public void setExcludes(List<String> excludes) {
        this.excludes = excludes;
    }

    public boolean isRegexp() {
        return regexp;
    }

    public void setRegexp(boolean regexp) {
        this.regexp = regexp;
    }

    public boolean isIncludeProperties() {
        return includeProperties;
    }

    public void setIncludeProperties(boolean includeProperties) {
        this.includeProperties = includeProperties;
    }

    public BuildPluginManager getPluginManager() {
        return pluginManager;
    }

    public void setPluginManager(BuildPluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }
}
