package com.deep3.medusLabs.aws.utilities;

import com.amazonaws.util.EC2MetadataUtils;

/**
 * Utility class for AWS Metadata
 */
public class AWSMetadataUtils {

    private AWSMetadataUtils() {
        throw new IllegalStateException("Static Utility class");
    }

    /**
     * Get the Amazon Resource name for a given role
     * @return - the ARN
     */
    public static String getRoleArn(){
        return EC2MetadataUtils.getIAMInstanceProfileInfo().instanceProfileArn;
    }

    /**
     * Get the instance ID
     * @return instance ID
     */
    public static String getInstanceId(){
        return EC2MetadataUtils.getInstanceId();
    }
}
