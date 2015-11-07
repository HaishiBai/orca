
package com.netflix.spinnaker.orca.kato.pipeline.azure

import com.netflix.spinnaker.orca.clouddriver.tasks.MonitorKatoTask
import com.netflix.spinnaker.orca.clouddriver.tasks.ServerGroupCacheForceRefreshTask
import com.netflix.spinnaker.orca.kato.tasks.azure.UpsertAzureServerGroupTagsTask
import com.netflix.spinnaker.orca.pipeline.LinearStage
import com.netflix.spinnaker.orca.pipeline.model.Stage
import groovy.transform.CompileStatic
import org.springframework.batch.core.Step
import org.springframework.stereotype.Component

@Component
@CompileStatic
class UpsertAzureServerGroupTagsStage extends LinearStage {
  public static final String PIPELINE_CONFIG_TYPE = "upsertServerGroupTags_azure"

  UpsertAzureServerGroupTagsStage() {
    super(PIPELINE_CONFIG_TYPE)
  }

  @Override
  public List<Step> buildSteps(Stage stage) {
    def step1 = buildStep(stage, "upsertAzureServerGroupTags", UpsertAzureServerGroupTagsTask)
    def step2 = buildStep(stage, "monitorUpsert", MonitorKatoTask)
    def step3 = buildStep(stage, "forceCacheRefresh", ServerGroupCacheForceRefreshTask)
    [step1, step2, step3]
  }
}
