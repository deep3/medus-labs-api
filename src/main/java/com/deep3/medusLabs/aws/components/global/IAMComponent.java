package com.deep3.medusLabs.aws.components.global;

import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder;
import com.amazonaws.services.identitymanagement.model.*;
import com.deep3.medusLabs.aws.exceptions.InvalidAWSCredentials;
import com.deep3.medusLabs.model.enums.LogLevel;
import com.deep3.medusLabs.aws.components.AWSGlobalServiceComponentInterface;
import com.deep3.medusLabs.service.DeployedLabLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 *  Manages interactions with amazon identity management / IAM
 */
public class IAMComponent implements AWSGlobalServiceComponentInterface {

    private static final Logger LOG = LoggerFactory.getLogger(IAMComponent.class);

    private final AmazonIdentityManagement amazonIdentityManagement;

    @Autowired
    DeployedLabLogService deployedLabLogService;

    public IAMComponent(STSAssumeRoleSessionCredentialsProvider credentials) throws InvalidAWSCredentials {
        if (credentials == null) {
            throw new InvalidAWSCredentials();
        }
        this.amazonIdentityManagement = AmazonIdentityManagementClientBuilder.standard().withCredentials(credentials).build();
    }

    /**
     * Delete all IAM groups, Users, Role & User-managed policies.
     * @param deployedLabId
     */
    @Override
    public void deleteAll(long deployedLabId) {
        deleteGroups(deployedLabId);
        deleteUsers(deployedLabId);
        deleteRoles(deployedLabId);
        deletePolicies(deployedLabId);
    }

    /**
     * Deletes IAM groups using the following steps:
     * <ol>
     * <li>Gets all groups</li>
     * <li>Remove all group members</li>
     * <li>Remove all permissions from group</li>
     * <li>Delete Group</li>
     * </ol>
     */
    private void deleteGroups(long deployedLabId) {
        // Get Groups
        ListGroupsResult groups = amazonIdentityManagement.listGroups(new ListGroupsRequest());
        List<Group> allGroups = new ArrayList<>(groups.getGroups());

        while (groups.getMarker() != null) {
            groups = amazonIdentityManagement.listGroups(new ListGroupsRequest()
                    .withMarker(groups.getMarker()));
            allGroups.addAll(groups.getGroups());
        }

        for (Group group : allGroups) {

            // Remove all members from all groups
            GetGroupResult groupResult = amazonIdentityManagement.getGroup(new GetGroupRequest()
                    .withGroupName(group.getGroupName()));
            List<User> groupMembers = new ArrayList<>(groupResult.getUsers());

            while (groupResult.getMarker() != null) {
                groupResult = amazonIdentityManagement.getGroup(new GetGroupRequest()
                        .withMarker(groupResult.getMarker())
                        .withGroupName(group.getGroupName()));
                groupMembers.addAll(groupResult.getUsers());
            }

            for (User user : groupMembers) {
                try {
                    amazonIdentityManagement.removeUserFromGroup(new RemoveUserFromGroupRequest()
                            .withGroupName(group.getGroupName())
                            .withUserName(user.getUserName()));
                } catch (AmazonIdentityManagementException e) {
                    deployedLabLogService.createLog(deployedLabId, LogLevel.ERROR,
                            "Failed to remove user [%s] from group [%s]: %s", user.getUserName(), group.getGroupName(), e.getMessage());
                    LOG.error("Failed to remove user [{}] from group [{}]: {}", user.getUserName(), group.getGroupName(), e.getMessage());
                }
            }

            // Remove all permissions
            ListAttachedGroupPoliciesResult groupPoliciesResult = amazonIdentityManagement.listAttachedGroupPolicies(new ListAttachedGroupPoliciesRequest()
                    .withGroupName(group.getGroupName()));
            List<AttachedPolicy> groupPolicies = groupPoliciesResult.getAttachedPolicies();

            while (groupPoliciesResult.getMarker() != null) {
                groupPoliciesResult = amazonIdentityManagement.listAttachedGroupPolicies(new ListAttachedGroupPoliciesRequest()
                        .withGroupName(group.getGroupName())
                        .withMarker(groupPoliciesResult.getMarker()));
                groupPolicies.addAll(groupPoliciesResult.getAttachedPolicies());
            }

            for (AttachedPolicy groupPolicy : groupPolicies) {
                try {
                    amazonIdentityManagement.detachGroupPolicy(new DetachGroupPolicyRequest()
                            .withGroupName(group.getGroupName())
                            .withPolicyArn(groupPolicy.getPolicyArn()));
                } catch (AmazonIdentityManagementException e) {
                    LOG.error("Failed to detach policy [{}] from group [{}]: {}", groupPolicy.getPolicyArn(), group.getGroupName(), e.getMessage());
                }
            }

            // Delete Group
            try {
                amazonIdentityManagement.deleteGroup(new DeleteGroupRequest()
                        .withGroupName(group.getGroupName()));
            } catch (AmazonIdentityManagementException e) {
                LOG.error("Failed to delete to group [{}]: {}", group.getGroupName(), e.getMessage());
            }
        }
    }

    /**
     * Deletes all IAM users using the following steps:
     * <ol>
     * <li>Gets all users</li>
     * <li>Deletes user login profile</li>
     * <li>Deletes access keys</li>
     * <li>Removes all inline policies</li>
     * <li>Removes all mfa devices</li>
     * <li>Deletes User</li>
     * </ol>
     */
    private void deleteUsers(long deployedLabId) {
        // Get Users
        ListUsersResult users = amazonIdentityManagement.listUsers(new ListUsersRequest());
        List<User> allUsers = new ArrayList<>(users.getUsers());

        // Collect all Users
        while (users.isTruncated()) {
            users = amazonIdentityManagement.listUsers(new ListUsersRequest()
                    .withMarker(users.getMarker()));
            allUsers.addAll(users.getUsers());
        }

        for (User user : allUsers) {

            // Delete Login profile - 404's if the account does not have a login
            try {
                amazonIdentityManagement.deleteLoginProfile(new DeleteLoginProfileRequest()
                        .withUserName(user.getUserName()));
            } catch (NoSuchEntityException noSuchEntityException) {
                // No login profile
            }

            // Delete access keys
            ListAccessKeysResult accessKeysResult = amazonIdentityManagement.listAccessKeys(new ListAccessKeysRequest()
                    .withUserName(user.getUserName()));

            for (AccessKeyMetadata key : accessKeysResult.getAccessKeyMetadata()) {
                try {
                    amazonIdentityManagement.deleteAccessKey(new DeleteAccessKeyRequest()
                            .withAccessKeyId(key.getAccessKeyId())
                            .withUserName(user.getUserName()));
                } catch (AmazonIdentityManagementException e) {
                    deployedLabLogService.createLog(deployedLabId, LogLevel.ERROR,
                            "Failed to delete access key [%s] from user [%s]: %s", key.getAccessKeyId(), user.getUserName(), e.getMessage());
                    LOG.error("Failed to delete access key [{}] from user [{}]: {}", key.getAccessKeyId(), user.getUserName(), e.getMessage());
                }
            }

            // Remove all managed inline policies
            ListAttachedUserPoliciesResult attachedUserPoliciesResult = amazonIdentityManagement.listAttachedUserPolicies(new ListAttachedUserPoliciesRequest()
                    .withUserName(user.getUserName()));
            List<AttachedPolicy> attachedUserPolicies = new ArrayList<>(attachedUserPoliciesResult.getAttachedPolicies());

            while (attachedUserPoliciesResult.getMarker() != null) {
                attachedUserPoliciesResult = amazonIdentityManagement.listAttachedUserPolicies(new ListAttachedUserPoliciesRequest()
                        .withUserName(user.getUserName())
                        .withMarker(attachedUserPoliciesResult.getMarker()));
                attachedUserPolicies.addAll(attachedUserPoliciesResult.getAttachedPolicies());
            }

            for (AttachedPolicy attachedPolicy : attachedUserPolicies) {
                try {
                    amazonIdentityManagement.detachUserPolicy(new DetachUserPolicyRequest()
                            .withPolicyArn(attachedPolicy.getPolicyArn())
                            .withUserName(user.getUserName()));
                } catch (AmazonIdentityManagementException e) {
                    deployedLabLogService.createLog(deployedLabId, LogLevel.ERROR,
                            "Failed to detach policy [%s] from user [%s]: %s", attachedPolicy.getPolicyName(), user.getUserName(), e.getMessage());
                    LOG.error("Failed to detach policy [{}] from user [{}]: {}", attachedPolicy.getPolicyName(), user.getUserName(), e.getMessage());
                }
            }

            // Remove MFA devices
            ListMFADevicesResult mfaDevicesResult = amazonIdentityManagement.listMFADevices(new ListMFADevicesRequest()
                    .withUserName(user.getUserName()));

            for (MFADevice mfaDevice : mfaDevicesResult.getMFADevices()) {
                try {
                    amazonIdentityManagement.deactivateMFADevice(new DeactivateMFADeviceRequest()
                            .withSerialNumber(mfaDevice.getSerialNumber())
                            .withUserName(user.getUserName()));
                } catch (AmazonIdentityManagementException e) {
                    deployedLabLogService.createLog(deployedLabId, LogLevel.ERROR,
                            "Failed to deactivate MFA device [%s] for user [%s]: %s", mfaDevice.getSerialNumber(), user.getUserName(), e.getMessage());
                    LOG.error("Failed to deactivate MFA device [{}] for user [{}]: {}", mfaDevice.getSerialNumber(), user.getUserName(), e.getMessage());
                }

                try {
                    amazonIdentityManagement.deleteVirtualMFADevice(new DeleteVirtualMFADeviceRequest()
                            .withSerialNumber(mfaDevice.getSerialNumber()));
                } catch (AmazonIdentityManagementException e) {
                    deployedLabLogService.createLog(deployedLabId, LogLevel.ERROR,
                            "Failed to delete MFA device [%s] for user [%s]: %s", mfaDevice.getSerialNumber(), user.getUserName(), e.getMessage());
                    LOG.error("Failed to delete MFA device [{}] for user [{}]: {}", mfaDevice.getSerialNumber(), user.getUserName(), e.getMessage());
                }
            }

            // Delete User
            try {
                amazonIdentityManagement.deleteUser(new DeleteUserRequest()
                        .withUserName(user.getUserName()));
            } catch (AmazonIdentityManagementException e) {
                deployedLabLogService.createLog(deployedLabId, LogLevel.ERROR,
                        "Failed to delete user [%s]: %s", user.getUserName(), e.getMessage());
                LOG.error("Failed to delete user [{}]: {}", user.getUserName(), e.getMessage());
            }
        }
    }

    /**
     * Deletes all IAM role using the following steps:
     * <ol>
     * <li>Gets all instance profiles</li>
     * <li>Disassociates them from roles</li>
     * <li>Deletes instance profiles</li>
     * <li>Gets all roles - excluding service and organisation required roles</li>
     * <li>Removes attached policies</li>
     * <li>Deletes role</li>
     * </ol>
     */
    private void deleteRoles(long deployedLabId) {
        // Remove roles from instance profiles then delete instance profiles
        ListInstanceProfilesResult instanceProfilesResult = amazonIdentityManagement.listInstanceProfiles(new ListInstanceProfilesRequest());
        List<InstanceProfile> instanceProfiles = new ArrayList<>(instanceProfilesResult.getInstanceProfiles());

        while (instanceProfilesResult.getMarker() != null) {
            instanceProfilesResult = amazonIdentityManagement.listInstanceProfiles(new ListInstanceProfilesRequest()
                    .withMarker(instanceProfilesResult.getMarker()));
            instanceProfiles.addAll(instanceProfilesResult.getInstanceProfiles());
        }

        for (InstanceProfile instanceProfile : instanceProfiles) {
            for (Role role : instanceProfile.getRoles()) {
                try {
                    amazonIdentityManagement.removeRoleFromInstanceProfile(new RemoveRoleFromInstanceProfileRequest()
                            .withInstanceProfileName(instanceProfile.getInstanceProfileName())
                            .withRoleName(role.getRoleName()));
                } catch (AmazonIdentityManagementException e) {
                    deployedLabLogService.createLog(deployedLabId, LogLevel.ERROR,
                            "Failed to remove role [%s] from instance profile [%s]: %s", role.getRoleName(), instanceProfile.getInstanceProfileName(), e.getMessage());
                    LOG.error("Failed to remove role [{}] from instance profile [{}]: {}", role.getRoleName(), instanceProfile.getInstanceProfileName(), e.getMessage());
                }
            }
            try {
                amazonIdentityManagement.deleteInstanceProfile(new DeleteInstanceProfileRequest()
                        .withInstanceProfileName(instanceProfile.getInstanceProfileName()));
            } catch (Exception e) {
                deployedLabLogService.createLog(deployedLabId, LogLevel.ERROR,
                        "Failed to delete instance profile [%s]: %s", instanceProfile.getInstanceProfileName(), e.getMessage());
                LOG.error("Failed to delete instance profile [{}]: {}", instanceProfile.getInstanceProfileName(), e.getMessage());
            }
        }

        // Get Roles
        ListRolesResult roles = amazonIdentityManagement.listRoles(new ListRolesRequest());
        List<Role> allRoles = new ArrayList<>(roles.getRoles());

        while (roles.getMarker() != null) {
            roles = amazonIdentityManagement.listRoles(new ListRolesRequest()
                    .withMarker(roles.getMarker()));
            allRoles.addAll(roles.getRoles());
        }

        for (Role role : allRoles) {
            if (!role.getPath().startsWith("/aws-service-role/") // Can't delete service roles
                    && !role.getRoleName().equals("OrganizationAccountAccessRole")) { // Don't delete the organization role

                // Remove Attached Policies
                ListAttachedRolePoliciesResult attachedRolePoliciesResult = amazonIdentityManagement.listAttachedRolePolicies(new ListAttachedRolePoliciesRequest()
                        .withRoleName(role.getRoleName()));
                List<AttachedPolicy> attachedRolePolicies = new ArrayList<>(attachedRolePoliciesResult.getAttachedPolicies());

                while (attachedRolePoliciesResult.getMarker() != null) {
                    attachedRolePoliciesResult = amazonIdentityManagement.listAttachedRolePolicies(new ListAttachedRolePoliciesRequest()
                            .withRoleName(role.getRoleName())
                            .withMarker(attachedRolePoliciesResult.getMarker()));
                    attachedRolePolicies.addAll(attachedRolePoliciesResult.getAttachedPolicies());
                }

                for (AttachedPolicy attachedPolicy : attachedRolePolicies) {
                    try {
                        amazonIdentityManagement.detachRolePolicy(new DetachRolePolicyRequest()
                                .withRoleName(role.getRoleName())
                                .withPolicyArn(attachedPolicy.getPolicyArn()));
                    } catch (AmazonIdentityManagementException e) {
                        deployedLabLogService.createLog(deployedLabId, LogLevel.ERROR,
                                "Failed to detach policy [%s] from role [%s]: %s", attachedPolicy.getPolicyName(), role.getRoleName(), e.getMessage());
                        LOG.error("Failed to detach policy [{}] from role [{}]: {}", attachedPolicy.getPolicyName(), role.getRoleName(), e.getMessage());
                    }
                }

                // Remove Role Policies
                ListRolePoliciesResult rolePoliciesResult = amazonIdentityManagement.listRolePolicies(new ListRolePoliciesRequest()
                        .withRoleName(role.getRoleName()));
                List<String> rolePolicies = new ArrayList<>(rolePoliciesResult.getPolicyNames());

                while (rolePoliciesResult.getMarker() != null) {
                    rolePoliciesResult = amazonIdentityManagement.listRolePolicies(new ListRolePoliciesRequest()
                            .withRoleName(role.getRoleName())
                            .withMarker(rolePoliciesResult.getMarker()));
                    rolePolicies.addAll(rolePoliciesResult.getPolicyNames());
                }

                for (String rolePolicy : rolePolicies) {
                    try {
                        amazonIdentityManagement.deleteRolePolicy(new DeleteRolePolicyRequest()
                                .withRoleName(role.getRoleName())
                                .withPolicyName(rolePolicy));
                    } catch (AmazonIdentityManagementException e) {
                        deployedLabLogService.createLog(deployedLabId, LogLevel.ERROR,
                                "Failed to delete role policy [%s] from role [%s]: %s", rolePolicy, role.getRoleName(), e.getMessage());
                        LOG.error("Failed to delete role policy [{}] from role [{}]: {}", rolePolicy, role.getRoleName(), e.getMessage());
                    }
                }

                // Delete Role
                try {
                    amazonIdentityManagement.deleteRole(new DeleteRoleRequest()
                            .withRoleName(role.getRoleName()));
                } catch (AmazonIdentityManagementException e) {
                    deployedLabLogService.createLog(deployedLabId, LogLevel.ERROR,
                            "Failed to delete role [%s]: %s", role.getRoleName(), e.getMessage());
                    LOG.error("Failed to delete role [{}]: {}", role.getRoleName(), e.getMessage());
                }
            }
        }
    }

    /**
     * Deletes all user managed IAM policies.
     */
    private void deletePolicies(long deployedLabId) {
        ListPoliciesResult policies = amazonIdentityManagement.listPolicies(new ListPoliciesRequest()
                .withScope(PolicyScopeType.Local));
        List<Policy> allPolicies = new ArrayList<>(policies.getPolicies());

        while (policies.getMarker() != null) {
            policies = amazonIdentityManagement.listPolicies(new ListPoliciesRequest()
                    .withScope(PolicyScopeType.Local)
                    .withMarker(policies.getMarker()));
            allPolicies.addAll(policies.getPolicies());
        }

        for (Policy policy : allPolicies) {
            try {
                amazonIdentityManagement.deletePolicy(new DeletePolicyRequest()
                        .withPolicyArn(policy.getArn()));
            } catch (AmazonIdentityManagementException e) {
                deployedLabLogService.createLog(deployedLabId, LogLevel.ERROR,
                        "Failed to delete policy [%s]: %s", policy.getPolicyName(), e.getMessage());
                LOG.error("Failed to delete policy [{}]: {}", policy.getPolicyName(), e.getMessage());
            }
        }
    }
}
