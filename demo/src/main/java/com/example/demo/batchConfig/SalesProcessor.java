package com.example.demo.batchConfig;

import org.springframework.batch.item.ItemProcessor;

import com.example.demo.model.SalesRecord;

public class SalesProcessor  implements ItemProcessor<SalesRecord, SalesRecord> {
    @Override
    public SalesRecord process(SalesRecord salesRecord) throws Exception {
        return salesRecord;
    }
    
}
