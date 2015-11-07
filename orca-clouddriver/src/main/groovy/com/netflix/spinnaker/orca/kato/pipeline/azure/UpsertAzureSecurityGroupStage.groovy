
package com.netflix.spinnaker.orca.kato.pipeline.azure

import com.netflix.spinnaker.orca.clouddriver.tasks.MonitorKatoTask
import com.netflix.spinnaker.orca.kato.tasks.azure.securitygroup.UpsertAzureSecurityGroupTask
import com.netflix.spinnaker.orca.kato.tasks.azure.securitygroup.WaitForAzureUpsertedSecurityGroupTask
import com.netflix.spinnaker.orca.clouddriver.tasks.SecurityGroupForceCacheRefreshTask
import com.netflix.spinnaker.orca.pipeline.LinearStage
import com.netflix.spinnaker.orca.pipeline.model.Stage
import groovy.transform.CompileStatic
import org.springframework.batch.core.Step
import org.springframework.stereotype.Component

@Component
@CompileStatic
class UpsertAzureSecurityGroupStage extends LinearStage {

  public static final String PIPELINE_CONFIG_TYPE = "upsertSecurityGroup_azure"

  UpsertAzureSecurityGroupStage() {
    super(PIPELINE_CONFIG_TYPE)
  }

  @Override
  public List<Step> buildSteps(Stage stage) {
    def step1 = buildStep(stage, "upsertSecurityGroup", UpsertAzureSecurityGroupTask)
    def step2 = buildStep(stage, "monitorUpsert", MonitorKatoTask)
    def step3 = buildStep(stage, "forceCacheRefresh", SecurityGroupForceCacheRefreshTask)
    def step4 = buildStep(stage, "waitForUpsertedSecurityGroup", WaitForAzureUpsertedSecurityGroupTask)
    [step1, step2, step3, step4]
  }
}
