
package com.netflix.spinnaker.orca.kato.pipeline.azure

import com.netflix.spinnaker.orca.clouddriver.tasks.MonitorKatoTask
import com.netflix.spinnaker.orca.clouddriver.tasks.ServerGroupCacheForceRefreshTask
import com.netflix.spinnaker.orca.clouddriver.tasks.WaitForUpInstancesTask
import com.netflix.spinnaker.orca.kato.tasks.azure.CreateCopyLastAzureServerGroupTask
import com.netflix.spinnaker.orca.pipeline.LinearStage
import com.netflix.spinnaker.orca.pipeline.model.Stage
import groovy.transform.CompileStatic
import org.springframework.batch.core.Step
import org.springframework.stereotype.Component

@Component
@CompileStatic
class CopyLastAzureServerGroupStage extends LinearStage {

  public static final String PIPELINE_CONFIG_TYPE = "copyLastAsg_azure"

  CopyLastAzureServerGroupStage() {
    super(PIPELINE_CONFIG_TYPE)
  }

  @Override
  public List<Step> buildSteps(Stage stage) {
    def step1 = buildStep(stage, "copyLastServerGroup", CreateCopyLastAzureServerGroupTask)
    def step2 = buildStep(stage, "monitorDeploy", MonitorKatoTask)
    def step3 = buildStep(stage, "forceCacheRefresh", ServerGroupCacheForceRefreshTask)
    def step4 = buildStep(stage, "waitForUpInstances", WaitForUpInstancesTask)
    [step1, step2, step3, step4]
  }

}
