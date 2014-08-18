package main;

import fileinfo.FileInfoParser;
import http.HttpExecutor;

/**
 *
 * @author zoli
 */
public class Server {
        
    public final String visibleName, rootUrl;
    public final HttpExecutor executor;
    public final FileInfoParser parser;

    public Server(String visibleName, String rootUrl, HttpExecutor executor, FileInfoParser parser) {
        this.visibleName = visibleName;
        this.rootUrl = rootUrl;
        this.executor = executor;
        this.parser = parser;
    }

    public static Server create(String visibleName, String rootUrl, FileInfoParser parser, boolean validCert) {
        return new Server(visibleName, rootUrl, new HttpExecutor(rootUrl, validCert).setRequestType(HttpExecutor.RequestType.GET), parser);
    }

    @Override
    public String toString() {
        return visibleName;
    }

}
