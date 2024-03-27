package com.aca.aem.core.schedulers;

import com.aca.aem.core.utils.ResourceService;
import com.adobe.cq.dam.cfm.ContentElement;
import com.adobe.cq.dam.cfm.ContentFragment;
import com.adobe.cq.dam.cfm.FragmentTemplate;
import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.ReplicationException;
import com.day.cq.replication.ReplicationStatus;
import com.day.cq.replication.Replicator;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.commons.scheduler.ScheduleOptions;
import org.apache.sling.commons.scheduler.Scheduler;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Iterator;
import java.util.Objects;

@Designate(ocd=PublishScheduler.Config.class)
@Component(service=Runnable.class)
public class PublishScheduler implements Runnable {


    @ObjectClassDefinition(name="A scheduled task",
            description = "Simple demo for cron-job like task with properties")
    public static @interface Config {

        @AttributeDefinition(
                name = "Scheduler name",
                description = "Scheduler name",
                type = AttributeType.STRING)
        String scheduler_name() default "Publish CF based on Date";

        @AttributeDefinition(name = "Cron-job expression")
        String scheduler_expression() default "0 */5 * * *";

        @AttributeDefinition(
                name = "Enable Scheduler",
                description = "Enable Scheduler",
                type = AttributeType.BOOLEAN)
        boolean enable_scheduler() default true;

        @AttributeDefinition(name = "Concurrent task",
                description = "Whether or not to schedule this task concurrently")
        boolean scheduler_concurrent() default false;

        @AttributeDefinition(name = "A parameter",
                description = "Can be configured in /system/console/configMgr")
        String damPath() default "/content/dam/aca-aem/article-form";
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String damPath;


    @Reference
    private Scheduler scheduler;

    @Reference
    private Replicator replicator;

    @Reference
    ResourceService resourceService;

    @Activate
    protected void activate(final Config config) {

        logger.error(" PublishScheduledTask activate method called");

        // Execute this method to add scheduler.
        addScheduler(config);
        damPath = config.damPath();

    }

    // Add all configurations to Schedule a scheduler depending on name and expression.
    public void addScheduler(Config config) {
        logger.error("Scheduler added successfully >>>>>>>   ");
        if (config.enable_scheduler()) {
            ScheduleOptions options = scheduler.EXPR(config.scheduler_expression());
            options.name(config.scheduler_name());
            options.canRunConcurrently(config.scheduler_concurrent());

            // Add scheduler to call depending on option passed.
            scheduler.schedule(this, options);
            logger.error("Scheduler added successfully name='{}'", config.scheduler_name());
        } else {
            logger.error("SimpleScheduledTask disabled");
        }
    }

    public void removeScheduler(Config config) {
        scheduler.unschedule(config.scheduler_name());
    }

    // On deactivate component it will unschedule scheduler
    @Deactivate
    protected void deactivate(Config config) {
        removeScheduler(config);
    }

    // On component modification change status will remove and add scheduler
    @Modified
    protected void modified(Config config) {
        removeScheduler(config);
        addScheduler(config);
    }

    // run() method will get call every minute
    @Override
    public void run() {
        getCfPath();
    }

    public void getCfPath(){
        ResourceResolver resourceResolver = resourceService.getResourceResolver();
        Session session = resourceResolver.adaptTo(Session.class);
        Resource pathResource = resourceResolver.getResource("/content/dam/aca-aem/article-form");

        Iterator<Resource> children = pathResource.listChildren();
        while(children.hasNext()){
            Resource res = children.next();
            String cfPath = res.getPath();
            if(res.getResourceType().equals("dam:Asset")){

                ContentFragment cf = resourceResolver.resolve(cfPath).adaptTo(ContentFragment.class);
                String date = cf.getElement("publishingDate").getContent();
                boolean checkDate = compareDate(date);

                if(checkDate){
                    Resource cfres = resourceResolver.getResource(cfPath);
                    ReplicationStatus replicationStatus = cfres.adaptTo(ReplicationStatus.class);
                    if (replicationStatus != null){
                        boolean isActivated = replicationStatus.isActivated();
                        if (!isActivated){
                            activateCF(session,cfPath);
                            logger.error("Content Fragment is activated" +cfPath);
                        }
                    }
                }
            }
        }
    }

    public boolean compareDate(String date){
        String pattern = "yyyy-MM-dd";
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        LocalDate parsedStringDate = LocalDate.parse(date.replace(" ", "T"));

        LocalDate currentDate = LocalDate.now();

        if (parsedStringDate.isEqual(currentDate)){
            return true;
        }

        return false;
    }

    public void activateCF(Session session, String path){

        try {
            replicator.replicate(session, ReplicationActionType.ACTIVATE, path);
        } catch (ReplicationException e) {
            throw new RuntimeException(e);
        }

    }


}
