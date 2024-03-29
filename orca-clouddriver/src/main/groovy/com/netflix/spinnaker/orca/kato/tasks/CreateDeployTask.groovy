/*
 * Copyright 2014 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.orca.kato.tasks

import com.fasterxml.jackson.databind.ObjectMapper
import com.netflix.spinnaker.orca.DefaultTaskResult
import com.netflix.spinnaker.orca.ExecutionStatus
import com.netflix.spinnaker.orca.Task
import com.netflix.spinnaker.orca.TaskResult
import com.netflix.spinnaker.orca.clouddriver.KatoService
import com.netflix.spinnaker.orca.clouddriver.model.TaskId
import com.netflix.spinnaker.orca.clouddriver.tasks.AbstractCloudProviderAwareTask
import com.netflix.spinnaker.orca.clouddriver.utils.HealthHelper
import com.netflix.spinnaker.orca.kato.pipeline.support.StageData
import com.netflix.spinnaker.orca.pipeline.model.Stage
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Slf4j
@Component
class CreateDeployTask extends AbstractCloudProviderAwareTask implements Task {

  static final List<String> DEFAULT_VPC_SECURITY_GROUPS = ["nf-infrastructure-vpc", "nf-datacenter-vpc"]
  static final List<String> DEFAULT_SECURITY_GROUPS = ["nf-infrastructure", "nf-datacenter"]

  @Autowired
  KatoService kato

  @Autowired
  ObjectMapper mapper

  @Value('${default.bake.account:default}')
  String defaultBakeAccount

  @Value('${default.vpc.securityGroups:#{T(com.netflix.spinnaker.orca.kato.tasks.CreateDeployTask).DEFAULT_VPC_SECURITY_GROUPS}}')
  List<String> defaultVpcSecurityGroups = DEFAULT_VPC_SECURITY_GROUPS

  @Value('${default.securityGroups:#{T(com.netflix.spinnaker.orca.kato.tasks.CreateDeployTask).DEFAULT_SECURITY_GROUPS}}')
  List<String> defaultSecurityGroups = DEFAULT_SECURITY_GROUPS

  @Override
  TaskResult execute(Stage stage) {
    String cloudProvider = getCloudProvider(stage)
    Map deployOperations = deployOperationFromContext(cloudProvider, stage)
    TaskId taskId = deploy(cloudProvider, deployOperations)

    Map outputs = [
      "notification.type"  : "createdeploy",
      "kato.result.expected": true,
      "kato.last.task.id"  : taskId,
      "deploy.account.name": deployOperations.credentials
    ]

    def suspendedProcesses = stage.context.suspendedProcesses as Set<String>
    if (suspendedProcesses?.contains("AddToLoadBalancer")) {
      outputs.interestingHealthProviderNames = HealthHelper.getInterestingHealthProviderNames(stage, ["Amazon"])
    }

    return new DefaultTaskResult(ExecutionStatus.SUCCEEDED, outputs)
  }

  private Map deployOperationFromContext(String cloudProvider, Stage stage) {
    def operation = [:]
    def context = stage.context

    if (context.containsKey("cluster")) {
      operation.putAll(context.cluster as Map)
    } else {
      operation.putAll(context)
    }

    def targetRegion = operation.region ?: (operation.availabilityZones as Map<String, Object>).keySet()[0]
    def deploymentDetails = (context.deploymentDetails ?: []) as List<Map>
    if (!operation.amiName && deploymentDetails) {
      operation.amiName = deploymentDetails.find { it.region == targetRegion }?.ami
    }
    if (!operation.imageId && deploymentDetails) {
      operation.imageId = deploymentDetails[0]?.imageId // Because docker image ids are not region or cloud provider specific
    }

    log.info("Deploying ${operation.amiName ?: operation.imageId} to ${targetRegion}")

    if (context.account && !operation.credentials) {
      operation.credentials = context.account
    }
    operation.keyPair = (operation.keyPair ?: "nf-${operation.credentials}-keypair-a").toString()
    return operation
  }

  @CompileStatic(TypeCheckingMode.SKIP)
  private TaskId deploy(String cloudProvider, Map deployOperation) {
    deployOperation.securityGroups = deployOperation.securityGroups ?: []

    //TODO(cfieber)- remove the VPC special case asap
    if (deployOperation.subnetType && !deployOperation.subnetType.contains('vpc0')) {
      addAllNonEmpty(deployOperation.securityGroups, defaultVpcSecurityGroups)
    } else {
      addAllNonEmpty(deployOperation.securityGroups, defaultSecurityGroups)
    }

    List<Map<String, Object>> descriptions = []

    if (deployOperation.credentials != defaultBakeAccount) {
      if (deployOperation.availabilityZones) {
        descriptions.addAll(deployOperation.availabilityZones.collect { String region, List<String> azs ->
          [allowLaunchDescription: convertAllowLaunch(deployOperation.credentials, defaultBakeAccount, region, deployOperation.amiName)]
        })

        log.info("Generated `allowLaunchDescriptions` (allowLaunchDescriptions: ${descriptions})")
      }
    }

    descriptions.add([createServerGroup: deployOperation])
    def result = kato.requestOperations(cloudProvider, descriptions).toBlocking().first()
    result
  }

  private static Map convertAllowLaunch(String targetAccount, String sourceAccount, String region, String ami) {
    [account: targetAccount, credentials: sourceAccount, region: region, amiName: ami]
  }

  private static void addAllNonEmpty(List<String> baseList, List<String> listToBeAdded) {
    if (listToBeAdded) {
      listToBeAdded.each { itemToBeAdded ->
        if (itemToBeAdded) {
          baseList << itemToBeAdded
        }
      }
    }
  }
}
