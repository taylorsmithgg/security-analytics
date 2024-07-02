/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.securityanalytics.resthandler;

import org.opensearch.client.node.NodeClient;
import org.opensearch.core.xcontent.XContentParser;
import org.opensearch.rest.BaseRestHandler;
import org.opensearch.rest.RestRequest;
import org.opensearch.rest.action.RestToXContentListener;
import org.opensearch.securityanalytics.SecurityAnalyticsPlugin;
import org.opensearch.securityanalytics.action.AckAlertsAction;
import org.opensearch.securityanalytics.action.AckAlertsRequest;
import org.opensearch.securityanalytics.util.DetectorUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.opensearch.core.xcontent.XContentParserUtils.ensureExpectedToken;

/**
 * Acknowledge list of alerts generated by a detector.
 */
public class    RestAcknowledgeAlertsAction extends BaseRestHandler {
    @Override
    public String getName() {
        return "ack_detector_alerts_action";
    }

    @Override
    public List<Route> routes() {
        return Collections.singletonList(
                new Route(RestRequest.Method.POST, String.format(
                        Locale.getDefault(),
                        "%s/{%s}/_acknowledge/alerts",
                        SecurityAnalyticsPlugin.DETECTOR_BASE_URI,
                        DetectorUtils.DETECTOR_ID_FIELD)
                ));
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient nodeClient) throws IOException {
        String detectorId = request.param(DetectorUtils.DETECTOR_ID_FIELD);
        List<String> alertIds = getAlertIds(request.contentParser());
        AckAlertsRequest ackAlertsRequest = new AckAlertsRequest(detectorId, alertIds);
        return channel -> nodeClient.execute(
                AckAlertsAction.INSTANCE,
                ackAlertsRequest,
                new RestToXContentListener<>(channel)
        );
    }

    private List<String> getAlertIds(XContentParser xcp) throws IOException {
        List<String> ids = new ArrayList<>();
        ensureExpectedToken(XContentParser.Token.START_OBJECT, xcp.nextToken(), xcp);
        while (xcp.nextToken() != XContentParser.Token.END_OBJECT) {
            String fieldName = xcp.currentName();
            xcp.nextToken();
            if (fieldName.equals("alerts")) {
                ensureExpectedToken(XContentParser.Token.START_ARRAY, xcp.currentToken(), xcp);
                while (xcp.nextToken() != XContentParser.Token.END_ARRAY) {
                    ids.add(xcp.text());
                }
            }

        }
        return ids;
    }
}
