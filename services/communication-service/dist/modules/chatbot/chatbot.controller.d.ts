import { ChatService } from './chatbot.service';
import { ChatDto } from './dto/chat.dto';
export declare class ChatController {
    private service;
    constructor(service: ChatService);
    chat(dto: ChatDto): Promise<string | null>;
}
