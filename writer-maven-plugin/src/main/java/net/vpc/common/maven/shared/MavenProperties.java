package net.vpc.common.maven.shared;

import net.vpc.common.maven.util.StringFilter;
import org.apache.maven.model.Contributor;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Developer;
import org.apache.maven.model.License;
import org.apache.maven.project.MavenProject;

import java.util.*;

/**
 * Created by vpc on 8/9/16.
 */
public class MavenProperties {
    private MavenProject project;
    private StringFilter filter;
    private boolean includeProperties;

    public boolean isIncludeProperties() {
        return includeProperties;
    }

    public void setIncludeProperties(boolean includeProperties) {
        this.includeProperties = includeProperties;
    }

    public MavenProject getProject() {
        return project;
    }

    public void setProject(MavenProject project) {
        this.project = project;
    }

    public StringFilter getFilter() {
        return filter;
    }

    public void setFilter(StringFilter filter) {
        this.filter = filter;
    }

    public Properties createProperties() {
        Properties m = new Properties() {
            @Override
            public synchronized Enumeration<Object> keys() {
                return Collections.enumeration(new TreeSet<Object>(super.keySet()));
            }

            @Override
            public synchronized Object get(Object key) {
                return super.get(key);
            }
        };
        declare(m, "basedir", project.getFile().getParent());
        declare(m, "project.id", project.getId());
        declare(m, "project.name", project.getName());
        declare(m, "project.inceptionYear", project.getInceptionYear());
        declare(m, "project.url", project.getUrl());
        if (project.getOrganization() != null) {
            declare(m, "project.organization.name", project.getOrganization().getName());
            declare(m, "project.organization.url", project.getOrganization().getUrl());
        }
        declare(m, "project.groupId", project.getGroupId());
        declare(m, "project.artifactId", project.getArtifactId());
        declare(m, "project.version", project.getVersion());
        declare(m, "project.build.directory", project.getBuild().getDirectory());
        declare(m, "project.build.finalName", project.getBuild().getFinalName());
        declare(m, "project.build.outputDirectory", project.getBuild().getOutputDirectory());
        declare(m, "project.dependencies.size", "" + project.getDependencies().size());
        List<Dependency> dependencies = project.getDependencies();
        Map<String, StringBuilder> dependenciesBuilder = new HashMap<String, StringBuilder>();
        for (int i = 0; i < dependencies.size(); i++) {
            Dependency dependency = dependencies.get(i);
            declare(m, "project.dependencies[" + i + "].groupId", dependency.getGroupId());
            declare(m, "project.dependencies[" + i + "].artifactId", dependency.getArtifactId());
            declare(m, "project.dependencies[" + i + "].version", dependency.getVersion());
            declare(m, "project.dependencies[" + i + "].scope", dependency.getScope());
            StringBuilder sb = dependenciesBuilder.get(dependency.getScope());
            if (sb == null) {
                sb = new StringBuilder();
                dependenciesBuilder.put(dependency.getScope(), sb);
            }
            if (sb.length() > 0) {
                sb.append(";");
            }
            sb.append(dependency.getGroupId() + ":" + dependency.getArtifactId() + ":" + dependency.getVersion());
        }
        for (Map.Entry<String, StringBuilder> entry : dependenciesBuilder.entrySet()) {
            declare(m, "project.dependencies[" + entry.getKey() + "]", entry.getValue().toString());
        }
        declare(m, "project.developers.size", "" + project.getDevelopers().size());
        List<Developer> developers = project.getDevelopers();
        for (int i = 0; i < developers.size(); i++) {
            Developer developer = developers.get(i);
            declare(m, "project.developers[" + i + "].id", "" + developer.getId());
            declare(m, "project.developers[" + i + "].name", "" + developer.getName());
            declare(m, "project.developers[" + i + "].email", "" + developer.getEmail());
            declare(m, "project.developers[" + i + "].organization", "" + developer.getOrganization());
            declare(m, "project.developers[" + i + "].organizationUrl", "" + developer.getOrganizationUrl());
            StringBuilder roles = new StringBuilder();
            if (developer.getRoles() != null && !developer.getRoles().isEmpty()) {
                for (String r : developer.getRoles()) {
                    if (roles.length() > 0) {
                        roles.append("; ");
                    }
                    roles.append(r);
                }
            }
            declare(m, "project.developers[" + i + "].roles", "" + roles);
        }
        List<Contributor> contributors = project.getContributors();
        declare(m, "project.contributors.size", "" + contributors.size());
        for (int i = 0; i < contributors.size(); i++) {
            Contributor contributor = developers.get(i);
            declare(m, "project.contributors[" + i + "].name", "" + contributor.getName());
            declare(m, "project.contributors[" + i + "].email", "" + contributor.getEmail());
            declare(m, "project.contributors[" + i + "].organization", "" + contributor.getOrganization());
            declare(m, "project.contributors[" + i + "].organizationUrl", "" + contributor.getOrganizationUrl());
            StringBuilder roles = new StringBuilder();
            if (contributor.getRoles() != null && !contributor.getRoles().isEmpty()) {
                for (String r : contributor.getRoles()) {
                    if (roles.length() > 0) {
                        roles.append("; ");
                    }
                    roles.append(r);
                }
            }
            declare(m, "project.contributors[" + i + "].roles", "" + roles);
        }

        List<License> licenses = project.getLicenses();
        declare(m, "project.licenses.size", "" + licenses.size());
        for (int i = 0; i < licenses.size(); i++) {
            License license = licenses.get(i);
            declare(m, "project.contributors[" + i + "].name", "" + license.getName());
            declare(m, "project.contributors[" + i + "].url", "" + license.getUrl());
        }

        if (includeProperties) {
            for (Map.Entry<Object, Object> entry : project.getProperties().entrySet()) {
                declare(m, (String) entry.getKey(), (String) entry.getValue());
            }
        }
        return m;
    }

    public void declare(Properties m, String key, String value) {
        if (value != null) {
            if (filter==null || filter.accept(key)) {
                m.put(key, value);
            }
        }
    }
}
