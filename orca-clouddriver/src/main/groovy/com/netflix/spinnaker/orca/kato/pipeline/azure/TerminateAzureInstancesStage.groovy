
package com.netflix.spinnaker.orca.kato.pipeline.azure

import groovy.transform.CompileStatic
import com.netflix.spinnaker.orca.clouddriver.tasks.MonitorKatoTask
import com.netflix.spinnaker.orca.kato.tasks.azure.TerminateAzureInstancesTask
import com.netflix.spinnaker.orca.kato.tasks.azure.WaitForRecreatedAzureInstancesTask
import com.netflix.spinnaker.orca.pipeline.LinearStage
import com.netflix.spinnaker.orca.pipeline.model.Stage
import org.springframework.batch.core.Step
import org.springframework.stereotype.Component

@Component
@CompileStatic
class TerminateAzureInstancesStage extends LinearStage {

  public static final String PIPELINE_CONFIG_TYPE = "terminateInstances_azure"

  TerminateAzureInstancesStage() {
    super(PIPELINE_CONFIG_TYPE)
  }

  @Override
  public List<Step> buildSteps(Stage stage) {
    def step1 = buildStep(stage, "terminateInstances", TerminateAzureInstancesTask)
    def step2 = buildStep(stage, "monitorTermination", MonitorKatoTask)
    def step3 = buildStep(stage, "waitForRecreatedInstances", WaitForRecreatedAzureInstancesTask)
    [step1, step2, step3]
  }
}
