package org.openmrs.module.coreapps.web.controller;

import org.junit.Test;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class LatestObsRestControllerTest extends MainResourceControllerTest {

    @Override
    public String getURI() {
        return "latestobs";
    }

    @Override
    public String getUuid() {
        return RestTestConstants1_8.OBS_UUID;
    }

    @Override
    public long getAllCount() {
        return 0;
    }

    @Test
    public void testSearchForSingleConcept() throws Exception {
        MockHttpServletRequest request = newGetRequest(getURI());
        request.addParameter("patient", "5946f880-b197-400b-9caa-a3c661d23041");
        request.addParameter("concept", "11716f9c-1434-4f8d-b9fc-9aa14c4d6126");
        MockHttpServletResponse response = handle(request);
        List<Object> allNonVoidedObsList = deserialize(response).get("results");

        assertEquals(1, allNonVoidedObsList.size());
    }

    @Test
    public void testSearchForMultipleConcept() throws Exception {
        MockHttpServletRequest request = newGetRequest(getURI());
        request.addParameter("patient", "5946f880-b197-400b-9caa-a3c661d23041");
        request.addParameter("concept", "11716f9c-1434-4f8d-b9fc-9aa14c4d6126, 95312123-e0c2-466d-b6b1-cb6e990d0d65");
        MockHttpServletResponse response = handle(request);
        List<Object> allNonVoidedObsList = deserialize(response).get("results");

        assertEquals(2, allNonVoidedObsList.size());
    }

    @Test
    public void testSearchForMultipleLatestObsBasedOnNLatestObsParameter() throws Exception {
        MockHttpServletRequest request = newGetRequest(getURI());
        request.addParameter("nLatestObs", "2");
        request.addParameter("concept", "c607c80f-1ea9-4da3-bb88-6276ce8868dd");
        MockHttpServletResponse response = handle(request);
        List<Object> nLatestNonVoidedObs = deserialize(response).get("results");

        assertEquals(nLatestNonVoidedObs.size(), 2);
    }

    @Test
    public void testSearchForSingleLatestObsBasedOnMissingNLatestObsParameter() throws Exception {
        MockHttpServletRequest request = newGetRequest(getURI());
        request.addParameter("concept", "c607c80f-1ea9-4da3-bb88-6276ce8868dd");
        MockHttpServletResponse response = handle(request);
        List<Object> nLatestNonVoidedObs = deserialize(response).get("results");

        assertEquals(nLatestNonVoidedObs.size(), 1);
    }
}