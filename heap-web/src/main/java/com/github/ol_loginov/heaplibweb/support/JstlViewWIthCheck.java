package com.github.ol_loginov.heaplibweb.support;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.view.JstlView;

import java.util.Locale;
import java.util.Map;

public class JstlViewWIthCheck extends JstlView {
	@Override
	public boolean checkResource(@NonNull Locale locale) throws Exception {
		var sc = getServletContext();
		return sc != null && sc.getResource(getUrl()) != null;
	}

	@Override
	protected void renderMergedOutputModel(@NonNull Map<String, Object> model, @NonNull HttpServletRequest request, @NonNull HttpServletResponse response) throws Exception {
		var ac = getApplicationContext();
		if (ac != null) {
			var messageSource = ac.getBean(MessageSource.class);
			model.put("h", new JstlTool(messageSource, response.getLocale()));
		}
		super.renderMergedOutputModel(model, request, response);
	}
}

