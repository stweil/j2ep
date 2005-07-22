/*
 * Copyright 2005 Anders Nyman.
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

package net.sf.j2ep;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.j2ep.factories.MethodNotAllowedException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A reverse proxy using a set of Rules to identify which resource to proxy.
 * 
 * @author Anders Nyman
 */
public class ProxyFilter implements Filter {

    private static Log log;
    private RuleChain ruleChain;
    private HttpClient httpClient;

    /**
     * Simple implementation of a reverse-proxy. All request go through here.
     * 
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
     *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    /**
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain filterChain) throws IOException, ServletException {

        if (response.isCommitted()) {
            log.info("Not proxying, already commited.");
            filterChain.doFilter(request, response);
        } else if (!(request instanceof HttpServletRequest)) {
            log.info("Request is not HttpRequest, will only handle HttpRequests.");
            filterChain.doFilter(request, response);
        } else if (!(response instanceof HttpServletResponse)) {
            log.info("Request is not HttpResponse, will only handle HttpResponses.");
            filterChain.doFilter(request, response);
        } else {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            
            Rule rule = ruleChain.evaluate(httpRequest);
            Server server = rule.getServer();
            String uri = rule.process(getURI(httpRequest));

            ResponseHandler responseHandler = null;
            try {
                responseHandler = server.connect(httpRequest, uri, httpClient);
            } catch (HttpException e) {
                log.error("Problem while connection to server", e);
                httpResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            } catch (UnknownHostException e) {
                log.error("Could not connection to the host specified", e);
                httpResponse.setStatus(HttpServletResponse.SC_GATEWAY_TIMEOUT);
                return;
            } catch (IOException e) {
                log.error("Problem probably with the input being send, either in a Header or as a Stream", e);
                httpResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            } catch (MethodNotAllowedException e) {
                log.error("Incoming method could not be handled", e);
                httpResponse.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                httpResponse.setHeader("Allow", e.getAllowedMethods());
                return;
            }
            
            responseHandler.process(httpResponse);
            responseHandler.close();
        }

    }

    /**
     * Will build a URI but including the Query String. That means that it realy
     * isn't a real URI but quite near.
     * 
     * @param httpRequest
     *            Request to get the uri and query string from
     * @return String The URI for this request including the query string
     */
    private String getURI(HttpServletRequest httpRequest) {
        String uri = httpRequest.getServletPath();
        if (httpRequest.getQueryString() != null) {
            uri += "?" + httpRequest.getQueryString();
        }
        return uri;
    }

    /**
     * Called upon initialization, Will create the ConfigParser and get the
     * RuleChain back.
     * 
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init(FilterConfig filterConfig) throws ServletException {
        log = LogFactory.getLog("org.apache.webapp.reverseproxy");
        
        //TODO only temporary debug, need output to console directly since 
        //I'm running tomcat inside eclipse.
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
        System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
        System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire.header", "debug");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "debug");

        httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
        httpClient.getParams().setBooleanParameter(HttpClientParams.USE_EXPECT_CONTINUE, false);
        httpClient.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
        httpClient.getParams().setIntParameter(HttpClientParams.MAX_REDIRECTS, 0);

        String data = filterConfig.getInitParameter("dataUrl");
        if (data == null) {
            throw new ServletException("dataUrl is required.");
        }
        try {
            File dataFile = new File(filterConfig.getServletContext()
                    .getRealPath(data));
            ConfigParser parser = new ConfigParser(dataFile);
            ruleChain = parser.getRuleChain();
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    /**
     * Called when this filter is destoryed.
     * 
     * @see javax.servlet.Filter#destroy()
     */
    public void destroy() {
        log = null;
        ruleChain = null;
        httpClient = null;
    }
}