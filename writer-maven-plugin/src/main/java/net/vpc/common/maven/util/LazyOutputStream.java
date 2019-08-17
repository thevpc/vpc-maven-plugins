package net.vpc.common.maven.util;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by vpc on 11/13/16.
 */
public class LazyOutputStream extends OutputStream {
    private OutputStreamFactory factory;
    private OutputStream os;

    public LazyOutputStream(OutputStreamFactory factory) {
        this.factory = factory;
    }

    private final OutputStream ensureOutputStreamCreation() throws IOException {
        if(os==null){
            os=factory.createOutputStream();
        }
        return os;
    }
    @Override
    public void write(int b) throws IOException {
        ensureOutputStreamCreation().write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        ensureOutputStreamCreation().write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        ensureOutputStreamCreation().write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        if(os!=null) {
            os.flush();
        }
    }

    @Override
    public void close() throws IOException {
        if(os!=null) {
            os.close();
        }
    }

    public boolean isInitialized(){
        if(os==null){
            return false;
        }
        if(os instanceof LazyOutputStream){
            return ((LazyOutputStream) os).isInitialized();
        }
        return true;
    }
}
