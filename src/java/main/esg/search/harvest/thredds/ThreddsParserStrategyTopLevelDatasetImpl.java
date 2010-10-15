/*******************************************************************************
 * Copyright (c) 2010 Earth System Grid Federation
 * ALL RIGHTS RESERVED. 
 * U.S. Government sponsorship acknowledged.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of the <ORGANIZATION> nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package esg.search.harvest.thredds;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import thredds.catalog.InvAccess;
import thredds.catalog.InvDataset;
import thredds.catalog.InvDocumentation;
import thredds.catalog.InvProperty;
import thredds.catalog.ThreddsMetadata.Variable;
import thredds.catalog.ThreddsMetadata.Variables;
import esg.search.core.Record;
import esg.search.core.RecordImpl;
import esg.search.query.impl.solr.SolrXmlPars;

/**
 * Implementation of {@link ThreddsParserStrategy} that produces a single {@link Record} for each top-level THREDDS dataset.
 */
public class ThreddsParserStrategyTopLevelDatasetImpl implements ThreddsParserStrategy {
	
	//private final Log LOG = LogFactory.getLog(this.getClass());
	
	/**
	 * Default URL builder
	 */
	private ThreddsDataseUrlBuilder urlBuilder = new ThreddsDatasetUrlBuilderCatalogUrlImpl();
		
	public ThreddsParserStrategyTopLevelDatasetImpl() {}
	
	public List<Record> parseDataset(final InvDataset dataset) {
		
		final List<Record> records = new ArrayList<Record>();
		
		// <dataset name="...." ID="..." restrictAccess="...">
		final String id = dataset.getID();
		Assert.notNull(id,"Dataset ID cannot be null");
		final Record record = new RecordImpl(id);
		final String name = dataset.getName();
		Assert.notNull(name, "Dataset name cannot be null");
		record.addField(SolrXmlPars.FIELD_TITLE, name);
		
		// catalog URL
		record.addField(SolrXmlPars.FIELD_URL, urlBuilder.buildUrl(dataset));
		
		// type
		record.addField(SolrXmlPars.FIELD_TYPE, "Dataset");
		
		// <documentation type="...">.......</documentation>
		for (final InvDocumentation documentation : dataset.getDocumentation()) {
			final String content = documentation.getInlineContent();
			if (StringUtils.hasText(content)) {
				record.addField(SolrXmlPars.FIELD_DESCRIPTION, content);
			}
		}
		
		// <variables vocabulary="CF-1.0">
        //   <variable name="hfss" vocabulary_name="surface_upward_sensible_heat_flux" units="W m-2">Surface Sensible Heat Flux</variable>
        // </variables>
		for (final Variables variables : dataset.getVariables()) {
			final String vocabulary = variables.getVocabulary();
			for (final Variable variable : variables.getVariableList()) {
				record.addField(SolrXmlPars.FIELD_VARIABLE, variable.getName());
				if (vocabulary.equals(ThreddsPars.CF)) record.addField(SolrXmlPars.FIELD_CF_VARIABLE, variable.getDescription());
			}
		}
		
		// <property name="..." value="..." />
		for (final InvProperty property : dataset.getProperties()) {
			if (property.getName().equals(ThreddsPars.DATASET_ID)) {
				// note: override record ID to get rid of version
				// <dataset name="TES Level 3 Monthly Data (NetCDF)" ID="nasa.jpl.tes.monthly.v1" restrictAccess="esg-user">
				// <property name="dataset_id" value="nasa.jpl.tes.monthly" />
				record.setId(property.getValue());
			} else if (property.getName().equals(SolrXmlPars.FIELD_TITLE)) {
				// note: record title already set from dataset name
				record.addField(SolrXmlPars.FIELD_DESCRIPTION, property.getValue());
			} else if (property.getName().equals(ThreddsPars.DATASET_VERSION)) {
				record.addField(SolrXmlPars.FIELD_VERSION, property.getValue());
			} else {
				record.addField(property.getName(), property.getValue());
			}
		}
		
		// <access urlPath="/ipcc/sresb1/atm/3h/hfss/miroc3_2_hires/run1/hfss_A3_2050.nc" serviceName="GRIDFTPatPCMDI" dataFormat="NetCDF" />
		for (final InvAccess access : dataset.getAccess()) {
			//
		}

		records.add(record);
		return records;
		
	}

	/**
	 * Method to set the builder for the URL to be associated with each record
	 * (overriding the default strategy).
	 * 
	 * @param urlBuilder
	 */
	public void setUrlBuilder(ThreddsDataseUrlBuilder urlBuilder) {
		this.urlBuilder = urlBuilder;
	}

}
