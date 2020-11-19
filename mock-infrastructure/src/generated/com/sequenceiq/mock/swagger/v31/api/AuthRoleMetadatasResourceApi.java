/**
 * NOTE: This class is auto generated by the swagger code generator program (2.4.16).
 * https://github.com/swagger-api/swagger-codegen
 * Do not edit the class manually.
 */
package com.sequenceiq.mock.swagger.v31.api;

import com.sequenceiq.mock.swagger.model.ApiAuthRoleMetadataList;
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

@Api(value = "AuthRoleMetadatasResource", description = "the AuthRoleMetadatasResource API")
@RequestMapping(value = "/{mockUuid}/api/v31")
public interface AuthRoleMetadatasResourceApi {

    Logger log = LoggerFactory.getLogger(AuthRoleMetadatasResourceApi.class);

    default Optional<ObjectMapper> getObjectMapper() {
        return Optional.empty();
    }

    default Optional<HttpServletRequest> getRequest() {
        return Optional.empty();
    }

    default Optional<String> getAcceptHeader() {
        return getRequest().map(r -> r.getHeader("Accept"));
    }

    @ApiOperation(value = "Returns a list of the auth roles' metadata for the built-in roles.", nickname = "readAuthRolesMetadata", notes = "Returns a list of the auth roles' metadata for the built-in roles.", response = ApiAuthRoleMetadataList.class, authorizations = {
        @Authorization(value = "basic")
    }, tags={ "AuthRoleMetadatasResource", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ApiAuthRoleMetadataList.class) })
    @RequestMapping(value = "/authRoleMetadatas",
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    default ResponseEntity<ApiAuthRoleMetadataList> readAuthRolesMetadata(@ApiParam(value = "The unique id of CB cluster (works in CB test framework only)",required=true) @PathVariable("mockUuid") String mockUuid,@ApiParam(value = "", allowableValues = "EXPORT, EXPORT_REDACTED, FULL, FULL_WITH_HEALTH_CHECK_EXPLANATION, SUMMARY", defaultValue = "summary") @Valid @RequestParam(value = "view", required = false, defaultValue="summary") String view) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                try {
                    return new ResponseEntity<>(getObjectMapper().get().readValue("{  \"items\" : [ {    \"displayName\" : \"...\",    \"uuid\" : \"...\",    \"role\" : \"...\",    \"authorities\" : [ {      \"name\" : \"...\",      \"description\" : \"...\"    }, {      \"name\" : \"...\",      \"description\" : \"...\"    } ],    \"allowedScopes\" : [ \"...\", \"...\" ]  }, {    \"displayName\" : \"...\",    \"uuid\" : \"...\",    \"role\" : \"...\",    \"authorities\" : [ {      \"name\" : \"...\",      \"description\" : \"...\"    }, {      \"name\" : \"...\",      \"description\" : \"...\"    } ],    \"allowedScopes\" : [ \"...\", \"...\" ]  } ]}", ApiAuthRoleMetadataList.class), HttpStatus.NOT_IMPLEMENTED);
                } catch (IOException e) {
                    log.error("Couldn't serialize response for content type application/json", e);
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default AuthRoleMetadatasResourceApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

}
