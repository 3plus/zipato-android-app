/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.network;

import android.util.Log;

import com.zipato.model.Configuration;
import com.zipato.model.UUIDObjectRepository;
import com.zipato.model.attribute.Attribute;
import com.zipato.model.attribute.AttributeRepository;
import com.zipato.model.device.Device;
import com.zipato.model.device.DeviceRepository;
import com.zipato.model.endpoint.ClusterEndpoint;
import com.zipato.model.endpoint.ClusterEndpointRepository;
import com.zipato.model.endpoint.Endpoint;
import com.zipato.model.endpoint.EndpointRepository;
import com.zipato.util.TagFactoryUtils;

import java.util.Arrays;
import java.util.UUID;

/**
 * Created by dbudor on 10/06/2014.
 */
public class NetworkRepository extends UUIDObjectRepository<Network> {

    public void loadTree() {
        Network[] networks = factory.getRestTemplate().getForObject("v2/networks/trees", Network[].class);
        clear();
        addAll(networks);
        addTree(Arrays.asList(networks));
        addAttributesToTree();
    }

    public Configuration getNetworkConfig(UUID uuid) {
        Configuration config = factory.getRestTemplate().getForObject("v2/networks/{uuid}/config", Configuration.class, uuid);
        return config;
    }

    public void restoreTree() throws Exception {
        restore();
        restoreTree(values());
    }

    public void restoreTree(Iterable<Network> networks) {
        addTree(networks);
    }

    private void addAttributesToTree() {
        ClusterEndpointRepository cepRepo = factory.getRepository(ClusterEndpointRepository.class);
        final AttributeRepository attributeRepository = factory.getRepository(AttributeRepository.class);
        long startTime = System.currentTimeMillis();
        attributeRepository.fetchAll();
        Log.d(TagFactoryUtils.getTag(this), String.format("attributeRepository loaded...in %s", ((System.currentTimeMillis() - startTime)) + "ms"));

        for (Attribute attr : attributeRepository.values()) {  // apply attributes to the tree if not empty
            if (attr.getClusterEndpoint() != null) {
                UUID cepUUID = attr.getClusterEndpoint().getUuid();
                ClusterEndpoint cep = cepRepo.get(cepUUID);
                if (cep != null) {
                    cep.addAttribute(attr);
                    attr.setClusterEndpoint(cep);
                }
            }
        }
    }

    private void addTree(Iterable<Network> networks) {
        DeviceRepository deviceRepository = factory.getRepository(DeviceRepository.class);
        EndpointRepository endpointRepository = factory.getRepository(EndpointRepository.class);
        ClusterEndpointRepository cepRepo = factory.getRepository(ClusterEndpointRepository.class);
        AttributeRepository attributeRepository = factory.getRepository(AttributeRepository.class);
        deviceRepository.clear();
        endpointRepository.clear();
        cepRepo.clear();
        //   Log.d("NetworkRepository", "All repo cleared");
        for (Network net : networks) {
            if (net.getDevices() == null) {
                continue;
            }
            deviceRepository.addAll(net.getDevices());
            //  Log.e("NetworkRepository", "Added Device to DeviceRepository");
            for (Device dev : net.getDevices()) {
                dev.setParent(net);

                if (dev.getEndpoints() == null) {
                    continue;
                }
                endpointRepository.addAll(dev.getEndpoints());
                //     Log.e("NetworkRepository", "Added Endpoints to EndpointRepository");
                for (Endpoint endp : dev.getEndpoints()) {
                    endp.setParent(dev);
                    if (endp.getClusterEndpoints() == null) {
                        continue;
                    }
                    cepRepo.addAll(endp.getClusterEndpoints());
                    //  Log.e("NetworkRepository", "Added ClusterEndpoints to ClusterEndpointRepository");
                    for (ClusterEndpoint clep : endp.getClusterEndpoints()) {
                        clep.setParent(endp);
                        if (clep.getAttributes() == null)
                            continue;
                        //  Log.e("NetworkRepository", "attribute found ooooouuuuuuuuuu");

                        attributeRepository.addAll(clep.getAttributes());
                        for (Attribute attr : clep.getAttributes()) {
                            //if(attr !=null)
                            attr.setClusterEndpoint(clep);
                        }
                    }
                }
            }
        }
    }

    public DiscoveryRest getDiscoveryRes(UUID uuid, Object object) {

        return factory.getRestTemplate().postForObject("v2/networks/{uuid}/discovery/", object, DiscoveryRest.class, uuid);
    }

    public DiscoveryStatus getDiscoveryStatus(UUID uuidNet, UUID uuidDiscovery) {

        return factory.getRestTemplate().getForObject("v2/networks/{uuidNet}/discovery/{uuidDis}", DiscoveryStatus.class, uuidNet, uuidDiscovery);
    }

    public void deleteDiscovery(UUID uuidNet, UUID uuidDiscovery) {

        factory.getRestTemplate().delete("v2/networks/{uuidNet}/discovery/{uuidDiscovery}", uuidNet, uuidDiscovery);
    }
}
