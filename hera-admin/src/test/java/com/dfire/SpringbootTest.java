package com.dfire;

import com.alibaba.druid.support.json.JSONUtils;
import com.dfire.common.entity.HeraAction;
import com.dfire.common.mapper.HeraJobActionMapper;
import com.dfire.common.mapper.HeraJobMapper;
import org.apache.commons.collections.map.HashedMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AdminBootstrap.class)
@ActiveProfiles("dev")
public class SpringbootTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private HeraJobMapper heraJobMapper;
    @Autowired
    HeraJobActionMapper heraJobActionMapper;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }
    @Test
    public void T5() {

        HeraAction todayEarliestActionId = heraJobMapper.getTodayEarliestActionId("28");
        HashSet hs = new HashSet();
        String actionId = todayEarliestActionId.getId() + "";
        System.err.println("actionId :" + actionId);
        List<String> downJobIds = heraJobMapper.getDownJobIds(actionId);
        T4(downJobIds, hs);
        hs.add(actionId);
        System.err.println(hs);

    }


    public void T2(String actionId, HashSet hs) {

        System.err.println(actionId);
        HeraAction heraAction = heraJobMapper.getHeraAction(actionId);

        String dependencies = heraAction.getDependencies();
        Map map = new HashedMap();
        if (dependencies != null) {
            String[] split = dependencies.split(",");
            for (int i = 0; i < split.length; i++) {
                map.put(split[i], "ssss" + System.currentTimeMillis() + "");
            }
        }

        Iterator it = hs.iterator();
        while (it.hasNext()) {
            map.remove(it.next());
        }
        String string = JSONUtils.toJSONString(map);
        heraAction.setReadyDependency(string);
        heraAction.setStatus(null);
        heraAction.setStartTime(new Date());
        // heraJobActionMapper.update(heraAction);

        List<String> downJobIds = heraJobMapper.getDownJobIds(actionId);

        if (downJobIds != null && downJobIds.size() > 0) {
            for (int i = 0; i < downJobIds.size(); i++) {
                T2(downJobIds.get(i), hs);
            }
        }

    }


    int i = 0;

    public void T4(List<String> downJobIds, HashSet hs) {

        System.out.println("count : " + i++);
        if (i > 5) {
            return;
        }
        if (downJobIds == null || downJobIds.size() == 0) {
            return;
        }
        for (int i = 0; i < downJobIds.size(); i++) {
            List<String> downJobIds1 = heraJobMapper.getDownJobIds(downJobIds.get(i).trim());
            System.out.println(downJobIds.get(i));
            hs.add(downJobIds.get(i));
            T4(downJobIds1, hs);
        }
    }


    @Test
    public void T3() {

        HashSet hs = new HashSet();
        String actionId = "202008040800000700";
        List<String> downJobIds = heraJobMapper.getDownJobIds(actionId);
        T4(downJobIds, hs);
        System.out.println(hs);

    }

    @Test
    public void testGetAllTagInfo()
            throws Exception
    {
        mockMvc.perform(get("/tagManageController/getAllTagInfo"))
                .andExpect(status().isOk())
                .andDo(print());
    }

}
