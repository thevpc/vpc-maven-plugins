package net.thevpc.maven.util;

/**
 * Created by vpc on 8/10/16.
 */
public class PlaceHolder {
    private String prefix;
    private String suffix;
    private StringConverter converter;

    public PlaceHolder(String prefix, String suffix, StringConverter converter) {
        this.prefix = prefix;
        this.suffix = suffix;
        this.converter = converter;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public StringConverter getConverter() {
        return converter;
    }
}
