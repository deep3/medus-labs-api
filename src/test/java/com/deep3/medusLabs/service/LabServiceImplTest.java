package com.deep3.medusLabs.service;

import com.deep3.medusLabs.aws.exceptions.ObjectNotFoundException;
import com.deep3.medusLabs.model.Lab;
import com.deep3.medusLabs.repository.LabRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class LabServiceImplTest {

    @Mock
    Lab labTest;

    @Mock
    LabRepository labRepository;

    @Before
    public void initialize()
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFindOne() throws ObjectNotFoundException {

        LabServiceImpl labServiceImpl = new LabServiceImpl(labRepository);

        when(labRepository.findById(anyLong())).thenReturn(Optional.of(labTest));

        Lab lab = labServiceImpl.findOne(anyLong());

        Assert.assertNotNull(lab);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testFindOneNullResult() throws ObjectNotFoundException {

        LabServiceImpl labServiceImpl = new LabServiceImpl(labRepository);

        when(labRepository.findById(anyLong())).thenReturn(Optional.empty());

        Lab lab = labServiceImpl.findOne(anyLong());

        Assert.assertNotNull(lab);
    }

    @Test
    public void testFindByNameNotNull() throws ObjectNotFoundException {

        LabServiceImpl labServiceImpl = new LabServiceImpl(labRepository);

        when(labServiceImpl.findByName(anyString())).thenReturn(labTest);

        Lab lab = labServiceImpl.findByName(anyString());

        Assert.assertNotNull(lab);

    }

    @Test
    public void testFindByNameNull() throws ObjectNotFoundException {

        LabServiceImpl labServiceImpl = new LabServiceImpl(labRepository);

        Lab lab = labServiceImpl.findByName("");

        Assert.assertNull(lab);

    }

    @Test
    public void testFindAllNotNull() {
        LabServiceImpl labServiceImpl = new LabServiceImpl(labRepository);

        when(labServiceImpl.findAll()).thenReturn(Collections.singletonList(labTest));

        List<Lab> labList = labServiceImpl.findAll();

        Assert.assertNotNull(labList);
    }

    @Test
    public void testFindAllNull() {
        LabServiceImpl labServiceImpl = new LabServiceImpl(labRepository);

        when(labServiceImpl.findAll()).thenReturn(Collections.emptyList());

        List<Lab> labList = labServiceImpl.findAll();

        Assert.assertTrue(labList.isEmpty());
    }

    @Test
    public void testSaveLab() {
        LabServiceImpl labServiceImpl = new LabServiceImpl(labRepository);

        when(labServiceImpl.save(labTest)).thenReturn(labTest);

        Lab lab = labServiceImpl.save(labTest);

        Assert.assertNotNull(lab);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testFindByNameIfException() throws ObjectNotFoundException {
        LabServiceImpl labServiceImpl = new LabServiceImpl(labRepository);
        // Emulate exception by throwing runtime exception.
        Mockito.when(labRepository.findByName(any())).thenThrow(RuntimeException.class);
        labServiceImpl.findByName("test!");
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testFindByIdIfException() throws ObjectNotFoundException {
        LabServiceImpl labServiceImpl = new LabServiceImpl(labRepository);
        // Emulate exception by throwing runtime exception.
        Mockito.when(labRepository.findById(any())).thenThrow(RuntimeException.class);
        labServiceImpl.findOne(5L);
    }

    @Test
    public void testDeleteAll(){
        LabServiceImpl labServiceImpl = new LabServiceImpl(labRepository);
        labServiceImpl.deleteAll();

        verify(labRepository).deleteAll();
    }

    @Test
    public void updateLab()
    {
        LabServiceImpl labServiceImpl = new LabServiceImpl(labRepository);

        Lab labToUpdate = new Lab("testLab", "test-template-url");
        Lab updateLab = new Lab("testLabChanged", "test-template-url-2");

        Lab updatedLab = labServiceImpl.updateLab(labToUpdate, updateLab);

        Assert.assertEquals(updatedLab.getName(), updateLab.getName());
        Assert.assertEquals(updatedLab.getCloudFormationPrivateParameters(), updateLab.getCloudFormationPrivateParameters());
        Assert.assertEquals(updatedLab.getCloudFormationPublicParameters(), updateLab.getCloudFormationPublicParameters());
        Assert.assertEquals(updatedLab.getTemplateUrl(), updateLab.getTemplateUrl());
    }
}
