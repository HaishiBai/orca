
package com.netflix.spinnaker.orca.kato.pipeline.azure

import com.netflix.spinnaker.orca.clouddriver.tasks.MonitorKatoTask
import com.netflix.spinnaker.orca.kato.tasks.azure.UpsertAzureLoadBalancerTask
import com.netflix.spinnaker.orca.pipeline.LinearStage
import com.netflix.spinnaker.orca.pipeline.model.Stage
import groovy.transform.CompileStatic
import org.springframework.batch.core.Step
import org.springframework.stereotype.Component

@Component
@CompileStatic
class UpsertAzureLoadBalancerStage extends LinearStage {

  public static final String PIPELINE_CONFIG_TYPE = "upsertAmazonLoadBalancer_azure"

  UpsertAzureLoadBalancerStage() {
    super(PIPELINE_CONFIG_TYPE)
  }

  @Override
  public List<Step> buildSteps(Stage stage) {
    def step1 = buildStep(stage, "upsertAzureLoadBalancer", UpsertAzureLoadBalancerTask)
    def step2 = buildStep(stage, "monitorUpsert", MonitorKatoTask)
    // TODO(duftler): Implement DeleteGoogleLoadBalancerForceRefreshTask.
    [step1, step2]
  }
}
