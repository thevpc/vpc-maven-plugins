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

import bsh.EvalError;
import bsh.Interpreter;
import bsh.TargetError;
import net.thevpc.maven.shared.MavenProperties;
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
import net.thevpc.maven.util.PrintStreamContext;
import net.thevpc.maven.util.LazyOutputStream;
import net.thevpc.maven.util.OutputStreamFactory;
import net.thevpc.maven.util.SimpleStringFilter;
import net.thevpc.maven.util.UserCancelException;

/**
 * Transforms an input file or text snipped using BSH expressions
 */
@Mojo(name = "write-bsh",
        threadSafe = true,
        defaultPhase = LifecyclePhase.PROCESS_SOURCES,
        requiresDependencyResolution = ResolutionScope.TEST
)
public class WriteBSHMojo
        extends AbstractMojo {
    /**
     * Location of the file.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    @Parameter
    private File outputFile;
    @Parameter
    private File inputFile;
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;
    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession mavenSession;
    @Parameter
    private String text;
    @Parameter(defaultValue = "true")
    private boolean buffered;

    @Component
    private BuildPluginManager buildPluginManager;

    public Properties createProperties() {
        MavenProperties p = new MavenProperties();
        p.setFilter(new SimpleStringFilter(null, null, false));
        p.setProject(project);
        p.setIncludeProperties(true);
        return p.createProperties();
    }

    public void execute()
            throws MojoExecutionException {
        Map pluginContext = getPluginContext();
        LazyOutputStream lazyOutputStream=null;
        PrintStream out = null;
        ByteArrayOutputStream bufferOut = null;
        if (outputFile == null) {
            out = System.out;
        } else {
            if (outputFile.isDirectory()) {
                outputFile = new File(outputFile, "project.properties");
            }
            if (buffered) {
                bufferOut = new ByteArrayOutputStream();
                out = new PrintStream(bufferOut);
            } else {
                try {
                    lazyOutputStream=new LazyOutputStream(
                            new OutputStreamFactory() {
                                @Override
                                public OutputStream createOutputStream() throws IOException {
                                    if (outputFile.getParentFile() != null && !outputFile.getParentFile().exists()) {
                                        outputFile.getParentFile().mkdirs();
                                    }
                                    return new FileOutputStream(outputFile);
                                }
                            }
                    );
                    out = new PrintStream(lazyOutputStream);
                } catch (Exception ex) {
                    getLog().error(ex);
                    throw new MojoExecutionException("Error creating file " + outputFile, ex);
                }
            }
        }
        final Properties properties = createProperties();
        try {
//            StringConverter stringConverter = new StringConverter() {
//                public String convert(String str) {
//                    String s = properties.getProperty(str);
//                    return s == null ? "" : s;
//                }
//            };

            try {
                try {
//                Interpreter.DEBUG=true;
                    ContextExt context = new ContextExt(out);
                    Interpreter interpreter = new Interpreter();
                    interpreter.set("project", project);
                    interpreter.set("session", mavenSession);
                    interpreter.set("buildPluginManager", buildPluginManager);
                    interpreter.set("out", context);
                    interpreter.set("properties", properties);
                    interpreter.set("pluginContext", getPluginContext());
                    interpreter.set("log", getLog());
                    interpreter.getNameSpace().importObject(context);
                    if (inputFile != null) {
                        FileReader fr = null;
                        try {
                            fr = new FileReader(inputFile);
                            interpreter.eval(fr);
                        } finally {
                            if (fr != null) {
                                fr.close();
                            }
                        }
                    }
                    interpreter.eval(new StringReader(text));
                    if (bufferOut != null) {
                        out.flush();
                        if (outputFile.getParentFile() != null && !outputFile.getParentFile().exists()) {
                            outputFile.getParentFile().mkdirs();
                        }
                        try {
                            out = new PrintStream(outputFile);
                        } catch (IOException ex) {
                            getLog().error(ex);
                            throw new MojoExecutionException("Error creating file " + outputFile, ex);
                        }
                        out.print(new String(bufferOut.toByteArray()));
                        out.close();
                    }
                } finally {
                    if (out != null) {
                        out.close();
                    }
                }
            } catch (UserCancelException e) {
                if (!buffered) {
                    boolean cancelAccepted=lazyOutputStream!=null && !lazyOutputStream.isInitialized();
                    if(!cancelAccepted){
                        getLog().error("Could not use cancel() when buffered is disabled. File is very likely to be written partially");
                        throw new MojoExecutionException("Could not use cancel() when buffered is disabled. File is very likely to be written partially");
                    }
                } else {
                    //getLog().error("Write Cancelled");
                }
            } catch (MojoExecutionException e) {
                throw e;
            } catch (TargetError e) {
               if(e.getTarget() instanceof UserCancelException){
                   UserCancelException ee=(UserCancelException) e.getTarget();
                   if (!buffered) {
                       getLog().error("Could not use cancel() when buffered is disabled. File is very likely to be written partially");
                   } else {
                       getLog().error("Write Cancelled");
                   }
               }else {
                   getLog().error(
                           "The script or code called by the script threw an exception: "
                                   + e.getTarget());
                   getLog().error(e);
                   throw new MojoExecutionException("The script or code called by the script threw an exception: "
                           + e.getTarget(), e);
               }
            } catch (EvalError e2) {
                getLog().error(
                        "There was an error in evaluating the script:" + e2);
                getLog().error(e2);
                throw new MojoExecutionException("There was an error in evaluating the script:" + e2, e2);
            }

//            w.print(reformatText(StringUtils.replacePlaceHolders(getText(),
//                    new PlaceHolder[]{
//                new PlaceHolder("${","}",stringConverter),
//                new PlaceHolder("#{","}",stringConverter),
//                new PlaceHolder("@{","}",stringConverter)
//                    }
//            )));
        } catch (MojoExecutionException e) {
            throw e;
        } catch (Exception e) {
            throw new MojoExecutionException("error write-bsh " + outputFile, e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    // ignore
                }
            }
        }


//        pluginManager.executeMojo(
//                plugin( groupId( "org.apache.maven.plugins" ), artifactId( "maven-resources-plugin" ), version( "2.7" ) ),
//                goal( "copy-resources" ),
//                configuration(
//                        element( name( "outputDirectory" ), project.getBuild().getDirectory() + "/test" ),
//                        element( name( "overwrite" ), "true" ),
//                        element(
//                                name( "resources" ),
//                                element(
//                                        name( "resource" ),
//                                        element( name( "directory" ), project.getBuild().getDirectory() + "/" ),
//                                        element( name( "excludes" ), element( name( "exclude" ), "**/com/sercanozdemir/**" ),
//                                                element( name( "exclude" ), "**/WEB-INF/**" ) ) ) ) ), executionEnvironment( project, mavenSession, pluginManager ) );
    }


    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public File getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    public File getInputFile() {
        return inputFile;
    }

    public void setInputFile(File inputFile) {
        this.inputFile = inputFile;
    }

    public boolean isBuffered() {
        return buffered;
    }

    public void setBuffered(boolean buffered) {
        this.buffered = buffered;
    }

    public class ContextExt extends PrintStreamContext{
        public ContextExt(PrintStream out) {
            super(out);
        }
        public void printProjectProperties(){
            WritePropertiesMojo.printProjectProperties(this,project);
        }
    }
}
