package uk.co.eelpieconsulting.osm.nominatim.views;

import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class JsonView implements View {
	
	private final WebStyleJsonSerializer jsonSerializer;
	private final EtagGenerator etagGenerator;
	private final Integer maxAge;

	public JsonView(WebStyleJsonSerializer jsonSerializer, EtagGenerator etagGenerator, Integer maxAge) {
		this.jsonSerializer = jsonSerializer;
		this.etagGenerator = etagGenerator;
		this.maxAge = maxAge;
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
		final String json = jsonSerializer.serialize(model.get("data"));
		response.setHeader("Etag", etagGenerator.makeEtagFor(json));

		response.setHeader("Access-Control-Allow-Origin",  "*");
		response.setHeader("Access-Control-Allow-Method",  "GET");

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
