package com.adobe.aemaacs.internal.search.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.adobe.aemaacs.external.search.services.SearchCriteria;
import com.adobe.aemaacs.external.search.services.SearchService;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;

@Component(immediate = true, service = SearchService.class)
public class SearchServiceImpl implements SearchService {
	
	@Reference
	private transient QueryBuilder queryBuilder;

	@Override
	public List<String> getPages(SearchCriteria searchCriteria, Session session) {
		Map<String, String> predicates = new HashMap<String, String>();
		List<String> pageList = new ArrayList<>();
        predicates.put("type", "cq:PageContent");
        predicates.put("path", searchCriteria.getSearchPath());
        predicates.put("1_daterange.property", "cq:lastModified");
        predicates.put("1_daterange.lowerBound", searchCriteria.getStartDate());
        predicates.put("1_daterange.lowerOperation", ">=");
        predicates.put("1_daterange.upperOperation", "<=");
        predicates.put("1_daterange.upperBound", searchCriteria.getEndDate());
        PredicateGroup pg = PredicateGroup.create(predicates);
        Query query = this.queryBuilder.createQuery(pg, session);
        
        query.getResult().getHits().forEach(result -> {
			try {
				pageList.add(result.getResource().getParent().getPath());
			} catch (RepositoryException e) {
				return;
			}
		});
        
		return pageList;
	}

	@Override
	public List<String> getAssets(SearchCriteria searchCriteria, Session session) {
		Map<String, String> predicates = new HashMap<String, String>();
		List<String> assetList = new ArrayList<>();
        predicates.put("type", "dam:AssetContent");
        predicates.put("path", searchCriteria.getSearchPath());
        predicates.put("1_daterange.property", "cq:lastModified");
        predicates.put("1_daterange.lowerBound", searchCriteria.getStartDate());
        predicates.put("1_daterange.lowerOperation", ">=");
        predicates.put("1_daterange.upperOperation", "<=");
        predicates.put("1_daterange.upperBound", searchCriteria.getEndDate());
        PredicateGroup pg = PredicateGroup.create(predicates);
        Query query = this.queryBuilder.createQuery(pg, session);
        
        query.getResult().getHits().forEach(result -> {
			try {
				assetList.add(result.getResource().getParent().getPath());
			} catch (RepositoryException e) {
				return;
			}
		});
        
		return assetList;
	}

}
