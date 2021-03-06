package com.sequenceiq.cloudbreak.cloud.aws.connector.resource;

import static com.sequenceiq.cloudbreak.cloud.aws.connector.resource.AwsResourceConstants.SUSPENDED_PROCESSES;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.amazonaws.services.autoscaling.model.AutoScalingGroup;
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingGroupsRequest;
import com.amazonaws.services.autoscaling.model.ResumeProcessesRequest;
import com.amazonaws.services.autoscaling.model.SuspendProcessesRequest;
import com.amazonaws.services.autoscaling.model.TerminateInstanceInAutoScalingGroupRequest;
import com.amazonaws.services.autoscaling.model.UpdateAutoScalingGroupRequest;
import com.sequenceiq.cloudbreak.cloud.aws.AwsClient;
import com.sequenceiq.cloudbreak.cloud.aws.CloudFormationStackUtil;
import com.sequenceiq.cloudbreak.cloud.aws.client.AmazonAutoScalingRetryClient;
import com.sequenceiq.cloudbreak.cloud.aws.client.AmazonCloudFormationRetryClient;
import com.sequenceiq.cloudbreak.cloud.aws.scheduler.AwsBackoffSyncPollingScheduler;
import com.sequenceiq.cloudbreak.cloud.aws.task.AwsPollTaskFactory;
import com.sequenceiq.cloudbreak.cloud.aws.view.AwsCredentialView;
import com.sequenceiq.cloudbreak.cloud.context.AuthenticatedContext;
import com.sequenceiq.cloudbreak.cloud.model.CloudStack;
import com.sequenceiq.cloudbreak.cloud.model.Group;
import com.sequenceiq.cloudbreak.cloud.task.PollTask;

@Service
public class AwsAutoScalingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AwsAutoScalingService.class);

    @Inject
    private CloudFormationStackUtil cfStackUtil;

    @Inject
    private AwsClient awsClient;

    @Inject
    private AwsPollTaskFactory awsPollTaskFactory;

    @Inject
    private AwsBackoffSyncPollingScheduler<Boolean> awsBackoffSyncPollingScheduler;

    public void suspendAutoScaling(AuthenticatedContext ac, CloudStack stack) {
        AmazonAutoScalingRetryClient amazonASClient = awsClient.createAutoScalingRetryClient(new AwsCredentialView(ac.getCloudCredential()),
                ac.getCloudContext().getLocation().getRegion().value());
        for (Group group : stack.getGroups()) {
            String asGroupName = cfStackUtil.getAutoscalingGroupName(ac, group.getName(), ac.getCloudContext().getLocation().getRegion().value());
            LOGGER.info("Suspend autoscaling group '{}'", asGroupName);
            amazonASClient.suspendProcesses(new SuspendProcessesRequest().withAutoScalingGroupName(asGroupName).withScalingProcesses(SUSPENDED_PROCESSES));
        }
    }

    public void resumeAutoScaling(AmazonAutoScalingRetryClient amazonASClient, Collection<String> groupNames, List<String> autoScalingPolicies) {
        for (String groupName : groupNames) {
            LOGGER.info("Resume autoscaling group '{}'", groupName);
            amazonASClient.resumeProcesses(new ResumeProcessesRequest().withAutoScalingGroupName(groupName).withScalingProcesses(autoScalingPolicies));
        }
    }

    public void scheduleStatusChecks(Map<String, Integer> groupsWithSize, AuthenticatedContext ac, Date timeBeforeASUpdate)
            throws AmazonAutoscalingFailed {
        for (Map.Entry<String, Integer> groupWithSize : groupsWithSize.entrySet()) {
            PollTask<Boolean> task = awsPollTaskFactory.newASGroupStatusCheckerTask(ac, groupWithSize.getKey(), groupWithSize.getValue(),
                    awsClient, cfStackUtil, timeBeforeASUpdate);
            try {
                awsBackoffSyncPollingScheduler.schedule(task);
            } catch (Exception e) {
                throw new AmazonAutoscalingFailed(e.getMessage(), e);
            }
        }
    }

    public void scheduleStatusChecks(List<Group> groups, AuthenticatedContext ac, AmazonCloudFormationRetryClient cloudFormationClient)
            throws AmazonAutoscalingFailed {
        scheduleStatusChecks(groups, ac, cloudFormationClient, null);
    }

    public void scheduleStatusChecks(List<Group> groups, AuthenticatedContext ac, AmazonCloudFormationRetryClient cloudFormationClient, Date timeBeforeASUpdate)
            throws AmazonAutoscalingFailed {
        for (Group group : groups) {
            String asGroupName = cfStackUtil.getAutoscalingGroupName(ac, cloudFormationClient, group.getName());
            LOGGER.debug("Polling Auto Scaling group until new instances are ready. [stack: {}, asGroup: {}]", ac.getCloudContext().getId(),
                    asGroupName);
            PollTask<Boolean> task = awsPollTaskFactory.newASGroupStatusCheckerTask(ac, asGroupName, group.getInstancesSize(),
                    awsClient, cfStackUtil, timeBeforeASUpdate);
            try {
                awsBackoffSyncPollingScheduler.schedule(task);
            } catch (Exception e) {
                throw new AmazonAutoscalingFailed(e.getMessage(), e);
            }
        }
    }

    public List<AutoScalingGroup> getAutoscalingGroups(AmazonAutoScalingRetryClient amazonASClient, Set<String> groupNames) {
        return amazonASClient.describeAutoScalingGroups(new DescribeAutoScalingGroupsRequest().withAutoScalingGroupNames(groupNames)).getAutoScalingGroups();
    }

    public void updateAutoscalingGroup(AmazonAutoScalingRetryClient amazonASClient, String groupName, Integer newSize) {
        LOGGER.info("Update '{}' Auto Scaling groups max size to {}, desired capacity to {}", groupName, newSize, newSize);
        amazonASClient.updateAutoScalingGroup(new UpdateAutoScalingGroupRequest()
                .withAutoScalingGroupName(groupName)
                .withMaxSize(newSize)
                .withDesiredCapacity(newSize));
        LOGGER.debug("Updated Auto Scaling group's desiredCapacity: [to: '{}']", newSize);
    }

    public void terminateInstance(AmazonAutoScalingRetryClient amazonASClient, String instanceId) {
        amazonASClient.terminateInstance(new TerminateInstanceInAutoScalingGroupRequest().withShouldDecrementDesiredCapacity(true).withInstanceId(instanceId));
    }
}
