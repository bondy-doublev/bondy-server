import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  CreateDateColumn,
  OneToMany,
} from 'typeorm';
import { AdvertMedia } from './advert_media.entity';

export enum AdvertRequestStatus {
  PENDING = 'pending',
  RUNNING = 'running',
  DONE = 'done',
  REJECTED = 'rejected',
  CANCELLED = 'cancelled',
  ACCEPTED = 'accepted',
}

@Entity('advert_requests')
export class AdvertRequest {
  @PrimaryGeneratedColumn()
  id: number;

  @Column()
  userId: number;

  @Column({ nullable: true })
  userAvatar?: string;

  @Column({ nullable: true })
  userEmail?: string;

  @Column({ length: 255 })
  accountName: string;

  // 🔹 nội dung quảng cáo
  @Column({ length: 255 })
  title: string;

  // 🔹 nếu quảng cáo từ post gốc (optional)
  @Column({ nullable: true })
  postId?: number;

  // 🔹 pricing
  @Column({ type: 'int' })
  pricePerDay: number; // 20000

  @Column({ type: 'int' })
  totalDays: number;

  @Column({ type: 'date' })
  startDate: string;

  @Column({ type: 'date' })
  endDate: string;

  @Column({ type: 'int' })
  totalPrice: number;

  @Column({
    type: 'enum',
    enum: AdvertRequestStatus,
    default: AdvertRequestStatus.PENDING,
  })
  status: AdvertRequestStatus;

  @OneToMany(() => AdvertMedia, (media: any) => media.advert, {
    cascade: true,
    eager: true,
  })
  media: AdvertMedia[];

  @CreateDateColumn()
  createdAt: Date;
}
