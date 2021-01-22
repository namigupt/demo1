package com.adobe.aemaacs.internal.search.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.jackrabbit.JcrConstants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.adobe.aemaacs.external.search.services.SearchCriteria;
import com.adobe.aemaacs.external.search.services.SearchService;
import com.day.cq.dam.api.DamConstants;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.wcm.api.NameConstants;

@Component(immediate = true, service = SearchService.class)
public class SearchServiceImpl implements SearchService {

	@Reference
	private QueryBuilder queryBuilder;

	@Override
	public List<String> getPages(SearchCriteria searchCriteria, Session session) {
		Map<String, String> predicates = new HashMap<>();
		List<String> pageList = new ArrayList<>();
		predicates.put("type", "cq:PageContent");
		predicates.put("path", searchCriteria.getSearchPath());
		predicates.put(Constants.DATE_RANGE_PROPERTY, NameConstants.PN_PAGE_LAST_MOD);
		predicates.put(Constants.DATE_RANGE_LOWER_BOUND, searchCriteria.getStartDate());
		predicates.put(Constants.DATE_RANGE_LOWER_OPERATION, ">=");
		predicates.put(Constants.DATE_RANGE_UPPER_OPERATION, "<=");
		predicates.put(Constants.DATE_RANGE_UPPER_BOUND, searchCriteria.getEndDate());
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
		Map<String, String> predicates = new HashMap<>();
		List<String> assetList = new ArrayList<>();
		predicates.put("type", DamConstants.NT_DAM_ASSETCONTENT);
		predicates.put("path", searchCriteria.getSearchPath());
		predicates.put(Constants.DATE_RANGE_PROPERTY, JcrConstants.JCR_LASTMODIFIED);
		predicates.put(Constants.DATE_RANGE_LOWER_BOUND, searchCriteria.getStartDate());
		predicates.put(Constants.DATE_RANGE_LOWER_OPERATION, ">=");
		predicates.put(Constants.DATE_RANGE_UPPER_OPERATION, "<=");
		predicates.put(Constants.DATE_RANGE_UPPER_BOUND, searchCriteria.getEndDate());
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

	@Override
	public List<String> getDeletedArtifacts(SearchCriteria searchCriteria, Session session) {
		Map<String, String> predicates = new HashMap<>();
		List<String> artifactList = new ArrayList<>();
		predicates.put("type", "cq:AuditEvent");
		predicates.put("path", searchCriteria.getSearchPath());
		predicates.put(Constants.DATE_RANGE_PROPERTY, "cq:time");
		predicates.put(Constants.DATE_RANGE_LOWER_BOUND, searchCriteria.getStartDate());
		predicates.put(Constants.DATE_RANGE_LOWER_OPERATION, ">=");
		predicates.put(Constants.DATE_RANGE_UPPER_OPERATION, "<=");
		predicates.put(Constants.DATE_RANGE_UPPER_BOUND, searchCriteria.getEndDate());
		predicates.put("property", "cq:type");
		predicates.put("property.value", searchCriteria.getEventType());
		PredicateGroup pg = PredicateGroup.create(predicates);
		Query query = this.queryBuilder.createQuery(pg, session);

		query.getResult().getHits().forEach(result -> {
			try {
				artifactList.add(result.getProperties().get("cq:path", String.class));
			} catch (RepositoryException e) {
				return;
			}
		});

		return artifactList;
	}
	
	@Override
	public List<String> getArtifacts(SearchCriteria searchCriteria, Session session){
		switch (searchCriteria.getEventType()) {
		
		case "assets":
			return getAssets(searchCriteria, session);
		
		case "pages":
			return getPages(searchCriteria, session);
			
		default:
			return Collections.emptyList();
		}
	}
}
