package uk.co.eelpieconsulting.osm.nominatim.views;

import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class JsonView implements View {
	
	private final WebStyleJsonSerializer jsonSerializer;
	private final EtagGenerator etagGenerator;
	private Integer maxAge;
	private String dataField;

	public JsonView(WebStyleJsonSerializer jsonSerializer, EtagGenerator etagGenerator) {
		this.jsonSerializer = jsonSerializer;
		this.etagGenerator = etagGenerator;
		this.dataField = "data";
	}
	
	public void setMaxAge(Integer maxAge) {
		this.maxAge = maxAge;
	}
	
	public void setDataField(String dataField) {
		this.dataField = dataField;
	}

	@Override
	public String getContentType() {
		return "application/json";
	}

	@Override
	public void render(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.setCharacterEncoding("UTF-8");
		response.setContentType(getContentType());
		if (maxAge != null) {			
			response.setHeader("Cache-Control", "max-age=" + maxAge);		
		}
		final String json = jsonSerializer.serialize(model.get(dataField));
		response.setHeader("Etag", etagGenerator.makeEtagFor(json));
		
		String callbackFunction = null;
		if (model.containsKey("callback")) {			
			callbackFunction = (String) model.get("callback");
			response.getWriter().write(callbackFunction + "(");			
		}
		
		response.getWriter().write(json);
		
		if (callbackFunction != null) {
			response.getWriter().write(");");			
		}
		
		response.getWriter().flush();
	}

}
