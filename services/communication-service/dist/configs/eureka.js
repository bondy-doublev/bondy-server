"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.eurekaClient = void 0;
const eureka_js_client_1 = require("eureka-js-client");
const os_1 = __importDefault(require("os"));
const SERVER_PORT = process.env.SERVER_PORT;
const ACTUATOR_PORT = process.env.ACTUATOR_PORT;
const HOST = process.env.HOST;
const EUREKA_HOST = process.env.EUREKA_HOST;
const EUREKA_PORT = process.env.EUREKA_PORT;
exports.eurekaClient = new eureka_js_client_1.Eureka({
    instance: {
        app: 'communication-service',
        instanceId: `${os_1.default.hostname()}:communication-service:${SERVER_PORT}`,
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
//# sourceMappingURL=eureka.js.map