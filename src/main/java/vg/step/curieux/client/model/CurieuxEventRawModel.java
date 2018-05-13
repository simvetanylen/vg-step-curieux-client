package vg.step.curieux.client.model;

import lombok.Data;

import java.util.List;

@Data
public class CurieuxEventRawModel {

	private Long rowId;

	private String title;

	private String description;

	private String rawAddress;

	private String rawDate;

	private String rawPrice;

	private String imageUrl;

	private String mapAddress;

	private Double latitude;

	private Double longitude;

	private List<String> tags;

}
