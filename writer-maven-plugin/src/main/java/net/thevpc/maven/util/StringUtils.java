package net.thevpc.maven.util;

/**
 * Created by vpc on 8/9/16.
 */
public class StringUtils {
    public static String replaceDollarPlaceHolders(String s, StringConverter converter) {
        return replacePlaceHolders(s, "${", "}", converter);
    }

    public static String replacePlaceHolders(String s, String prefix, String suffix, StringConverter converter) {
        int i = 0;
        StringBuilder sb = new StringBuilder();
        while (i < s.length()) {
            int u = s.indexOf(prefix, i);
            if (u < 0) {
                sb.append(substring(s, i, s.length()));
                i = u;
                break;
            } else {
                sb.append(substring(s, i, u));
                i = u + prefix.length();
                u = s.indexOf(suffix, i);
                if (u <= 0) {
                    String var = substring(s, i, s.length());
                    sb.append(converter.convert(var));
                    i = u;
                    break;
                } else {
                    String var = substring(s, i, u);
                    sb.append(converter.convert(var));
                    i = u + suffix.length();
                }
            }
        }
        return sb.toString();
    }

    public static String replacePlaceHolders(String s, PlaceHolder[] holders) {
        int i = 0;
        StringBuilder sb = new StringBuilder();
        while (i < s.length()) {
            PlaceHolder current=null;
            int u=-1;
            for (int j = 0; j < holders.length; j++) {
                current=holders[j];
                u = s.indexOf(current.getPrefix(), i);
                if(u>=0){
                    break;
                }
            }
            if (u < 0) {
                sb.append(substring(s, i, s.length()));
                i = u;
                break;
            } else {
                sb.append(substring(s, i, u));
                i = u + current.getPrefix().length();
                u = s.indexOf(current.getSuffix(), i);
                if (u <= 0) {
                    String var = substring(s, i, s.length());
                    sb.append(current.getConverter().convert(var));
                    i = u;
                    break;
                } else {
                    String var = substring(s, i, u);
                    sb.append(current.getConverter().convert(var));
                    i = u + current.getSuffix().length();
                }
            }
        }
        return sb.toString();
    }

    public static String substring(String full, int from, int to) {
        if (full == null) {
            full = "";
        }
        if (from < 0) {
            from = 0;
        }
        if (to >= full.length()) {
            to = full.length();
        }
        if (to <= from) {
            return "";
        }
        return full.substring(from, to);
    }
    public static String wildcardToRegex(String pattern) {
        if (pattern == null) {
            pattern = "*";
        }
        int i = 0;
        char[] cc = pattern.toCharArray();
        StringBuilder sb = new StringBuilder("^");
        while (i < cc.length) {
            char c = cc[i];
            switch (c) {
                case '.':
                case '!':
                case '$':
                case '{':
                case '}':
                case '+': {
                    sb.append('\\').append(c);
                    break;
                }
                case '?': {
                    sb.append("[a-zA-Z_0-9$.]");
                    break;
                }
                case '*': {
                    if (i + 1 < cc.length && cc[i + 1] == '*') {
                        i++;
                        sb.append("[a-zA-Z_0-9$.]*");
                    } else {
                        sb.append("[a-zA-Z_0-9$]*");
                    }
                    break;
                }
                default: {
                    sb.append(c);
                }
            }
            i++;
        }
        sb.append('$');
        return sb.toString();
    }

}
