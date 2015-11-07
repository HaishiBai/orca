
package com.netflix.spinnaker.orca.kato.tasks.azure

import com.netflix.spinnaker.orca.DefaultTaskResult
import com.netflix.spinnaker.orca.ExecutionStatus
import com.netflix.spinnaker.orca.Task
import com.netflix.spinnaker.orca.TaskResult
import com.netflix.spinnaker.orca.clouddriver.KatoService
import com.netflix.spinnaker.orca.pipeline.model.Stage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class UpsertAzureLoadBalancerTask implements Task {

  @Autowired
  KatoService kato

  @Override
  TaskResult execute(Stage stage) {
    def taskId = kato.requestOperations([[upsertAzureLoadBalancerDescription: stage.context]])
                     .toBlocking()
                     .first()

    Map outputs = [
        "notification.type": "upsertazureloadbalancer",
        "kato.last.task.id": taskId,
        "upsert.account"   : stage.context.credentials,
        "upsert.regions"   : [stage.context.region]
    ]

    if (stage.context.clusterName) {
      outputs.clusterName = stage.context.clusterName
    }

    if (stage.context.name) {
      outputs.name = stage.context.name
    }

    new DefaultTaskResult(ExecutionStatus.SUCCEEDED, outputs)
  }
}
