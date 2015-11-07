
package com.netflix.spinnaker.orca.kato.tasks.azure

import com.netflix.spinnaker.orca.kato.tasks.azure.AbstractInstanceAzureLoadBalancerRegistrationTask
import groovy.transform.CompileStatic
import org.springframework.stereotype.Component

@Component
@CompileStatic
class RegisterInstancesWithAzureLoadBalancerTask extends AbstractInstanceAzureLoadBalancerRegistrationTask {
  @Override
  String getAction() {
    return "registerInstancesWithAzureLoadBalancer"
  }
}
