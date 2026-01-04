"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.ProxyService = void 0;
const common_1 = require("@nestjs/common");
const config_1 = require("@nestjs/config");
const axios_1 = __importDefault(require("axios"));
let ProxyService = class ProxyService {
    configService;
    constructor(configService) {
        this.configService = configService;
    }
    getApiConfig(apiName) {
        const url = this.configService.get(`${apiName.toUpperCase()}_SERVER_URL`);
        const key = this.configService.get(`${apiName.toUpperCase()}_KEY`);
        if (!url || !key) {
            throw new common_1.HttpException(`API config not found for ${apiName}`, common_1.HttpStatus.BAD_REQUEST);
        }
        return { url, key };
    }
    async forwardStreamOrJson(apiName, path, method, req, res) {
        const { url, key } = this.getApiConfig(apiName);
        const fullUrl = `${url}/${path}`;
        const contentType = req.headers['content-type'] || '';
        console.log('==============================');
        console.log('🚀 Proxy Forward');
        console.log('API Name:', apiName);
        console.log('Target URL:', fullUrl);
        console.log('Using API KEY:', key);
        console.log('Method:', method);
        console.log('Content-Type:', contentType);
        console.log('Headers:', req.headers);
        if (!contentType.includes('multipart/form-data')) {
            console.log('Body:', JSON.stringify(req.body, null, 2));
        }
        try {
            if (contentType.includes('multipart/form-data')) {
                const forwardHeaders = {
                    ...req.headers,
                    'x-api-key': key,
                    cookie: req.headers.cookie,
                };
                console.log('--- Axios Final Headers ---', forwardHeaders);
                const response = await (0, axios_1.default)({
                    method,
                    url: fullUrl,
                    headers: forwardHeaders,
                    data: req,
                    responseType: 'stream',
                    maxContentLength: Infinity,
                    maxBodyLength: Infinity,
                });
                console.log('📥 Response Headers:', response.headers);
                console.log('📥 Response Status:', response.status);
                res.status(response.status);
                Object.entries(response.headers).forEach(([k, v]) => {
                    if (k.toLowerCase() !== 'transfer-encoding') {
                        res.setHeader(k, v);
                    }
                });
                return response.data.pipe(res);
            }
            const response = await (0, axios_1.default)({
                method,
                url: fullUrl,
                headers: {
                    ...req.headers,
                    'x-api-key': key,
                },
                data: req.body,
            });
            console.log('📥 Response Status:', response.status);
            console.log('📥 Response Data:', response.data);
            res.status(response.status).json(response.data);
        }
        catch (err) {
            const status = err.response?.status || 500;
            console.error('❌ Proxy Error');
            console.error('Message:', err.message);
            console.error('Status:', status);
            console.error('Error Response Headers:', err.response?.headers);
            console.error('Error Response Data:', err.response?.data);
            return res.status(status).json(err.response?.data || {
                success: false,
                code: status,
                data: { type: 'PROXY_ERROR', message: err.message },
            });
        }
    }
};
exports.ProxyService = ProxyService;
exports.ProxyService = ProxyService = __decorate([
    (0, common_1.Injectable)(),
    __metadata("design:paramtypes", [config_1.ConfigService])
], ProxyService);
//# sourceMappingURL=proxy.service.js.map