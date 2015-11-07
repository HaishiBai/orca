
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
class CreateCopyLastAzureServerGroupTask implements Task {

  @Autowired
  KatoService kato

  @Override
  TaskResult execute(Stage stage) {
    def taskId = kato.requestOperations([[copyLastAzureServerGroupDescription: stage.context]]).toBlocking().first()
    new DefaultTaskResult(ExecutionStatus.SUCCEEDED,
        [
            "notification.type"  : "createcopylastasg",
            "kato.last.task.id"  : taskId,
            "deploy.account.name": stage.context.credentials,
        ]
    )
  }
}
