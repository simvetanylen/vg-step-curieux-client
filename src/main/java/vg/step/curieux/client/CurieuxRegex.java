package vg.step.curieux.client;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.javatuples.Pair;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import vg.step.curieux.client.exception.WrongRowIdException;
import vg.step.curieux.client.model.CurieuxEventRawModel;

public class CurieuxRegex {

	public static CurieuxEventRawModel extractRawData(Document document) throws WrongRowIdException {
		Element usefulContent = document.select("#infoPrincipaleEvent").first();
		String mapContent = CurieuxRegex.extractMapContent(document.html());

		if (CurieuxRegex.isValid(usefulContent)) {
			CurieuxEventRawModel model = new CurieuxEventRawModel();
			model.setTitle(usefulContent.select(".titreEvent").first().html());
			try {
				model.setRawAddress(usefulContent.select(".lieuEvent").text().trim());
			} catch (Exception e) {
			}
			// model.setRawPrice(usefulContent.select(".prixEvent").first().text());
			try {
				model.setImageUrl(CurieuxRegex.extractImageUrl(usefulContent));
			} catch (Exception e) {
			}
			model.setRawDate(usefulContent.select(".dateTexteEvent").first().text());
			model.setDescription(CurieuxRegex.extractDescription(usefulContent));

			try {
				model.setTags(extractTags(usefulContent));
			} catch (Exception e) {
			}

			if (mapContent != null) {
				model.setMapAddress(CurieuxRegex.extractMapAddress(mapContent));
				Pair<Double, Double> coords = CurieuxRegex.extractCoordinates(mapContent);
				if (coords != null) {
					model.setLatitude(coords.getValue0());
					model.setLongitude(coords.getValue1());
				}
			}

			return model;

		} else {
			throw new WrongRowIdException();
		}
	}

	public static boolean urlIsrelative(String url) {
		return url.contains("../");
	}

	private static boolean isValid(Element element) {
		String title = element.select(".titreEvent").first().html();
		return title != null && !title.trim().isEmpty();
	}

	private static List<String> extractTags(Element element) {

		List<String> tags = new ArrayList<>();

		Elements tagElements = element.select("#blocRubrique");

		for (Element tagElement : tagElements) {
			String tag = tagElement.text().trim().replaceAll("\\s+", "_");
			tags.add(tag);
		}
		return tags;
	}

	private static String extractImageUrl(Element element) {
		return element.select(".zoneFondLisible img").first().attr("src");
	}

	private static String extractDescription(Element element) {

		String content = element.select(".zoneFondLisible").html();
		return removeHtml(replaceBr(removeCarriageReturn(content))).trim();
	}

	private static String removeCarriageReturn(String content) {
		String result = content.replaceAll("\\r", "");
		return result.replaceAll("\\n", "");
	}

	private static String removeHtml(String content) {
		return content.replaceAll("<[^>]+>", "");
	}

	private static String replaceBr(String content) {
		return content.replaceAll("<br>", "\\\n");
	}

	private static String extractMapAddress(String content) {
		Pattern pattern = Pattern.compile("<div class='adresseBloc'>(.*?)\\s*</div>",
				Pattern.MULTILINE | Pattern.DOTALL);
		Matcher matcher = pattern.matcher(content);
		if (matcher.find()) {
			return matcher.group(1);
		} else {
			return null;
		}
	}

	private static Pair<Double, Double> extractCoordinates(String content) {
		Pattern pattern = Pattern.compile("&location=(.*?),(.*?)&");
		Matcher matcher = pattern.matcher(content);
		if (matcher.find()) {
			Pair<Double, Double> coords = new Pair<>(Double.parseDouble(matcher.group(1)),
					Double.parseDouble(matcher.group(2)));
			return coords;
		} else {
			return null;
		}
	}

	private static String extractMapContent(String content) {
		Pattern pattern = Pattern.compile("<iframe class='vueGoogle'.*?<div class='blocInfoLieu' >",
				Pattern.MULTILINE | Pattern.DOTALL);
		Matcher matcher = pattern.matcher(content);
		if (matcher.find()) {
			return matcher.group(0);
		} else {
			return null;
		}
	}
}
