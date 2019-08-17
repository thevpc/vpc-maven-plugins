package net.vpc.common.maven;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import net.vpc.common.maven.shared.MavenProperties;
import net.vpc.common.maven.util.*;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Contributor;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Developer;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * write a poperties file with pre-formatted property names
 * reflection the project pom.xml
 */
@Mojo(name = "write-properties",
        threadSafe = true,
        defaultPhase = LifecyclePhase.PROCESS_SOURCES,
        requiresDependencyResolution = ResolutionScope.TEST
)
public class WritePropertiesMojo
        extends AbstractMojo {
    /**
     * Location of the file.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    @Parameter
    private File outputFile;
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;
    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession mavenSession;
    @Parameter(defaultValue = "true")
    private boolean trimLines;

    @Component
    private BuildPluginManager pluginManager;

    public void execute()
            throws MojoExecutionException {
        File f = outputFile;
        if (f == null) {
            f = new File(project.getBuild().getOutputDirectory(), "project.properties");
        }
        if (f.getParentFile() != null && !f.getParentFile().exists()) {
            f.getParentFile().mkdirs();
        }

        PrintStream w = null;
        try {
            w = new PrintStream(f);
            Context c=new Context(w);
            printProjectProperties(c,project);
        } catch (IOException e) {
            throw new MojoExecutionException("Error creating file " + f, e);
        } finally {
            if (w != null) {
                try {
                    w.close();
                } catch (Exception e) {
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

    public boolean isTrimLines() {
        return trimLines;
    }

    public void setTrimLines(boolean trimLines) {
        this.trimLines = trimLines;
    }

    public BuildPluginManager getPluginManager() {
        return pluginManager;
    }

    public void setPluginManager(BuildPluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    public static void printProjectProperties(Context c,MavenProject project){
        c.printlnf("project.id=%s", (project.getGroupId()+":"+project.getArtifactId()));
        c.printlnf("project.version=%s", project.getVersion());
        c.printlnf("project.name=%s",project.getName());
        c.printlnfc("project.description=%s",project.getDescription());
        if(project.getOrganization()!=null){
            c.printlnfc("project.organization.name=%s",project.getOrganization().getName());
            c.printlnfc("project.organization.url=%s",project.getOrganization().getUrl());
        }
        int index=0;
        for(Developer dev : project.getDevelopers()){
            c.printlnfc("project.developers["+index+"].id=%s",dev.getId());
            c.printlnfc("project.developers["+index+"].name=%s",dev.getName());
            c.printlnfc("project.developers["+index+"].url=%s",dev.getUrl());
            c.printlnfc("project.developers["+index+"].organization.name=%s",dev.getOrganization());
            c.printlnfc("project.developers["+index+"].organization.url=%s",dev.getOrganizationUrl());
            c.printlnfc("project.developers["+index+"].roles=%s",c.strlist(" ",dev.getRoles()));
            index++;
        }
        index=0;
        for(Contributor dev : project.getContributors()){
            c.printlnfc("project.contributors["+index+"].name="+dev.getName());
            c.printlnfc("project.contributors["+index+"].url="+dev.getUrl());
            c.printlnfc("project.contributors["+index+"].organization.name="+dev.getOrganization());
            c.printlnfc("project.contributors["+index+"].organization.url="+dev.getOrganizationUrl());
            c.printlnfc("project.contributors["+index+"].roles=%s",c.strlist(" ",dev.getRoles()));
            index++;
        }
        java.util.Map<String,List<String>> scopes=new HashMap<>();
        for(Dependency dep : project.getDependencies()){
            java.util.List<String> deps=(List<String>) scopes.get(dep.getScope());
            if(deps==null){
                deps=new java.util.ArrayList<String>();
                scopes.put(dep.getScope(),deps);
            }
            deps.add(dep.getGroupId()+":"+dep.getArtifactId()+":"+dep.getVersion());
        }
        for(Map.Entry<String,List<String>> scope : scopes.entrySet()){
            c.printlnfc("project.dependencies.%s=%s",scope.getKey(),c.strlist(";",scope.getValue()));
        }
    }
}
