package com.itextos.beacon.commonlib.commondbpool.tracker;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.commondbpool.tracker.extended.ExtendedCallableStatement;
import com.itextos.beacon.commonlib.commondbpool.tracker.extended.ExtendedConnection;
import com.itextos.beacon.commonlib.commondbpool.tracker.extended.ExtendedPreparedStatement;
import com.itextos.beacon.commonlib.commondbpool.tracker.extended.ExtendedStatement;

public class ConnectionTrackerIgnoreList
{

    private ConnectionTrackerIgnoreList()
    {}

    public static List<String> getAllIgnoreList()
    {
        final ArrayList<String> ignoreAllList = new ArrayList<>();

        ignoreAllList.addAll(getJavaIgnoreList());
        ignoreAllList.addAll(getWeblogicIgnoreList());
        ignoreAllList.addAll(getWebsphereIgnoreList());
        ignoreAllList.addAll(getLocaIgnoreList());
        ignoreAllList.addAll(getJBossIgnoreList());

        return ignoreAllList;
    }

    private static ArrayList<String> getJavaIgnoreList()
    {
        final ArrayList<String> ignoreList = new ArrayList<>();

        ignoreList.add("java.lang.Thread");
        ignoreList.add("javax.servlet.http.HttpServlet");
        ignoreList.add("jrockit.vm.ThreadDump");
        ignoreList.add("jrockit.vm.RNI");
        ignoreList.add("sun.reflect.NativeMethodAccessorImpl");
        ignoreList.add("sun.reflect.DelegatingMethodAccessorImpl");
        ignoreList.add("java.lang.reflect.Method");

        return ignoreList;
    }

    private static ArrayList<String> getWeblogicIgnoreList()
    {
        final ArrayList<String> ignoreList = new ArrayList<>();

        ignoreList.add("weblogic.servlet.internal.StubSecurityHelper$ServletServiceAction");
        ignoreList.add("weblogic.servlet.internal.WebAppServletContext$ServletInvocationAction");
        ignoreList.add("weblogic.servlet.internal.StubSecurityHelper");
        ignoreList.add("weblogic.servlet.internal.ServletStubImpl");
        ignoreList.add("weblogic.servlet.ServletServlet");
        ignoreList.add("weblogic.servlet.internal.FilterChainImpl");
        ignoreList.add("weblogic.servlet.internal.TailFilter");
        ignoreList.add("weblogic.servlet.internal.WebAppServletContext");
        ignoreList.add("weblogic.servlet.internal.ServletRequestImpl");
        ignoreList.add("weblogic.servlet.internal.TailFilter");
        ignoreList.add("weblogic.security.acl.internal.AuthenticatedSubject");
        ignoreList.add("weblogic.security.service.SecurityManager");
        ignoreList.add("weblogic.work.ExecuteThread");
        ignoreList.add("weblogic.servlet.jsp.JspBase");

        return ignoreList;
    }

    private static ArrayList<String> getWebsphereIgnoreList()
    {
        final ArrayList<String> ignoreList = new ArrayList<>();

        ignoreList.add("com.ibm.io.async.AbstractAsyncFuture");
        ignoreList.add("com.ibm.io.async.AsyncChannelFuture");
        ignoreList.add("com.ibm.io.async.AsyncFuture");
        ignoreList.add("com.ibm.io.async.ResultHandler");
        ignoreList.add("com.ibm.io.async.ResultHandler$2");
        ignoreList.add("com.ibm.ws.http.channel.inbound.impl.HttpICLReadCallback");
        ignoreList.add("com.ibm.ws.http.channel.inbound.impl.HttpInboundLink");
        ignoreList.add("com.ibm.ws.jsp.runtime.HttpJspBase");
        ignoreList.add("com.ibm.ws.jsp.webcontainerext.AbstractJSPExtensionServletWrapper");
        ignoreList.add("com.ibm.ws.tcp.channel.impl.AioReadCompletionListener");
        ignoreList.add("com.ibm.ws.util.ThreadPool$Worker");
        ignoreList.add("com.ibm.ws.webcontainer.WSWebContainer");
        ignoreList.add("com.ibm.ws.webcontainer.WebContainer");
        ignoreList.add("com.ibm.ws.webcontainer.channel.WCChannelLink");
        ignoreList.add("com.ibm.ws.webcontainer.servlet.CacheServletWrapper");
        ignoreList.add("com.ibm.ws.webcontainer.servlet.ServletWrapper");
        ignoreList.add("com.ibm.ws.webcontainer.servlet.ServletWrapperImpl");
        ignoreList.add("com.ibm.ws.webcontainer.webapp.WebAppRequestDispatcher");
        ignoreList.add("com.ibm.wsspi.webcontainer.servlet.GenericServletWrapper");
        ignoreList.add("com.ibm.ws.webcontainer.webapp.WebApp");
        ignoreList.add("com.ibm.ws.webcontainer.webapp.WebGroup");
        ignoreList.add("com.ibm.io.async.ResultHandler");
        ignoreList.add("com.ibm.io.async.ResultHandler$2");

        return ignoreList;
    }

    private static ArrayList<String> getLocaIgnoreList()
    {
        final ArrayList<String> ignoreList = new ArrayList<>();

        ignoreList.add(StackTraceCollection.class.getName());
        ignoreList.add(ConnectionsTracker.class.getName());
        ignoreList.add(Connection.class.getName());
        ignoreList.add(DBDataSourceFactory.class.getName());
        ignoreList.add(ExtendedStatement.class.getName());
        ignoreList.add(ExtendedPreparedStatement.class.getName());
        ignoreList.add(ExtendedCallableStatement.class.getName());
        ignoreList.add(ExtendedConnection.class.getName());

        ignoreList.add("com.itextos.beacon.commonlib.dbpool.DataSourceCollection");
        ignoreList.add("com.itextos.beacon.commonlib.dbpool.DBDataSource");
        // TODO May need to add more here.

        return ignoreList;
    }

    private static ArrayList<String> getJBossIgnoreList()
    {
        final ArrayList<String> ignoreList = new ArrayList<>();

        ignoreList.add("org.apache.catalina.connector.CoyoteAdapter");
        ignoreList.add("org.apache.catalina.core.ApplicationFilterChain");
        ignoreList.add("org.apache.catalina.core.StandardContextValve");
        ignoreList.add("org.apache.catalina.core.StandardEngineValve");
        ignoreList.add("org.apache.catalina.core.StandardHostValve");
        ignoreList.add("org.apache.catalina.core.StandardWrapperValve");
        ignoreList.add("org.apache.catalina.valves.AccessLogValve");
        ignoreList.add("org.apache.catalina.valves.ErrorReportValve");
        ignoreList.add("org.apache.coyote.http11.Http11Processor");
        ignoreList.add("org.apache.coyote.http11.Http11Protocol");
        ignoreList.add("org.apache.coyote.http11.Http11Protocol$Http11ConnectionHandler");
        ignoreList.add("org.apache.tomcat.util.net.JIoEndpoint$Worker");
        ignoreList.add("org.jboss.as.web.security.SecurityContextAssociationValve");
        ignoreList.add("org.jboss.jca.adapters.jdbc.WrapperDataSource");
        ignoreList.add("org.jboss.jca.core.connectionmanager.AbstractConnectionManager");
        ignoreList.add("org.jboss.jca.core.connectionmanager.ccm.CachedConnectionManagerImpl");

        return ignoreList;
    }

}