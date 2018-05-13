package vg.step.curieux.client;

import java.io.IOException;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import vg.step.curieux.client.exception.CurieuxEventFinishedException;
import vg.step.curieux.client.exception.ProcessingException;
import vg.step.curieux.client.exception.WrongRowIdException;

public class CurieuxHttpUtil {

	private static final String IN_CHARSET_NAME = "Cp1252";

	private static String generateUrl(String target, Long rowId) {
		return generateRootUrl(target) + "agenda/sortie?row=" + rowId;
	}

	public static String generateRootUrl(String target) {
		return "http://" + target + ".curieux.net/";
	}

	public static Document getContent(String target, Long rowId) throws ProcessingException,
			UnsupportedOperationException, WrongRowIdException, CurieuxEventFinishedException {

		try {
			String url = generateUrl(target, rowId);
			return Jsoup.parse(new URL(url).openStream(), IN_CHARSET_NAME, url);
		} catch (IOException e) {
			e.printStackTrace();
			throw new ProcessingException();
		}
	}
}
