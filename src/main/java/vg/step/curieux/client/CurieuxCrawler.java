package vg.step.curieux.client;

import java.util.List;

import org.jsoup.nodes.Document;
import vg.step.curieux.client.exception.CurieuxEventFinishedException;
import vg.step.curieux.client.exception.ProcessingException;
import vg.step.curieux.client.exception.UnableToParseDateException;
import vg.step.curieux.client.exception.UnableToReadLocationException;
import vg.step.curieux.client.exception.WrongRowIdException;
import vg.step.curieux.client.model.CurieuxEventModel;
import vg.step.curieux.client.model.CurieuxEventRawModel;

public class CurieuxCrawler {

	private final CurieuxTargetEnum target;

	private final Long rowId;

	public CurieuxCrawler(CurieuxTargetEnum target, Long rowId) {
		super();
		this.target = target;
		this.rowId = rowId;
	}

	public List<CurieuxEventModel> start(String googleMapApiKey) throws ProcessingException, WrongRowIdException, UnsupportedOperationException,
			CurieuxEventFinishedException, UnableToReadLocationException, UnableToParseDateException {

		try {
			Document document = CurieuxHttpUtil.getContent(target.getUrlComponent(), rowId);
			CurieuxEventRawModel rawModel = CurieuxRegex.extractRawData(document);
			rawModel.setRowId(rowId);

			CurieuxEventModel model = new CurieuxEventModel();
			model.setRowId(rowId);
			model.setTitle(rawModel.getTitle());
			model.setDescription(rawModel.getDescription());
			if (rawModel.getImageUrl() != null) {
				if (CurieuxRegex.urlIsrelative(rawModel.getImageUrl())) {
					model.setImageUrl(CurieuxHttpUtil.generateRootUrl(target.getUrlComponent()) + rawModel
							.getImageUrl().replaceAll("\\.\\./", ""));
				} else {
					model.setImageUrl(rawModel.getImageUrl());
				}
			}
			model.setTags(rawModel.getTags());

			model = new GoogleMapUtil(googleMapApiKey).processAddress(target, rawModel, model);
			List<CurieuxEventModel> modelList = CurieuxDateRegex.processDate(rawModel, model);

			return modelList;
		} catch (NullPointerException e) {
			e.printStackTrace();
			throw new ProcessingException();
		}
	}
}
