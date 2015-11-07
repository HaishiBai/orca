
package com.netflix.spinnaker.orca.kato.tasks.azure

import com.fasterxml.jackson.databind.ObjectMapper
import com.netflix.spinnaker.orca.DefaultTaskResult
import com.netflix.spinnaker.orca.ExecutionStatus
import com.netflix.spinnaker.orca.RetryableTask
import com.netflix.spinnaker.orca.TaskResult
import com.netflix.spinnaker.orca.clouddriver.OortService
import com.netflix.spinnaker.orca.pipeline.model.Stage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import retrofit.RetrofitError

@Component
class WaitForRecreatedAzureInstancesTask implements RetryableTask {
  long backoffPeriod = 1000
  long timeout = 600000

  @Autowired
  OortService oortService

  @Autowired
  ObjectMapper objectMapper

  @Override
  TaskResult execute(Stage stage) {
    List<String> instanceIds = stage.context."terminate.instance.ids"
    List<Long> launchTimes = stage.context.launchTimes

    if (!instanceIds || !launchTimes || instanceIds.size() != launchTimes.size()) {
      return new DefaultTaskResult(ExecutionStatus.FAILED)
    }

    Map<String, Long> launchTimesMap = new HashMap<String, Long>()

    for (int i = 0; i < instanceIds.size; i++) {
      launchTimesMap[instanceIds[i]] = launchTimes[i]
    }

    def notAllRecreated = instanceIds.find { String instanceId ->
      try {
        def response = oortService.getInstance(stage.context.credentials, stage.context.region, instanceId)
        def instanceQueryResult = objectMapper.readValue(response.body.in().text, Map)

        return !instanceQueryResult || instanceQueryResult.launchTime == launchTimesMap[instanceId]
      } catch (RetrofitError e) {
        // 404 causes this Error to be thrown. If the cache was refreshed while the instance was non-existent,
        // oort will 404.
        return true
      }
    }

    def status = notAllRecreated ? ExecutionStatus.RUNNING : ExecutionStatus.SUCCEEDED

    new DefaultTaskResult(status)
  }
}
