
package com.adobe.aemaacs.external.ui;
import java.util.List;

import org.osgi.annotation.versioning.ConsumerType;

import com.drew.lang.annotations.NotNull;

/**
 * Defines the {@code CloudConfigurationList} Sling Model used for the cloudconfig component.
 *
 */
@ConsumerType
public interface CloudConfigurationList {

  /**
   * Retrieve the list of CloudConfigurations for the specified request.
   *
   * @return the list of {@code CloudConfiguration}s
   */
  @NotNull
  default List<Configuration> getCloudConfigurations() {
      throw new UnsupportedOperationException();
  }
}