/*
Copyright (c) 2011, salesforce.com, inc.
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided
that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the
following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this list of conditions and
the following disclaimer in the documentation and/or other materials provided with the distribution.

Neither the name of salesforce.com, inc. nor the names of its contributors may be used to endorse or
promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

import org.apache.tomcat.util.scan.StandardJarScanner;
import org.eclipse.jetty.apache.jsp.JettyJasperInitializer;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 *
 * This class launches the web application in an embedded Jetty container.
 * This is the entry point to your application. The Java command that is used for
 * launching should fire this main method.
 *
 * @author John Simone
 */
public class Webapp {

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception{

        String port = System.getenv("PORT");

        String webappDirLocation = "src/main/webapp/";

        Server server = null;

        if (port == null) {
            // running locally, thus we need SSL
            server = new Server();

            ServerConnector httpConnector = new ServerConnector(server);
            httpConnector.setPort(8080);
            server.addConnector(httpConnector);

            SslContextFactory sslContextFactory = new SslContextFactory("keystore");
            sslContextFactory.setKeyStorePassword("123456");

            ServerConnector httpsConnector = new ServerConnector(server, sslContextFactory);
            httpsConnector.setPort(8443);
            server.addConnector(httpsConnector);
        }
        else {
            // running on Heroku
            server = new Server(Integer.valueOf(port));
        }

        // this enables running pre-compiled jsps
        new JettyJasperInitializer();

        WebAppContext rootContext = new WebAppContext();
        rootContext.setContextPath("/");
        //rootContext.setDescriptor(webappDirLocation + "/WEB-INF/web.xml");
        rootContext.setDescriptor("target/web.xml");
        rootContext.setResourceBase(webappDirLocation);
        rootContext.setParentLoaderPriority(true);

        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(false);
        resourceHandler.setResourceBase(Main.class.getResource("META-INF/resources/webjars").toExternalForm());

        ContextHandler webJarContext = new ContextHandler();
        webJarContext.setContextPath("/webjars");
        webJarContext.setHandler(resourceHandler);

        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { webJarContext, rootContext });

        server.setHandler(handlers);
        server.start();
        server.join();
    }

}
