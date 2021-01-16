package com.adobe.aemaacs.external.search.services;

import java.util.List;

import javax.jcr.Session;

public interface SearchService {
	
	List<String> getPages(SearchCriteria searchCriteria, Session session);

	List<String> getAssets(SearchCriteria searchCriteria, Session session);

}
