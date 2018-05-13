package vg.step.curieux.client;

import com.google.maps.PlacesApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.PlaceDetails;
import vg.step.curieux.client.exception.UnableTOReadPlaceDetailsException;
import vg.step.curieux.client.exception.UnableToReadLocationException;
import vg.step.curieux.client.model.CurieuxEventModel;
import vg.step.curieux.client.model.CurieuxEventRawModel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.AddressComponent;
import com.google.maps.model.AddressComponentType;
import com.google.maps.model.GeocodingResult;

import java.io.IOException;

public class GoogleMapUtil {

    private final GeoApiContext context;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public GoogleMapUtil(String apiKey) {
	    this.context = new GeoApiContext().setApiKey(apiKey);
    }

	private GeocodingResult get(String address) throws UnableToReadLocationException {
		GeocodingResult[] results;
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		try {
			results = GeocodingApi.geocode(context, address).await();
			if (results == null || results.length == 0) {
				throw new UnableToReadLocationException();
			} else {
				return results[0];
			}
		} catch (Exception e) {
			throw new UnableToReadLocationException();
		}
	}

	public PlaceDetails getPlaceDetails(String placeId) throws UnableTOReadPlaceDetailsException {
		try {
			return PlacesApi.placeDetails(context, placeId).await();
		} catch (InterruptedException | ApiException | IOException e) {
			throw new UnableTOReadPlaceDetailsException();
		}
	}

	public CurieuxEventModel processAddress(CurieuxTargetEnum target, CurieuxEventRawModel rawmodel,
                                            CurieuxEventModel model) throws UnableToReadLocationException {
		GeocodingResult result;
		if (rawmodel.getMapAddress() != null) {
			result = get(rawmodel.getMapAddress() + " " + target.getDefaultCountry());
		} else if (rawmodel.getRawAddress() != null) {
			result = get(rawmodel.getRawAddress() + " " + target.getDefaultCity() + " " + target.getDefaultCountry());
		} else {
			throw new UnableToReadLocationException();
		}

		String placeName = null;

		try {
			if (result.placeId != null) {
				PlaceDetails placeDetails = getPlaceDetails(result.placeId);
				if (placeDetails != null) {
					placeName = placeDetails.name;
				}
			}
		} catch (UnableTOReadPlaceDetailsException e) {
		}

		model.setAddress(result.formattedAddress);
		model.setLatitude(result.geometry.location.lat);
		model.setLongitude(result.geometry.location.lng);
		model.setPlaceName(placeName);
		for (AddressComponent addressComponent : result.addressComponents) {
			for (AddressComponentType type : addressComponent.types) {
				if (AddressComponentType.LOCALITY.equals(type)) {
					model.setCity(addressComponent.longName);
				} else if (AddressComponentType.COUNTRY.equals(type)) {
					model.setCountry(addressComponent.longName);
				}
			}
		}
		if (model.getAddress() == null || model.getLatitude() == null || model.getLongitude() == null
				|| model.getCity() == null || model.getCountry() == null) {
			throw new UnableToReadLocationException();
		}
		return model;
	}
}
