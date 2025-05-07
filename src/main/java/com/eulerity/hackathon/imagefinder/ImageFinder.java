package com.eulerity.hackathon.imagefinder;

import java.io.IOException;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@WebServlet(
    name = "ImageFinder",
    urlPatterns = {"/main"}
)
public class ImageFinder extends HttpServlet{
	private static final long serialVersionUID = 1L;

	protected static final Gson GSON = new GsonBuilder().create();

	//This is just a test array
	public static final String[] testImages = {
			"https://www.geeksforgeeks.org/web-scraping-in-java-with-jsoup/",
			"https://media.geeksforgeeks.org/gfg-gg-logo.svg",
			"https://media.geeksforgeeks.org/auth-dashboard-uploads/appstore.png",
			"https://media.geeksforgeeks.org/auth-dashboard-uploads/ep_right.svg",
			"https://media.geeksforgeeks.org/auth-dashboard-uploads/createImprovementIcon.png",
			"https://media.geeksforgeeks.org/auth/profile/0z9wl9cdf531g3qt3uda",
			"https://media.geeksforgeeks.org/auth-dashboard-uploads/gfgFooterLogo.png",
			"https://media.geeksforgeeks.org/auth-dashboard-uploads/googleplay.png",
			"https://media.geeksforgeeks.org/auth-dashboard-uploads/suggestChangeIcon.png",
			"https://media.geeksforgeeks.org/wp-content/uploads/20240210162725/JsoupCreateProject.PNG"
  };

	@Override
	protected final void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("application/json");
		String url = req.getParameter("url");
		System.out.println("Got request to crawl: " + url);

		try {
			Crawler crawler = new Crawler(url);
			Set<String> images = crawler.getImageLinks();
			resp.getWriter().print(GSON.toJson(images));
		} catch (Exception e) {
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			resp.getWriter().print("{\"error\": \"Failed to crawl URL\"}");
			e.printStackTrace();
		}
	}
}
