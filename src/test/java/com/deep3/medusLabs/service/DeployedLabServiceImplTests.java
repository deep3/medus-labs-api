package com.deep3.medusLabs.service;

import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;
import com.amazonaws.services.organizations.model.Account;
import com.deep3.medusLabs.aws.exceptions.ObjectNotFoundException;
import com.deep3.medusLabs.aws.model.AWSAccountWrapper;
import com.deep3.medusLabs.aws.service.OrganisationsService;
import com.deep3.medusLabs.model.DeployedLab;
import com.deep3.medusLabs.model.Lab;
import com.deep3.medusLabs.repository.DeployedLabRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.*;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
public class DeployedLabServiceImplTests {

    @Mock
    DeployedLab deployedLabTest;

    @Mock
    DeployedLabRepository deployedLabRepository;

    @Mock
    OrganisationsService organisationsService;

    @Mock
    STSAssumeRoleSessionCredentialsProvider credentials;

    @Before
    public void initialize()
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testSaveDeployedLab() {
        DeployedLabServiceImpl deployedLabService = new DeployedLabServiceImpl(deployedLabRepository, organisationsService);

        when(deployedLabService.saveDeployedLab(deployedLabTest)).thenReturn(deployedLabTest);

        DeployedLab lab = deployedLabService.saveDeployedLab(deployedLabTest);

        Assert.assertNotNull(lab);
    }

    @Test
    public void testRecordDeployedLab() {
        Lab lab = PowerMockito.mock(Lab.class);
        AWSAccountWrapper account = PowerMockito.mock(AWSAccountWrapper.class);
        when(account.getAccount()).thenReturn(new Account().withId("123456789"));

        DeployedLabServiceImpl deployedLabService = new DeployedLabServiceImpl(deployedLabRepository, organisationsService);
        deployedLabService.recordDeployingLab(lab, Collections.singletonList(account));

        verify(deployedLabRepository, times(1)).save(any());
    }

    @Test
    public void testGetNumberOfCleanAccounts() {

        DeployedLabServiceImpl deployedLabService = new DeployedLabServiceImpl(deployedLabRepository, organisationsService);

        DeployedLab lab = new DeployedLab();

        List<String> data = new ArrayList<>();
        data.add("123456789");
        data.add("987654321");

        Account account = new Account();
        ArrayList<Account> accountList = new ArrayList<>(Arrays.asList(account, account, account, account));

        List<DeployedLab> labs = new ArrayList<>();
        lab.setDeployedLabStatus(DeployedLab.DeployedLabStatus.ACTIVE);
        lab.setLabAccounts(data);
        labs.add(lab);

        when(deployedLabRepository.findAll()).thenReturn(labs);
        when(organisationsService.getValidAccounts()).thenReturn(accountList);

        int clean = deployedLabService.getNumberOfCleanAccounts();

        Assert.assertNotNull(clean);
        Assert.assertEquals("Expected 2 clean accounts avaliable", 2, clean);
    }

    @Test
    public void testFindDeployedLabById() throws ObjectNotFoundException {
        DeployedLabServiceImpl deployedLabService = new DeployedLabServiceImpl(deployedLabRepository, organisationsService);

        when(deployedLabRepository.findById(anyLong())).thenReturn(Optional.of(deployedLabTest));

        DeployedLab deployedLab = deployedLabService.findDeployedLabById(anyLong());

        Assert.assertNotNull(deployedLab);
    }

    @Test
    public void testFindDeletedLabs() {
        DeployedLabServiceImpl deployedLabService = new DeployedLabServiceImpl(deployedLabRepository, organisationsService);

        List<DeployedLab> data = new ArrayList();

        DeployedLab lab1 = new DeployedLab();
        lab1.setDeployedLabStatus(DeployedLab.DeployedLabStatus.DELETED);
        DeployedLab lab2 = new DeployedLab();
        lab2.setDeployedLabStatus(DeployedLab.DeployedLabStatus.ACTIVE);

        data.add(lab1);
        data.add(lab2);

        when(deployedLabRepository.findAll()).thenReturn(data);

        List<DeployedLab> res = deployedLabService.findDeletedLabs();

        Assert.assertNotNull(res);
        Assert.assertEquals(res.size(), 1);
    }

    @Test
    public void testDeleteDeployedLab() throws ObjectNotFoundException {
        DeployedLabServiceImpl deployedLabService = new DeployedLabServiceImpl(deployedLabRepository, organisationsService);

        when(deployedLabRepository.findById(anyLong())).thenReturn(Optional.of(deployedLabTest));

        deployedLabService.deleteDeployedLab(anyLong());

        verify(deployedLabRepository, times(1)).findById(anyLong());
        verify(deployedLabRepository, times(1)).save(any(DeployedLab.class));
    }

    @Test
    public void testDeleteAll() {
        DeployedLabServiceImpl deployedLabService = new DeployedLabServiceImpl(deployedLabRepository, organisationsService);
        deployedLabService.deleteAll();

        verify(deployedLabRepository, times(1)).deleteAll();
    }

    @Test
    public void testLabDeployedTime() {
        int year = new GregorianCalendar().get(Calendar.YEAR);
        int monthOfYear = new GregorianCalendar().get(Calendar.MONTH);
        int dayOfWeek = new GregorianCalendar().get(Calendar.DAY_OF_WEEK);

        DeployedLabServiceImpl deployedLabService = new DeployedLabServiceImpl(deployedLabRepository, organisationsService);
        GregorianCalendar cal = new GregorianCalendar(year, monthOfYear + 1, dayOfWeek + 2, 12, 01);

        DeployedLab deployedLab = new DeployedLab();

        deployedLab.setDeployed(java.util.Date.from(cal.toZonedDateTime().toInstant()));
        deployedLabService.labDeployedGreaterThan30Mins(deployedLab);

        verify(deployedLabRepository, times(1)).save(any());
    }
}
