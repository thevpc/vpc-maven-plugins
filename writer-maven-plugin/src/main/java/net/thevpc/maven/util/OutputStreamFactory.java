package net.thevpc.maven.util;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by vpc on 11/13/16.
 */
public interface OutputStreamFactory {
    OutputStream createOutputStream() throws IOException;
}
