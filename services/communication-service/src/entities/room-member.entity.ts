import { Entity, PrimaryGeneratedColumn, Column, ManyToOne } from 'typeorm';
import { ChatRoom } from './chat-room.entity';

@Entity('room_members')
export class RoomMember {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Column()
  userId: number; // Nhận từ User service

  @ManyToOne(() => ChatRoom, (room) => room.members, { onDelete: 'CASCADE' })
  room: ChatRoom;
}
