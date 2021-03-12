package net.thevpc.maven;

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

import net.thevpc.maven.shared.MavenProperties;
import net.thevpc.maven.util.PlaceHolder;
import net.thevpc.maven.util.SimpleStringFilter;
import net.thevpc.maven.util.StringConverter;
import net.thevpc.maven.util.StringUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;

import java.io.*;
import java.util.*;

/**
 * Transforms an input file or text snipped by replacing ${...}
 * properties from the project pom file
 */
@Mojo(name = "write-text",
        threadSafe = true,
        defaultPhase = LifecyclePhase.PROCESS_SOURCES,
        requiresDependencyResolution = ResolutionScope.TEST
)
public class WriteTextMojo
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
    @Parameter
    private String text;
    @Parameter(defaultValue = "true")
    private boolean trimLines;

    @Component
    private BuildPluginManager pluginManager;

    public Properties createProperties(){
        MavenProperties p=new MavenProperties();
        p.setFilter(new SimpleStringFilter(null, null, false));
        p.setProject(project);
        p.setIncludeProperties(true);
        return p.createProperties();
    }

    public void execute()
            throws MojoExecutionException {
        Map pluginContext = getPluginContext();
        File f = outputFile;
        if (f == null) {
            f = new File(project.getBuild().getOutputDirectory(), "project.properties");
        }
        if (f.getParentFile() != null && !f.getParentFile().exists()) {
            f.getParentFile().mkdirs();
        }

        final Properties properties = createProperties();
        FileWriter w = null;
        try {
            w = new FileWriter(f);
            StringConverter stringConverter = new StringConverter() {
                public String convert(String str) {
                    String s = properties.getProperty(str);
                    return s == null ? "" : s;
                }
            };
            w.write(reformatText(StringUtils.replacePlaceHolders(getText(),
                    new PlaceHolder[]{
                new PlaceHolder("${","}",stringConverter),
                new PlaceHolder("#{","}",stringConverter),
                new PlaceHolder("@{","}",stringConverter)
                    }
            )));
        } catch (IOException e) {
            throw new MojoExecutionException("Error creating file " + f, e);
        } finally {
            if (w != null) {
                try {
                    w.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }


    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isTrimLines() {
        return trimLines;
    }

    public void setTrimLines(boolean trimLines) {
        this.trimLines = trimLines;
    }
    private String reformatText(String s){
        if(s==null){
            return "";
        }
        if(isTrimLines()){
            BufferedReader r=new BufferedReader(new StringReader(s));
            StringBuilder sb=new StringBuilder();
            String line=null;
            String lineSeparator = System.getProperty("line.separator");
            try {
                while((line=r.readLine())!=null){
                    if(sb.length()>0){
                        sb.append(lineSeparator);
                    }
                    sb.append(line.trim());
                }
            } catch (IOException e) {
                throw new IllegalArgumentException("Should never happen");
            }
            s=sb.toString();
        }
        return s;
    }
}
