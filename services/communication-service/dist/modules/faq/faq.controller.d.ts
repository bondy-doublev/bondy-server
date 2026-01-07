import { FaqService } from './faq.service';
import { CreateFaqDto } from './dto/create-faq.dto';
export declare class FaqController {
    private service;
    constructor(service: FaqService);
    create(dto: CreateFaqDto): Promise<import("@supabase/postgrest-js").PostgrestSingleResponse<null>>;
    findAll(): import("@supabase/postgrest-js").PostgrestFilterBuilder<any, any, any, any[], "faqs", unknown, "GET">;
    remove(id: string): import("@supabase/postgrest-js").PostgrestFilterBuilder<any, any, any, null, "faqs", unknown, "DELETE">;
}
