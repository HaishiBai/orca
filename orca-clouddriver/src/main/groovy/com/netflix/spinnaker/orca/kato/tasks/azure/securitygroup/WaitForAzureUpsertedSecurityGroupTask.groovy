
package com.netflix.spinnaker.orca.kato.tasks.azure.securitygroup

import com.netflix.spinnaker.orca.DefaultTaskResult
import com.netflix.spinnaker.orca.ExecutionStatus
import com.netflix.spinnaker.orca.RetryableTask
import com.netflix.spinnaker.orca.TaskResult
import com.netflix.spinnaker.orca.clouddriver.MortService
import com.netflix.spinnaker.orca.pipeline.model.Stage
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import retrofit.RetrofitError

@Component
@CompileStatic
class WaitForAzureUpsertedSecurityGroupTask implements RetryableTask {

  long backoffPeriod = 1000
  long timeout = 600000

  @Autowired
  MortService mortService

  @Override
  TaskResult execute(Stage stage) {
    def status = ExecutionStatus.SUCCEEDED
    stage.context.targets.each { Map<String, Object> target ->
      try {
        MortService.SecurityGroup securityGroup =
          mortService.getSecurityGroup(target.credentials as String, 'azure', target.name as String, target.region as String)

        if (!securityGroup) {
          status = ExecutionStatus.RUNNING
        }
      } catch (RetrofitError e) {
        if (e.response?.status == 404) {
          status = ExecutionStatus.RUNNING
          return
        }

        throw e
      }
    }

    return new DefaultTaskResult(status)
  }
}
