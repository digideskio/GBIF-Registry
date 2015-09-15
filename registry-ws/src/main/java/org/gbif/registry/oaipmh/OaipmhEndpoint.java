/*
 * Copyright 2015 Global Biodiversity Information Facility (GBIF)
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.registry.oaipmh;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.lyncode.xml.exceptions.XmlWriteException;
import com.lyncode.xoai.dataprovider.DataProvider;
import com.lyncode.xoai.dataprovider.builder.OAIRequestParametersBuilder;
import com.lyncode.xoai.dataprovider.model.Context;
import com.lyncode.xoai.dataprovider.model.MetadataFormat;
import com.lyncode.xoai.dataprovider.parameters.OAIRequest;
import com.lyncode.xoai.dataprovider.repository.Repository;
import com.lyncode.xoai.dataprovider.repository.RepositoryConfiguration;
import com.lyncode.xoai.model.oaipmh.DeletedRecord;
import com.lyncode.xoai.model.oaipmh.Granularity;
import com.lyncode.xoai.model.oaipmh.OAIPMH;
import com.lyncode.xoai.model.oaipmh.Verb;
import com.lyncode.xoai.services.impl.SimpleResumptionTokenFormat;
import com.lyncode.xoai.xml.XmlWritable;
import com.lyncode.xoai.xml.XmlWriter;
import org.gbif.api.exception.ServiceUnavailableException;

import javax.annotation.Nullable;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.util.Date;


/**
 * An OAI-PMH endpoint, using the XOAI library.
 */
@Path("oaipmh")
@Singleton
public class OaipmhEndpoint {

  private final MetadataFormat OAIDC_METADATA_FORMAT = new MetadataFormat()
          .withPrefix("oai_dc")
          .withNamespace("http://www.openarchives.org/OAI/2.0/oai_dc/")
          .withSchemaLocation("http://www.openarchives.org/OAI/2.0/oai_dc.xsd");

  private final MetadataFormat EML_METADATA_FORMAT = new MetadataFormat()
            .withPrefix("eml")
            .withNamespace("eml://ecoinformatics.org/eml-2.1.1")
            .withSchemaLocation("http://rs.gbif.org/schema/eml-gbif-profile/1.0.2/eml.xsd");

  private Context context = new Context()
          .withMetadataFormat(OAIDC_METADATA_FORMAT)
          .withMetadataFormat(EML_METADATA_FORMAT);

  private final OaipmhItemRepository oaipmhItemRepository;

  private RepositoryConfiguration repositoryConfiguration;
  private Repository repository;
  private DataProvider dataProvider;

  @Inject
  public OaipmhEndpoint(OaipmhItemRepository oaipmhItemRepository) {
    this.oaipmhItemRepository = oaipmhItemRepository;

    this.repositoryConfiguration = new RepositoryConfiguration()
            .withRepositoryName("GBIF Registry")
            .withAdminEmails()
            .withAdminEmail("admin@gbif.org")
            .withBaseUrl("http://localhost")
            .withEarliestDate(new Date())
            .withMaxListIdentifiers(100)
            .withMaxListSets(100)
            .withMaxListRecords(100)
            .withGranularity(Granularity.Second)
            .withDeleteMethod(DeletedRecord.NO) // TODO Not yet implemented
            .withDescription("<TestDescription xmlns=\"\">Test description</TestDescription>")
    ;

    this.repository = new Repository()
            .withItemRepository(oaipmhItemRepository)
            .withResumptionTokenFormatter(new SimpleResumptionTokenFormat())
            .withConfiguration(repositoryConfiguration);

    this.dataProvider = new DataProvider(context, repository);
  }

  @GET
  @Produces(MediaType.APPLICATION_XML)
  public InputStream oaipmh(
          @QueryParam("verb") String verb,
          @Nullable @QueryParam("identifier") String identifier,
          @Nullable @QueryParam("metadataPrefix") String metadataPrefix) {

    OAIRequestParametersBuilder reqBuilder = new OAIRequestParametersBuilder()
            .withVerb(verb)
            .withMetadataPrefix(metadataPrefix)
            .withIdentifier(identifier);

    // to enable later when we'll have a ItemRepository implementation
//  if(identifier != null){
//    reqBuilder.withIdentifier(identifier);
//  }

    //eventually we can simply do that:
    //return handle(reqBuilder.build());
    //but for development we control the 'verbs' available
    switch (verb) {
      case "GetRecord":
      case "Identify":
      case "ListMetadataFormats":
        return handle(reqBuilder.build());
      default:
        throw new RuntimeException("Invalid verb"); // TODO Incorrect exception.
    }
  }

  private InputStream handle(OAIRequest request) {
    try {
      OAIPMH oaipmh = dataProvider.handle(request);
      return new ByteArrayInputStream(write(oaipmh).getBytes("UTF-8"));

    } catch (Exception e) {
      throw new ServiceUnavailableException("OAI Failed to serialize dataset", e);
    }
  }

  protected String write(final XmlWritable handle) throws XMLStreamException, XmlWriteException {
    return XmlWriter.toString(new XmlWritable() {
      @Override
      public void write(XmlWriter writer) throws XmlWriteException {
        writer.write(handle);
      }
    });
  }
}
