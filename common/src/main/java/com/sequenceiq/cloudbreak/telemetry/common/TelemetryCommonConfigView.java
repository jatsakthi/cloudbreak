package com.sequenceiq.cloudbreak.telemetry.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import com.sequenceiq.cloudbreak.telemetry.TelemetryClusterDetails;
import com.sequenceiq.cloudbreak.telemetry.TelemetryConfigView;
import com.sequenceiq.common.api.telemetry.model.AnonymizationRule;

public class TelemetryCommonConfigView implements TelemetryConfigView {

    private final TelemetryClusterDetails clusterDetails;

    private final List<AnonymizationRule> rules;

    private TelemetryCommonConfigView(Builder builder) {
        clusterDetails = builder.clusterDetails;
        rules = builder.rules;
    }

    public static final class Builder {

        private TelemetryClusterDetails clusterDetails;

        private List<AnonymizationRule> rules;

        public TelemetryCommonConfigView build() {
            return new TelemetryCommonConfigView(this);
        }

        public Builder withClusterDetails(TelemetryClusterDetails clusterDetails) {
            this.clusterDetails = clusterDetails;
            return this;
        }

        public Builder withRules(List<AnonymizationRule> rules) {
            this.rules = rules;
            return this;
        }
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        if (this.clusterDetails != null) {
            map.putAll(clusterDetails.toMap());
        }
        if (CollectionUtils.isNotEmpty(this.rules)) {
            map.put("anonymizationRules", this.rules);
        }
        return map;
    }
}
