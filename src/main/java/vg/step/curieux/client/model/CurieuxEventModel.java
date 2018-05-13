package vg.step.curieux.client.model;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class CurieuxEventModel {

	private Long rowId;

	private String title;

	private String description;

	private Date dateBeginning;

	private Date dateEnding;

	private String address;

	private String city;

	private String country;

	private String placeName;

	private Double latitude;

	private Double longitude;

	private String imageUrl;

	private List<String> tags;

}
