package vg.step.curieux.client;

public enum CurieuxTargetEnum {

	STRABOURG("strasbourg", "Strasbourg", "France");

	private final String urlComponent;

	private final String defaultCity;

	private final String defaultCountry;

	private CurieuxTargetEnum(String urlComponent, String defaultCity, String defaultCountry) {
		this.urlComponent = urlComponent;
		this.defaultCity = defaultCity;
		this.defaultCountry = defaultCountry;
	}

	public String getUrlComponent() {
		return urlComponent;
	}

	public String getDefaultCity() {
		return defaultCity;
	}

	public String getDefaultCountry() {
		return defaultCountry;
	}

}
