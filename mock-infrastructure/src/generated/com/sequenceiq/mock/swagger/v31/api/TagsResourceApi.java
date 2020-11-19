/**
 * NOTE: This class is auto generated by the swagger code generator program (2.4.16).
 * https://github.com/swagger-api/swagger-codegen
 * Do not edit the class manually.
 */
package com.sequenceiq.mock.swagger.v31.api;

import com.sequenceiq.mock.swagger.model.ApiTagToEntitiesList;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.*;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2020-11-16T20:16:58.188+01:00")

@Api(value = "TagsResource", description = "the TagsResource API")
@RequestMapping(value = "/{mockUuid}/api/v31")
public interface TagsResourceApi {

    Logger log = LoggerFactory.getLogger(TagsResourceApi.class);

    default Optional<ObjectMapper> getObjectMapper() {
        return Optional.empty();
    }

    default Optional<HttpServletRequest> getRequest() {
        return Optional.empty();
    }

    default Optional<String> getAcceptHeader() {
        return getRequest().map(r -> r.getHeader("Accept"));
    }

    @ApiOperation(value = "Current CM entities and their tags.", nickname = "getTags", notes = "Current CM entities and their tags.", response = ApiTagToEntitiesList.class, authorizations = {
        @Authorization(value = "basic")
    }, tags={ "TagsResource", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ApiTagToEntitiesList.class) })
    @RequestMapping(value = "/tags",
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    default ResponseEntity<ApiTagToEntitiesList> getTags(@ApiParam(value = "The unique id of CB cluster (works in CB test framework only)",required=true) @PathVariable("mockUuid") String mockUuid,@ApiParam(value = "Number of tags to search for. Maximum value for limit is 1000.", defaultValue = "10") @Valid @RequestParam(value = "limit", required = false, defaultValue="10") Integer limit,@ApiParam(value = "Starting index of the list", defaultValue = "0") @Valid @RequestParam(value = "offset", required = false, defaultValue="0") Integer offset) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                try {
                    return new ResponseEntity<>(getObjectMapper().get().readValue("{  \"items\" : [ {    \"name\" : \"...\",    \"value\" : \"...\",    \"apiClusterRefs\" : [ {      \"clusterName\" : \"...\",      \"displayName\" : \"...\"    }, {      \"clusterName\" : \"...\",      \"displayName\" : \"...\"    } ],    \"apiServiceRefs\" : [ {      \"peerName\" : \"...\",      \"clusterName\" : \"...\",      \"serviceName\" : \"...\",      \"serviceDisplayName\" : \"...\",      \"serviceType\" : \"...\"    }, {      \"peerName\" : \"...\",      \"clusterName\" : \"...\",      \"serviceName\" : \"...\",      \"serviceDisplayName\" : \"...\",      \"serviceType\" : \"...\"    } ],    \"apiRoleRefs\" : [ {      \"clusterName\" : \"...\",      \"serviceName\" : \"...\",      \"roleName\" : \"...\"    }, {      \"clusterName\" : \"...\",      \"serviceName\" : \"...\",      \"roleName\" : \"...\"    } ],    \"apiHostRefs\" : [ {      \"hostId\" : \"...\",      \"hostname\" : \"...\"    }, {      \"hostId\" : \"...\",      \"hostname\" : \"...\"    } ]  }, {    \"name\" : \"...\",    \"value\" : \"...\",    \"apiClusterRefs\" : [ {      \"clusterName\" : \"...\",      \"displayName\" : \"...\"    }, {      \"clusterName\" : \"...\",      \"displayName\" : \"...\"    } ],    \"apiServiceRefs\" : [ {      \"peerName\" : \"...\",      \"clusterName\" : \"...\",      \"serviceName\" : \"...\",      \"serviceDisplayName\" : \"...\",      \"serviceType\" : \"...\"    }, {      \"peerName\" : \"...\",      \"clusterName\" : \"...\",      \"serviceName\" : \"...\",      \"serviceDisplayName\" : \"...\",      \"serviceType\" : \"...\"    } ],    \"apiRoleRefs\" : [ {      \"clusterName\" : \"...\",      \"serviceName\" : \"...\",      \"roleName\" : \"...\"    }, {      \"clusterName\" : \"...\",      \"serviceName\" : \"...\",      \"roleName\" : \"...\"    } ],    \"apiHostRefs\" : [ {      \"hostId\" : \"...\",      \"hostname\" : \"...\"    }, {      \"hostId\" : \"...\",      \"hostname\" : \"...\"    } ]  } ]}", ApiTagToEntitiesList.class), HttpStatus.NOT_IMPLEMENTED);
                } catch (IOException e) {
                    log.error("Couldn't serialize response for content type application/json", e);
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default TagsResourceApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }


    @ApiOperation(value = "Get Entities given the Tag name, grouped by entity type.", nickname = "readTagsByName", notes = "Get Entities given the Tag name, grouped by entity type.", response = ApiTagToEntitiesList.class, authorizations = {
        @Authorization(value = "basic")
    }, tags={ "TagsResource", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ApiTagToEntitiesList.class) })
    @RequestMapping(value = "/tags/{tagName}",
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    default ResponseEntity<ApiTagToEntitiesList> readTagsByName(@ApiParam(value = "The unique id of CB cluster (works in CB test framework only)",required=true) @PathVariable("mockUuid") String mockUuid,@ApiParam(value = "Name of the tag",required=true) @PathVariable("tagName") String tagName,@ApiParam(value = "Number of entries to search for. Maximum value for limit is 1000.", defaultValue = "10") @Valid @RequestParam(value = "limit", required = false, defaultValue="10") Integer limit,@ApiParam(value = "Starting index of the list", defaultValue = "0") @Valid @RequestParam(value = "offset", required = false, defaultValue="0") Integer offset) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                try {
                    return new ResponseEntity<>(getObjectMapper().get().readValue("{  \"items\" : [ {    \"name\" : \"...\",    \"value\" : \"...\",    \"apiClusterRefs\" : [ {      \"clusterName\" : \"...\",      \"displayName\" : \"...\"    }, {      \"clusterName\" : \"...\",      \"displayName\" : \"...\"    } ],    \"apiServiceRefs\" : [ {      \"peerName\" : \"...\",      \"clusterName\" : \"...\",      \"serviceName\" : \"...\",      \"serviceDisplayName\" : \"...\",      \"serviceType\" : \"...\"    }, {      \"peerName\" : \"...\",      \"clusterName\" : \"...\",      \"serviceName\" : \"...\",      \"serviceDisplayName\" : \"...\",      \"serviceType\" : \"...\"    } ],    \"apiRoleRefs\" : [ {      \"clusterName\" : \"...\",      \"serviceName\" : \"...\",      \"roleName\" : \"...\"    }, {      \"clusterName\" : \"...\",      \"serviceName\" : \"...\",      \"roleName\" : \"...\"    } ],    \"apiHostRefs\" : [ {      \"hostId\" : \"...\",      \"hostname\" : \"...\"    }, {      \"hostId\" : \"...\",      \"hostname\" : \"...\"    } ]  }, {    \"name\" : \"...\",    \"value\" : \"...\",    \"apiClusterRefs\" : [ {      \"clusterName\" : \"...\",      \"displayName\" : \"...\"    }, {      \"clusterName\" : \"...\",      \"displayName\" : \"...\"    } ],    \"apiServiceRefs\" : [ {      \"peerName\" : \"...\",      \"clusterName\" : \"...\",      \"serviceName\" : \"...\",      \"serviceDisplayName\" : \"...\",      \"serviceType\" : \"...\"    }, {      \"peerName\" : \"...\",      \"clusterName\" : \"...\",      \"serviceName\" : \"...\",      \"serviceDisplayName\" : \"...\",      \"serviceType\" : \"...\"    } ],    \"apiRoleRefs\" : [ {      \"clusterName\" : \"...\",      \"serviceName\" : \"...\",      \"roleName\" : \"...\"    }, {      \"clusterName\" : \"...\",      \"serviceName\" : \"...\",      \"roleName\" : \"...\"    } ],    \"apiHostRefs\" : [ {      \"hostId\" : \"...\",      \"hostname\" : \"...\"    }, {      \"hostId\" : \"...\",      \"hostname\" : \"...\"    } ]  } ]}", ApiTagToEntitiesList.class), HttpStatus.NOT_IMPLEMENTED);
                } catch (IOException e) {
                    log.error("Couldn't serialize response for content type application/json", e);
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default TagsResourceApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

}
