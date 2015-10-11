/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.helper;

import com.zipato.model.attribute.Attribute;
import com.zipato.model.attribute.AttributeRepository;
import com.zipato.model.device.DeviceRepository;
import com.zipato.model.device.DeviceState;
import com.zipato.model.device.DeviceStateRepository;
import com.zipato.model.endpoint.ClusterEndpoint;
import com.zipato.model.endpoint.ClusterEndpointRepository;
import com.zipato.model.endpoint.Endpoint;
import com.zipato.model.endpoint.EndpointRepository;
import com.zipato.model.typereport.TypeReportItem;

/**
 * Created by murielK on 8/24/2015.
 */
public class DeviceStateHelper {

    private final DeviceRepository deviceRepository;
    private final EndpointRepository endpointRepository;
    private final ClusterEndpointRepository clusterEndpointRepository;
    private final AttributeRepository attributeRepository;
    private final DeviceStateRepository deviceStateRepository;

    public DeviceStateHelper(AttributeRepository attributeRepository, DeviceRepository deviceRepository, EndpointRepository endpointRepository, ClusterEndpointRepository clusterEndpointRepository, DeviceStateRepository deviceStateRepository) {
        this.attributeRepository = attributeRepository;
        this.deviceRepository = deviceRepository;
        this.endpointRepository = endpointRepository;
        this.clusterEndpointRepository = clusterEndpointRepository;
        this.deviceStateRepository = deviceStateRepository;
    }

    public boolean isDeviceOnline(TypeReportItem item) { // to force null devices to return true, for some reason weather station were always offline
        DeviceState deviceState = getDeviceState(item);
        return (deviceState == null) || deviceState.isOnline();
    }

    public boolean isDeviceTrouble(TypeReportItem item) {
        final DeviceState deviceState = getDeviceState(item);
        return (deviceState != null) && deviceState.isTrouble();
    }

    public boolean isBatteryLow(TypeReportItem item) {
        final DeviceState deviceState = getDeviceState(item);
        return (deviceState != null) && (deviceState.getBatteryLevel() == -1);
    }

    public DeviceState getDeviceState(TypeReportItem item) {
        if ((item == null) || (item.getEntityType() == null))
            return null;

        switch (item.getEntityType()) {
            case DEVICE:
                return deviceStateRepository.get(item.getUuid());
            case ENDPOINT:
                final Endpoint endpoint = endpointRepository.get(item.getUuid());
                if ((endpoint != null) && (endpoint.getParent() != null))
                    return deviceStateRepository.get(endpoint.getParent().getUuid());
                break;
            case CLUSTER_ENDPOINT:
                final ClusterEndpoint clusterEndpoint = clusterEndpointRepository.get(item.getUuid());
                if ((clusterEndpoint != null) && (clusterEndpoint.getParent() != null)) {
                    final Endpoint endpoint1 = endpointRepository.get(clusterEndpoint.getParent().getUuid());
                    if ((endpoint1 != null) && (endpoint1.getParent() != null))
                        return deviceStateRepository.get(endpoint1.getParent().getUuid());
                }
                break;
            case ATTRIBUTE:
                final Attribute attr = attributeRepository.get(item.getUuid());
                if ((attr != null) && (attr.getParent() != null)) {
                    final ClusterEndpoint clusterEndpoint2 = clusterEndpointRepository.get(attr.getParent().getUuid());
                    if ((clusterEndpoint2 != null) && (clusterEndpoint2.getParent() != null)) {
                        final Endpoint endpoint2 = endpointRepository.get(clusterEndpoint2.getParent().getUuid());
                        if ((endpoint2 != null) && (endpoint2.getParent() != null))
                            return deviceStateRepository.get(endpoint2.getParent().getUuid());
                    }
                }
                break;
            case NETWORK:
                return null;

        }
        return null;
    }
}
