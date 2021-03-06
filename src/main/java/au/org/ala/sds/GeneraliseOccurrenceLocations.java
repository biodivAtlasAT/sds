/***************************************************************************
 * Copyright (C) 2010 Atlas of Living Australia
 * All Rights Reserved.
 *
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 ***************************************************************************/
package au.org.ala.sds;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import au.org.ala.names.search.ALANameSearcher;
import au.org.ala.names.search.SearchResultException;
import au.org.ala.sds.model.SensitiveTaxon;
import au.org.ala.sds.util.Configuration;
import au.org.ala.sds.validation.FactCollection;
import au.org.ala.sds.validation.ServiceFactory;
import au.org.ala.sds.validation.ValidationOutcome;
import au.org.ala.sds.validation.ValidationService;

/**
 *
 * @author Peter Flemming (peter.flemming@csiro.au)
 */
public class GeneraliseOccurrenceLocations {

    protected static final Logger logger = Logger.getLogger(GeneraliseOccurrenceLocations.class);

    private static BasicDataSource occurrenceDataSource;
    private static ALANameSearcher nameSearcher;
    private static SensitiveSpeciesFinder sensitiveSpeciesFinder;

    public static void main(String[] args) throws Exception {
        nameSearcher = new ALANameSearcher(Configuration.getInstance().getNameMatchingIndex());
        sensitiveSpeciesFinder = SensitiveSpeciesFinderFactory.getSensitiveSpeciesFinder(
                "file:///data/sds/sensitive-species.xml", nameSearcher);
        occurrenceDataSource = new BasicDataSource();
        occurrenceDataSource.setDriverClassName("com.mysql.jdbc.Driver");
        occurrenceDataSource.setUrl("jdbc:mysql://localhost/portal");
        occurrenceDataSource.setUsername("root");
        occurrenceDataSource.setPassword("password");
        run(args.length == 1 ? args[0] : null);
    }

    private static void run(String startAt) throws SQLException, SearchResultException {
        Connection conn = occurrenceDataSource.getConnection();
        PreparedStatement pst = conn.prepareStatement(
                "SELECT id, scientific_name, latitude, longitude, generalised_metres, raw_latitude, raw_longitude FROM raw_occurrence_record LIMIT ?,?");
        int offset = startAt == null ? 0 : Integer.parseInt(startAt);
        int stride = 10000;
        int recCount = 0;
        pst.setInt(2, stride);
        ResultSet rs;

        for (pst.setInt(1, offset); true; offset += stride, pst.setInt(1, offset)) {
            rs = pst.executeQuery();
            if (!rs.isBeforeFirst()) {
                break;
            }
            while (rs.next()) {
                recCount++;

                String rawScientificName = (rs.getString("scientific_name"));
                int id = rs.getInt("id");
                String latitude = rs.getString("latitude");
                String longitude = rs.getString("longitude");
                String generalised_metres = rs.getString("generalised_metres");
                String raw_latitude = rs.getString("raw_latitude");
                String raw_longitude = rs.getString("raw_longitude");

                if (StringUtils.isEmpty(rawScientificName)) continue;
                if (StringUtils.isEmpty(latitude) || StringUtils.isEmpty(longitude)) continue;

               // See if it's sensitive
                SensitiveTaxon ss = sensitiveSpeciesFinder.findSensitiveSpecies(rawScientificName);
                if (ss != null) {
                    Map<String, String> facts = new HashMap<String, String>();
                    facts.put(FactCollection.DECIMAL_LATITUDE_KEY, latitude);
                    facts.put(FactCollection.DECIMAL_LONGITUDE_KEY, longitude);

                    ValidationService service = ServiceFactory.createValidationService(ss);
                    ValidationOutcome outcome = service.validate(facts);
                    Map<String, Object> result = outcome.getResult();

                    String speciesName = ss.getTaxonName();
                    if (StringUtils.isNotEmpty(ss.getCommonName())) {
                        speciesName += " [" + ss.getCommonName() + "]";
                    }

                    if (!result.get("decimalLatitude").equals(facts.get("decimalLatitude")) || !result.get("decimalLongitude").equals(facts.get("decimalLongitude"))) {
                       if (StringUtils.isEmpty(generalised_metres)) {
                            logger.info("Generalising location for " + id + " '" + rawScientificName + "' using Name='" + speciesName +
                                         "', Lat=" + result.get("decimalLatitude") +
                                         ", Long=" + result.get("decimalLongitude"));
                            //rawOccurrenceDao.updateLocation(id, result.get("decimalLatitude"), result.get("decimalLongitude"), result.getGeneralisationInMetres(), latitude, longitude);
                        } else {
                            if (generalised_metres != result.get("generalisationInMetres")) {
                                logger.info("Re-generalising location for " + id + " '" + rawScientificName + "' using Name='" + speciesName +
                                             "', Lat=" + result.get("decimalLatitude") +
                                             ", Long=" + result.get("decimalLongitude"));
                                //rawOccurrenceDao.updateLocation(id, result.get("decimalLatitude"), result.get("decimalLongitude"), result.getGeneralisationInMetres());
                            }
                        }
                    } else {
                        logger.info("Not generalising location for " + id + " '" + rawScientificName + "' using Name='" + speciesName +
                                    "', Lat=" + result.get("decimalLatitude") +
                                    ", Long=" + result.get("decimalLongitude") + " - " + result.get("dataGeneralizations"));
                    }
                } else {
                    // See if was sensitive but not now
                    if (StringUtils.isNotEmpty(generalised_metres)) {
                        logger.info("De-generalising location for " + id + " '" + rawScientificName + "', Lat=" + raw_latitude + ", Long=" + raw_longitude);
                        //rawOccurrenceDao.updateLocation(id, raw_latitude, raw_longitude, null, null, null);
                    }
                }
            }
            rs.close();
            logger.info("Processed " + recCount + " occurrence records.");
        }

        rs.close();
        pst.close();
        conn.close();
    }
}
