
package com.netflix.spinnaker.orca.kato.tasks.azure

import com.netflix.spinnaker.orca.clouddriver.tasks.AbstractCloudProviderAwareTask
import groovy.transform.CompileStatic
import com.netflix.spinnaker.orca.DefaultTaskResult
import com.netflix.spinnaker.orca.ExecutionStatus
import com.netflix.spinnaker.orca.Task
import com.netflix.spinnaker.orca.TaskResult
import com.netflix.spinnaker.orca.clouddriver.KatoService
import com.netflix.spinnaker.orca.pipeline.model.Stage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@CompileStatic
class TerminateAzureInstancesTask extends AbstractCloudProviderAwareTask implements Task {
  @Autowired
  KatoService kato

  @Override
  TaskResult execute(Stage stage) {
    String cloudProvider = getCloudProvider(stage)
    String account = getCredentials(stage)

    def taskId = kato.requestOperations(cloudProvider, [[terminateInstances: stage.context]])
                     .toBlocking()
                     .first()

    // TODO(duftler): Reconcile the mismatch between region and zone here.
    new DefaultTaskResult(ExecutionStatus.SUCCEEDED, [
        "notification.type"     : "terminateazureinstances",
        "terminate.account.name": account,
        "terminate.region"      : stage.context.zone,
        "kato.last.task.id"     : taskId,
        "terminate.instance.ids": stage.context.instanceIds,
    ])
  }
}
