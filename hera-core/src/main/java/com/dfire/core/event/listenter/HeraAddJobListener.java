package com.dfire.core.event.listenter;


import com.dfire.core.event.HeraJobMaintenanceEvent;
import com.dfire.core.event.base.ApplicationEvent;
import com.dfire.core.event.base.Events;
import com.dfire.core.event.base.MvcEvent;
import com.dfire.core.event.handler.AbstractHandler;
import com.dfire.core.event.handler.JobHandler;
import com.dfire.core.netty.master.Master;
import com.dfire.core.netty.master.MasterContext;
import com.dfire.common.logs.ScheduleLog;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午5:19 2018/4/19
 * @desc 增操作，添加controller
 */
public class HeraAddJobListener extends AbstractListener {

    private final Master master;
    private final MasterContext masterContext;

    public HeraAddJobListener(Master master, MasterContext masterContext) {
        this.master = master;
        this.masterContext = masterContext;
    }

    @Override
    public void beforeDispatch(MvcEvent mvcEvent) {
        if (mvcEvent.getApplicationEvent() instanceof HeraJobMaintenanceEvent) {
            HeraJobMaintenanceEvent maintenanceEvent = (HeraJobMaintenanceEvent) mvcEvent.getApplicationEvent();
            if (mvcEvent.getType() == Events.UpdateActions) {
                String actionId = maintenanceEvent.getId();
                boolean exist = false;
                for (AbstractHandler handler : masterContext.getDispatcher().getJobHandlers()) {
                    if (handler instanceof JobHandler) {
                        JobHandler jobHandler = (JobHandler) handler;
                        if (jobHandler.getActionId().equals(actionId)) {
                            exist = true;
                            break;
                        }
                    }
                }
                if (!exist) {
                    JobHandler handler = new JobHandler(actionId, master, masterContext);
                    masterContext.getDispatcher().addJobHandler(handler);
                    handler.handleEvent(new ApplicationEvent(Events.Initialize));
                    mvcEvent.setCancelled(true);
                    ScheduleLog.info("schedule add job with actionId:" + actionId);
                }
            }

        }

    }
}
