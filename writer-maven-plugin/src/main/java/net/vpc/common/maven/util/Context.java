package net.vpc.common.maven.util;

import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.List;

/**
 * Created by vpc on 8/10/16.
 */
public class Context extends PrintStream{
    public Context(PrintStream out) {
        super(out);
    }

    public Object nonnull(Object o) {
        if (o != null) {
            if(o instanceof Object[]){
                Object[] arr = (Object[]) o;
                Object[] t=new Object[arr.length];
                for (int i = 0; i < t.length; i++) {
                    t[i]=nonnull(arr[i]);
                }
                return t;
            }
            return (o);
        }
        return "";
    }

    public boolean empty(Object o) {
        if(o==null){
            return true;
        }
        String s=String.valueOf(o).trim();
        if(s.length()==0){
            return true;
        }
        return false;
    }

    public String trim(Object o) {
        if (o != null) {
            return String.valueOf(o).trim();
        }
        return "";
    }

    public Object nullify(String o) {
        if (o != null) {
            String t = o.trim();
            if (t.length() > 0) {
                return t;
            }
        }
        return null;
    }

    public String nvl(Object... o) {
        for (Object o1 : o) {
            if (o1 != null) {
                return String.valueOf(o1);
            }
        }
        return "";
    }

    public String nvlt(Object... o) {
        for (Object o1 : o) {
            String trim = trim(o1);
            if (trim.length()>0) {
                return trim;
            }
        }
        return "";
    }

    public String strformat(String msg,Object... o) {
        Object[] o2=new Object[o.length];
        for (int i = 0; i < o2.length; i++) {
            o2[i]=nonnull(o[i]);
        }
        MessageFormat f=new MessageFormat(msg);
        return f.format(o2);
    }

    public String strlist(String sep,List list) {
        return strlist(sep,list.toArray(new Object[list.size()]));
    }

    public String strlist(String sep,Object... o) {
        Object[] o2=new Object[o.length];
        for (int i = 0; i < o2.length; i++) {
            o2[i]=nonnull(o[i]);
        }
        StringBuilder sb=new StringBuilder();
        for (int i = 0; i < o.length; i++) {
            String v=trim(o[i]);
            if(!empty(v)){
                if(sb.length()>0){
                    sb.append(sep);
                }
                sb.append(v);
            }
        }
        return sb.toString();
    }

    public void println(Object o){
        super.println(nonnull(o));
    }

    public void print(Object o){
        super.print(nonnull(o));
    }

    public void println(Object ... o){
        for (Object anO : o) {
            print(anO);
        }
        super.println();
    }

    public PrintStream printf(String msg,Object ... args){
        Object[] args2=new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            args2[i]=nonnull(args[i]);
        }
        return super.printf(msg,args2);
    }

    public PrintStream printlnf(String msg,Object ... args){
        Object[] args2=new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            args2[i]=nonnull(args[i]);
        }
        return super.printf(msg+"%n",args2);
    }

    public PrintStream printlnfc(String msg,Object ... args){
        if(args==null){
            return this;
        }
        for (Object arg : args) {
            if(empty(arg)){
                return this;
            }
        }
        return printlnf(msg,args);
    }
    public PrintStream printfc(String msg,Object ... args){
        if(args==null){
            return this;
        }
        for (Object arg : args) {
            if(empty(arg)){
                return this;
            }
        }
        return printf(msg+"%n",args);
    }

    public void printm(String msg,Object ... args){
        println(strformat(msg,args));
    }

    public void cprintm(String msg,Object ... args){

        String format = strformat(msg, args);
        if(format.length()>0) {
            println(format);
        }
    }

    public void println(){
        super.println();
    }

    public void cancel(){
        throw new UserCancelException();
    }
}
