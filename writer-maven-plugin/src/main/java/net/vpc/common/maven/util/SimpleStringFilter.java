package net.vpc.common.maven.util;

import java.util.List;

/**
 * Created by vpc on 8/9/16.
 */
public class SimpleStringFilter implements StringFilter{
    private List<String> includes;
    private List<String> excludes;
    private boolean regexp;

    public SimpleStringFilter(List<String> includes, List<String> excludes, boolean regexp) {
        this.includes = includes;
        this.excludes = excludes;
        this.regexp = regexp;
    }

    public boolean accept(String key) {
        if (includes != null || excludes != null) {
            if (excludes != null) {
                for (String exclude : excludes) {
                    String p = exclude;
                    if (!regexp) {
                        p = StringUtils.wildcardToRegex(p);
                    }
                    if (key.matches(p)) {
                        return false;
                    }
                }
            }
            if (includes != null) {
                for (String pp : includes) {
                    String p = pp;
                    if (!regexp) {
                        p = StringUtils.wildcardToRegex(p);
                    }
                    if (!key.matches(p)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
