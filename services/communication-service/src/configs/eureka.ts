import {Eureka} from 'eureka-js-client';
import os from 'os';

const SERVER_PORT = process.env.SERVER_PORT;
const ACTUATOR_PORT = process.env.ACTUATOR_PORT;
const HOST = process.env.HOST; // Docker service name
const EUREKA_HOST = process.env.EUREKA_HOST;
const EUREKA_PORT = process.env.EUREKA_PORT;

export const eurekaClient = new Eureka({
    instance: {
        app: 'communication-service',
        instanceId: `${os.hostname()}:communication-service:${SERVER_PORT}`,
        hostName: HOST,
        ipAddr: '127.0.0.1',
        preferIpAddress: false,
        statusPageUrl: `http://${HOST}:${ACTUATOR_PORT}/actuator/info`,
        port: {
            $: SERVER_PORT,
            '@enabled': true,
        },
        vipAddress: 'communication-service',
        dataCenterInfo: {
            '@class': 'com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo',
            name: 'MyOwn',
        },
    },
    eureka: {
        host: EUREKA_HOST,
        port: Number(EUREKA_PORT),
        servicePath: '/eureka/apps/',
    },
});
