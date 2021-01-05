package com.sequenceiq.cloudbreak.service.upgrade.image;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.cloud.model.catalog.Image;
import com.sequenceiq.cloudbreak.api.endpoint.v4.common.StackType;

@Component
public class ClouderaManagerPackageLocationFilter implements PackageLocationFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClouderaManagerPackageLocationFilter.class);

    @Override
    public boolean filterImage(Image image, Image currentImage, StackType stackType) {
        if (image == null || image.getRepo() == null || currentImage == null || StringUtils.isBlank(currentImage.getOsType())) {
            LOGGER.debug("Image or repo is null: {}", image);
            return false;
        } else {
            String repoUrl = image.getRepo().getOrDefault(currentImage.getOsType(), "");
            LOGGER.debug("Matching URL: [{}]", repoUrl);
            return URL_PATTERN.matcher(repoUrl).find();
        }
    }
}
