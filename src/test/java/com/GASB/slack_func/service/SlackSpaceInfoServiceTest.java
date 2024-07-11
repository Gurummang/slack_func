package com.GASB.slack_func.service;

import com.GASB.slack_func.entity.SpaceList;
import com.GASB.slack_func.mapper.SpaceMapper;
import com.GASB.slack_func.repository.space.SpaceRepository;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static org.mockito.Mockito.*;

public class SlackSpaceInfoServiceTest {

    @InjectMocks
    private SlackSpaceInfoService slackSpaceInfoService;

    @Mock
    private SlackApiService slackApiService;

    @Mock
    private SpaceMapper spaceMapper;

    @Mock
    private SpaceRepository spaceRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testSlackSpaceRegister() throws IOException, SlackApiException {
        Team mockTeam = new Team();
        SpaceList mockSpaceList = new SpaceList();

        when(slackApiService.fetchTeamInfo()).thenReturn(mockTeam);
        when(spaceMapper.toSpaceEntity(mockTeam, 1)).thenReturn(mockSpaceList);

        slackSpaceInfoService.slackSpaceRegister();

        verify(slackApiService, times(1)).fetchTeamInfo();
        verify(spaceMapper, times(1)).toSpaceEntity(mockTeam, 1);
        verify(spaceRepository, times(1)).save(mockSpaceList);
    }
}
