
package com.netflix.spinnaker.orca.kato.tasks.azure.securitygroup

import com.netflix.spinnaker.orca.DefaultTaskResult
import com.netflix.spinnaker.orca.ExecutionStatus
import com.netflix.spinnaker.orca.Task
import com.netflix.spinnaker.orca.TaskResult
import com.netflix.spinnaker.orca.clouddriver.KatoService
import com.netflix.spinnaker.orca.clouddriver.MortService
import com.netflix.spinnaker.orca.clouddriver.tasks.AbstractCloudProviderAwareTask
import com.netflix.spinnaker.orca.pipeline.model.Stage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class UpsertAzureSecurityGroupTask extends AbstractCloudProviderAwareTask implements Task {

  @Autowired
  KatoService kato

  @Autowired
  MortService mortService

  @Override
  TaskResult execute(Stage stage) {
    String cloudProvider = getCloudProvider(stage)
    String account = getCredentials(stage)

    def taskId = kato.requestOperations(cloudProvider, [[upsertSecurityGroup: stage.context]])
                     .toBlocking()
                     .first()

    Map outputs = [
      "notification.type"   : "upsertsecuritygroup",
      "kato.last.task.id"   : taskId,
      "targets"             : [
        [
          credentials: account,
          region     : stage.context.region,
          name       : stage.context.name
        ]
      ]
    ]

    new DefaultTaskResult(ExecutionStatus.SUCCEEDED, outputs)
  }
}
