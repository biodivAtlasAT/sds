/**************************************************************************
 *  Copyright (C) 2010 Atlas of Living Australia
 *  All Rights Reserved.
 *
 *  The contents of this file are subject to the Mozilla Public
 *  License Version 1.1 (the "License"); you may not use this file
 *  except in compliance with the License. You may obtain a copy of
 *  the License at http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS
 *  IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 *  implied. See the License for the specific language governing
 *  rights and limitations under the License.
 ***************************************************************************/
package org.ala.biocache.web;

import javax.inject.Inject;

import org.ala.biocache.dao.SearchDAO;
import org.ala.biocache.dto.BreakdownRequestParams;
import org.ala.biocache.dto.TaxaRankCountDTO;
import org.ala.biocache.util.SearchUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * A simple controller for providing breakdowns on top of the biocache.
 *  
 * @author Dave Martin (David.Martin@csiro.au)
 * @author Natasha Carter (Natasha.Carter@csiro.au)
 */
@Controller
public class BreakdownController {

	@Inject
	protected SearchDAO searchDAO;

	protected SearchUtils searchUtils = new SearchUtils();
	
	/**
	 * Performs a breakdown based on a collection
	 * @param requestParams
	 * @param uid
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/breakdown/collections/{uid}*")
	public @ResponseBody TaxaRankCountDTO breakdownByCollection(BreakdownRequestParams requestParams,
	            @PathVariable("uid") String uid) throws Exception{
	    return performBreakdown("collection_uid", uid, requestParams);
	}
	/**
	 * Performs a breakdown based on an institution
	 * @param requestParams
	 * @param uid
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/breakdown/institutions/{uid}*")
    public @ResponseBody TaxaRankCountDTO breakdownByInstitution(BreakdownRequestParams requestParams,
                @PathVariable("uid") String uid) throws Exception{
        return performBreakdown("institution_uid", uid, requestParams);
    }
	/**
	 * Performs a breakdown based on a data resource
	 * @param requestParams
	 * @param uid
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/breakdown/dataResources/{uid}*")
    public @ResponseBody TaxaRankCountDTO breakdownByDataResource(BreakdownRequestParams requestParams,
                @PathVariable("uid") String uid) throws Exception{
        return performBreakdown("data_resource_uid", uid, requestParams);
    }
	/**
	 * Performs a breakdown based on a data provider
	 * @param requestParams
	 * @param uid
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/breakdown/dataProviders/{uid}*")
    public @ResponseBody TaxaRankCountDTO breakdownByDataProvider(BreakdownRequestParams requestParams,
                @PathVariable("uid") String uid) throws Exception{
        return performBreakdown("data_provider_uid", uid, requestParams);
    }
	/**
	 * Performs a breakdown based on a data hub
	 * @param requestParams
	 * @param uid
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/breakdown/dataHubs/{uid}*")
    public @ResponseBody TaxaRankCountDTO breakdownByDataHub(BreakdownRequestParams requestParams,
                @PathVariable("uid") String uid) throws Exception{
        return performBreakdown("data_hub_uid", uid, requestParams);
    }
	/**
	 * performs the actual breakdown.  The type of breakdown will depend on which arguments were supplied to the webservice
	 * @param source
	 * @param uid
	 * @param requestParams
	 * @return
	 * @throws Exception
	 */
	private TaxaRankCountDTO performBreakdown(String source, String uid, BreakdownRequestParams requestParams) throws Exception{
	    StringBuilder sb = new StringBuilder();
	    sb.append(source).append(":").append(uid);
	    
	    
	    if(requestParams.getMax() != null && requestParams.getMax() >0){
	        /*
	          Returns a breakdown of collection,institution by a specific rank where the breakdown is limited to the
              supplied max number. The rank that is returned depends on which rank contains closest to max
              distinct values.
	         */
	        return searchDAO.findTaxonCountForUid(sb.toString(), requestParams.getQc(), requestParams.getMax());
	    }
	    else if(requestParams.getRank() != null){
	        if(requestParams.getName() != null && requestParams.getRank() != null)
	            sb.append(" AND ").append(requestParams.getRank()).append(":").append(requestParams.getName());
	        return searchDAO.findTaxonCountForUid(requestParams,sb.toString());
	    }
	    return null;
	}
	
   
    /**
     * Performs a breakdown without limiting the collection or institution
     * @param uuid
     * @param max
     * @param model
     * @return
     * @throws Exception
     */
    @RequestMapping(value = {"/breakdown/institutions*","/breakdown/collections*", "/breakdown/data-resources*","/breakdowns/data-providers*","/breakdowns/data-hubs*"}, method = RequestMethod.GET)
    public @ResponseBody TaxaRankCountDTO limitBreakdown(BreakdownRequestParams requestParams) throws Exception {
        return performBreakdown("*", "*", requestParams);                
    }





	/**
	 * @param searchDAO
	 *            the searchDAO to set
	 */
	public void setSearchDAO(SearchDAO searchDAO) {
		this.searchDAO = searchDAO;
	}
}