package com.deep3.medusLabs.aws.components.global;

import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.deep3.medusLabs.aws.components.AWSRegionalServiceComponentInterface;

public class TestMockGoodClass implements AWSRegionalServiceComponentInterface {

    public TestMockGoodClass(STSAssumeRoleSessionCredentialsProvider c, Regions r){}
    @Override

    public void deleteAll(long deployedLabId) {}
}
