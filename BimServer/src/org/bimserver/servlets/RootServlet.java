package org.bimserver.servlets;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.bimserver.BimServer;
import org.bimserver.plugins.web.WebModulePlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RootServlet extends HttpServlet {

	private static final long serialVersionUID = -6631574771887074019L;

	private static final Logger LOGGER = LoggerFactory.getLogger(RootServlet.class);
	private WebServiceServlet11 soap11Servlet;
	private WebServiceServlet12 soap12Servlet;
	private SyndicationServlet syndicationServlet;
	private JsonApiServlet jsonApiServlet;
	private UploadServlet uploadServlet;
	private DownloadServlet downloadServlet;

	private BimServer bimServer;

	@Override
	public void init() throws ServletException {
		super.init();
		bimServer = (BimServer) getServletContext().getAttribute("bimserver");
		jsonApiServlet = new JsonApiServlet(bimServer, getServletContext());
		syndicationServlet = new SyndicationServlet(bimServer, getServletContext());
		uploadServlet = new UploadServlet(bimServer, getServletContext());
		downloadServlet = new DownloadServlet(bimServer, getServletContext());
		soap11Servlet = new WebServiceServlet11(bimServer, getServletContext());
		soap12Servlet = new WebServiceServlet12(bimServer, getServletContext());
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			if (request.getPathInfo().startsWith("/soap11")) {
				soap11Servlet.service(request, response);
			} else if (request.getPathInfo().startsWith("/soap12")) {
				soap12Servlet.service(request, response);
			} else if (request.getPathInfo().startsWith("/syndication")) {
				syndicationServlet.service(request, response);
			} else if (request.getPathInfo().startsWith("/json")) {
				jsonApiServlet.service(request, response);
			} else if (request.getPathInfo().startsWith("/upload")) {
				uploadServlet.service(request, response);
			} else if (request.getPathInfo().startsWith("/download")) {
				downloadServlet.service(request, response);
			} else {
				String pathInfo = request.getPathInfo();
				if (pathInfo.equals("") || pathInfo.equals("/") || pathInfo == null) {
					pathInfo = "/index.html";
				}
				if (bimServer.getWebModules() != null) {
					for (WebModulePlugin webModulePlugin : bimServer.getWebModules()) {
						if (request.getPathInfo().startsWith(webModulePlugin.getContextPath())) {
							webModulePlugin.service(request, response);
							return;
						}
					}
				}
				
				File file = new File("../BimServer/www" + pathInfo);
				if (file.exists()) {
					FileInputStream fos = new FileInputStream(file);
					IOUtils.copy(fos, response.getOutputStream());
					fos.close();
				}
			}
		} catch (Throwable e) {
			LOGGER.error("", e);
		}
	}
}