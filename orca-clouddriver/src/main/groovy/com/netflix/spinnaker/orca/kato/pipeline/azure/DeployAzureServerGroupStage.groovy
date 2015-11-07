
package com.netflix.spinnaker.orca.kato.pipeline.azure

import com.netflix.spinnaker.orca.clouddriver.tasks.MonitorKatoTask
import com.netflix.spinnaker.orca.clouddriver.tasks.ServerGroupCacheForceRefreshTask
import com.netflix.spinnaker.orca.clouddriver.tasks.WaitForUpInstancesTask
import com.netflix.spinnaker.orca.kato.pipeline.strategy.DeployStrategyStage
import com.netflix.spinnaker.orca.kato.tasks.azure.CreateAzureServerGroupTask
import com.netflix.spinnaker.orca.pipeline.model.Stage
import groovy.transform.CompileStatic
import org.springframework.batch.core.Step
import org.springframework.stereotype.Component

@Component
@CompileStatic
class DeployAzureServerGroupStage extends DeployStrategyStage {

  public static final String PIPELINE_CONFIG_TYPE = "linearDeploy_azure"

  DeployAzureServerGroupStage() {
    super(PIPELINE_CONFIG_TYPE)
  }

  @Override
  protected List<Step> basicSteps(Stage stage) {
    def step1 = buildStep(stage, "createDeploy", CreateAzureServerGroupTask)
    def step2 = buildStep(stage, "monitorDeploy", MonitorKatoTask)
    def step3 = buildStep(stage, "forceCacheRefresh", ServerGroupCacheForceRefreshTask)
    def step4 = buildStep(stage, "waitForUpInstances", WaitForUpInstancesTask)
    [step1, step2, step3, step4]
  }
}
