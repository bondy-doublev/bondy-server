import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  ManyToOne,
  CreateDateColumn,
  UpdateDateColumn,
} from 'typeorm';
import { ChatRoom } from './chat-room.entity';

@Entity('messages')
export class Message {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Column()
  roomId: string;

  @ManyToOne(() => ChatRoom, (room) => room.messages, { onDelete: 'CASCADE' })
  room: ChatRoom;

  @Column()
  senderId: string;

  @Column({ nullable: true })
  replyToMessageId?: string;

  @Column({ nullable: true })
  content?: string;

  @Column({ nullable: true })
  fileUrl?: string;

  @Column({ nullable: true })
  imageUrl?: string;

  @Column('json', { nullable: true })
  attachments?: { url: string; type: 'image' | 'file'; fileName?: string }[];

  @Column('json', { nullable: true })
  sharedPost?: {
    postId: string;
    title: string;
    image?: string;
    link: string;
    authorName?: string;
    authorAvatar?: string;
  };

  @Column({ default: false })
  isEdited: boolean;

  @Column({ default: false })
  isDeleted: boolean;

  @Column('text', { array: true, default: '{}' })
  readBy: string[];

  @CreateDateColumn()
  createdAt: Date;

  @UpdateDateColumn()
  updatedAt: Date;
}
