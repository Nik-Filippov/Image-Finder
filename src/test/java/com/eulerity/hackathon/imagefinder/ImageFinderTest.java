package com.eulerity.hackathon.imagefinder;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class ImageFinderTest {

	public HttpServletRequest request;
	public HttpServletResponse response;
	public StringWriter sw;
	public HttpSession session;

	@Before
	public void setUp() throws Exception {
		request = Mockito.mock(HttpServletRequest.class);
		response = Mockito.mock(HttpServletResponse.class);
		sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		Mockito.when(response.getWriter()).thenReturn(pw);
		Mockito.when(request.getRequestURI()).thenReturn("/foo/foo/foo");
		Mockito.when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8080/foo/foo/foo"));
		session = Mockito.mock(HttpSession.class);
		Mockito.when(request.getSession()).thenReturn(session);
	}

	@Test
	public void test() throws IOException, ServletException {
		Mockito.when(request.getServletPath()).thenReturn("/main");
		Mockito.when(request.getParameter("url"))
				.thenReturn("https://www.geeksforgeeks.org/web-scraping-in-java-with-jsoup/");

		new ImageFinder().doPost(request, response);

		// Expected data structure
		Set<String> images = new LinkedHashSet<>(Arrays.asList(ImageFinder.testImages));
		Set<String> logos = new LinkedHashSet<>(Arrays.asList(ImageFinder.testLogos));
		Map<String, Set<String>> expected = new LinkedHashMap<>();
		expected.put("images", images);
		expected.put("logos", logos);

		String expectedJson = new Gson().toJson(expected);
		Assert.assertEquals(expectedJson, sw.toString());
	}
}