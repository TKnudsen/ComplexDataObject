package com.github.TKnudsen.ComplexDataObject.model.tools;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

/**
 * General-purpose network utility methods.
 *
 * <p>
 * This class is not instantiable.
 */
public class NetworkTools {

	private NetworkTools() {
		// utility class -- not instantiable
	}

	/**
	 * Tests whether an Internet connection is currently available by attempting to
	 * open a connection to {@code http://www.google.com}.
	 *
	 * <p>
	 * This is a best-effort check. A {@code false} result means the test host could
	 * not be reached at the moment of the call; a {@code true} result means at
	 * least one TCP connection succeeded, but does not guarantee that all network
	 * destinations are reachable.
	 *
	 * @return {@code true} if the Internet appears to be available; {@code false}
	 *         otherwise
	 * @throws RuntimeException if the test URL is malformed -- this should never
	 *                          happen in practice
	 */
	public static boolean isInternetAvailable() {
		try {
			final URL url = URI.create("http://www.google.com").toURL();
			final URLConnection connection = url.openConnection();
			connection.connect();
			connection.getInputStream().close();
			return true;
		} catch (MalformedURLException e) {
			throw new RuntimeException("NetworkUtils: malformed test URL  -  this should never happen", e);
		} catch (IOException e) {
			return false;
		}
	}
}