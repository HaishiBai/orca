
package com.netflix.spinnaker.orca.kato.pipeline.azure

import com.fasterxml.jackson.databind.ObjectMapper
import com.netflix.spinnaker.orca.clouddriver.tasks.MonitorKatoTask
import com.netflix.spinnaker.orca.clouddriver.tasks.WaitForUpInstanceHealthTask
import com.netflix.spinnaker.orca.clouddriver.OortService
import com.netflix.spinnaker.orca.kato.tasks.azure.RegisterInstancesWithAzureLoadBalancerTask
import com.netflix.spinnaker.orca.pipeline.LinearStage
import com.netflix.spinnaker.orca.pipeline.model.Stage
import groovy.transform.CompileStatic
import org.springframework.batch.core.Step
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@CompileStatic
class RegisterInstancesWithAzureLoadBalancerStage extends LinearStage {
  public static final String PIPELINE_CONFIG_TYPE = "registerInstancesWithLoadBalancer_azure"

  @Autowired
  OortService oortService

  @Autowired
  ObjectMapper objectMapper

  RegisterInstancesWithAzureLoadBalancerStage() {
    super(PIPELINE_CONFIG_TYPE)
  }

  @Override
  public List<Step> buildSteps(Stage stage) {
    def step1 = buildStep(stage, "registerInstances", RegisterInstancesWithAzureLoadBalancerTask)
    def step2 = buildStep(stage, "monitorInstances", MonitorKatoTask)
    def step3 = buildStep(stage, "waitForLoadBalancerState", WaitForUpInstanceHealthTask)
    [step1, step2, step3]
  }
}
