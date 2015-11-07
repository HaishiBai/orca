
package com.netflix.spinnaker.orca.kato.tasks.azure

import groovy.transform.CompileStatic
import org.springframework.stereotype.Component

@Component
@CompileStatic
class DeregisterInstancesFromAzureLoadBalancerTask extends AbstractInstanceAzureLoadBalancerRegistrationTask {
  @Override
  String getAction() {
    return "deregisterInstancesFromAzureLoadBalancer"
  }
}
