
package com.netflix.spinnaker.orca.kato.tasks.azure

import com.netflix.spinnaker.orca.DefaultTaskResult
import com.netflix.spinnaker.orca.ExecutionStatus
import com.netflix.spinnaker.orca.Task
import com.netflix.spinnaker.orca.TaskResult
import com.netflix.spinnaker.orca.clouddriver.KatoService
import com.netflix.spinnaker.orca.clouddriver.utils.HealthHelper
import com.netflix.spinnaker.orca.pipeline.model.Stage
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired

@CompileStatic
abstract class AbstractInstanceAzureLoadBalancerRegistrationTask implements Task {
  @Autowired
  KatoService kato

  abstract String getAction()

  @Override
  TaskResult execute(Stage stage) {
    def taskId = kato.requestOperations([[("${action}Description".toString()): stage.context]])
                     .toBlocking()
                     .first()
    new DefaultTaskResult(ExecutionStatus.SUCCEEDED, [
      "notification.type"           : getAction().toLowerCase(),
      "kato.last.task.id"           : taskId,
      interestingHealthProviderNames: HealthHelper.getInterestingHealthProviderNames(stage, ["LoadBalancer"])
    ])
  }
}
