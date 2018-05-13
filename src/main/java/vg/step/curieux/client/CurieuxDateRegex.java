package vg.step.curieux.client;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dozer.DozerBeanMapper;
import vg.step.curieux.client.exception.CurieuxEventFinishedException;
import vg.step.curieux.client.exception.UnableToParseDateException;
import vg.step.curieux.client.model.CurieuxEventModel;
import vg.step.curieux.client.model.CurieuxEventRawModel;

public class CurieuxDateRegex {

	private static final DozerBeanMapper mapper = new DozerBeanMapper();

	private static final ZoneId TIMEZONE = ZoneId.of("Europe/Paris");

	/**
	 * Espaces blancs.
	 */
	private static final String WSP = "(?:\\s+)";

	/**
	 * Jour : lundi, mardi... etc...
	 */
	private static final String DAY_LABEL_RX = "((?:l|L)undi|(?:m|M)ardi|(?:m|M)ercredi|(?:j|J)eudi|(?:v|V)endredi|(?:s|S)amedi|(?:d|D)imanche)";

	/**
	 * Numéro de jour dans le mois, par ex 12.
	 */
	private static final String DAY_NUMBER_RX = "([1-9][0-9]?)(?:<sup>er</sup>|er)?";

	/**
	 * Jour et numéro de jour, par ex : lundi 20.
	 */
	private static final String DAY_RX = DAY_LABEL_RX + WSP + DAY_NUMBER_RX;

	private static final String JANUARY_RX = "(?:j|J)anvier";
	private static final String FEBUARY_RX = "(?:f|F)évrier";
	private static final String MARCH_RX = "(?:m|M)ars";
	private static final String APRIL_RX = "(?:a|A)vril";
	private static final String MAY_RX = "(?:m|M)ai";
	private static final String JUNE_RX = "(?:j|J)uin";
	private static final String JULLY_RX = "(?:j|J)uillet";
	private static final String AUGUST_RX = "(?:a|A)oût";
	private static final String SEPTEMBER_RX = "(?:s|S)eptembre";
	private static final String OCTOBER_RX = "(?:o|O)ctobre";
	private static final String NOVEMBER_RX = "(?:n|N)ovembre";
	private static final String DECEMBER_RX = "(?:d|D)écembre";

	/**
	 * Mois, par ex janvier, février.
	 */
	private static final String MONTH_RX = "(" + JANUARY_RX + "|" + FEBUARY_RX + "|" + MARCH_RX + "|" + APRIL_RX + "|"
			+ MAY_RX + "|" + JUNE_RX + "|" + JULLY_RX + "|" + AUGUST_RX + "|" + SEPTEMBER_RX + "|" + OCTOBER_RX + "|"
			+ NOVEMBER_RX + "|" + DECEMBER_RX + ")";

	/**
	 * Heure, par ex 20h ou 18h20.
	 */
	private static final String HOUR_RX = "([0-9][0-9]?h[0-9]{0,2})";

	/**
	 * Année, par ex 2017.
	 */
	private static final String YEAR_RX = "(20[1-2][0-9])";

	/**
	 * Date du type : Jeudi 27 avril 2017.
	 */
	private static final String STANDARD_DATE_RX = DAY_RX + WSP + MONTH_RX + WSP + YEAR_RX;

	/**
	 * Deux jours : Vendredi 17 et samedi 18 février 2017.
	 */
	private static final String TWO_DAYS_RX = DAY_LABEL_RX + WSP + DAY_NUMBER_RX + WSP + "et" + WSP + STANDARD_DATE_RX;

	/**
	 * Liste de jours : Du 15 février au 24 avril 2017.
	 */
	private static final String LIST_DAYS_RX = "Du" + WSP + DAY_NUMBER_RX + WSP + MONTH_RX + WSP + "au" + WSP
			+ DAY_NUMBER_RX + WSP + MONTH_RX + WSP + YEAR_RX;

	/**
	 * Heure de début : à partir de 21h30.
	 */
	private static final String HOUR_BEGINNING_RX = "à partir de" + WSP + HOUR_RX;

	/**
	 * Heure de début et de fin : de 9h30 à 17h.
	 */
	private static final String HOUR_RANGE_RX = "de" + WSP + HOUR_RX + WSP + "à" + WSP + HOUR_RX;

	/**
	 * Date du type : Du 15 février au 24 avril 2017 à partir de 21h30
	 */
	// private static final String DATE_TYPE_01_RX = LIST_DAYS_RX + WSP +
	// HOUR_BEGINNING_RX;

	/**
	 * Date du type : Du 15 février au 24 avril 2017 de 10h à 19h
	 */
	// private static final String DATE_TYPE_02_RX = LIST_DAYS_RX + WSP +
	// HOUR_RANGE_RX;

	/**
	 * Date du type : Vendredi 17 et samedi 18 février 2017 à partir de 21h30
	 */
	private static final String DATE_TYPE_11_RX = TWO_DAYS_RX + WSP + HOUR_BEGINNING_RX;

	/**
	 * Date du type : Samedi 20 et dimanche 21 mai 2017 de 10h à 19h
	 */
	private static final String DATE_TYPE_12_RX = TWO_DAYS_RX + WSP + HOUR_RANGE_RX;

	/**
	 * Date du type : Jeudi 27 avril 2017 à partir de 20h
	 */
	private static final String DATE_TYPE_21_RX = STANDARD_DATE_RX + WSP + HOUR_BEGINNING_RX;

	/**
	 * Date du type : Samedi 18 février 2017 de 09h30 à 17h
	 */
	private static final String DATE_TYPE_22_RX = STANDARD_DATE_RX + WSP + HOUR_RANGE_RX;

	public static List<CurieuxEventModel> processDate(CurieuxEventRawModel rawmodel, CurieuxEventModel model)
			throws CurieuxEventFinishedException, UnableToParseDateException {

		String rawDate = rawmodel.getRawDate();
		if (eventIsFinished(rawDate)) {
			throw new CurieuxEventFinishedException();
		}

		boolean matched = false;
		Matcher matcher;

		if (!matched) {
			matcher = getMatcher(DATE_TYPE_11_RX, rawDate);
			if (matcher.find()) {
				matched = true;
				/**
				 * grp 1 : Vendredi <br>
				 * grp 2 : 17 <br>
				 * grp 3 : samedi <br>
				 * grp 4 : 18 <br>
				 * grp 5 : mars <br>
				 * grp 6 : 2017 <br>
				 * grp 7 : 20h30 <br>
				 */
				LocalDateTime dateBeginning1 = createDate(matcher.group(7), matcher.group(2), matcher.group(5),
						matcher.group(6));
				LocalDateTime dateBeginning2 = createDate(matcher.group(7), matcher.group(4), matcher.group(5),
						matcher.group(6));

				CurieuxEventModel model1 = mapper.map(model, CurieuxEventModel.class);
				model1.setDateBeginning(convertDate(dateBeginning1));

				CurieuxEventModel model2 = mapper.map(model, CurieuxEventModel.class);
				model2.setDateBeginning(convertDate(dateBeginning2));

				List<CurieuxEventModel> cemList = new ArrayList<>();
				cemList.add(model1);
				cemList.add(model2);
				return cemList;
			}
		}

		if (!matched) {
			matcher = getMatcher(DATE_TYPE_12_RX, rawDate);
			if (matcher.find()) {
				matched = true;
				/**
				 * grp 1 : Samedi <br>
				 * grp 2 : 20 <br>
				 * grp 3 : dimanche <br>
				 * grp 4 : 21 <br>
				 * grp 5 : mai <br>
				 * grp 6 : 2017 <br>
				 * grp 7 : 10h <br>
				 * grp 8 : 19h <br>
				 */
				LocalDateTime dateBeginning1 = createDate(matcher.group(7), matcher.group(2), matcher.group(5),
						matcher.group(6));
				LocalDateTime dateBeginning2 = createDate(matcher.group(7), matcher.group(4), matcher.group(5),
						matcher.group(6));
				LocalDateTime dateEnding1 = createDate(matcher.group(8), matcher.group(2), matcher.group(5),
						matcher.group(6));
				LocalDateTime dateEnding2 = createDate(matcher.group(8), matcher.group(4), matcher.group(5),
						matcher.group(6));
				if (dateBeginning1.isAfter(dateEnding1)) {
					dateEnding1 = dateEnding1.plusDays(1L);
				}
				if (dateBeginning2.isAfter(dateEnding2)) {
					dateEnding2 = dateEnding2.plusDays(1L);
				}

				CurieuxEventModel model1 = mapper.map(model, CurieuxEventModel.class);
				model1.setDateBeginning(convertDate(dateBeginning1));
				model1.setDateEnding(convertDate(dateEnding1));
				CurieuxEventModel model2 = mapper.map(model, CurieuxEventModel.class);
				model2.setDateBeginning(convertDate(dateBeginning2));
				model2.setDateEnding(convertDate(dateEnding2));

				List<CurieuxEventModel> cemList = new ArrayList<>();
				cemList.add(model1);
				cemList.add(model2);
				return cemList;
			}
		}

		if (!matched) {
			matcher = getMatcher(DATE_TYPE_21_RX, rawDate);
			if (matcher.find()) {
				matched = true;
				/**
				 * grp 1 : Samedi <br>
				 * grp 2 : 25 <br>
				 * grp 3 : février <br>
				 * grp 4 : 2017 <br>
				 * grp 5 : 20h30 <br>
				 */
				LocalDateTime dateBeginning = createDate(matcher.group(5), matcher.group(2), matcher.group(3),
						matcher.group(4));
				model.setDateBeginning(convertDate(dateBeginning));
				List<CurieuxEventModel> cemList = new ArrayList<>();
				cemList.add(model);
				return cemList;
			}
		}

		if (!matched) {
			matcher = getMatcher(DATE_TYPE_22_RX, rawDate);
			if (matcher.find()) {
				matched = true;
				/**
				 * grp 1 : Samedi <br>
				 * grp 2 : 1 <br>
				 * grp 3 : avril <br>
				 * grp 4 : 2017 <br>
				 * grp 5 : 20h <br>
				 * grp 6 : 23h <br>
				 */
				LocalDateTime dateBeginning = createDate(matcher.group(5), matcher.group(2), matcher.group(3),
						matcher.group(4));
				LocalDateTime dateEnding = createDate(matcher.group(6), matcher.group(2), matcher.group(3),
						matcher.group(4));
				if (dateBeginning.isAfter(dateEnding)) {
					dateEnding = dateEnding.plusDays(1L);
				}
				model.setDateBeginning(convertDate(dateBeginning));
				model.setDateEnding(convertDate(dateEnding));
				List<CurieuxEventModel> cemList = new ArrayList<>();
				cemList.add(model);
				return cemList;
			}
		}

		throw new UnableToParseDateException();
	}

	private static Date convertDate(LocalDateTime ldt) {
		return Date.from(ldt.atZone(TIMEZONE).toInstant());
	}

	private static LocalDateTime createDate(String hourAndMinutesStr, String dayNumberStr, String monthStr,
			String yearStr) throws UnableToParseDateException {

		String[] tabHour = hourAndMinutesStr.split("h");
		if (tabHour.length == 0) {
			throw new UnableToParseDateException();
		}

		Integer hour = Integer.parseInt(tabHour[0]);

		Integer minutes = 0;
		if (tabHour.length > 1) {
			minutes = Integer.parseInt(tabHour[1]);
		}

		Integer day = Integer.parseInt(dayNumberStr);

		Integer month = getMonthNumber(monthStr);

		Integer year = Integer.parseInt(yearStr);

		LocalDate date = LocalDate.of(year, month, day);
		LocalTime time = LocalTime.of(hour, minutes);
		return LocalDateTime.of(date, time);
	}

	private static int getMonthNumber(String month) throws UnableToParseDateException {

		if (month.matches(JANUARY_RX)) {
			return 1;
		} else if (month.matches(FEBUARY_RX)) {
			return 2;
		} else if (month.matches(MARCH_RX)) {
			return 3;
		} else if (month.matches(APRIL_RX)) {
			return 4;
		} else if (month.matches(MAY_RX)) {
			return 5;
		} else if (month.matches(JUNE_RX)) {
			return 6;
		} else if (month.matches(JULLY_RX)) {
			return 7;
		} else if (month.matches(AUGUST_RX)) {
			return 8;
		} else if (month.matches(SEPTEMBER_RX)) {
			return 9;
		} else if (month.matches(OCTOBER_RX)) {
			return 10;
		} else if (month.matches(NOVEMBER_RX)) {
			return 11;
		} else if (month.matches(DECEMBER_RX)) {
			return 12;
		} else {
			throw new UnableToParseDateException();
		}
	}

	private static void sysoutGroups(Matcher matcher) {
		int count = matcher.groupCount();
		for (int i = 0; i <= count; i++) {
			System.out.println("grp " + i + " : " + matcher.group(i));
		}
	}

	private static boolean eventIsFinished(String content) {
		return content.contains("Cet évènement est terminé");
	}

	private static Matcher getMatcher(String regex, String content) {
		Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE | Pattern.DOTALL);
		return pattern.matcher(content);
	}
}
