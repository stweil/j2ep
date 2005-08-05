package net.sf.j2ep.servers;

import java.util.Iterator;
import java.util.LinkedList;

import net.sf.j2ep.Server;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A class that will check if servers are online and
 * notify a listener when servers goes down and comes
 * back online again.
 *
 * @author Anders Nyman
 */
public class ServerStatusChecker extends Thread {
    
    /** 
     * The online servers.
     */
    private LinkedList online;
    
    /** 
     * The offline servers.
     */
    private LinkedList offline;
    
    /** 
     * Client used to make the connections.
     */
    private HttpClient httpClient;
    
    /** 
     * The listener we notify when a servers status changes.
     */
    private ServerStatusListener listener;
    
    /** 
     * The time we wait between checking the servers status.
     */
    private long pollingTime;
    
    /** 
     * Logging element supplied by commons-logging.
     */
    private static Log log = LogFactory.getLog(ServerStatusChecker.class);
    
    /**
     * Basic constructor sets the listener to notify when
     * servers goes down/up. Also sets the polling time
     * which decides how long we wait between doing checks.
     * 
     * @param listener The listener
     * @param pollingTime The time we wait between checks, in milliseconds
     */
    public ServerStatusChecker(ServerStatusListener listener, long pollingTime) {
        this.listener = listener;
        this.pollingTime = Math.max(10000, pollingTime);
        online = new LinkedList();
        offline = new LinkedList();
        httpClient = new HttpClient(); 
        httpClient.getParams().setBooleanParameter(HttpClientParams.USE_EXPECT_CONTINUE, false);
        httpClient.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
    }
    
    /**
     * Runs the tests
     * @see java.lang.Runnable#run()
     */
    public void run() {
        for(;;) {
            checkOnlineServers();
            checkOfflineServers();
            try {
                Thread.sleep(pollingTime);
            } catch (InterruptedException ie) {
                return;
            }
        }
    }
    
    /**
     * Checks all the servers marked as being online
     * if they still are online.
     */
    private void checkOnlineServers() {
        Iterator itr;
        itr = online.listIterator();
        while (itr.hasNext()) {
            Server server = (Server) itr.next();
            String url = "http://" + server.getDomainName();
            GetMethod get = new GetMethod(url);
            get.setFollowRedirects(false);
            
            try {
                httpClient.executeMethod(get);
            } catch (Exception e) { 
                offline.add(server);
                itr.remove();
                log.debug("Server going OFFLINE! domainName: " + server.getDomainName());
                listener.serverOffline(server);
            }
        }
    }

    /**
     * Checks if the offline servers has come back online
     * again.
     */
    private void checkOfflineServers() {
        Iterator itr = offline.listIterator();
        while (itr.hasNext()) {
            Server server = (Server) itr.next();
            String url = "http://" + server.getDomainName();
            GetMethod get = new GetMethod(url);
            get.setFollowRedirects(false);
            
            try {
                httpClient.executeMethod(get);
                online.add(server);
                itr.remove();
                log.debug("Server back online, domainName: " + server.getDomainName());
                listener.serverOnline(server);
            } catch (Exception e) {
                listener.serverOffline(server);
            }
        }
    }
 
    /**
     * Adds a server that we will check for it's status.
     * The server is added to the offline list and will first
     * come online when we have managed to make a connection
     * to it.
     * 
     * @param server The server to add
     */
    public void addServer(Server server) {
        offline.add(server);
    }
}