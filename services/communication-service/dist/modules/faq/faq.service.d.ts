import { OpenaiService } from 'src/common/openai/openai.service';
import { SupabaseService } from 'src/common/supabase/supabase.service';
import { CreateFaqDto } from './dto/create-faq.dto';
export declare class FaqService {
    private supabase;
    private openai;
    constructor(supabase: SupabaseService, openai: OpenaiService);
    create(dto: CreateFaqDto): Promise<import("@supabase/postgrest-js").PostgrestSingleResponse<null>>;
    findAll(): import("@supabase/postgrest-js").PostgrestFilterBuilder<any, any, any, any[], "faqs", unknown, "GET">;
    delete(id: string): import("@supabase/postgrest-js").PostgrestFilterBuilder<any, any, any, null, "faqs", unknown, "DELETE">;
}
