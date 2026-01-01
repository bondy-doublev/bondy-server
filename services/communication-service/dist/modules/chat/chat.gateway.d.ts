import { OnGatewayConnection } from '@nestjs/websockets';
import { Server, Socket } from 'socket.io';
import { ChatService } from './chat.service';
export declare class ChatGateway implements OnGatewayConnection {
    private chatService;
    server: Server;
    constructor(chatService: ChatService);
    handleConnection(socket: Socket): Promise<void>;
    handleJoinRoom({ roomId, userId }: any, socket: Socket): void;
    handleSend(data: any): Promise<void>;
    handleEdit(data: any): Promise<void>;
    handleDelete(data: any): Promise<void>;
    handleOpenRoom({ userId, roomId }: any, socket: Socket): Promise<void>;
}
