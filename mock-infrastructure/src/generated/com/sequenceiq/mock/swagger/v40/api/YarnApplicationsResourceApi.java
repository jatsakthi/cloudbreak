/**
 * NOTE: This class is auto generated by the swagger code generator program (2.4.16).
 * https://github.com/swagger-api/swagger-codegen
 * Do not edit the class manually.
 */
package com.sequenceiq.mock.swagger.v40.api;

import com.sequenceiq.mock.swagger.model.ApiYarnApplicationAttributeList;
import com.sequenceiq.mock.swagger.model.ApiYarnApplicationResponse;
import com.sequenceiq.mock.swagger.model.ApiYarnKillResponse;
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
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2020-11-16T21:48:33.802+01:00")

@Api(value = "YarnApplicationsResource", description = "the YarnApplicationsResource API")
@RequestMapping(value = "/{mockUuid}/api/v40")
public interface YarnApplicationsResourceApi {

    Logger log = LoggerFactory.getLogger(YarnApplicationsResourceApi.class);

    default Optional<ObjectMapper> getObjectMapper() {
        return Optional.empty();
    }

    default Optional<HttpServletRequest> getRequest() {
        return Optional.empty();
    }

    default Optional<String> getAcceptHeader() {
        return getRequest().map(r -> r.getHeader("Accept"));
    }

    @ApiOperation(value = "Returns the list of all attributes that the Service Monitor can associate with YARN applications.", nickname = "getYarnApplicationAttributes", notes = "Returns the list of all attributes that the Service Monitor can associate with YARN applications. <p> Examples of attributes include the user who ran the application and the number of maps completed by the application. <p> These attributes can be used to search for specific YARN applications through the getYarnApplications API. For example the 'user' attribute could be used in the search 'user = root'. If the attribute is numeric it can also be used as a metric in a tsquery (ie, 'select maps_completed from YARN_APPLICATIONS'). <p> Note that this response is identical for all YARN services. <p> Available since API v6.", response = ApiYarnApplicationAttributeList.class, authorizations = {
        @Authorization(value = "basic")
    }, tags={ "YarnApplicationsResource", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ApiYarnApplicationAttributeList.class) })
    @RequestMapping(value = "/clusters/{clusterName}/services/{serviceName}/yarnApplications/attributes",
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    default ResponseEntity<ApiYarnApplicationAttributeList> getYarnApplicationAttributes(@ApiParam(value = "The unique id of CB cluster (works in CB test framework only)",required=true) @PathVariable("mockUuid") String mockUuid,@ApiParam(value = "",required=true) @PathVariable("clusterName") String clusterName,@ApiParam(value = "",required=true) @PathVariable("serviceName") String serviceName) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                try {
                    return new ResponseEntity<>(getObjectMapper().get().readValue("{  \"items\" : [ {    \"name\" : \"...\",    \"type\" : \"...\",    \"displayName\" : \"...\",    \"supportsHistograms\" : true,    \"description\" : \"...\"  }, {    \"name\" : \"...\",    \"type\" : \"...\",    \"displayName\" : \"...\",    \"supportsHistograms\" : true,    \"description\" : \"...\"  } ]}", ApiYarnApplicationAttributeList.class), HttpStatus.NOT_IMPLEMENTED);
                } catch (IOException e) {
                    log.error("Couldn't serialize response for content type application/json", e);
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default YarnApplicationsResourceApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }


    @ApiOperation(value = "Returns a list of applications that satisfy the filter.", nickname = "getYarnApplications", notes = "Returns a list of applications that satisfy the filter <p> Available since API v6.", response = ApiYarnApplicationResponse.class, authorizations = {
        @Authorization(value = "basic")
    }, tags={ "YarnApplicationsResource", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ApiYarnApplicationResponse.class) })
    @RequestMapping(value = "/clusters/{clusterName}/services/{serviceName}/yarnApplications",
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    default ResponseEntity<ApiYarnApplicationResponse> getYarnApplications(@ApiParam(value = "The unique id of CB cluster (works in CB test framework only)",required=true) @PathVariable("mockUuid") String mockUuid,@ApiParam(value = "",required=true) @PathVariable("clusterName") String clusterName,@ApiParam(value = "The name of the service",required=true) @PathVariable("serviceName") String serviceName,@ApiParam(value = "A filter to apply to the applications. A basic filter tests the value of an attribute and looks something like 'executing = true' or 'user = root'. Multiple basic filters can be combined into a complex expression using standard and / or boolean logic and parenthesis. An example of a complex filter is: 'application_duration > 5s and (user = root or user = myUserName').", defaultValue = "") @Valid @RequestParam(value = "filter", required = false, defaultValue="") String filter,@ApiParam(value = "Start of the period to query in ISO 8601 format (defaults to 5 minutes before the 'to' time).") @Valid @RequestParam(value = "from", required = false) String from,@ApiParam(value = "The maximum number of applications to return. Applications will be returned in the following order: <ul> <li> All executing applications, ordered from longest to shortest running </li> <li> All completed applications order by end time descending. </li> </ul>", defaultValue = "100") @Valid @RequestParam(value = "limit", required = false, defaultValue="100") Integer limit,@ApiParam(value = "The offset to start returning applications from. This is useful for paging through lists of applications. Note that this has non-deterministic behavior if executing applications are included in the response because they can disappear from the list while paging. To exclude executing applications from the response and a 'executing = false' clause to your filter.", defaultValue = "0") @Valid @RequestParam(value = "offset", required = false, defaultValue="0") Integer offset,@ApiParam(value = "End of the period to query in ISO 8601 format (defaults to now).", defaultValue = "now") @Valid @RequestParam(value = "to", required = false, defaultValue="now") String to) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                try {
                    return new ResponseEntity<>(getObjectMapper().get().readValue("{  \"applications\" : [ {    \"allocatedMB\" : 12345,    \"allocatedVCores\" : 12345,    \"runningContainers\" : 12345,    \"applicationTags\" : [ \"...\", \"...\" ],    \"allocatedMemorySeconds\" : 12345,    \"allocatedVcoreSeconds\" : 12345,    \"applicationId\" : \"...\",    \"name\" : \"...\",    \"startTime\" : \"...\",    \"endTime\" : \"...\",    \"user\" : \"...\",    \"pool\" : \"...\",    \"progress\" : 12345.0,    \"attributes\" : {      \"property1\" : \"...\",      \"property2\" : \"...\"    },    \"mr2AppInformation\" : {      \"jobState\" : \"...\"    },    \"state\" : \"...\",    \"containerUsedMemorySeconds\" : 12345.0,    \"containerUsedMemoryMax\" : 12345.0,    \"containerUsedCpuSeconds\" : 12345.0,    \"containerUsedVcoreSeconds\" : 12345.0,    \"containerAllocatedMemorySeconds\" : 12345.0,    \"containerAllocatedVcoreSeconds\" : 12345.0  }, {    \"allocatedMB\" : 12345,    \"allocatedVCores\" : 12345,    \"runningContainers\" : 12345,    \"applicationTags\" : [ \"...\", \"...\" ],    \"allocatedMemorySeconds\" : 12345,    \"allocatedVcoreSeconds\" : 12345,    \"applicationId\" : \"...\",    \"name\" : \"...\",    \"startTime\" : \"...\",    \"endTime\" : \"...\",    \"user\" : \"...\",    \"pool\" : \"...\",    \"progress\" : 12345.0,    \"attributes\" : {      \"property1\" : \"...\",      \"property2\" : \"...\"    },    \"mr2AppInformation\" : {      \"jobState\" : \"...\"    },    \"state\" : \"...\",    \"containerUsedMemorySeconds\" : 12345.0,    \"containerUsedMemoryMax\" : 12345.0,    \"containerUsedCpuSeconds\" : 12345.0,    \"containerUsedVcoreSeconds\" : 12345.0,    \"containerAllocatedMemorySeconds\" : 12345.0,    \"containerAllocatedVcoreSeconds\" : 12345.0  } ],  \"warnings\" : [ \"...\", \"...\" ]}", ApiYarnApplicationResponse.class), HttpStatus.NOT_IMPLEMENTED);
                } catch (IOException e) {
                    log.error("Couldn't serialize response for content type application/json", e);
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default YarnApplicationsResourceApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }


    @ApiOperation(value = "Kills an YARN Application.", nickname = "killYarnApplication", notes = "Kills an YARN Application <p> Available since API v6.", response = ApiYarnKillResponse.class, authorizations = {
        @Authorization(value = "basic")
    }, tags={ "YarnApplicationsResource", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Success", response = ApiYarnKillResponse.class) })
    @RequestMapping(value = "/clusters/{clusterName}/services/{serviceName}/yarnApplications/{applicationId}/kill",
        produces = { "application/json" }, 
        method = RequestMethod.POST)
    default ResponseEntity<ApiYarnKillResponse> killYarnApplication(@ApiParam(value = "The unique id of CB cluster (works in CB test framework only)",required=true) @PathVariable("mockUuid") String mockUuid,@ApiParam(value = "The applicationId to kill",required=true) @PathVariable("applicationId") String applicationId,@ApiParam(value = "",required=true) @PathVariable("clusterName") String clusterName,@ApiParam(value = "The name of the service",required=true) @PathVariable("serviceName") String serviceName) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                try {
                    return new ResponseEntity<>(getObjectMapper().get().readValue("{  \"warning\" : \"...\"}", ApiYarnKillResponse.class), HttpStatus.NOT_IMPLEMENTED);
                } catch (IOException e) {
                    log.error("Couldn't serialize response for content type application/json", e);
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default YarnApplicationsResourceApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

}
